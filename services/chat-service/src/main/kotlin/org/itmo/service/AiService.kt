package org.itmo.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.itmo.api.logger
import org.itmo.config.Config
import org.itmo.db.RedisClient
import java.security.cert.X509Certificate
import java.util.UUID
import javax.net.ssl.X509TrustManager

class AiService(
    private val apiKey: String,
    private val redisClient: RedisClient
) {
//    private val apiUrl = "https://gigachat.devices.sberbank.ru/"
    private val apiUrl = "http://172.20.10.5:8000/"
//    private val authUrl = "api/v2/oauth"
//    private val summarizeUrl = "api/v1/chat/completions"
    private val summarizeUrl = "api/summarize"
    private val authTokenKey = "ai_access_token"

    private val authUrl = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            }
        }
    }

    fun generateUID(): String {
        return UUID.randomUUID().toString()
    }

    suspend fun authenticate(): String {
        val token = redisClient.get(authTokenKey)
        if (token != null) {
            logger.info("cache hit!")
            return token
        }

        val a = client.post(authUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            header("Authorization", "Basic $apiKey")
            header("RqUID", generateUID())
            accept(ContentType.Application.Json)
            setBody("scope=GIGACHAT_API_PERS")
        }
        logger.info("received from ai: ${a.bodyAsText()}")

        val response: AuthResponse = a.body()

        redisClient.setex(authTokenKey, response.access_token, response.expires_at / 1000)
        return response.access_token
    }

    suspend fun summarizeDialog(messages: List<DialogMessage>): String {
        val accessToken = authenticate()

        val formattedDialog = messages.joinToString("\n") {
            "${it.senderId}: ${it.text}"
        }

        val prompt = """
            Суммаризируй следующий диалог, выделив ключевые темы и решения:
            
            $formattedDialog
        """.trimIndent()

        val request = ChatRequest(
            model = Config.gigachatModel,
            messages = listOf(
                Message(role = "system", content = "Ты помощник для суммаризации диалогов."),
                Message(role = "user", content = prompt)
            )
        )
        logger.info(request.toString())

        val a = client.post(apiUrl + summarizeUrl) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(request)
        }
        logger.info("received from ai: ${a.bodyAsText()}")
        val response: ChatResponse = a.body()

//        val response: ChatResponse = client.post(apiUrl + summarizeUrl) {
//            contentType(ContentType.Application.Json)
//            header("Authorization", "Bearer $accessToken")
//            setBody(request)
//        }.body()

        return response.choices.firstOrNull()?.message?.content
            ?: "Не удалось создать суммаризацию"
    }

    suspend fun summarizeJson(json: String): String {
        val a = client.post(apiUrl + summarizeUrl) {
            contentType(ContentType.Application.Json)
            setBody(json)
            timeout {
                connectTimeoutMillis = 100000
                requestTimeoutMillis = 100000
            }
        }
        val response: ArtyomChatResponse = a.body()

        return response.summary
    }

    fun close() {
        client.close()
    }
}

data class DialogMessage(
    val senderId: Long,
    val text: String,
    val timestamp: Long? = null
)

@Serializable
data class ChatRequest(
    val model: String = "GigaChat-2",
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
data class AuthResponse(
    val access_token: String,
    val expires_at: Long,
)

@Serializable
data class Choice(
    val message: Message
)

@Serializable
data class ArtyomChatResponse(
    val summary: String
)

package org.itmo.config

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*

/**
 * После запуска приложения документация будет доступна по адресам:
 * - Swagger UI: http://localhost:8080/swagger
 * - OpenAPI JSON: http://localhost:8080/openapi
 */
fun Application.configureSwagger() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
            version = "5.17.14"
        }

        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
    }
}


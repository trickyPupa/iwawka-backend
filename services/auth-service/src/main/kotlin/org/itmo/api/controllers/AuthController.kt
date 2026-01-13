package org.itmo.api.controllers

import org.itmo.api.request.LoginRequest
import org.itmo.api.request.RefreshRequest
import org.itmo.api.request.RegisterRequest
import org.itmo.api.response.AuthResponse
import org.itmo.service.AuthService

class AuthController(private val authService: AuthService) {

    fun register(request: RegisterRequest): AuthResponse {
        val userId = authService.register(request.username, request.email, request.password)
        val tokens = authService.issueTokens(userId)
        return AuthResponse(tokens.accessToken, tokens.refreshToken, tokens.expiresIn)
    }

    fun login(request: LoginRequest): AuthResponse {
        val tokens = authService.login(request.email, request.password)
        return AuthResponse(tokens.accessToken, tokens.refreshToken, tokens.expiresIn)
    }

    fun refresh(request: RefreshRequest): AuthResponse {
        val tokens = authService.refresh(request.refreshToken)
        return AuthResponse(tokens.accessToken, tokens.refreshToken, tokens.expiresIn)
    }

    fun logout(request: RefreshRequest) {
        authService.revoke(request.refreshToken)
    }
}


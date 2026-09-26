package com.aimemory.controller;

import com.aimemory.dto.auth.RegisterTenantRequest;
import com.aimemory.dto.auth.RegisterTenantResponse;
import com.aimemory.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Cadastro de tenants e emissão de API keys")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements   // remove o cadeado global (esse endpoint é público)
    @Operation(
            summary = "Registrar novo tenant",
            description = """
            Cadastra um novo tenant (dev) e retorna a API key.
            ⚠️ A API key é mostrada APENAS UMA VEZ. Guarde em local seguro.
            """
    )
    @ApiResponse(responseCode = "201", description = "Tenant criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Email já cadastrado ou dados inválidos")
    public RegisterTenantResponse register(@Valid @RequestBody RegisterTenantRequest req) {
        return authService.register(req);
    }
}
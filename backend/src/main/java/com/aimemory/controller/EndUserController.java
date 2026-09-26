package com.aimemory.controller;

import com.aimemory.dto.enduser.CreateEndUserRequest;
import com.aimemory.dto.enduser.EndUserResponse;
import com.aimemory.service.EndUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/v1/end-users")
@RequiredArgsConstructor
@Tag(name = "End Users", description = "Usuários finais do seu app (do ponto de vista do dev)")
public class EndUserController {

    private final EndUserService endUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar ou buscar end-user (idempotente)",
            description = """
            Cria um end-user se não existir; retorna o existente se já houver.
            Use o `externalId` como identificador do usuário no SEU sistema
            (ex: o ID do usuário no seu banco).
            """
    )
    public EndUserResponse create(@Valid @RequestBody CreateEndUserRequest req) {
        return endUserService.create(req);
    }

    @GetMapping
    @Operation(summary = "Listar end-users do tenant autenticado")
    public List<EndUserResponse> list() {
        return endUserService.listAll();
    }
}
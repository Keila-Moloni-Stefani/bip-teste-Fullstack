package com.example.backend.controller;

import com.example.backend.dto.BeneficioDTO;
import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.TransferenciaRequest;
import com.example.backend.service.BeneficioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficios")
@CrossOrigin(origins = "*")
@Tag(name = "Benefícios", description = "CRUD e transferência de benefícios")
public class BeneficioController {

    private final BeneficioService service;

    public BeneficioController(BeneficioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os benefícios")
    public List<BeneficioDTO> list(
            @RequestParam(defaultValue = "false") boolean apenasAtivos) {
        return service.findAll(apenasAtivos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um benefício por ID")
    public BeneficioDTO get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo benefício")
    public BeneficioDTO create(@Valid @RequestBody BeneficioRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um benefício existente")
    public BeneficioDTO update(@PathVariable Long id,
                               @Valid @RequestBody BeneficioRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um benefício")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/transferencia")
    @Operation(summary = "Transfere valor entre dois benefícios via EJB")
    public ResponseEntity<String> transfer(
            @Valid @RequestBody TransferenciaRequest request) {
        service.transfer(request);
        return ResponseEntity.ok("Transferência realizada com sucesso.");
    }
}

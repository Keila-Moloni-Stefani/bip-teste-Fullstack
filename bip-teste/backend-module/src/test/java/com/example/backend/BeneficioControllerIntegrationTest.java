package com.example.backend;

import com.example.backend.dto.BeneficioDTO;
import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.TransferenciaRequest;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.service.BeneficioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.example.backend.controller.BeneficioController.class)
class BeneficioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BeneficioService service;

    private BeneficioDTO dto(Long id, String nome) {
        return new BeneficioDTO(id, nome, "Desc", new BigDecimal("500.00"), true, 0L);
    }

    // ── GET /api/v1/beneficios ────────────────────────────────────────────────

    @Test
    void list_retornaListaJson() throws Exception {
        when(service.findAll(false)).thenReturn(List.of(dto(1L, "A"), dto(2L, "B")));

        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("A"));
    }

    @Test
    void list_apenasAtivos_passaParametro() throws Exception {
        when(service.findAll(true)).thenReturn(List.of(dto(1L, "A")));

        mockMvc.perform(get("/api/v1/beneficios?apenasAtivos=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/v1/beneficios/{id} ───────────────────────────────────────────

    @Test
    void getById_existente_retorna200() throws Exception {
        when(service.findById(1L)).thenReturn(dto(1L, "Teste"));

        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Teste"));
    }

    @Test
    void getById_inexistente_retorna404() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Não encontrado"));

        mockMvc.perform(get("/api/v1/beneficios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Não encontrado"));
    }

    // ── POST /api/v1/beneficios ───────────────────────────────────────────────

    @Test
    void create_dadosValidos_retorna201() throws Exception {
        BeneficioRequest req = new BeneficioRequest();
        req.setNome("Novo");
        req.setValor(new BigDecimal("300.00"));
        req.setAtivo(true);

        when(service.create(any())).thenReturn(dto(5L, "Novo"));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void create_nomeEmBranco_retorna400() throws Exception {
        BeneficioRequest req = new BeneficioRequest();
        req.setNome("");
        req.setValor(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    // ── PUT /api/v1/beneficios/{id} ───────────────────────────────────────────

    @Test
    void update_existente_retorna200() throws Exception {
        BeneficioRequest req = new BeneficioRequest();
        req.setNome("Atualizado");
        req.setValor(new BigDecimal("800.00"));
        req.setAtivo(true);

        when(service.update(eq(1L), any())).thenReturn(dto(1L, "Atualizado"));

        mockMvc.perform(put("/api/v1/beneficios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Atualizado"));
    }

    // ── DELETE /api/v1/beneficios/{id} ────────────────────────────────────────

    @Test
    void delete_existente_retorna204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/beneficios/1"))
                .andExpect(status().isNoContent());
    }

    // ── POST /api/v1/beneficios/transferencia ────────────────────────────────

    @Test
    void transfer_dadosValidos_retorna200() throws Exception {
        TransferenciaRequest req = new TransferenciaRequest();
        req.setFromId(1L);
        req.setToId(2L);
        req.setAmount(new BigDecimal("200.00"));

        doNothing().when(service).transfer(any());

        mockMvc.perform(post("/api/v1/beneficios/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferência realizada com sucesso."));
    }
}

package com.example.backend;

import com.example.backend.dto.BeneficioDTO;
import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.TransferenciaRequest;
import com.example.backend.entity.Beneficio;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.BeneficioRepository;
import com.example.backend.service.BeneficioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock
    private BeneficioRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BeneficioService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "ejbUrl", "http://ejb/api/beneficios");
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_semFiltro_retornaTodos() {
        when(repository.findAll()).thenReturn(List.of(beneficio(1L, "A", true), beneficio(2L, "B", false)));
        List<BeneficioDTO> result = service.findAll(false);
        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void findAll_apenasAtivos_retornaFiltrado() {
        when(repository.findByAtivo(true)).thenReturn(List.of(beneficio(1L, "A", true)));
        List<BeneficioDTO> result = service.findAll(true);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAtivo());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_existente_retornaDTO() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficio(1L, "Teste", true)));
        BeneficioDTO dto = service.findById(1L);
        assertEquals("Teste", dto.getNome());
        assertEquals(1L, dto.getId());
    }

    @Test
    void findById_inexistente_lancaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_dadosValidos_retornaDTO() {
        BeneficioRequest req = request("Novo", new BigDecimal("300.00"), true);
        Beneficio saved = beneficio(5L, "Novo", true);
        when(repository.save(any())).thenReturn(saved);

        BeneficioDTO result = service.create(req);
        assertEquals("Novo", result.getNome());
        assertEquals(5L, result.getId());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_existente_atualizaCampos() {
        Beneficio existing = beneficio(2L, "Antigo", true);
        BeneficioRequest req = request("Novo Nome", new BigDecimal("999.00"), false);
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        BeneficioDTO result = service.update(2L, req);
        assertEquals("Novo Nome", result.getNome());
    }

    @Test
    void update_inexistente_lancaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, request("X", BigDecimal.ONE, true)));
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_existente_removeSemErro() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficio(1L, "A", true)));
        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository).delete(any());
    }

    @Test
    void delete_inexistente_lancaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }

    // ── transfer ─────────────────────────────────────────────────────────────

    @Test
    void transfer_ejbRetornaOk_semExcecao() {
        TransferenciaRequest req = transferRequest(1L, 2L, new BigDecimal("100.00"));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));
        assertDoesNotThrow(() -> service.transfer(req));
    }

    @Test
    void transfer_ejbRetornaErro_lancaBusinessException() {
        TransferenciaRequest req = transferRequest(1L, 2L, new BigDecimal("9999.00"));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("Saldo insuficiente"));
        assertThrows(BusinessException.class, () -> service.transfer(req));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Beneficio beneficio(Long id, String nome, boolean ativo) {
        Beneficio b = new Beneficio();
        b.setId(id);
        b.setNome(nome);
        b.setValor(new BigDecimal("500.00"));
        b.setAtivo(ativo);
        b.setVersao(0L);
        return b;
    }

    private BeneficioRequest request(String nome, BigDecimal valor, boolean ativo) {
        BeneficioRequest r = new BeneficioRequest();
        r.setNome(nome);
        r.setValor(valor);
        r.setAtivo(ativo);
        return r;
    }

    private TransferenciaRequest transferRequest(Long from, Long to, BigDecimal amount) {
        TransferenciaRequest r = new TransferenciaRequest();
        r.setFromId(from);
        r.setToId(to);
        r.setAmount(amount);
        return r;
    }
}

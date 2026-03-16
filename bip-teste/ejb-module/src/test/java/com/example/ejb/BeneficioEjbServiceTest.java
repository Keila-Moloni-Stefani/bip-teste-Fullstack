package com.example.ejb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioEjbServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private BeneficioEjbService service;

    private Beneficio origem;
    private Beneficio destino;

    @BeforeEach
    void setup() {
        origem  = beneficio(1L, new BigDecimal("1000.00"));
        destino = beneficio(2L, new BigDecimal("500.00"));

        // lock em ordem crescente: ID 1 primeiro, depois ID 2
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);
    }

    @Test
    void transfer_valorValido_atualizaSaldos() {
        service.transferInternal(1L, 2L, new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), origem.getValor());
        assertEquals(new BigDecimal("800.00"), destino.getValor());
        verify(em).merge(origem);
        verify(em).merge(destino);
    }

    @Test
    void transfer_saldoExato_permitido() {
        service.transferInternal(1L, 2L, new BigDecimal("1000.00"));
        assertEquals(BigDecimal.ZERO.setScale(2), origem.getValor().stripTrailingZeros());
    }

    @Test
    void transfer_valorNulo_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transferInternal(1L, 2L, null));
    }

    @Test
    void transfer_valorZero_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transferInternal(1L, 2L, BigDecimal.ZERO));
    }

    @Test
    void transfer_valorNegativo_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transferInternal(1L, 2L, new BigDecimal("-50.00")));
    }

    @Test
    void transfer_mesmoId_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transferInternal(1L, 1L, new BigDecimal("100.00")));
    }

    @Test
    void transfer_fromIdNulo_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transferInternal(null, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transfer_saldoInsuficiente_lancaIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> service.transferInternal(1L, 2L, new BigDecimal("9999.00")));
    }

    @Test
    void transfer_origemNaoEncontrada_lancaIllegalStateException() {
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);
        assertThrows(IllegalStateException.class,
                () -> service.transferInternal(1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transfer_destinoNaoEncontrado_lancaIllegalStateException() {
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);
        assertThrows(IllegalStateException.class,
                () -> service.transferInternal(1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transfer_lockOrdemCrescente_antiDeadlock() {
        // Transferência com fromId > toId — lock deve ser adquirido em ordem crescente
        Beneficio b3 = beneficio(3L, new BigDecimal("200.00"));
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);
        when(em.find(Beneficio.class, 3L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(b3);

        // fromId=3, toId=2 → lock(2) antes de lock(3)
        service.transferInternal(3L, 2L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), b3.getValor());
        assertEquals(new BigDecimal("600.00"), destino.getValor());
    }

    // ── helper ───────────────────────────────────────────────────────────────
    private Beneficio beneficio(Long id, BigDecimal valor) {
        Beneficio b = new Beneficio();
        b.setId(id);
        b.setNome("Benefício " + id);
        b.setValor(valor);
        b.setAtivo(true);
        return b;
    }
}

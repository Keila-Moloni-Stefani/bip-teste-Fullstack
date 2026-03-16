package com.example.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransferenciaRequest {

    @NotNull(message = "ID de origem é obrigatório.")
    private Long fromId;

    @NotNull(message = "ID de destino é obrigatório.")
    private Long toId;

    @NotNull(message = "Valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero.")
    private BigDecimal amount;

    public Long getFromId() { return fromId; }
    public void setFromId(Long fromId) { this.fromId = fromId; }

    public Long getToId() { return toId; }
    public void setToId(Long toId) { this.toId = toId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

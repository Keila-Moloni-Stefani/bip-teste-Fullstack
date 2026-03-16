package com.example.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;

/**
 * Serviço EJB Stateless para operações de transferência entre Benefícios.
 *
 * <h3>Bugs corrigidos em relação ao código original:</h3>
 * <ul>
 *   <li><b>Bug 1 – Sem validação de entrada:</b> verificações de nulidade,
 *       valor positivo e IDs distintos antes de qualquer acesso ao banco.</li>
 *   <li><b>Bug 2 – Sem verificação de saldo:</b> lança {@link IllegalStateException}
 *       quando o benefício de origem não possui saldo suficiente.</li>
 *   <li><b>Bug 3 – Sem locking (condição de corrida / lost update):</b>
 *       usa {@link LockModeType#PESSIMISTIC_WRITE} em ordem crescente de ID
 *       para evitar deadlocks entre transações concorrentes.</li>
 *   <li><b>Bug 4 – Transação não declarada explicitamente:</b>
 *       {@link TransactionAttribute}{@code (REQUIRED)} garante que o método
 *       sempre execute dentro de uma transação com rollback automático.</li>
 * </ul>
 */
@Stateless
@Path("/beneficios")
public class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    public static class TransferRequest {
        public Long fromId;
        public Long toId;
        public BigDecimal amount;
    }

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response transfer(TransferRequest req) {
        try {
            transferInternal(req.fromId, req.toId, req.amount);
            return Response.ok("Transferência realizada com sucesso.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.UNPROCESSABLE_ENTITY).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Erro interno na transferência.").build();
        }
    }

    /**
     * Transfere {@code quantia} do benefício {@code fromId} para {@code toId}.
     *
     * @param fromId  ID do benefício de origem
     * @param toId    ID do benefício de destino
     * @param quantia Valor a transferir (deve ser positivo)
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void transferInternal(Long fromId, Long toId, BigDecimal quantia) {

        // ── 1. Validações de entrada ──────────────────────────────────────────
        if (fromId == null || toId == null) {
            throw new IllegalArgumentException("IDs de origem e destino não podem ser nulos.");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Origem e destino não podem ser o mesmo benefício.");
        }
        if (quantia == null || quantia.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser positivo.");
        }

        // ── 2. Lock pessimista em ordem crescente (anti-deadlock) ─────────────
        //    Thread A: lock(1) → lock(2)  /  Thread B: lock(1) → lock(2)
        //    Ambas sempre adquirem na mesma ordem → sem deadlock.
        Long primeiroId = Math.min(fromId, toId);
        Long segundoId  = Math.max(fromId, toId);

        Beneficio primeiro = em.find(Beneficio.class, primeiroId, LockModeType.PESSIMISTIC_WRITE);
        Beneficio segundo  = em.find(Beneficio.class, segundoId,  LockModeType.PESSIMISTIC_WRITE);

        // ── 3. Verificação de existência ──────────────────────────────────────
        if (primeiro == null) {
            throw new IllegalStateException("Benefício não encontrado: id=" + primeiroId);
        }
        if (segundo == null) {
            throw new IllegalStateException("Benefício não encontrado: id=" + segundoId);
        }

        // Reatribui corretamente após ordenação por ID
        Beneficio de   = primeiroId.equals(fromId) ? primeiro : segundo;
        Beneficio para = primeiroId.equals(fromId) ? segundo  : primeiro;

        // ── 4. Verificação de saldo ───────────────────────────────────────────
        if (de.getValor().compareTo(quantia) < 0) {
            throw new IllegalStateException(String.format(
                "Saldo insuficiente no benefício id=%d. Disponível: %.2f, Solicitado: %.2f",
                fromId, de.getValor(), quantia));
        }

        // ── 5. Atualização atômica ────────────────────────────────────────────
        de.setValor(de.getValor().subtract(quantia));
        para.setValor(para.getValor().add(quantia));

        em.merge(de);
        em.merge(para);
        // Container EJB faz flush + commit ao final do método.
        // Qualquer RuntimeException => rollback automático.
    }
}

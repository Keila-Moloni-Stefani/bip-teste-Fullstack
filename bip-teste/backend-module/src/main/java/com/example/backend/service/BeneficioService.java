package com.example.backend.service;

import com.example.backend.dto.BeneficioDTO;
import com.example.backend.dto.BeneficioRequest;
import com.example.backend.dto.TransferenciaRequest;
import com.example.backend.entity.Beneficio;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.BeneficioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BeneficioService {

    private final BeneficioRepository repository;
    private final RestTemplate restTemplate;

    @Value("${ejb.service.url}")
    private String ejbUrl;

    public BeneficioService(BeneficioRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
    }

    public List<BeneficioDTO> findAll(boolean apenasAtivos) {
        List<Beneficio> lista = apenasAtivos
                ? repository.findByAtivo(true)
                : repository.findAll();
        return lista.stream().map(this::toDTO).toList();
    }

    public BeneficioDTO findById(Long id) {
        return toDTO(findEntityById(id));
    }

    @Transactional
    public BeneficioDTO create(BeneficioRequest request) {
        Beneficio beneficio = new Beneficio();
        applyRequest(beneficio, request);
        return toDTO(repository.save(beneficio));
    }

    @Transactional
    public BeneficioDTO update(Long id, BeneficioRequest request) {
        Beneficio existing = findEntityById(id);
        applyRequest(existing, request);
        return toDTO(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }

    public void transfer(TransferenciaRequest request) {
        ResponseEntity<String> response = restTemplate.postForEntity(
                ejbUrl + "/transfer", request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(
                    "Falha na transferência via EJB: " + response.getBody());
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Beneficio findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Benefício não encontrado: id=" + id));
    }

    private void applyRequest(Beneficio b, BeneficioRequest req) {
        b.setNome(req.getNome());
        b.setDescricao(req.getDescricao());
        b.setValor(req.getValor());
        b.setAtivo(req.getAtivo() != null ? req.getAtivo() : true);
    }

    private BeneficioDTO toDTO(Beneficio b) {
        return new BeneficioDTO(
                b.getId(), b.getNome(), b.getDescricao(),
                b.getValor(), b.getAtivo(), b.getVersao());
    }
}

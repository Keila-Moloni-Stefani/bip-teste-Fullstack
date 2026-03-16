import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BeneficioService } from './beneficio.service';
import { Beneficio, BeneficioRequest, TransferenciaRequest } from '../models/beneficio.model';
import { environment } from '../../environments/environment';

describe('BeneficioService', () => {
  let service: BeneficioService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/beneficios`;

  const mockBeneficios: Beneficio[] = [
    { id: 1, nome: 'Benefício A', descricao: 'Desc A', valor: 1000, ativo: true,  versao: 0 },
    { id: 2, nome: 'Benefício B', descricao: 'Desc B', valor: 500,  ativo: false, versao: 0 },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BeneficioService],
    });
    service  = TestBed.inject(BeneficioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('listar() deve fazer GET com apenasAtivos=false', () => {
    service.listar().subscribe(data => {
      expect(data.length).toBe(2);
      expect(data[0].nome).toBe('Benefício A');
    });
    const req = httpMock.expectOne(`${apiUrl}?apenasAtivos=false`);
    expect(req.request.method).toBe('GET');
    req.flush(mockBeneficios);
  });

  it('listar(true) deve enviar apenasAtivos=true', () => {
    service.listar(true).subscribe(data => expect(data.length).toBe(1));
    const req = httpMock.expectOne(`${apiUrl}?apenasAtivos=true`);
    expect(req.request.method).toBe('GET');
    req.flush([mockBeneficios[0]]);
  });

  it('buscarPorId() deve fazer GET em /beneficios/:id', () => {
    service.buscarPorId(1).subscribe(b => expect(b.id).toBe(1));
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockBeneficios[0]);
  });

  it('criar() deve fazer POST com body correto', () => {
    const payload: BeneficioRequest = { nome: 'Novo', valor: 300, ativo: true };
    service.criar(payload).subscribe(b => expect(b.nome).toBe('Novo'));
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 5, ...payload, versao: 0 });
  });

  it('atualizar() deve fazer PUT em /beneficios/:id', () => {
    const payload: BeneficioRequest = { nome: 'Atualizado', valor: 999, ativo: false };
    service.atualizar(1, payload).subscribe(b => expect(b.nome).toBe('Atualizado'));
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 1, ...payload, versao: 1 });
  });

  it('deletar() deve fazer DELETE em /beneficios/:id', () => {
    service.deletar(1).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('transferir() deve fazer POST em /beneficios/transferencia', () => {
    const payload: TransferenciaRequest = { fromId: 1, toId: 2, amount: 200 };
    service.transferir(payload).subscribe(msg =>
      expect(msg).toBe('Transferência realizada com sucesso.'));
    const req = httpMock.expectOne(`${apiUrl}/transferencia`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush('Transferência realizada com sucesso.');
  });
});

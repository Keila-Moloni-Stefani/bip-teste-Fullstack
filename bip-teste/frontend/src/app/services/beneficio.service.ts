import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Beneficio, BeneficioRequest, TransferenciaRequest } from '../models/beneficio.model';
import { environment } from '../../environments/environment';

/**
 * Serviço Angular responsável por toda comunicação com a API de Benefícios.
 */
@Injectable({ providedIn: 'root' })
export class BeneficioService {
  private readonly apiUrl = `${environment.apiUrl}/beneficios`;

  constructor(private http: HttpClient) {}

  listar(apenasAtivos = false): Observable<Beneficio[]> {
    const params = new HttpParams().set('apenasAtivos', String(apenasAtivos));
    return this.http.get<Beneficio[]>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<Beneficio> {
    return this.http.get<Beneficio>(`${this.apiUrl}/${id}`);
  }

  criar(data: BeneficioRequest): Observable<Beneficio> {
    return this.http.post<Beneficio>(this.apiUrl, data);
  }

  atualizar(id: number, data: BeneficioRequest): Observable<Beneficio> {
    return this.http.put<Beneficio>(`${this.apiUrl}/${id}`, data);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  transferir(req: TransferenciaRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/transferencia`, req, { responseType: 'text' });
  }
}

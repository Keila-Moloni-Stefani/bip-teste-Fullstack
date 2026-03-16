import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Beneficio } from '../../models/beneficio.model';
import { BeneficioService } from '../../services/beneficio.service';

@Component({
  selector: 'app-beneficio-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, CurrencyPipe],
  templateUrl: './beneficio-list.component.html',
  styleUrls: ['./beneficio-list.component.css'],
})
export class BeneficioListComponent implements OnInit {
  beneficios: Beneficio[] = [];
  apenasAtivos = false;
  loading = false;
  erro: string | null = null;
  sucesso: string | null = null;

  constructor(private service: BeneficioService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.erro = null;
    this.service.listar(this.apenasAtivos).subscribe({
      next: (data) => { this.beneficios = data; this.loading = false; },
      error: () => { this.erro = 'Erro ao carregar benefícios.'; this.loading = false; },
    });
  }

  deletar(id: number): void {
    if (!confirm('Confirma a exclusão deste benefício?')) return;
    this.service.deletar(id).subscribe({
      next: () => {
        this.sucesso = 'Benefício excluído com sucesso.';
        this.carregar();
        setTimeout(() => (this.sucesso = null), 3000);
      },
      error: () => { this.erro = 'Erro ao excluir benefício.'; },
    });
  }
}

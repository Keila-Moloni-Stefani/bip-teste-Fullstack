import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Beneficio } from '../../models/beneficio.model';
import { BeneficioService } from '../../services/beneficio.service';

@Component({
  selector: 'app-transferencia',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, CurrencyPipe],
  templateUrl: './transferencia.component.html',
  styleUrls: ['./transferencia.component.css'],
})
export class TransferenciaComponent implements OnInit {
  form!: FormGroup;
  beneficios: Beneficio[] = [];
  loading = false;
  sucesso: string | null = null;
  erro: string | null = null;

  constructor(private fb: FormBuilder, private service: BeneficioService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      fromId: [null, Validators.required],
      toId:   [null, Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
    });

    this.service.listar(true).subscribe({
      next: (data) => (this.beneficios = data),
      error: () => (this.erro = 'Erro ao carregar benefícios.'),
    });
  }

  transferir(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.form.value.fromId === this.form.value.toId) {
      this.erro = 'Origem e destino não podem ser o mesmo benefício.';
      return;
    }
    this.loading = true;
    this.sucesso = null;
    this.erro = null;

    this.service.transferir(this.form.value).subscribe({
      next: (msg) => {
        this.sucesso = msg;
        this.loading = false;
        this.form.reset();
        this.service.listar(true).subscribe((d) => (this.beneficios = d));
      },
      error: (err) => {
        this.loading = false;
        this.erro = err?.error?.message || 'Erro ao realizar transferência.';
      },
    });
  }

  get f() { return this.form.controls; }

  saldoOrigem(): number | null {
    const id = this.form.value.fromId;
    return id ? (this.beneficios.find((b) => b.id == id)?.valor ?? null) : null;
  }
}

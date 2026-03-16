import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BeneficioService } from '../../services/beneficio.service';

@Component({
  selector: 'app-beneficio-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './beneficio-form.component.html',
  styleUrls: ['./beneficio-form.component.css'],
})
export class BeneficioFormComponent implements OnInit {
  form!: FormGroup;
  editando = false;
  id?: number;
  loading = false;
  erro: string | null = null;

  constructor(
    private fb: FormBuilder,
    private service: BeneficioService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nome:      ['', [Validators.required, Validators.maxLength(100)]],
      descricao: ['', Validators.maxLength(255)],
      valor:     [null, [Validators.required, Validators.min(0.01)]],
      ativo:     [true],
    });

    const paramId = this.route.snapshot.paramMap.get('id');
    if (paramId) {
      this.editando = true;
      this.id = +paramId;
      this.service.buscarPorId(this.id).subscribe({
        next: (b) => this.form.patchValue(b),
        error: () => { this.erro = 'Benefício não encontrado.'; },
      });
    }
  }

  salvar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.erro = null;

    const op = this.editando
      ? this.service.atualizar(this.id!, this.form.value)
      : this.service.criar(this.form.value);

    op.subscribe({
      next: () => this.router.navigate(['/beneficios']),
      error: (err) => {
        this.loading = false;
        this.erro = err?.error?.message || 'Erro ao salvar benefício.';
      },
    });
  }

  get f() { return this.form.controls; }
}

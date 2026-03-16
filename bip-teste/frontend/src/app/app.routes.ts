import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'beneficios', pathMatch: 'full' },
  {
    path: 'beneficios',
    loadComponent: () =>
      import('./components/beneficio-list/beneficio-list.component')
        .then(m => m.BeneficioListComponent),
  },
  {
    path: 'beneficios/novo',
    loadComponent: () =>
      import('./components/beneficio-form/beneficio-form.component')
        .then(m => m.BeneficioFormComponent),
  },
  {
    path: 'beneficios/editar/:id',
    loadComponent: () =>
      import('./components/beneficio-form/beneficio-form.component')
        .then(m => m.BeneficioFormComponent),
  },
  {
    path: 'transferencia',
    loadComponent: () =>
      import('./components/transferencia/transferencia.component')
        .then(m => m.TransferenciaComponent),
  },
];

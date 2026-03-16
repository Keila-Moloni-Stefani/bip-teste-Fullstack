import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule],
  template: `
    <nav class="navbar">
      <div class="container nav-inner">
        <span class="brand">💼 BIP Benefícios</span>
        <div class="nav-links">
          <a routerLink="/beneficios" routerLinkActive="active">Benefícios</a>
          <a routerLink="/transferencia" routerLinkActive="active">Transferência</a>
        </div>
      </div>
    </nav>
    <main class="main-content">
      <router-outlet />
    </main>
  `,
})
export class AppComponent {}

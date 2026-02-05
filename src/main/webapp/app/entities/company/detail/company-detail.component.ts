import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { ICompany } from '../company.model';

@Component({
  selector: 'jhi-company-detail',
  templateUrl: './company-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class CompanyDetailComponent {
  company = input<ICompany | null>(null);

  previousState(): void {
    window.history.back();
  }
}

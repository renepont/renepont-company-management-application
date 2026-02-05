import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ICompany, NewCompany } from '../company.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICompany for edit and NewCompanyFormGroupInput for create.
 */
type CompanyFormGroupInput = ICompany | PartialWithRequiredKeyOf<NewCompany>;

type CompanyFormDefaults = Pick<NewCompany, 'id' | 'userProfiles'>;

type CompanyFormGroupContent = {
  id: FormControl<ICompany['id'] | NewCompany['id']>;
  name: FormControl<ICompany['name']>;
  nif: FormControl<ICompany['nif']>;
  userProfiles: FormControl<ICompany['userProfiles']>;
};

export type CompanyFormGroup = FormGroup<CompanyFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CompanyFormService {
  createCompanyFormGroup(company: CompanyFormGroupInput = { id: null }): CompanyFormGroup {
    const companyRawValue = {
      ...this.getFormDefaults(),
      ...company,
    };
    return new FormGroup<CompanyFormGroupContent>({
      id: new FormControl(
        { value: companyRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(companyRawValue.name, {
        validators: [Validators.required, Validators.minLength(3)],
      }),
      nif: new FormControl(companyRawValue.nif, {
        validators: [Validators.required],
      }),
      userProfiles: new FormControl(companyRawValue.userProfiles ?? []),
    });
  }

  getCompany(form: CompanyFormGroup): ICompany | NewCompany {
    return form.getRawValue() as ICompany | NewCompany;
  }

  resetForm(form: CompanyFormGroup, company: CompanyFormGroupInput): void {
    const companyRawValue = { ...this.getFormDefaults(), ...company };
    form.reset(
      {
        ...companyRawValue,
        id: { value: companyRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): CompanyFormDefaults {
    return {
      id: null,
      userProfiles: [],
    };
  }
}

import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ITransaction, NewTransaction } from '../transaction.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITransaction for edit and NewTransactionFormGroupInput for create.
 */
type TransactionFormGroupInput = ITransaction | PartialWithRequiredKeyOf<NewTransaction>;

type TransactionFormDefaults = Pick<NewTransaction, 'id'>;

type TransactionFormGroupContent = {
  id: FormControl<ITransaction['id'] | NewTransaction['id']>;
  transactionDate: FormControl<ITransaction['transactionDate']>;
  type: FormControl<ITransaction['type']>;
  amount: FormControl<ITransaction['amount']>;
  category: FormControl<ITransaction['category']>;
  company: FormControl<ITransaction['company']>;
};

export type TransactionFormGroup = FormGroup<TransactionFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TransactionFormService {
  createTransactionFormGroup(transaction: TransactionFormGroupInput = { id: null }): TransactionFormGroup {
    const transactionRawValue = {
      ...this.getFormDefaults(),
      ...transaction,
    };
    return new FormGroup<TransactionFormGroupContent>({
      id: new FormControl(
        { value: transactionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      transactionDate: new FormControl(transactionRawValue.transactionDate, {
        validators: [Validators.required],
      }),
      type: new FormControl(transactionRawValue.type, {
        validators: [Validators.required],
      }),
      amount: new FormControl(transactionRawValue.amount, {
        validators: [Validators.required],
      }),
      category: new FormControl(transactionRawValue.category),
      company: new FormControl(transactionRawValue.company),
    });
  }

  getTransaction(form: TransactionFormGroup): ITransaction | NewTransaction {
    return form.getRawValue() as ITransaction | NewTransaction;
  }

  resetForm(form: TransactionFormGroup, transaction: TransactionFormGroupInput): void {
    const transactionRawValue = { ...this.getFormDefaults(), ...transaction };
    form.reset(
      {
        ...transactionRawValue,
        id: { value: transactionRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): TransactionFormDefaults {
    return {
      id: null,
    };
  }
}

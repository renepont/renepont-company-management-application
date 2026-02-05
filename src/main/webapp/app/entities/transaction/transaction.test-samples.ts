import dayjs from 'dayjs/esm';

import { ITransaction, NewTransaction } from './transaction.model';

export const sampleWithRequiredData: ITransaction = {
  id: 3793,
  transactionDate: dayjs('2026-02-05'),
  type: 'INCOME',
  amount: 23189.74,
};

export const sampleWithPartialData: ITransaction = {
  id: 21074,
  transactionDate: dayjs('2026-02-04'),
  type: 'EXPENSE',
  amount: 1969.88,
};

export const sampleWithFullData: ITransaction = {
  id: 20285,
  transactionDate: dayjs('2026-02-04'),
  type: 'EXPENSE',
  amount: 19965.99,
};

export const sampleWithNewData: NewTransaction = {
  transactionDate: dayjs('2026-02-05'),
  type: 'EXPENSE',
  amount: 31394.35,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);

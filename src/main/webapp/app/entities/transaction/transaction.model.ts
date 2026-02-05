import dayjs from 'dayjs/esm';
import { ICategory } from 'app/entities/category/category.model';
import { ICompany } from 'app/entities/company/company.model';
import { TransactionType } from 'app/entities/enumerations/transaction-type.model';

export interface ITransaction {
  id: number;
  transactionDate?: dayjs.Dayjs | null;
  type?: keyof typeof TransactionType | null;
  amount?: number | null;
  category?: Pick<ICategory, 'id' | 'name'> | null;
  company?: Pick<ICompany, 'id' | 'name'> | null;
}

export type NewTransaction = Omit<ITransaction, 'id'> & { id: null };

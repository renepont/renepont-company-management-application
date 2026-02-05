import { ICompany } from 'app/entities/company/company.model';

export interface ICustomer {
  id: number;
  name?: string | null;
  vatRequired?: boolean | null;
  company?: Pick<ICompany, 'id' | 'name'> | null;
}

export type NewCustomer = Omit<ICustomer, 'id'> & { id: null };

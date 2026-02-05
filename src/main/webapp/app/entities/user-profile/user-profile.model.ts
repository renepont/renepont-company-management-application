import { IUser } from 'app/entities/user/user.model';
import { ICompany } from 'app/entities/company/company.model';

export interface IUserProfile {
  id: number;
  phoneNumber?: string | null;
  jobTitle?: string | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
  companies?: Pick<ICompany, 'id'>[] | null;
}

export type NewUserProfile = Omit<IUserProfile, 'id'> & { id: null };

import { IUserProfile } from 'app/entities/user-profile/user-profile.model';

export interface ICompany {
  id: number;
  name?: string | null;
  nif?: string | null;
  userProfiles?: Pick<IUserProfile, 'id'>[] | null;
}

export type NewCompany = Omit<ICompany, 'id'> & { id: null };

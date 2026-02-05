import { IUserProfile, NewUserProfile } from './user-profile.model';

export const sampleWithRequiredData: IUserProfile = {
  id: 4033,
};

export const sampleWithPartialData: IUserProfile = {
  id: 6123,
  phoneNumber: 'nor winged which',
  jobTitle: 'Central Integration Strategist',
};

export const sampleWithFullData: IUserProfile = {
  id: 9570,
  phoneNumber: 'folklore glisten cuddly',
  jobTitle: 'District Solutions Executive',
};

export const sampleWithNewData: NewUserProfile = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);

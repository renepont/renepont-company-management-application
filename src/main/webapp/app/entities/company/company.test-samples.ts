import { ICompany, NewCompany } from './company.model';

export const sampleWithRequiredData: ICompany = {
  id: 6800,
  name: 'reschedule ah',
  nif: 'brr gurn',
};

export const sampleWithPartialData: ICompany = {
  id: 6304,
  name: 'qua inure of',
  nif: 'rag kit',
};

export const sampleWithFullData: ICompany = {
  id: 14111,
  name: 'clean',
  nif: 'considering',
};

export const sampleWithNewData: NewCompany = {
  name: 'forenenst haze',
  nif: 'qua pish cake',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);

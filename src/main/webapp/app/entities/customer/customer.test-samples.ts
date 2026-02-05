import { ICustomer, NewCustomer } from './customer.model';

export const sampleWithRequiredData: ICustomer = {
  id: 3366,
  name: 'slake splurge',
  vatRequired: true,
};

export const sampleWithPartialData: ICustomer = {
  id: 29162,
  name: 'puzzled modulo wildly',
  vatRequired: false,
};

export const sampleWithFullData: ICustomer = {
  id: 4149,
  name: 'where known',
  vatRequired: true,
};

export const sampleWithNewData: NewCustomer = {
  name: 'thyme inasmuch',
  vatRequired: true,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);

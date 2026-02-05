import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'renepontCmaApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'company',
    data: { pageTitle: 'renepontCmaApp.company.home.title' },
    loadChildren: () => import('./company/company.routes'),
  },
  {
    path: 'user-profile',
    data: { pageTitle: 'renepontCmaApp.userProfile.home.title' },
    loadChildren: () => import('./user-profile/user-profile.routes'),
  },
  {
    path: 'customer',
    data: { pageTitle: 'renepontCmaApp.customer.home.title' },
    loadChildren: () => import('./customer/customer.routes'),
  },
  {
    path: 'transaction',
    data: { pageTitle: 'renepontCmaApp.transaction.home.title' },
    loadChildren: () => import('./transaction/transaction.routes'),
  },
  {
    path: 'category',
    data: { pageTitle: 'renepontCmaApp.category.home.title' },
    loadChildren: () => import('./category/category.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;

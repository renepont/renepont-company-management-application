import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ICategory } from 'app/entities/category/category.model';
import { CategoryService } from 'app/entities/category/service/category.service';
import { ICompany } from 'app/entities/company/company.model';
import { CompanyService } from 'app/entities/company/service/company.service';
import { ITransaction } from '../transaction.model';
import { TransactionService } from '../service/transaction.service';
import { TransactionFormService } from './transaction-form.service';

import { TransactionUpdateComponent } from './transaction-update.component';

describe('Transaction Management Update Component', () => {
  let comp: TransactionUpdateComponent;
  let fixture: ComponentFixture<TransactionUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let transactionFormService: TransactionFormService;
  let transactionService: TransactionService;
  let categoryService: CategoryService;
  let companyService: CompanyService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TransactionUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(TransactionUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(TransactionUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    transactionFormService = TestBed.inject(TransactionFormService);
    transactionService = TestBed.inject(TransactionService);
    categoryService = TestBed.inject(CategoryService);
    companyService = TestBed.inject(CompanyService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Category query and add missing value', () => {
      const transaction: ITransaction = { id: 15110 };
      const category: ICategory = { id: 6752 };
      transaction.category = category;

      const categoryCollection: ICategory[] = [{ id: 6752 }];
      jest.spyOn(categoryService, 'query').mockReturnValue(of(new HttpResponse({ body: categoryCollection })));
      const additionalCategories = [category];
      const expectedCollection: ICategory[] = [...additionalCategories, ...categoryCollection];
      jest.spyOn(categoryService, 'addCategoryToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ transaction });
      comp.ngOnInit();

      expect(categoryService.query).toHaveBeenCalled();
      expect(categoryService.addCategoryToCollectionIfMissing).toHaveBeenCalledWith(
        categoryCollection,
        ...additionalCategories.map(expect.objectContaining),
      );
      expect(comp.categoriesSharedCollection).toEqual(expectedCollection);
    });

    it('should call Company query and add missing value', () => {
      const transaction: ITransaction = { id: 15110 };
      const company: ICompany = { id: 29751 };
      transaction.company = company;

      const companyCollection: ICompany[] = [{ id: 29751 }];
      jest.spyOn(companyService, 'query').mockReturnValue(of(new HttpResponse({ body: companyCollection })));
      const additionalCompanies = [company];
      const expectedCollection: ICompany[] = [...additionalCompanies, ...companyCollection];
      jest.spyOn(companyService, 'addCompanyToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ transaction });
      comp.ngOnInit();

      expect(companyService.query).toHaveBeenCalled();
      expect(companyService.addCompanyToCollectionIfMissing).toHaveBeenCalledWith(
        companyCollection,
        ...additionalCompanies.map(expect.objectContaining),
      );
      expect(comp.companiesSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const transaction: ITransaction = { id: 15110 };
      const category: ICategory = { id: 6752 };
      transaction.category = category;
      const company: ICompany = { id: 29751 };
      transaction.company = company;

      activatedRoute.data = of({ transaction });
      comp.ngOnInit();

      expect(comp.categoriesSharedCollection).toContainEqual(category);
      expect(comp.companiesSharedCollection).toContainEqual(company);
      expect(comp.transaction).toEqual(transaction);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITransaction>>();
      const transaction = { id: 29476 };
      jest.spyOn(transactionFormService, 'getTransaction').mockReturnValue(transaction);
      jest.spyOn(transactionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ transaction });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: transaction }));
      saveSubject.complete();

      // THEN
      expect(transactionFormService.getTransaction).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(transactionService.update).toHaveBeenCalledWith(expect.objectContaining(transaction));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITransaction>>();
      const transaction = { id: 29476 };
      jest.spyOn(transactionFormService, 'getTransaction').mockReturnValue({ id: null });
      jest.spyOn(transactionService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ transaction: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: transaction }));
      saveSubject.complete();

      // THEN
      expect(transactionFormService.getTransaction).toHaveBeenCalled();
      expect(transactionService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITransaction>>();
      const transaction = { id: 29476 };
      jest.spyOn(transactionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ transaction });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(transactionService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareCategory', () => {
      it('should forward to categoryService', () => {
        const entity = { id: 6752 };
        const entity2 = { id: 4374 };
        jest.spyOn(categoryService, 'compareCategory');
        comp.compareCategory(entity, entity2);
        expect(categoryService.compareCategory).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareCompany', () => {
      it('should forward to companyService', () => {
        const entity = { id: 29751 };
        const entity2 = { id: 7586 };
        jest.spyOn(companyService, 'compareCompany');
        comp.compareCompany(entity, entity2);
        expect(companyService.compareCompany).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});

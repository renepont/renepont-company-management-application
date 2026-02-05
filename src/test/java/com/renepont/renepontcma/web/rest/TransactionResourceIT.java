package com.renepont.renepontcma.web.rest;

import static com.renepont.renepontcma.domain.TransactionAsserts.*;
import static com.renepont.renepontcma.web.rest.TestUtil.createUpdateProxyForBean;
import static com.renepont.renepontcma.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renepont.renepontcma.IntegrationTest;
import com.renepont.renepontcma.domain.Category;
import com.renepont.renepontcma.domain.Company;
import com.renepont.renepontcma.domain.Transaction;
import com.renepont.renepontcma.domain.enumeration.TransactionType;
import com.renepont.renepontcma.repository.TransactionRepository;
import com.renepont.renepontcma.service.TransactionService;
import com.renepont.renepontcma.service.dto.TransactionDTO;
import com.renepont.renepontcma.service.mapper.TransactionMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TransactionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TransactionResourceIT {

    private static final LocalDate DEFAULT_TRANSACTION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TRANSACTION_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_TRANSACTION_DATE = LocalDate.ofEpochDay(-1L);

    private static final TransactionType DEFAULT_TYPE = TransactionType.INCOME;
    private static final TransactionType UPDATED_TYPE = TransactionType.EXPENSE;

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_AMOUNT = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionRepository transactionRepositoryMock;

    @Autowired
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionService transactionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    private Transaction insertedTransaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createEntity() {
        return new Transaction().transactionDate(DEFAULT_TRANSACTION_DATE).type(DEFAULT_TYPE).amount(DEFAULT_AMOUNT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createUpdatedEntity() {
        return new Transaction().transactionDate(UPDATED_TRANSACTION_DATE).type(UPDATED_TYPE).amount(UPDATED_AMOUNT);
    }

    @BeforeEach
    void initTest() {
        transaction = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTransaction != null) {
            transactionRepository.delete(insertedTransaction);
            insertedTransaction = null;
        }
    }

    @Test
    @Transactional
    void createTransaction() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);
        var returnedTransactionDTO = om.readValue(
            restTransactionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transactionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TransactionDTO.class
        );

        // Validate the Transaction in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTransaction = transactionMapper.toEntity(returnedTransactionDTO);
        assertTransactionUpdatableFieldsEquals(returnedTransaction, getPersistedTransaction(returnedTransaction));

        insertedTransaction = returnedTransaction;
    }

    @Test
    @Transactional
    void createTransactionWithExistingId() throws Exception {
        // Create the Transaction with an existing ID
        transaction.setId(1L);
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transactionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTransactionDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        transaction.setTransactionDate(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        transaction.setType(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        transaction.setAmount(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        restTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTransactions() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(DEFAULT_TRANSACTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransactionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(transactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTransactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(transactionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransactionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(transactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTransactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(transactionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTransaction() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get the transaction
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
            .andExpect(jsonPath("$.transactionDate").value(DEFAULT_TRANSACTION_DATE.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)));
    }

    @Test
    @Transactional
    void getTransactionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        Long id = transaction.getId();

        defaultTransactionFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultTransactionFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultTransactionFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTransactionsByTransactionDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where transactionDate equals to
        defaultTransactionFiltering(
            "transactionDate.equals=" + DEFAULT_TRANSACTION_DATE,
            "transactionDate.equals=" + UPDATED_TRANSACTION_DATE
        );
    }

    @Test
    @Transactional
    void getAllTransactionsByTransactionDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where transactionDate in
        defaultTransactionFiltering(
            "transactionDate.in=" + DEFAULT_TRANSACTION_DATE + "," + UPDATED_TRANSACTION_DATE,
            "transactionDate.in=" + UPDATED_TRANSACTION_DATE
        );
    }

    @Test
    @Transactional
    void getAllTransactionsByTransactionDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where transactionDate is not null
        defaultTransactionFiltering("transactionDate.specified=true", "transactionDate.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByTransactionDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where transactionDate is greater than or equal to
        defaultTransactionFiltering(
            "transactionDate.greaterThanOrEqual=" + DEFAULT_TRANSACTION_DATE,
            "transactionDate.greaterThanOrEqual=" + UPDATED_TRANSACTION_DATE
        );
    }

    @Test
    @Transactional
    void getAllTransactionsByTransactionDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where transactionDate is less than or equal to
        defaultTransactionFiltering(
            "transactionDate.lessThanOrEqual=" + DEFAULT_TRANSACTION_DATE,
            "transactionDate.lessThanOrEqual=" + SMALLER_TRANSACTION_DATE
        );
    }

    @Test
    @Transactional
    void getAllTransactionsByTransactionDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where transactionDate is less than
        defaultTransactionFiltering(
            "transactionDate.lessThan=" + UPDATED_TRANSACTION_DATE,
            "transactionDate.lessThan=" + DEFAULT_TRANSACTION_DATE
        );
    }

    @Test
    @Transactional
    void getAllTransactionsByTransactionDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where transactionDate is greater than
        defaultTransactionFiltering(
            "transactionDate.greaterThan=" + SMALLER_TRANSACTION_DATE,
            "transactionDate.greaterThan=" + DEFAULT_TRANSACTION_DATE
        );
    }

    @Test
    @Transactional
    void getAllTransactionsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where type equals to
        defaultTransactionFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllTransactionsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where type in
        defaultTransactionFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllTransactionsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where type is not null
        defaultTransactionFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amount equals to
        defaultTransactionFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amount in
        defaultTransactionFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amount is not null
        defaultTransactionFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amount is greater than or equal to
        defaultTransactionFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amount is less than or equal to
        defaultTransactionFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amount is less than
        defaultTransactionFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList where amount is greater than
        defaultTransactionFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTransactionsByCategoryIsEqualToSomething() throws Exception {
        Category category;
        if (TestUtil.findAll(em, Category.class).isEmpty()) {
            transactionRepository.saveAndFlush(transaction);
            category = CategoryResourceIT.createEntity();
        } else {
            category = TestUtil.findAll(em, Category.class).get(0);
        }
        em.persist(category);
        em.flush();
        transaction.setCategory(category);
        transactionRepository.saveAndFlush(transaction);
        Long categoryId = category.getId();
        // Get all the transactionList where category equals to categoryId
        defaultTransactionShouldBeFound("categoryId.equals=" + categoryId);

        // Get all the transactionList where category equals to (categoryId + 1)
        defaultTransactionShouldNotBeFound("categoryId.equals=" + (categoryId + 1));
    }

    @Test
    @Transactional
    void getAllTransactionsByCompanyIsEqualToSomething() throws Exception {
        Company company;
        if (TestUtil.findAll(em, Company.class).isEmpty()) {
            transactionRepository.saveAndFlush(transaction);
            company = CompanyResourceIT.createEntity();
        } else {
            company = TestUtil.findAll(em, Company.class).get(0);
        }
        em.persist(company);
        em.flush();
        transaction.setCompany(company);
        transactionRepository.saveAndFlush(transaction);
        Long companyId = company.getId();
        // Get all the transactionList where company equals to companyId
        defaultTransactionShouldBeFound("companyId.equals=" + companyId);

        // Get all the transactionList where company equals to (companyId + 1)
        defaultTransactionShouldNotBeFound("companyId.equals=" + (companyId + 1));
    }

    private void defaultTransactionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultTransactionShouldBeFound(shouldBeFound);
        defaultTransactionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTransactionShouldBeFound(String filter) throws Exception {
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].transactionDate").value(hasItem(DEFAULT_TRANSACTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))));

        // Check, that the count call also returns 1
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTransactionShouldNotBeFound(String filter) throws Exception {
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTransaction() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTransaction() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the transaction
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTransaction are not directly saved in db
        em.detach(updatedTransaction);
        updatedTransaction.transactionDate(UPDATED_TRANSACTION_DATE).type(UPDATED_TYPE).amount(UPDATED_AMOUNT);
        TransactionDTO transactionDTO = transactionMapper.toDto(updatedTransaction);

        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(transactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTransactionToMatchAllProperties(updatedTransaction);
    }

    @Test
    @Transactional
    void putNonExistingTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        transaction.setId(longCount.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        transaction.setId(longCount.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        transaction.setId(longCount.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(transactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTransactionWithPatch() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the transaction using partial update
        Transaction partialUpdatedTransaction = new Transaction();
        partialUpdatedTransaction.setId(transaction.getId());

        partialUpdatedTransaction.transactionDate(UPDATED_TRANSACTION_DATE);

        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTransaction))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTransactionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTransaction, transaction),
            getPersistedTransaction(transaction)
        );
    }

    @Test
    @Transactional
    void fullUpdateTransactionWithPatch() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the transaction using partial update
        Transaction partialUpdatedTransaction = new Transaction();
        partialUpdatedTransaction.setId(transaction.getId());

        partialUpdatedTransaction.transactionDate(UPDATED_TRANSACTION_DATE).type(UPDATED_TYPE).amount(UPDATED_AMOUNT);

        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTransaction))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTransactionUpdatableFieldsEquals(partialUpdatedTransaction, getPersistedTransaction(partialUpdatedTransaction));
    }

    @Test
    @Transactional
    void patchNonExistingTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        transaction.setId(longCount.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, transactionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        transaction.setId(longCount.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        transaction.setId(longCount.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionMapper.toDto(transaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(transactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Transaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTransaction() throws Exception {
        // Initialize the database
        insertedTransaction = transactionRepository.saveAndFlush(transaction);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the transaction
        restTransactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, transaction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return transactionRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Transaction getPersistedTransaction(Transaction transaction) {
        return transactionRepository.findById(transaction.getId()).orElseThrow();
    }

    protected void assertPersistedTransactionToMatchAllProperties(Transaction expectedTransaction) {
        assertTransactionAllPropertiesEquals(expectedTransaction, getPersistedTransaction(expectedTransaction));
    }

    protected void assertPersistedTransactionToMatchUpdatableProperties(Transaction expectedTransaction) {
        assertTransactionAllUpdatablePropertiesEquals(expectedTransaction, getPersistedTransaction(expectedTransaction));
    }
}

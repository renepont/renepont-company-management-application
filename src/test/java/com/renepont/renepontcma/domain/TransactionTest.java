package com.renepont.renepontcma.domain;

import static com.renepont.renepontcma.domain.CategoryTestSamples.*;
import static com.renepont.renepontcma.domain.CompanyTestSamples.*;
import static com.renepont.renepontcma.domain.TransactionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.renepont.renepontcma.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Transaction.class);
        Transaction transaction1 = getTransactionSample1();
        Transaction transaction2 = new Transaction();
        assertThat(transaction1).isNotEqualTo(transaction2);

        transaction2.setId(transaction1.getId());
        assertThat(transaction1).isEqualTo(transaction2);

        transaction2 = getTransactionSample2();
        assertThat(transaction1).isNotEqualTo(transaction2);
    }

    @Test
    void categoryTest() {
        Transaction transaction = getTransactionRandomSampleGenerator();
        Category categoryBack = getCategoryRandomSampleGenerator();

        transaction.setCategory(categoryBack);
        assertThat(transaction.getCategory()).isEqualTo(categoryBack);

        transaction.category(null);
        assertThat(transaction.getCategory()).isNull();
    }

    @Test
    void companyTest() {
        Transaction transaction = getTransactionRandomSampleGenerator();
        Company companyBack = getCompanyRandomSampleGenerator();

        transaction.setCompany(companyBack);
        assertThat(transaction.getCompany()).isEqualTo(companyBack);

        transaction.company(null);
        assertThat(transaction.getCompany()).isNull();
    }
}

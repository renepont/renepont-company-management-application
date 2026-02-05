package com.renepont.renepontcma.domain;

import static com.renepont.renepontcma.domain.CompanyTestSamples.*;
import static com.renepont.renepontcma.domain.CustomerTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.renepont.renepontcma.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Customer.class);
        Customer customer1 = getCustomerSample1();
        Customer customer2 = new Customer();
        assertThat(customer1).isNotEqualTo(customer2);

        customer2.setId(customer1.getId());
        assertThat(customer1).isEqualTo(customer2);

        customer2 = getCustomerSample2();
        assertThat(customer1).isNotEqualTo(customer2);
    }

    @Test
    void companyTest() {
        Customer customer = getCustomerRandomSampleGenerator();
        Company companyBack = getCompanyRandomSampleGenerator();

        customer.setCompany(companyBack);
        assertThat(customer.getCompany()).isEqualTo(companyBack);

        customer.company(null);
        assertThat(customer.getCompany()).isNull();
    }
}

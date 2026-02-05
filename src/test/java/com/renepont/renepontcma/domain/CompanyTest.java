package com.renepont.renepontcma.domain;

import static com.renepont.renepontcma.domain.CompanyTestSamples.*;
import static com.renepont.renepontcma.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.renepont.renepontcma.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompanyTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Company.class);
        Company company1 = getCompanySample1();
        Company company2 = new Company();
        assertThat(company1).isNotEqualTo(company2);

        company2.setId(company1.getId());
        assertThat(company1).isEqualTo(company2);

        company2 = getCompanySample2();
        assertThat(company1).isNotEqualTo(company2);
    }

    @Test
    void userProfileTest() {
        Company company = getCompanyRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        company.addUserProfile(userProfileBack);
        assertThat(company.getUserProfiles()).containsOnly(userProfileBack);

        company.removeUserProfile(userProfileBack);
        assertThat(company.getUserProfiles()).doesNotContain(userProfileBack);

        company.userProfiles(new HashSet<>(Set.of(userProfileBack)));
        assertThat(company.getUserProfiles()).containsOnly(userProfileBack);

        company.setUserProfiles(new HashSet<>());
        assertThat(company.getUserProfiles()).doesNotContain(userProfileBack);
    }
}

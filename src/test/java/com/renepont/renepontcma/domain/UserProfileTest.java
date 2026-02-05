package com.renepont.renepontcma.domain;

import static com.renepont.renepontcma.domain.CompanyTestSamples.*;
import static com.renepont.renepontcma.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.renepont.renepontcma.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserProfile.class);
        UserProfile userProfile1 = getUserProfileSample1();
        UserProfile userProfile2 = new UserProfile();
        assertThat(userProfile1).isNotEqualTo(userProfile2);

        userProfile2.setId(userProfile1.getId());
        assertThat(userProfile1).isEqualTo(userProfile2);

        userProfile2 = getUserProfileSample2();
        assertThat(userProfile1).isNotEqualTo(userProfile2);
    }

    @Test
    void companyTest() {
        UserProfile userProfile = getUserProfileRandomSampleGenerator();
        Company companyBack = getCompanyRandomSampleGenerator();

        userProfile.addCompany(companyBack);
        assertThat(userProfile.getCompanies()).containsOnly(companyBack);
        assertThat(companyBack.getUserProfiles()).containsOnly(userProfile);

        userProfile.removeCompany(companyBack);
        assertThat(userProfile.getCompanies()).doesNotContain(companyBack);
        assertThat(companyBack.getUserProfiles()).doesNotContain(userProfile);

        userProfile.companies(new HashSet<>(Set.of(companyBack)));
        assertThat(userProfile.getCompanies()).containsOnly(companyBack);
        assertThat(companyBack.getUserProfiles()).containsOnly(userProfile);

        userProfile.setCompanies(new HashSet<>());
        assertThat(userProfile.getCompanies()).doesNotContain(companyBack);
        assertThat(companyBack.getUserProfiles()).doesNotContain(userProfile);
    }
}

package com.renepont.renepontcma.repository;

import com.renepont.renepontcma.domain.Company;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class CompanyRepositoryWithBagRelationshipsImpl implements CompanyRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String COMPANIES_PARAMETER = "companies";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Company> fetchBagRelationships(Optional<Company> company) {
        return company.map(this::fetchUserProfiles);
    }

    @Override
    public Page<Company> fetchBagRelationships(Page<Company> companies) {
        return new PageImpl<>(fetchBagRelationships(companies.getContent()), companies.getPageable(), companies.getTotalElements());
    }

    @Override
    public List<Company> fetchBagRelationships(List<Company> companies) {
        return Optional.of(companies).map(this::fetchUserProfiles).orElse(Collections.emptyList());
    }

    Company fetchUserProfiles(Company result) {
        return entityManager
            .createQuery("select company from Company company left join fetch company.userProfiles where company.id = :id", Company.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Company> fetchUserProfiles(List<Company> companies) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, companies.size()).forEach(index -> order.put(companies.get(index).getId(), index));
        List<Company> result = entityManager
            .createQuery(
                "select company from Company company left join fetch company.userProfiles where company in :companies",
                Company.class
            )
            .setParameter(COMPANIES_PARAMETER, companies)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}

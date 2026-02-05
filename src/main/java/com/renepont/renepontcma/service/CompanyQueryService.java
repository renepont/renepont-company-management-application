package com.renepont.renepontcma.service;

import com.renepont.renepontcma.domain.*; // for static metamodels
import com.renepont.renepontcma.domain.Company;
import com.renepont.renepontcma.repository.CompanyRepository;
import com.renepont.renepontcma.service.criteria.CompanyCriteria;
import com.renepont.renepontcma.service.dto.CompanyDTO;
import com.renepont.renepontcma.service.mapper.CompanyMapper;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Company} entities in the database.
 * The main input is a {@link CompanyCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CompanyDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CompanyQueryService extends QueryService<Company> {

    private static final Logger LOG = LoggerFactory.getLogger(CompanyQueryService.class);

    private final CompanyRepository companyRepository;

    private final CompanyMapper companyMapper;

    public CompanyQueryService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    /**
     * Return a {@link List} of {@link CompanyDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> findByCriteria(CompanyCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<Company> specification = createSpecification(criteria);
        return companyMapper.toDto(companyRepository.fetchBagRelationships(companyRepository.findAll(specification)));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CompanyCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Company> specification = createSpecification(criteria);
        return companyRepository.count(specification);
    }

    /**
     * Function to convert {@link CompanyCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Company> createSpecification(CompanyCriteria criteria) {
        Specification<Company> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Company_.id),
                buildStringSpecification(criteria.getName(), Company_.name),
                buildStringSpecification(criteria.getNif(), Company_.nif),
                buildSpecification(criteria.getUserProfileId(), root -> root.join(Company_.userProfiles, JoinType.LEFT).get(UserProfile_.id)
                )
            );
        }
        return specification;
    }
}

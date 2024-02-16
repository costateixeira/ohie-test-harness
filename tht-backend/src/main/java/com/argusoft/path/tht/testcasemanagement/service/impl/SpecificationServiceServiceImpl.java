package com.argusoft.path.tht.testcasemanagement.service.impl;

import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.models.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.utils.ValidationUtils;
import com.argusoft.path.tht.testcasemanagement.constant.ComponentServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.SpecificationServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.filter.SpecificationCriteriaSearchFilter;
import com.argusoft.path.tht.testcasemanagement.models.entity.SpecificationEntity;
import com.argusoft.path.tht.testcasemanagement.repository.SpecificationRepository;
import com.argusoft.path.tht.testcasemanagement.service.ComponentService;
import com.argusoft.path.tht.testcasemanagement.service.SpecificationService;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseService;
import com.argusoft.path.tht.testcasemanagement.validator.SpecificationValidator;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Multimap;
import io.astefanutti.metrics.aspectj.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.CachePut;
// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * This SpecificationServiceServiceImpl contains implementation for Specification service.
 *
 * @author Dhruv
 */
@Service
@Metrics(registry = "SpecificationServiceServiceImpl")
public class SpecificationServiceServiceImpl implements SpecificationService {

    public static final Logger LOGGER = LoggerFactory.getLogger(SpecificationServiceServiceImpl.class);

    @Autowired
    SpecificationRepository specificationRepository;

    @Autowired
    TestcaseService testcaseService;

    @Autowired
    ComponentService componentService;

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "createSpecification")
    // @Caching(evict = {
    //         @CacheEvict(value = "searchSpecifications", allEntries = true),
    //         @CacheEvict(value = "searchSpecificationsList", allEntries = true),
    //         @CacheEvict(value = "getSpecifications", allEntries = true)
    // })
    public SpecificationEntity createSpecification(SpecificationEntity specificationEntity,
                                                   ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException {

        if (specificationEntity == null) {
            LOGGER.error("caught InvalidParameterException in SpecificationServiceServiceImpl");
            throw new InvalidParameterException("specificationEntity is missing");
        }

        defaultValueCreateSpecification(specificationEntity,contextInfo);

        SpecificationValidator.validateCreateUpdateSpecification(Constant.CREATE_VALIDATION,
                this,
                testcaseService,
                componentService,
                specificationEntity,
                contextInfo);

        specificationEntity = specificationRepository.saveAndFlush(specificationEntity);
        return specificationEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "updateSpecification")
    // @Caching(
    //         evict = {
    //                 @CacheEvict(value = "searchSpecifications", allEntries = true),
    //                 @CacheEvict(value = "searchSpecificationsList", allEntries = true),
    //                 @CacheEvict(value = "getSpecifications", allEntries = true)
    //         }, put = {
    //         @CachePut(value = "getSpecificationById",
    //                 key = "#specificationEntity.getId()")
    // })
    public SpecificationEntity updateSpecification(SpecificationEntity specificationEntity,
                                                   ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException {

        if (specificationEntity == null) {
            LOGGER.error("caught InvalidParameterException in SpecificationServiceServiceImpl");
            throw new InvalidParameterException("specificationEntity is missing");
        }

        SpecificationValidator.validateCreateUpdateSpecification(Constant.UPDATE_VALIDATION,
                this,
                testcaseService,
                componentService,
                specificationEntity,
                contextInfo);

        specificationEntity = specificationRepository.saveAndFlush(specificationEntity);
        return specificationEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "searchSpecifications")
    // @Cacheable(value = "searchSpecifications", key = "{ #specificationSearchFilter, #pageable }")
    public Page<SpecificationEntity> searchSpecifications(
            SpecificationCriteriaSearchFilter specificationSearchFilter,
            Pageable pageable,
            ContextInfo contextInfo)
            throws InvalidParameterException {

        Specification<SpecificationEntity> specificationEntitySpecification = specificationSearchFilter.buildSpecification(contextInfo);
        return specificationRepository.findAll(specificationEntitySpecification, pageable);
    }


    @Override
    @Timed(name = "searchSpecificationsList")
    // @Cacheable(value = "searchSpecificationsList", key = "#specificationSearchFilter")
    public List<SpecificationEntity> searchSpecifications(
            SpecificationCriteriaSearchFilter specificationSearchFilter,
            ContextInfo contextInfo)
            throws InvalidParameterException {

        Specification<SpecificationEntity> specificationEntitySpecification = specificationSearchFilter.buildSpecification(contextInfo);
        return specificationRepository.findAll(specificationEntitySpecification);
    }


    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "getSpecificationById")
    // @Cacheable(value = "getSpecificationById", key = "#specificationId")
    public SpecificationEntity getSpecificationById(String specificationId,
                                                    ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException {
        if (!StringUtils.hasLength(specificationId)) {
            throw new InvalidParameterException("SpecificationId is missing");
        }
        SpecificationCriteriaSearchFilter specificationCriteriaSearchFilter = new SpecificationCriteriaSearchFilter(specificationId);
        List<SpecificationEntity> specificationEntities = this.searchSpecifications(specificationCriteriaSearchFilter, contextInfo);
        return specificationEntities.stream()
                .findFirst()
                .orElseThrow(() -> new DoesNotExistException("Specification does not found with id : " + specificationId));
    }

    /**
     * {@inheritdoc}
     */
    @Override
    @Timed(name = "validateSpecification")
    public List<ValidationResultInfo> validateSpecification(
            String validationTypeKey,
            SpecificationEntity specificationEntity,
            ContextInfo contextInfo)
            throws InvalidParameterException,
            OperationFailedException {
        if (specificationEntity == null) {
            LOGGER.error("caught InvalidParameterException in SpecificationServiceServiceImpl");
            throw new InvalidParameterException("specificationEntity is missing");
        }
        List<ValidationResultInfo> errors = SpecificationValidator.validateSpecification(validationTypeKey, specificationEntity, this, testcaseService, componentService, contextInfo);
        return errors;
    }

    @Override
    // @Timed(name = "changeState")
    // @Caching(
    //         evict = {
    //                 @CacheEvict(value = "searchSpecifications", allEntries = true),
    //                 @CacheEvict(value = "searchSpecificationsList", allEntries = true),
    //                 @CacheEvict(value = "getSpecifications", allEntries = true)
    //         }, put = {
    //         @CachePut(value = "getSpecificationById",
    //                 key = "#specificationId")
    // })
    public SpecificationEntity changeState(String specificationId, String stateKey, ContextInfo contextInfo) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, OperationFailedException, VersionMismatchException {
        List<ValidationResultInfo> errors = new ArrayList<>();

        //validate given stateKey
        ValidationUtils.statusPresent(SpecificationServiceConstants.SPECIFICATION_STATUS, stateKey, errors);

        SpecificationEntity specificationEntity = this.getSpecificationById(specificationId, contextInfo);

        //validate transition
        ValidationUtils.transitionValid(SpecificationServiceConstants.SPECIFICATION_STATUS_MAP, specificationEntity.getState(), stateKey, errors);

        if (ValidationUtils.containsErrors(errors, ErrorLevel.ERROR)) {
            throw new DataValidationErrorException(
                    "Error(s) occurred in the validating",
                    errors);
        }

        specificationEntity.setState(stateKey);
        specificationEntity = specificationRepository.saveAndFlush(specificationEntity);

        return specificationEntity;
    }

    private void defaultValueCreateSpecification(SpecificationEntity specificationEntity, ContextInfo contextInfo) throws InvalidParameterException {
        if (!StringUtils.hasLength(specificationEntity.getId())) {
            specificationEntity.setId(UUID.randomUUID().toString());
        }
        specificationEntity.setState(SpecificationServiceConstants.SPECIFICATION_STATUS_DRAFT);
        SpecificationCriteriaSearchFilter searchFilter = new SpecificationCriteriaSearchFilter();

        specificationEntity.setRank(1);
        if(specificationEntity.getComponent() != null) {
            searchFilter.setComponentId(specificationEntity.getComponent().getId());
            List<SpecificationEntity> specifications = this.searchSpecifications(searchFilter, Constant.SINGLE_PAGE_SORT_BY_RANK, contextInfo).getContent();
            if (!specifications.isEmpty()) {
                specificationEntity.setRank(specifications.get(0).getRank() + 1);
            }
        }
    }
}

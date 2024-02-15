/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argusoft.path.tht.testcasemanagement.service.impl;

import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.models.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.utils.ValidationUtils;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseOptionServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.filter.TestcaseCriteriaSearchFilter;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseEntity;
import com.argusoft.path.tht.testcasemanagement.repository.TestcaseRepository;
import com.argusoft.path.tht.testcasemanagement.service.SpecificationService;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseService;
import com.argusoft.path.tht.testcasemanagement.validator.TestcaseValidator;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Multimap;
import io.astefanutti.metrics.aspectj.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.CachePut;
// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 * This TestcaseServiceServiceImpl contains implementation for Testcase service.
 *
 * @author Dhruv
 */
@Service
@Metrics(registry = "TestcaseServiceServiceImpl")
public class TestcaseServiceServiceImpl implements TestcaseService {

    @Autowired
    TestcaseRepository testcaseRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpecificationService specificationService;


    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "createTestcase")
    // @Caching(evict = {
    //         @CacheEvict(value = "searchTestcases", allEntries = true),
    //         @CacheEvict(value = "searchTestcasesList", allEntries = true),
    //         @CacheEvict(value = "getTestcases", allEntries = true)
    // })
    public TestcaseEntity createTestcase(TestcaseEntity testcaseEntity,
                                         ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException {

        TestcaseValidator.validateCreateUpdateTestCase(Constant.CREATE_VALIDATION,
                testcaseEntity,
                this,
                specificationService,
                applicationContext,
                contextInfo);

        if (StringUtils.isEmpty(testcaseEntity.getId())) {
            testcaseEntity.setId(UUID.randomUUID().toString());
        }
        testcaseEntity.setState(TestcaseServiceConstants.TESTCASE_STATUS_DRAFT);
        testcaseEntity = testcaseRepository.saveAndFlush(testcaseEntity);
        return testcaseEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "updateTestcase")
    // @Caching(
    //         evict = {
    //                 @CacheEvict(value = "searchTestcases", allEntries = true),
    //                 @CacheEvict(value = "searchTestcasesList", allEntries = true),
    //                 @CacheEvict(value = "getTestcases", allEntries = true)
    //         }, put = {
    //         @CachePut(value = "getTestcaseById",
    //                 key = "#testcaseEntity.getId()")
    // })
    public TestcaseEntity updateTestcase(TestcaseEntity testcaseEntity,
                                         ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException {

        TestcaseValidator.validateCreateUpdateTestCase(Constant.UPDATE_VALIDATION,
                testcaseEntity,
                this,
                specificationService,
                applicationContext,
                contextInfo);

        testcaseEntity = testcaseRepository.saveAndFlush(testcaseEntity);
        return testcaseEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "searchTestcases")
    // @Cacheable(value = "searchTestcases", key = "{ #testcaseSearchFilter, #pageable }")
    public Page<TestcaseEntity> searchTestcases(
            TestcaseCriteriaSearchFilter testcaseSearchFilter,
            Pageable pageable,
            ContextInfo contextInfo)
            throws InvalidParameterException {
        Specification<TestcaseEntity> testcaseEntitySpecification = testcaseSearchFilter.buildSpecification(contextInfo);
        return this.testcaseRepository.findAll(testcaseEntitySpecification, pageable);
    }


    @Override
    @Timed(name = "searchTestcasesList")
    // @Cacheable(value = "searchTestcasesList", key = "#testcaseSearchFilter")
    public List<TestcaseEntity> searchTestcases(
            TestcaseCriteriaSearchFilter testcaseSearchFilter,
            ContextInfo contextInfo)
            throws InvalidParameterException {
        Specification<TestcaseEntity> testcaseEntitySpecification = testcaseSearchFilter.buildSpecification(contextInfo);
        return this.testcaseRepository.findAll(testcaseEntitySpecification);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    @Timed(name = "getTestcaseById")
    // @Cacheable(value = "getTestcaseById", key = "#testcaseId")
    public TestcaseEntity getTestcaseById(String testcaseId,
                                          ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException {
        if (StringUtils.isEmpty(testcaseId)) {
            throw new InvalidParameterException("TestcaseId is missing");
        }
        TestcaseCriteriaSearchFilter testcaseCriteriaSearchFilter = new TestcaseCriteriaSearchFilter(testcaseId);
        List<TestcaseEntity> testcaseEntities = this.searchTestcases(testcaseCriteriaSearchFilter, contextInfo);
        return testcaseEntities.stream()
                .findFirst()
                .orElseThrow(() -> new DoesNotExistException("Testcase does not found with id : " + testcaseId));
    }

    /**
     * {@inheritdoc}
     */
    @Override
    @Timed(name = "validateTestcase")
    public List<ValidationResultInfo> validateTestcase(
            String validationTypeKey,
            TestcaseEntity testcaseEntity,
            ContextInfo contextInfo)
            throws InvalidParameterException,
            OperationFailedException {

        List<ValidationResultInfo> errors = TestcaseValidator.validateTestCase(validationTypeKey, testcaseEntity, this, specificationService, applicationContext, contextInfo);
        return errors;
    }

    @Override
    @Timed(name = "changeState")
    // @Caching(
    //         evict = {
    //                 @CacheEvict(value = "searchTestcases", allEntries = true),
    //                 @CacheEvict(value = "searchTestcasesList", allEntries = true),
    //                 @CacheEvict(value = "getTestcases", allEntries = true)
    //         }, put = {
    //         @CachePut(value = "getTestcaseById",
    //                 key = "#testcaseId")
    // })
    public TestcaseEntity changeState(String testcaseId, String stateKey, ContextInfo contextInfo) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, OperationFailedException, VersionMismatchException {
        List<ValidationResultInfo> errors = new ArrayList<>();

        //validate given stateKey
        ValidationUtils.statusPresent(TestcaseServiceConstants.TESTCASE_STATUS, stateKey, errors);

        TestcaseEntity testcaseEntity = this.getTestcaseById(testcaseId, contextInfo);
        String currentState = testcaseEntity.getState();

        //validate transition
        ValidationUtils.transitionValid(TestcaseServiceConstants.TESTCASE_STATUS_MAP, currentState, stateKey, errors);

        if (ValidationUtils.containsErrors(errors, ErrorLevel.ERROR)) {
            throw new DataValidationErrorException(
                    "Error(s) occurred in the validating",
                    errors);
        }

        testcaseEntity.setState(stateKey);
        testcaseEntity = testcaseRepository.saveAndFlush(testcaseEntity);

        return testcaseEntity;
    }
}

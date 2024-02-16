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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


/**
 * This TestcaseServiceServiceImpl contains implementation for Testcase service.
 *
 * @author Dhruv
 */
@Service
@Metrics(registry = "TestcaseServiceServiceImpl")
public class TestcaseServiceServiceImpl implements TestcaseService {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestcaseServiceServiceImpl.class);

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

        if (testcaseEntity == null) {
            LOGGER.error("caught InvalidParameterException in TestcaseServiceServiceImpl");
            throw new InvalidParameterException("testcaseEntity is missing");
        }

        defaultValueCreateTestCase(testcaseEntity, contextInfo);

        TestcaseValidator.validateCreateUpdateTestCase(Constant.CREATE_VALIDATION,
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

        if (testcaseEntity == null) {
            LOGGER.error("caught InvalidParameterException in TestcaseServiceServiceImpl");
            throw new InvalidParameterException("testcaseEntity is missing");
        }

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
        if (!StringUtils.hasLength(testcaseId)) {
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

        if (testcaseEntity == null) {
            LOGGER.error("caught InvalidParameterException in TestcaseServiceServiceImpl");
            throw new InvalidParameterException("testcaseEntity is missing");
        }

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

        //validate transition
        ValidationUtils.transitionValid(TestcaseServiceConstants.TESTCASE_STATUS_MAP, testcaseEntity.getState(), stateKey, errors);

        if (ValidationUtils.containsErrors(errors, ErrorLevel.ERROR)) {
            throw new DataValidationErrorException(
                    "Error(s) occurred in the validating",
                    errors);
        }

        testcaseEntity.setState(stateKey);
        testcaseEntity = testcaseRepository.saveAndFlush(testcaseEntity);

        return testcaseEntity;
    }

    private void defaultValueCreateTestCase(TestcaseEntity testcaseEntity, ContextInfo contextInfo) throws InvalidParameterException {
        if (!StringUtils.hasLength(testcaseEntity.getId())) {
            testcaseEntity.setId(UUID.randomUUID().toString());
        }
        testcaseEntity.setState(TestcaseServiceConstants.TESTCASE_STATUS_DRAFT);

        TestcaseCriteriaSearchFilter searchFilter = new TestcaseCriteriaSearchFilter();

        testcaseEntity.setRank(1);
        if(testcaseEntity.getSpecification() != null) {
            searchFilter.setSpecificationId(testcaseEntity.getSpecification().getId());
            List<TestcaseEntity> testCases = this.searchTestcases(searchFilter, Constant.SINGLE_PAGE_SORT_BY_RANK, contextInfo).getContent();
            if (!testCases.isEmpty()) {
                testcaseEntity.setRank(testCases.get(0).getRank() + 1);
            }
        }
    }
}

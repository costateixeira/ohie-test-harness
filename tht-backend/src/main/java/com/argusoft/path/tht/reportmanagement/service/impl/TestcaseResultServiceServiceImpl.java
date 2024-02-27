package com.argusoft.path.tht.reportmanagement.service.impl;

import com.argusoft.path.tht.Audit.Service.AuditService;
import com.argusoft.path.tht.reportmanagement.constant.TestcaseResultServiceConstants;
import com.argusoft.path.tht.reportmanagement.evaluator.GradeEvaluator;
import com.argusoft.path.tht.reportmanagement.filter.TestResultRelationCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.filter.TestcaseResultCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.models.entity.TestResultRelationEntity;
import com.argusoft.path.tht.reportmanagement.models.entity.TestcaseResultEntity;
import com.argusoft.path.tht.reportmanagement.models.mapper.TestcaseResultMapper;
import com.argusoft.path.tht.reportmanagement.repository.TestcaseResultRepository;
import com.argusoft.path.tht.reportmanagement.service.TestResultRelationService;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultService;
import com.argusoft.path.tht.reportmanagement.validator.TestcaseResultValidator;
import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.models.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.models.entity.IdMetaEntity;
import com.argusoft.path.tht.systemconfiguration.utils.ValidationUtils;
import com.argusoft.path.tht.testcasemanagement.constant.ComponentServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.SpecificationServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseOptionServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseOptionEntity;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseOptionService;
import com.argusoft.path.tht.testprocessmanagement.constant.TestRequestServiceConstants;
import com.argusoft.path.tht.testprocessmanagement.models.entity.TestRequestEntity;
import com.argusoft.path.tht.testprocessmanagement.service.TestRequestService;
import com.argusoft.path.tht.usermanagement.models.entity.UserEntity;
import com.argusoft.path.tht.usermanagement.service.UserService;
import com.google.common.collect.Multimap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.TestCase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This TestcaseResultServiceServiceImpl contains implementation for TestcaseResult service.
 *
 * @author Dhruv
 */
@Service
public class TestcaseResultServiceServiceImpl implements TestcaseResultService {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestcaseResultServiceServiceImpl.class);

    @Autowired
    private TestcaseResultRepository testcaseResultRepository;

    @Autowired
    TestcaseResultMapper testcaseResultMapper;

    @Autowired
    private TestcaseOptionService testcaseOptionService;

    @Autowired
    private TestRequestService testRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private GradeEvaluator gradeEvaluator;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TestResultRelationService testResultRelationService;

    @Autowired
    SimpMessagingTemplate msgTemplate;

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public TestcaseResultEntity createTestcaseResult(TestcaseResultEntity testcaseResultEntity,
                                                     ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException, DoesNotExistException {

        if (testcaseResultEntity == null) {
            LOGGER.error("caught InvalidParameterException in TestcaseResultServiceServiceImpl ");
            throw new InvalidParameterException("TestcaseResultEntity is missing");
        }

        defaultValueCreateTestCaseResult(testcaseResultEntity, contextInfo);

        TestcaseResultValidator.validateCreateUpdateTestCaseResult(Constant.CREATE_VALIDATION,
                this,
                userService,
                testcaseOptionService,
                testRequestService,
                testcaseResultEntity,
                contextInfo);

        testcaseResultEntity = testcaseResultRepository.saveAndFlush(testcaseResultEntity);
        return testcaseResultEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */

    @Override
    public TestcaseResultEntity updateTestcaseResult(TestcaseResultEntity testcaseResultEntity,
                                                     ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException {

        if (testcaseResultEntity == null) {
            LOGGER.error("caught InvalidParameterException in TestcaseResultServiceServiceImpl ");
            throw new InvalidParameterException("TestcaseResultEntity is missing");
        }

        TestcaseResultValidator.validateCreateUpdateTestCaseResult(Constant.UPDATE_VALIDATION,
                this,
                userService,
                testcaseOptionService,
                testRequestService,
                testcaseResultEntity,
                contextInfo);

        testcaseResultEntity = testcaseResultRepository.saveAndFlush(testcaseResultEntity);
        return testcaseResultEntity;
    }


    @Override
    public TestcaseResultEntity submitTestcaseResult(String testcaseResultId, Set<String> selectedTestcaseOptionIds, ContextInfo contextInfo) throws OperationFailedException, VersionMismatchException, DataValidationErrorException, InvalidParameterException, DoesNotExistException {

        TestcaseResultValidator.validateSubmitTestcaseResult(
                testcaseResultId,
                selectedTestcaseOptionIds,
                Constant.SUBMIT_VALIDATION,
                this,
                testcaseOptionService,
                testResultRelationService,
                auditService,
                contextInfo);

        TestResultRelationCriteriaSearchFilter testResultRelationCriteriaSearchFilter = new TestResultRelationCriteriaSearchFilter();
        testResultRelationCriteriaSearchFilter.setTestcaseResultId(testcaseResultId);
        testResultRelationCriteriaSearchFilter.setRefObjUri(TestcaseOptionServiceConstants.TESTCASE_OPTION_REF_OBJ_URI);

        List<TestResultRelationEntity> testResultRelationEntities = testResultRelationService.searchTestResultRelation(testResultRelationCriteriaSearchFilter, contextInfo);

        List<String> resultRelationIds = testResultRelationEntities.stream().map(IdMetaEntity::getId).collect(Collectors.toList());

        List<TestcaseOptionEntity> testcaseOptionEntitiesFromAudit =
                testResultRelationService.getTestResultRelationEntitiesFromAuditMapping(resultRelationIds, contextInfo)
                        .stream()
                        .map(TestcaseOptionEntity.class::cast)
                        .toList();


        for (TestResultRelationEntity testResultRelationEntity : testResultRelationEntities) {
            testResultRelationEntity.setSelected(selectedTestcaseOptionIds.contains(testResultRelationEntity.getRefId()));
            testResultRelationService.updateTestcaseResult(testResultRelationEntity,contextInfo);
        }


        boolean isSuccess = isSuccessFromTestResultRelationAndSelectedOption(testResultRelationEntities, testcaseOptionEntitiesFromAudit);


        TestcaseResultEntity testcaseResultEntity
                = this.getTestcaseResultById(testcaseResultId, contextInfo);

        testcaseResultEntity.setSuccess(isSuccess);

        UserEntity userEntity =
                userService.getPrincipalUser(contextInfo);
        testcaseResultEntity.setTester(userEntity);

        testcaseResultEntity = testcaseResultRepository.saveAndFlush(testcaseResultEntity);

        if (!TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED.equals(testcaseResultEntity.getState())) {
            changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED, contextInfo);
        }

        return testcaseResultEntity;
    }

    private boolean isSuccessFromTestResultRelationAndSelectedOption(List<TestResultRelationEntity> testResultRelationEntities, List<TestcaseOptionEntity> testcaseOptionEntitiesFromAudit) {

        for (TestResultRelationEntity testResultRelationEntity : testResultRelationEntities) {
            // check if testResultRelationEntity is success or false
            boolean isItSuccess = testcaseOptionEntitiesFromAudit.stream()
                    .filter(testcaseOption -> testcaseOption.getId().equals(testResultRelationEntity.getRefId()))
                    .anyMatch(TestcaseOptionEntity::getSuccess);

            if (testResultRelationEntity.getSelected() != isItSuccess) {
                // if (relation.selected == true && isSuccess == false) return false;
                // if (relation.selected == false && isSuccess == true) return false;
                return false;
            }
        }

        return true;
    }



    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public Page<TestcaseResultEntity> searchTestcaseResults(
            TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter,
            Pageable pageable,
            ContextInfo contextInfo)
            throws InvalidParameterException {

        Specification<TestcaseResultEntity> testcaseEntitySpecification = testcaseResultCriteriaSearchFilter.buildSpecification(contextInfo);
        return testcaseResultRepository.findAll(testcaseEntitySpecification, pageable);
    }

    @Override
    public List<TestcaseResultEntity> searchTestcaseResults(
            TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter,
            ContextInfo contextInfo)
            throws InvalidParameterException {
        Specification<TestcaseResultEntity> testcaseResultEntitySpecification = testcaseResultCriteriaSearchFilter.buildSpecification(contextInfo);
        return testcaseResultRepository.findAll(testcaseResultEntitySpecification);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public TestcaseResultEntity getTestcaseResultById(String testcaseResultId,
                                                      ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException {
        if (!StringUtils.hasLength(testcaseResultId)) {
            throw new InvalidParameterException("TestcaseResultId is missing");
        }
        TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter(testcaseResultId);
        List<TestcaseResultEntity> testcaseResultEntities = this.searchTestcaseResults(testcaseResultCriteriaSearchFilter, contextInfo);
        return testcaseResultEntities.stream()
                .findFirst()
                .orElseThrow(() -> new DoesNotExistException("TestcaseResult does not found with id : " + testcaseResultId));
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public List<ValidationResultInfo> validateTestcaseResult(
            String validationTypeKey,
            TestcaseResultEntity testcaseResultEntity,
            ContextInfo contextInfo)
            throws InvalidParameterException,
            OperationFailedException {
        if (testcaseResultEntity == null) {
            LOGGER.error("caught InvalidParameterException in TestcaseResultServiceServiceImpl ");
            throw new InvalidParameterException("TestcaseResultEntity is missing");
        }
        List<ValidationResultInfo> errors = TestcaseResultValidator.validateTestCaseResult(validationTypeKey, testcaseResultEntity, userService, this, testcaseOptionService, testRequestService, contextInfo);
        return errors;
    }

    @Override
    public TestcaseResultEntity changeState(String testcaseResultId, String stateKey, ContextInfo contextInfo) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, OperationFailedException, VersionMismatchException {

        List<ValidationResultInfo> errors = new ArrayList<>();

        //validate given stateKey
        ValidationUtils.statusPresent(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS, stateKey, errors);

        TestcaseResultEntity testcaseResultEntity = this.getTestcaseResultById(testcaseResultId, contextInfo);

        //validate transition
        ValidationUtils.transitionValid(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_MAP, testcaseResultEntity.getState(), stateKey, errors);

        if (ValidationUtils.containsErrors(errors, ErrorLevel.ERROR)) {
            throw new DataValidationErrorException(
                    "Error(s) occurred in the validating",
                    errors);
        }

        if (!stateKey.equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
            testcaseResultEntity.setMessage(null);
            testcaseResultEntity.setHasSystemError(Boolean.FALSE);
            testcaseResultEntity.setSuccess(Boolean.FALSE);
            testcaseResultEntity.setSuccess(Boolean.FALSE);
        }

        if (ValidationUtils.containsErrors(errors, ErrorLevel.ERROR)) {
            throw new DataValidationErrorException(
                    "Error(s) occurred in the validating",
                    errors);
        }

        testcaseResultEntity.setState(stateKey);
        testcaseResultEntity = testcaseResultRepository.saveAndFlush(testcaseResultEntity);

        // Notify client if the state is changed to finished
        if(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI.equals(testcaseResultEntity.getRefObjUri())) {
            notifyTestCaseFinished(testcaseResultEntity, contextInfo);
        }

        if (!testcaseResultEntity.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            updateChildTestcaseResult(testcaseResultEntity, contextInfo);
        }

        if (testcaseResultEntity.getParentTestcaseResult() != null) {
            updateParentTestcaseResult(testcaseResultEntity.getParentTestcaseResult(), contextInfo);
        } else {
            updateTestRequestState(testcaseResultEntity.getTestRequest(), contextInfo);
        }
        return testcaseResultEntity;
    }

    private void updateTestRequestState(
            TestRequestEntity testRequestEntity,
            ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DoesNotExistException,
            DataValidationErrorException,
            VersionMismatchException {

        TestcaseResultCriteriaSearchFilter searchFilter = new TestcaseResultCriteriaSearchFilter();
        searchFilter.setTestRequestId(testRequestEntity.getId());

        List<TestcaseResultEntity> testcaseResultEntities = this.searchTestcaseResults(searchFilter, contextInfo);

        if (testcaseResultEntities.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            if (!testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_SKIPPED)) {
                testRequestService.changeState(testRequestEntity.getId(), TestRequestServiceConstants.TEST_REQUEST_STATUS_SKIPPED, contextInfo);
            }
        } else if (testcaseResultEntities.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)
                        || tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            if (!testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED)) {
                testRequestService.changeState(testRequestEntity.getId(), TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED, contextInfo);
            }
        } else if (testcaseResultEntities.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT))) {
            if (!testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED)) {
                testRequestService.changeState(testRequestEntity.getId(), TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED, contextInfo);
            }
        } else if (!testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_INPROGRESS)) {
            testRequestService.changeState(testRequestEntity.getId(), TestRequestServiceConstants.TEST_REQUEST_STATUS_INPROGRESS, contextInfo);
        }
    }

    private void updateChildTestcaseResult(
            TestcaseResultEntity testcaseResultEntity,
            ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DoesNotExistException,
            DataValidationErrorException,
            VersionMismatchException {
        if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP)) {
            return;
        }

        TestcaseResultCriteriaSearchFilter searchFilter = new TestcaseResultCriteriaSearchFilter();
        searchFilter.setParentTestcaseResultId(testcaseResultEntity.getId());


        List<TestcaseResultEntity> testcaseResultEntities = this.searchTestcaseResults(searchFilter, contextInfo);
        for (TestcaseResultEntity testcaseResult : testcaseResultEntities) {
            if (!testcaseResult.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP)) {
                changeState(testcaseResult.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP, contextInfo);
            }
        }
    }

    private void updateParentTestcaseResult(
            TestcaseResultEntity testcaseResultEntity,
            ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DoesNotExistException,
            DataValidationErrorException,
            VersionMismatchException {
        TestcaseResultCriteriaSearchFilter searchFilter = new TestcaseResultCriteriaSearchFilter();
        searchFilter.setParentTestcaseResultId(testcaseResultEntity.getId());

        List<TestcaseResultEntity> testcaseResultEntities = this.searchTestcaseResults(searchFilter, contextInfo);
        if (testcaseResultEntities.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP)) {
                testcaseResultEntity.setSuccess(Boolean.TRUE);
                updateTestcaseResult(testcaseResultEntity, contextInfo);
                changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP, contextInfo);
            }
        } else if (testcaseResultEntities.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)
                        || tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
                Long duration = 0L;
                for (TestcaseResultEntity tcr : testcaseResultEntities) {
                    if (tcr.getDuration() != null) {
                        duration = duration + tcr.getDuration();
                    }
                }
                testcaseResultEntity.setSuccess(
                        testcaseResultEntities.stream()
                                .allMatch(tre -> {
                                    return !Objects.equals(tre.getRequired(), Boolean.TRUE)
                                            || Objects.equals(tre.getSuccess(), Boolean.TRUE);
                                }));
                testcaseResultEntity.setDuration(duration);
                updateTestcaseResult(testcaseResultEntity, contextInfo);
                changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED, contextInfo);
                updateTestcaseResult(testcaseResultEntity, contextInfo);
            }
        } else if (testcaseResultEntities.stream()
                .anyMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS))) {
            if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS)) {
                changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS, contextInfo);
            }
        } else if (testcaseResultEntities.stream()
                .anyMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING))) {
            if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING)) {
                changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING, contextInfo);
            }
        } else {
            if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT)) {
                changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT, contextInfo);
            }
        }
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public TestcaseResultEntity getTestcaseResultStatus(
            String testcaseResultId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException, OperationFailedException {
        if (!StringUtils.hasLength(testcaseResultId)) {
            throw new InvalidParameterException("TestcaseResultId is missing");
        }

        TestcaseResultEntity testcaseResultEntity = this.getTestcaseResultById(testcaseResultId, contextInfo);

        return fetchTestcaseResultStatusByInputs(
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                testcaseResultEntity,
                contextInfo);
    }

    @Override
    public List<String> getSubClassesNameForTestCase() {
        Reflections reflections = new Reflections(TestcaseServiceConstants.PACKAGE_NAME);
        Set<Class<? extends TestCase>> subTypes = reflections.getSubTypesOf(TestCase.class);
        List<String> classNames = new ArrayList<>();
        for (Class<?> subType : subTypes) {
            classNames.add(subType.getSimpleName());
        }
        return classNames;
    }

    private TestcaseResultEntity fetchTestcaseResultStatusByInputs(
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            TestcaseResultEntity testcaseResultEntity,
            ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {

        if (testcaseResultEntity.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            if(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS.equals(testcaseResultEntity.getState())) {
                testcaseResultEntity.setDuration(System.currentTimeMillis() - testcaseResultEntity.getUpdatedAt().getTime());
            }
            return testcaseResultEntity;
        }

        List<TestcaseResultEntity> filteredTestcaseResults = new ArrayList<>();

        TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
        testcaseResultCriteriaSearchFilter.setTestRequestId(testcaseResultEntity.getTestRequest().getId());
        testcaseResultCriteriaSearchFilter.setManual(isManual);
        testcaseResultCriteriaSearchFilter.setAutomated(isAutomated);
        testcaseResultCriteriaSearchFilter.setRequired(isRequired);
        testcaseResultCriteriaSearchFilter.setRecommended(isRecommended);
        testcaseResultCriteriaSearchFilter.setFunctional(isFunctional);
        testcaseResultCriteriaSearchFilter.setWorkflow(isWorkflow);

        List<TestcaseResultEntity> testcaseResultEntities = this.searchTestcaseResults(testcaseResultCriteriaSearchFilter, Constant.FULL_PAGE, contextInfo).getContent();

        recalculateTestcaseResultEntity(testcaseResultEntity, testcaseResultEntities);

        if(Boolean.TRUE.equals(isRecommended) && testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
            if(ComponentServiceConstants.COMPONENT_REF_OBJ_URI.equals(testcaseResultEntity.getRefObjUri())) {
                List<TestcaseResultEntity> specificationsFromComponent = getChildTestcaseResultFromParentTestcaseResult(testcaseResultEntity, testcaseResultEntities);
                for (TestcaseResultEntity specificationTestcase : specificationsFromComponent) {
                    recalculateTestcaseResultEntity(specificationTestcase, testcaseResultEntities);
                }
                testcaseResultEntity.setGrade(gradeEvaluator.evaluate(specificationsFromComponent ,contextInfo));
            } else if(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI.equals(testcaseResultEntity.getRefObjUri())) {
                List<TestcaseResultEntity> componentFromTestRequest = getChildTestcaseResultFromParentTestcaseResult(testcaseResultEntity, testcaseResultEntities);

                for (TestcaseResultEntity componentTestRequest : componentFromTestRequest) {
                    List<TestcaseResultEntity> specificationsFromComponent = getChildTestcaseResultFromParentTestcaseResult(componentTestRequest, testcaseResultEntities);
                    for (TestcaseResultEntity specificationTestcase : specificationsFromComponent) {
                        recalculateTestcaseResultEntity(specificationTestcase, testcaseResultEntities);
                    }
                    componentTestRequest.setSuccess(specificationsFromComponent.stream().allMatch(entity -> Boolean.TRUE.equals(entity.getSuccess())));
                }
                testcaseResultEntity.setGrade(gradeEvaluator.evaluate(componentFromTestRequest ,contextInfo));
            }
        }
        return testcaseResultEntity;
    }

    private void recalculateTestcaseResultEntity(TestcaseResultEntity testcaseResultEntity, List<TestcaseResultEntity> testcaseResultEntities) {
        List<TestcaseResultEntity> filteredTestcaseResults;
        if (testcaseResultEntity.getRefObjUri().equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI)) {
            filteredTestcaseResults = getFilteredTestcaseResultsForSpecification(testcaseResultEntity, testcaseResultEntities);
        } else if (testcaseResultEntity.getRefObjUri().equals(ComponentServiceConstants.COMPONENT_REF_OBJ_URI)) {
            List<String> specificationTestcaseResultIds = getChildTestcaseResultFromParentTestcaseResult(testcaseResultEntity, testcaseResultEntities).stream().map(entity -> entity.getId()).toList();
            filteredTestcaseResults
                    = testcaseResultEntities.stream()
                    .filter(tcre -> {
                        return tcre.getParentTestcaseResult() != null
                                && specificationTestcaseResultIds.contains(tcre.getParentTestcaseResult().getId());
                    }).collect(Collectors.toList());
        } else {
            filteredTestcaseResults
                    = testcaseResultEntities.stream()
                    .filter(tcre -> {
                        return tcre.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI);
                    })
                    .collect(Collectors.toList());
        }

        if (filteredTestcaseResults.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP);
        } else if (filteredTestcaseResults.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)
                        || tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            //set duration
            Long duration = 0L;
            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                if (tcr.getDuration() != null) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);

            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED);
        } else if (filteredTestcaseResults.stream()
                .anyMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS))) {
            //set duration
            Long duration = System.currentTimeMillis() - testcaseResultEntity.getUpdatedAt().getTime();
            for (TestcaseResultEntity tcr : testcaseResultEntities) {
                if (tcr.getDuration() != null) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);


            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS);
        } else if (filteredTestcaseResults.stream()
                .anyMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING))) {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING);
        } else {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT);
        }

        //set success
        testcaseResultEntity.setSuccess(filteredTestcaseResults.stream().allMatch(testcaseResult -> Boolean.TRUE.equals(testcaseResult.getSuccess())));
    }

    private static List<TestcaseResultEntity> getChildTestcaseResultFromParentTestcaseResult(TestcaseResultEntity testcaseResultEntity, List<TestcaseResultEntity> testcaseResultEntities) {
        List<TestcaseResultEntity> specificationTestcaseResultIds = testcaseResultEntities.stream()
                .filter(tcre -> {
                    return tcre.getParentTestcaseResult() != null
                            && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                }).collect(Collectors.toList());
        return specificationTestcaseResultIds;
    }

    private static List<TestcaseResultEntity> getFilteredTestcaseResultsForSpecification(TestcaseResultEntity testcaseResultEntity, List<TestcaseResultEntity> testcaseResultEntities) {
        List<TestcaseResultEntity> filteredTestcaseResults;
        filteredTestcaseResults
                = testcaseResultEntities.stream()
                .filter(tcre -> {
                    return tcre.getParentTestcaseResult() != null
                            && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                }).collect(Collectors.toList());
        return filteredTestcaseResults;
    }


    private void defaultValueCreateTestCaseResult(TestcaseResultEntity testcaseResultEntity, ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, OperationFailedException {
        if (!StringUtils.hasLength(testcaseResultEntity.getId())) {
            testcaseResultEntity.setId(UUID.randomUUID().toString());
        }
        testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT);
        testcaseResultEntity.setDuration(null);
    }

    private void notifyTestCaseFinished(TestcaseResultEntity testcaseResultEntity, ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {

        if (testcaseResultEntity.getAutomated().equals(Boolean.TRUE)) {
            String destination = "/testcase-result/" + testcaseResultEntity.getId();
            fetchTestcaseResultStatusByInputs(
                    null,
                    true,
                    null,
                    null,
                    null,
                    null,
                    testcaseResultEntity,
                    contextInfo
            );
            msgTemplate.convertAndSend(destination, testcaseResultMapper.modelToDto(testcaseResultEntity));
            if (testcaseResultEntity.getParentTestcaseResult() != null) {
                notifyTestCaseFinished(testcaseResultEntity.getParentTestcaseResult(), contextInfo);
            }
        }
    }
}

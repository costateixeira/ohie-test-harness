package com.argusoft.path.tht.reportmanagement.service.impl;

import com.argusoft.path.tht.reportmanagement.constant.TestcaseResultServiceConstants;
import com.argusoft.path.tht.reportmanagement.evaluator.GradeEvaluator;
import com.argusoft.path.tht.reportmanagement.event.TestcaseResultStateChangedEvent;
import com.argusoft.path.tht.reportmanagement.filter.TestResultRelationCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.filter.TestcaseResultCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.models.dto.TestcaseResultAnswerInfo;
import com.argusoft.path.tht.reportmanagement.models.entity.TestResultRelationEntity;
import com.argusoft.path.tht.reportmanagement.models.entity.TestcaseResultEntity;
import com.argusoft.path.tht.reportmanagement.repository.TestcaseResultRepository;
import com.argusoft.path.tht.reportmanagement.service.TestResultRelationService;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultService;
import com.argusoft.path.tht.reportmanagement.validator.TestcaseResultValidator;
import com.argusoft.path.tht.systemconfiguration.audit.service.AuditService;
import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.constant.ValidateConstant;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.models.entity.IdMetaEntity;
import com.argusoft.path.tht.systemconfiguration.models.entity.IdStateNameMetaEntity;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.utils.CommonStateChangeValidator;
import com.argusoft.path.tht.testcasemanagement.constant.ComponentServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.SpecificationServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseOptionServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.models.entity.ComponentEntity;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseEntity;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseOptionEntity;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseOptionService;
import com.argusoft.path.tht.testcasemanagement.testbed.dto.start.response.StartResponse;
import com.argusoft.path.tht.testcasemanagement.testbed.dto.status.response.StatusResponse;
import com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.TestCase;
import com.argusoft.path.tht.testprocessmanagement.constant.TestRequestServiceConstants;
import com.argusoft.path.tht.testprocessmanagement.execute.strategy.impl.testbed.TestSessionStarter;
import com.argusoft.path.tht.testprocessmanagement.execute.strategy.impl.testbed.VerifyTestcaseSessionStatusAndUpdateAccordingly;
import com.argusoft.path.tht.testprocessmanagement.execute.strategy.impl.testbed.callback.impl.StatusCallbackLoggerHandler;
import com.argusoft.path.tht.testprocessmanagement.models.entity.TestRequestEntity;
import com.argusoft.path.tht.testprocessmanagement.service.TestRequestService;
import com.argusoft.path.tht.usermanagement.models.entity.UserEntity;
import com.argusoft.path.tht.usermanagement.service.UserService;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This TestcaseResultServiceServiceImpl contains implementation for
 * TestcaseResult service.
 *
 * @author Dhruv
 */
@Service
public class TestcaseResultServiceServiceImpl implements TestcaseResultService {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestcaseResultServiceServiceImpl.class);
    ApplicationEventPublisher applicationEventPublisher;
    private TestcaseResultRepository testcaseResultRepository;
    private TestcaseOptionService testcaseOptionService;
    private TestRequestService testRequestService;
    private UserService userService;
    private GradeEvaluator gradeEvaluator;
    private AuditService auditService;

    @Value("${testbed.actor-api}")
    private String testbedActorApiKey;

    private TestSessionStarter testSessionStarter;
    private StatusCallbackLoggerHandler statusCallback;

    private VerifyTestcaseSessionStatusAndUpdateAccordingly verifyTestcaseSessionStatusAndUpdateAccordingly;
    private TestResultRelationService testResultRelationService;

    private static String getMessage(String x, TestcaseResultEntity testcaseResultEntity, String x1, List<String> failedSpecificationTestcaseResultName) {
        StringBuilder message = new StringBuilder(x + testcaseResultEntity.getName() + x1);
        for (int i = 0; i < (failedSpecificationTestcaseResultName.size() - 1); i++) {
            message.append(" <b>").append(failedSpecificationTestcaseResultName.get(i)).append("<b>,");
        }
        if (failedSpecificationTestcaseResultName.size() > 1) {
            message.append(" and ");
        }

        message.append("<b>").append(failedSpecificationTestcaseResultName.get(failedSpecificationTestcaseResultName.size() - 1)).append("<b>");

        return message.toString();
    }

    private static List<TestcaseResultEntity> getFilteredChileTestcaseResultsForTestResult(TestcaseResultEntity testcaseResultEntity, List<TestcaseResultEntity> testcaseResultEntities) {
        return testcaseResultEntities.stream()
                .filter(tcre -> {
                    return tcre.getParentTestcaseResult() != null
                            && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                }).collect(Collectors.toList());
    }

    @Autowired
    public void setTestcaseResultRepository(TestcaseResultRepository testcaseResultRepository) {
        this.testcaseResultRepository = testcaseResultRepository;
    }

    @Autowired
    public void setTestcaseOptionService(TestcaseOptionService testcaseOptionService) {
        this.testcaseOptionService = testcaseOptionService;
    }

    @Autowired
    public void setTestRequestService(@Lazy TestRequestService testRequestService) {
        this.testRequestService = testRequestService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setGradeEvaluator(GradeEvaluator gradeEvaluator) {
        this.gradeEvaluator = gradeEvaluator;
    }

    @Autowired
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    @Autowired
    public void setTestResultRelationService(TestResultRelationService testResultRelationService) {
        this.testResultRelationService = testResultRelationService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

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
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestcaseResultServiceServiceImpl.class.getSimpleName());
            throw new InvalidParameterException(TestcaseResultServiceConstants.TESTCASE_RESULT_MISSING);
        }

        defaultValueCreateTestCaseResult(testcaseResultEntity);

        TestcaseResultValidator.validateCreateUpdateTestCaseResult(Constant.CREATE_VALIDATION,
                this,
                userService,
                testRequestService,
                testcaseResultEntity,
                contextInfo);

        if (!testcaseResultEntity.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            testcaseResultEntity.setMessage(null);
        }
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
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestcaseResultServiceServiceImpl.class.getSimpleName());
            throw new InvalidParameterException(TestcaseResultServiceConstants.TESTCASE_RESULT_MISSING);
        }

        TestcaseResultValidator.validateCreateUpdateTestCaseResult(Constant.UPDATE_VALIDATION,
                this,
                userService,
                testRequestService,
                testcaseResultEntity,
                contextInfo);

        if (!testcaseResultEntity.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            testcaseResultEntity.setMessage(null);
        }
        testcaseResultEntity = testcaseResultRepository.saveAndFlush(testcaseResultEntity);

        return testcaseResultEntity;
    }

    @Override
    public List<TestcaseResultEntity> submitTestcaseResult(List<TestcaseResultAnswerInfo> testcaseResultAnswerInfos, ContextInfo contextInfo) throws OperationFailedException, VersionMismatchException, DataValidationErrorException, InvalidParameterException, DoesNotExistException {
        List<TestcaseResultEntity> resultEntities = new ArrayList<>();
        for(TestcaseResultAnswerInfo testcaseResultAnswerInfo : testcaseResultAnswerInfos){
            resultEntities.add(submitTestcaseResult(testcaseResultAnswerInfo.getTestcaseResultId(), testcaseResultAnswerInfo.getSelectedTestcaseOptionIds(), contextInfo));
        }
        return  resultEntities;
    }

    private TestcaseResultEntity submitTestcaseResult (String testcaseResultId, Set<String> selectedTestcaseOptionIds, ContextInfo contextInfo ) throws DataValidationErrorException, InvalidParameterException, DoesNotExistException, OperationFailedException, VersionMismatchException {
        TestcaseResultValidator.validateSubmitTestcaseResult(
                testcaseResultId,
                selectedTestcaseOptionIds,
                this,
                testResultRelationService,
                contextInfo);

        TestResultRelationCriteriaSearchFilter testResultRelationCriteriaSearchFilter = new TestResultRelationCriteriaSearchFilter();
        testResultRelationCriteriaSearchFilter.setTestcaseResultId(testcaseResultId);
        testResultRelationCriteriaSearchFilter.setRefObjUri(TestcaseOptionServiceConstants.TESTCASE_OPTION_REF_OBJ_URI);

        List<TestResultRelationEntity> testResultRelationEntities = testResultRelationService.searchTestResultRelation(testResultRelationCriteriaSearchFilter, contextInfo);

        List<String> resultRelationIds = testResultRelationEntities.stream().map(IdMetaEntity::getId).collect(Collectors.toList());

        List<TestcaseOptionEntity> testcaseOptionEntitiesFromAudit
                = testResultRelationService.getTestResultRelationEntitiesFromAuditMapping(resultRelationIds, contextInfo)
                .stream()
                .map(TestcaseOptionEntity.class::cast)
                .toList();

        for (TestResultRelationEntity testResultRelationEntity : testResultRelationEntities) {
            testResultRelationEntity.setSelected(selectedTestcaseOptionIds.contains(testResultRelationEntity.getRefId()));
            testResultRelationService.updateTestcaseResult(testResultRelationEntity, contextInfo);
        }

        boolean isSuccess = isSuccessFromTestResultRelationAndSelectedOption(testcaseResultId, testResultRelationEntities, testcaseOptionEntitiesFromAudit, contextInfo);

        TestcaseResultEntity testcaseResultEntity
                = this.getTestcaseResultById(testcaseResultId, contextInfo);

        testcaseResultEntity.setSuccess(isSuccess);

        UserEntity userEntity
                = userService.getPrincipalUser(contextInfo);
        testcaseResultEntity.setTester(userEntity);

        testcaseResultEntity = testcaseResultRepository.saveAndFlush(testcaseResultEntity);

        changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED, contextInfo);

        return testcaseResultEntity;
    }

    private boolean isSuccessFromTestResultRelationAndSelectedOption(String testcaseResultId, List<TestResultRelationEntity> testResultRelationEntities, List<TestcaseOptionEntity> testcaseOptionEntitiesFromAudit, ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, DataValidationErrorException, OperationFailedException {

        TestcaseEntity auditTestcaseEntity = getTestcaseEntityFromAuditMapping(testcaseResultId, contextInfo);

        boolean isSingleSelectQuestion = TestcaseServiceConstants.QuestionType.SINGLE_SELECT.name().equals(auditTestcaseEntity.getQuestionType());

        boolean isTestSuccessful = !isSingleSelectQuestion;

        for (TestResultRelationEntity testResultRelationEntity : testResultRelationEntities) {
            // Check if the testResultRelationEntity is marked as success or failure
            Boolean isRelationSuccess = isTestResultRelationSuccess(testResultRelationEntity, testcaseOptionEntitiesFromAudit);

            if (isSingleSelectQuestion) {

                if (isRelationSuccess.equals(testResultRelationEntity.getSelected())) {
                    isTestSuccessful = true;
                    break;
                }
            } else {

                if (!isRelationSuccess.equals(testResultRelationEntity.getSelected())) {
                    isTestSuccessful = false;
                    break;
                }
            }
        }

        return isTestSuccessful;
    }

    private TestcaseEntity getTestcaseEntityFromAuditMapping(String testcaseResultId, ContextInfo contextInfo) throws DoesNotExistException, OperationFailedException, InvalidParameterException, DataValidationErrorException {
        List<Object> auditTestcaseEntities = testResultRelationService.getTestResultRelationEntitiesFromAuditMapping(testcaseResultId, TestcaseServiceConstants.TESTCASE_REF_OBJ_URI, contextInfo);
        return auditTestcaseEntities.stream()
                .findFirst()
                .map(TestcaseEntity.class::cast)
                .orElseThrow(() -> new DoesNotExistException("No TestcaseEntity found for testcaseResultId: " + testcaseResultId));
    }

    private boolean isTestResultRelationSuccess(TestResultRelationEntity testResultRelationEntity, List<TestcaseOptionEntity> testcaseOptionEntitiesFromAudit) {
        return testcaseOptionEntitiesFromAudit.stream()
                .filter(option -> option.getId().equals(testResultRelationEntity.getRefId()))
                .anyMatch(TestcaseOptionEntity::getSuccess);
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
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestcaseResultServiceServiceImpl.class.getSimpleName());
            throw new InvalidParameterException(TestcaseResultServiceConstants.TESTCASE_RESULT_MISSING);
        }
        return TestcaseResultValidator.validateTestCaseResult(validationTypeKey, testcaseResultEntity, userService, this, testRequestService, contextInfo);
    }

    @Override
    public TestcaseResultEntity changeState(String testcaseResultId, String stateKey, ContextInfo contextInfo) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, OperationFailedException, VersionMismatchException {

        List<ValidationResultInfo> errors = new ArrayList<>();

        TestcaseResultEntity testcaseResultEntity = this.getTestcaseResultById(testcaseResultId, contextInfo);

        CommonStateChangeValidator.validateStateChange(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS, TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_MAP, testcaseResultEntity.getState(), stateKey, errors);

        if (!stateKey.equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
            testcaseResultEntity.setMessage(null);
            testcaseResultEntity.setFailureMessage(null);
            testcaseResultEntity.setHasSystemError(Boolean.FALSE);
            testcaseResultEntity.setSuccess(Boolean.FALSE);
            testcaseResultEntity.setDuration(null);
        } else {
            if(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI.equals(testcaseResultEntity.getRefObjUri()) && !Boolean.TRUE.equals(testcaseResultEntity.getSuccess())) {
                List<Object> testResultRelationEntitiesFromAuditMapping = testResultRelationService.getTestResultRelationEntitiesFromAuditMapping(testcaseResultId, TestcaseServiceConstants.TESTCASE_REF_OBJ_URI, contextInfo);
                Optional<TestcaseEntity> testcaseEntityOptional = testResultRelationEntitiesFromAuditMapping.stream().findFirst().map(TestcaseEntity.class::cast);

                if(testcaseEntityOptional.isEmpty()){
                    LOGGER.error("Testcase not found in testcase result relation for testcaseResultId ->"+testcaseResultId);
                    throw new DoesNotExistException("Testcase not found in testcase result relation for testcaseResultId ->"+testcaseResultId);
                }

                TestcaseEntity testcase = testcaseEntityOptional.get();

                testcaseResultEntity.setFailureMessage(testcase.getFailureMessage());
            }
        }

        testcaseResultEntity.setState(stateKey);

        if (!testcaseResultEntity.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            testcaseResultEntity.setMessage(null);
        }
        testcaseResultEntity = testcaseResultRepository.saveAndFlush(testcaseResultEntity);

        if (!testcaseResultEntity.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            updateChildTestcaseResult(testcaseResultEntity, contextInfo);
        }

        if (testcaseResultEntity.getParentTestcaseResult() != null) {
            updateParentTestcaseResult(testcaseResultEntity.getParentTestcaseResult(), contextInfo);
        } else {
            updateTestRequestState(testcaseResultEntity.getTestRequest(), contextInfo);
        }

        // Notify client if the state is changed to finished
        if (TestcaseServiceConstants.TESTCASE_REF_OBJ_URI.equals(testcaseResultEntity.getRefObjUri())) {
            applicationEventPublisher.publishEvent(new TestcaseResultStateChangedEvent(testcaseResultId, testcaseResultEntity.getManual(), contextInfo));
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
                testRequestService.changeState(testRequestEntity.getId(), null, TestRequestServiceConstants.TEST_REQUEST_STATUS_SKIPPED, contextInfo);
            }
        } else if (!testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_INPROGRESS)) {
            testRequestService.changeState(testRequestEntity.getId(), null, TestRequestServiceConstants.TEST_REQUEST_STATUS_INPROGRESS, contextInfo);
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
                changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP, contextInfo);
            }
        } else if (testcaseResultEntities.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)
                        || tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
                changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED, contextInfo);
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
        } else if (!testcaseResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT)) {
            changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT, contextInfo);
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
                contextInfo).testcaseResultEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public List<TestcaseResultEntity> getMultipleTestcaseResultStatus(
            String testRequestId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            ContextInfo contextInfo)
            throws
            InvalidParameterException, OperationFailedException, DoesNotExistException, AccessDeniedException {
        if (!StringUtils.hasLength(testRequestId)){
            throw new InvalidParameterException("TestcaseRequestId is missing");
        }

        TestRequestEntity testRequest=testRequestService.getTestRequestById(testRequestId, contextInfo);
        String testRequestState=testRequest.getState();

        if((!contextInfo.isTesterOrAdminOrPublisher()) && !testRequestState.equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_PUBLISHED)){
              throw  new AccessDeniedException("Report can't be accessed until it is published");
        }

        TestcaseResultEntity testcaseResultEntity = new TestcaseResultEntity();
        testcaseResultEntity.setTestRequest(new TestRequestEntity(testRequestId));
        testcaseResultEntity.setRefObjUri(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI);
        return fetchTestcaseResultStatusByInputs(
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                testcaseResultEntity,
                contextInfo).testcaseResultEntities;
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

    @Override
    public StatusResponse startTestcaseAndStatusResponse(String  testSuiteId, ContextInfo contextInfo) throws Exception {
        try {
            StartResponse process = testSessionStarter.process(testSuiteId, testbedActorApiKey, contextInfo);
            verifyTestcaseSessionStatusAndUpdateAccordingly.process(process.getCreatedSessions().get(0).getSession(), statusCallback, contextInfo);
            return statusCallback.getStatusResponse();
        }catch (RestClientException e){
            throw new OperationFailedException("Rest Client error : "+ e.getMessage(),e);
        }
    }

    private RecursiveTestcaseResults fetchTestcaseResultStatusByInputs(
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            TestcaseResultEntity testcaseResultEntity,
            ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException, DoesNotExistException {

        if (StringUtils.hasLength(testcaseResultEntity.getId())) {
            testcaseResultEntity = new TestcaseResultEntity(testcaseResultEntity);
        }

        if (testcaseResultEntity.getRefObjUri().equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            return new RecursiveTestcaseResults(null, testcaseResultEntity);
        }

        TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
        testcaseResultCriteriaSearchFilter.setTestRequestId(testcaseResultEntity.getTestRequest().getId());
        testcaseResultCriteriaSearchFilter.setManual(isManual);
        testcaseResultCriteriaSearchFilter.setAutomated(isAutomated);
        testcaseResultCriteriaSearchFilter.setRequired(isRequired);
        testcaseResultCriteriaSearchFilter.setRecommended(isRecommended);
        testcaseResultCriteriaSearchFilter.setFunctional(isFunctional);
        testcaseResultCriteriaSearchFilter.setWorkflow(isWorkflow);

        List<TestcaseResultEntity> testcaseResultEntities = this.searchTestcaseResults(testcaseResultCriteriaSearchFilter, Constant.FULL_PAGE_SORT_BY_RANK, contextInfo).getContent().stream().map(testcaseResultEntity1 -> new TestcaseResultEntity(testcaseResultEntity1)).collect(Collectors.toList());
        if(testcaseResultEntities.isEmpty()){
            return new RecursiveTestcaseResults(new ArrayList<>(), testcaseResultEntity);
        }

        if (!StringUtils.hasLength(testcaseResultEntity.getId())) {
            testcaseResultEntity = new TestcaseResultEntity(testcaseResultEntities.get(0));
        }

        List<TestcaseResultEntity> recalculatedChilds = recalculateTestcaseResultEntity(testcaseResultEntity, isRecommended, testcaseResultEntities, contextInfo);
        recalculatedChilds.set(0, testcaseResultEntity);
        return new RecursiveTestcaseResults(recalculatedChilds, testcaseResultEntity);
    }

    private List<TestcaseResultEntity> recalculateTestcaseResultEntity(TestcaseResultEntity testcaseResultEntity, Boolean isRecommended, List<TestcaseResultEntity> testcaseResultEntities, ContextInfo contextInfo) throws OperationFailedException {
        List<TestcaseResultEntity> filteredTestcaseResults;
        if (testcaseResultEntity.getRefObjUri().equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI)) {
            filteredTestcaseResults = getFilteredChileTestcaseResultsForTestResult(testcaseResultEntity, testcaseResultEntities);

            boolean allTestcasesFinished = true;
            for (TestcaseResultEntity testcaseResult : filteredTestcaseResults) {
                if (!testcaseResult.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !testcaseResult.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP)) {
                    allTestcasesFinished = false;
                    break;
                }
            }

            //set duration
            Long duration = 0L;
            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                if (tcr.getDuration() != null && tcr.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);

            if (allTestcasesFinished) {
                List<String> failedTestcaseResultName = filteredTestcaseResults.stream().filter(tcre -> tcre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !tcre.getSuccess()).map(IdStateNameMetaEntity::getName).toList();
                String message;
                if (failedTestcaseResultName.isEmpty()) {
                    message = "Passed";
                } else {
                    message = getMessage("Specification <b>", testcaseResultEntity, "</b> has been failed due to failing following testcase failure: ", failedTestcaseResultName);
                }
                testcaseResultEntity.setMessage(message);

            }

        } else if (testcaseResultEntity.getRefObjUri().equals(ComponentServiceConstants.COMPONENT_REF_OBJ_URI)) {
            //Change to info
            filteredTestcaseResults = getFilteredChileTestcaseResultsForTestResult(testcaseResultEntity, testcaseResultEntities);

            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                this.recalculateTestcaseResultEntity(tcr, isRecommended, testcaseResultEntities, contextInfo);
            }

            //set duration
            Long duration = 0L;
            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                if (tcr.getDuration() != null && tcr.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);

            boolean allTestcasesFinished = true;
            for (TestcaseResultEntity specificationResultEntity : filteredTestcaseResults) {
                if (!specificationResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !specificationResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP)) {
                    allTestcasesFinished = false;
                    break;
                }
            }
            if (allTestcasesFinished) {
                List<String> failedSpecificationTestcaseResultName = filteredTestcaseResults.stream().filter(entity -> entity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !entity.getSuccess()).map(IdStateNameMetaEntity::getName)
                        .toList();
                String message;
                if (failedSpecificationTestcaseResultName.isEmpty()) {
                    message = "Passed";
                } else {
                    message = getMessage("Component <b>", testcaseResultEntity, "</b> has been failed due to failing following specification failure: ", failedSpecificationTestcaseResultName);
                }
                testcaseResultEntity.setMessage(message);
            }
        } else {
            //Change to info
            filteredTestcaseResults = getFilteredChileTestcaseResultsForTestResult(testcaseResultEntity, testcaseResultEntities);

            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                this.recalculateTestcaseResultEntity(tcr, isRecommended, testcaseResultEntities, contextInfo);
            }

            //set duration
            Long duration = 0L;
            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                if (tcr.getDuration() != null && tcr.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);

            boolean allTestcasesFinished = true;
            for (TestcaseResultEntity componentResultEntity : filteredTestcaseResults) {
                if (!componentResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !componentResultEntity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP)) {
                    allTestcasesFinished = false;
                    break;
                }
            }

            if (allTestcasesFinished) {
                List<String> failedComponentTestcaseResultName = filteredTestcaseResults.stream().filter(entity -> entity.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !entity.getSuccess()).map(IdStateNameMetaEntity::getName)
                        .toList();
                String message;
                if (failedComponentTestcaseResultName.isEmpty()) {
                    message = "Passed";
                } else {
                    message = getMessage("TestRequest <b>", testcaseResultEntity, "</b> has been failed due to failing following component failure: ", failedComponentTestcaseResultName);
                }
                testcaseResultEntity.setMessage(message);

            }
        }

        if (filteredTestcaseResults.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP);
        } else if (filteredTestcaseResults.stream()
                .allMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)
                        || tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED);
            if (Boolean.TRUE.equals(isRecommended)) {
                if (testcaseResultEntity.getRefObjUri().equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)) {
                    testcaseResultEntity.setGrade(gradeEvaluator.evaluate(
                            testcaseResultEntities.stream().filter(tcre -> tcre.getRefObjUri().equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI))
                                    .collect(Collectors.toList()),
                            contextInfo)
                    );
                    testcaseResultEntity.setCompliant(GradeEvaluator.getCompliance(filteredTestcaseResults));
                    testcaseResultEntity.setNonCompliant(GradeEvaluator.getNonCompliance(filteredTestcaseResults));
                } else if (testcaseResultEntity.getRefObjUri().equals(ComponentServiceConstants.COMPONENT_REF_OBJ_URI)) {
                    testcaseResultEntity.setGrade(gradeEvaluator.evaluate(filteredTestcaseResults, contextInfo));
                }
            } else {
                if (testcaseResultEntity.getRefObjUri().equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)){
                    testcaseResultEntity.setCompliant(GradeEvaluator.getCompliance(filteredTestcaseResults));
                    testcaseResultEntity.setNonCompliant(GradeEvaluator.getNonCompliance(filteredTestcaseResults));
                }
            }
        } else if (filteredTestcaseResults.stream()
                .anyMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS))) {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS);
        } else if (filteredTestcaseResults.stream()
                .anyMatch(tre -> tre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING))) {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING);
        } else {
            testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT);
        }

        //set success
        testcaseResultEntity.setSuccess(filteredTestcaseResults.stream().allMatch(testcaseResult -> Boolean.TRUE.equals(testcaseResult.getSuccess())));

        return testcaseResultEntities;
    }

    private void defaultValueCreateTestCaseResult(TestcaseResultEntity testcaseResultEntity) {
        if (!StringUtils.hasLength(testcaseResultEntity.getId())) {
            testcaseResultEntity.setId(UUID.randomUUID().toString());
        }
        testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT);
        testcaseResultEntity.setDuration(null);
    }

    private record RecursiveTestcaseResults(List<TestcaseResultEntity> testcaseResultEntities,
                                            TestcaseResultEntity testcaseResultEntity) {

    }

    @Override
    public List<TestcaseResultEntity> findTopFiveTestRequestsResult(){
        // Create a Pageable object with a page size of 5
        Pageable pageable = PageRequest.of(0, 5);

        // Call the repository method
        Page<TestcaseResultEntity> resultPage = testcaseResultRepository.findTopFiveTestRequestsResult(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI, pageable);

        // Extract the content (list of entities) from the result page
        return resultPage.getContent();
    }

    @Override
    public List<TestcaseResultEntity> findBestOfEachComponent(List<ComponentEntity> allComponents){

        List<TestcaseResultEntity> bestOfEachComponent = new ArrayList<>();
        for(ComponentEntity component : allComponents){
            List<TestcaseResultEntity> bestOfComponent = testcaseResultRepository.findBestOfEachComponent(component.getId());
            TestcaseResultEntity bestCase = bestOfComponent.stream()
                    .filter(testcaseResultEntity -> (testcaseResultEntity.getCompliant()+testcaseResultEntity.getNonCompliant())>0)
                    .max(Comparator.comparing(testcaseResultEntity-> (testcaseResultEntity.getCompliant()/(testcaseResultEntity.getCompliant() + testcaseResultEntity.getNonCompliant()))))
                    .orElse(new TestcaseResultEntity());

            if(bestCase.getId() == null){
                bestCase.setName(component.getName());
                bestCase.setCompliant(0);
                bestCase.setNonCompliant(0);
            }

            bestOfEachComponent.add(bestCase);
        }
        bestOfEachComponent.sort(Comparator.comparing(testcaseResultEntity -> {
            if ((testcaseResultEntity.getCompliant() + testcaseResultEntity.getNonCompliant()) > 0) {
                return testcaseResultEntity.getCompliant() / (testcaseResultEntity.getCompliant() + testcaseResultEntity.getNonCompliant());
            } else {
                return testcaseResultEntity.getCompliant();
            }
        }));

        return bestOfEachComponent;

    }

    @Override
    public List<Object[]> findBestFiveTestcaseResultPerComponent(String componentId){

        Pageable pageable = PageRequest.of(0, 5);

        // Call the repository method
        Page<Object[]> resultPage = testcaseResultRepository.findBestFiveTestcaseResultPerComponent(pageable,componentId,TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED);


        return resultPage.getContent();
    }

    @Autowired
    public void setTestSessionStarter(TestSessionStarter testSessionStarter) {
        this.testSessionStarter = testSessionStarter;
    }

    @Autowired
    public void setVerifyTestcaseSessionStatusAndUpdateAccordingly(VerifyTestcaseSessionStatusAndUpdateAccordingly verifyTestcaseSessionStatusAndUpdateAccordingly) {
        this.verifyTestcaseSessionStatusAndUpdateAccordingly = verifyTestcaseSessionStatusAndUpdateAccordingly;
    }

    @Autowired
    public void setStatusCallback(StatusCallbackLoggerHandler statusCallback) {
        this.statusCallback = statusCallback;
    }

    @Override
    public List<Object[]> complianceAndNonComplianceOfAllTestRequestResults(){
        return testcaseResultRepository.complianceAndNonComplianceOfAllTestRequestResults(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI);
    }

    @Override
    public List<Object[]> getFinishedSkippedAndAllTestRequestResults(){
        return testcaseResultRepository.getFinishedSkippedAndAllTestRequestResults(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI, TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED, TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP);
    }

    @Override
    public int countTestcaseResultsOfTestRequest(){
        return testcaseResultRepository.countTestcaseResultsOfTestRequest(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI);
    }
}

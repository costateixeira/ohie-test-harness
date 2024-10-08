package com.argusoft.path.tht.testprocessmanagement.service.impl;

import com.argusoft.path.tht.fileservice.constant.DocumentServiceConstants;
import com.argusoft.path.tht.fileservice.filter.DocumentCriteriaSearchFilter;
import com.argusoft.path.tht.fileservice.models.entity.DocumentEntity;
import com.argusoft.path.tht.fileservice.service.DocumentService;
import com.argusoft.path.tht.notificationmanagement.event.NotificationCreationEvent;
import com.argusoft.path.tht.notificationmanagement.models.entity.NotificationEntity;
import com.argusoft.path.tht.reportmanagement.constant.TestcaseResultServiceConstants;
import com.argusoft.path.tht.reportmanagement.evaluator.GradeEvaluator;
import com.argusoft.path.tht.reportmanagement.filter.TestcaseResultCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.models.dto.TestcaseResultViewInfo;
import com.argusoft.path.tht.reportmanagement.models.entity.TestResultRelationEntity;
import com.argusoft.path.tht.reportmanagement.models.entity.TestcaseResultEntity;
import com.argusoft.path.tht.reportmanagement.repository.TestcaseResultRepository;
import com.argusoft.path.tht.reportmanagement.service.TestResultRelationService;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultService;
import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.constant.ValidateConstant;
import com.argusoft.path.tht.systemconfiguration.email.service.EmailService;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.models.dto.MetaInfo;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.models.entity.IdStateNameMetaEntity;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.utils.CommonStateChangeValidator;
import com.argusoft.path.tht.systemconfiguration.utils.CommonUtil;
import com.argusoft.path.tht.testcasemanagement.constant.ComponentServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.SpecificationServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseOptionServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.filter.ComponentCriteriaSearchFilter;
import com.argusoft.path.tht.testcasemanagement.filter.TestcaseOptionCriteriaSearchFilter;
import com.argusoft.path.tht.testcasemanagement.models.entity.*;
import com.argusoft.path.tht.testcasemanagement.service.*;
import com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.util.TestcaseExecutioner;
import com.argusoft.path.tht.testprocessmanagement.constant.TestRequestServiceConstants;
import com.argusoft.path.tht.testprocessmanagement.filter.TestRequestCriteriaSearchFilter;
import com.argusoft.path.tht.testprocessmanagement.models.dto.*;
import com.argusoft.path.tht.testprocessmanagement.models.entity.TestRequestEntity;
import com.argusoft.path.tht.testprocessmanagement.models.entity.TestRequestUrlEntity;
import com.argusoft.path.tht.testprocessmanagement.models.entity.TestRequestValueEntity;
import com.argusoft.path.tht.testprocessmanagement.repository.TestRequestRepository;
import com.argusoft.path.tht.testprocessmanagement.service.TestRequestService;
import com.argusoft.path.tht.testprocessmanagement.service.TestRequestValueService;
import com.argusoft.path.tht.testprocessmanagement.validator.TestRequestValidator;
import com.argusoft.path.tht.usermanagement.constant.UserServiceConstants;
import com.argusoft.path.tht.usermanagement.models.entity.UserEntity;
import com.argusoft.path.tht.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * This TestRequestServiceServiceImpl contains implementation for TestRequest service.
 *
 * @author Dhruv
 */
@Service
public class TestRequestServiceServiceImpl implements TestRequestService {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestRequestServiceServiceImpl.class);

    private TestcaseResultRepository testcaseResultRepository;

    private TestRequestRepository testRequestRepository;

    private TestcaseResultService testcaseResultService;

    private ComponentService componentService;

    private SpecificationService specificationService;

    private TestcaseService testcaseService;

    private UserService userService;

    private TestcaseExecutioner testcaseExecutioner;

    private TestResultRelationService testResultRelationService;

    private TestcaseOptionService testcaseOptionService;

    private DocumentService documentService;

    private EmailService emailService;

    private TestcaseVariableService testcaseVariableService;

    private TestRequestValueService testRequestValueService;

    ApplicationEventPublisher applicationEventPublisher;

    @Value("${base-url}")
    private String baseUrl;

    @Value("${message-configuration.test-request.create.mail}")
    private boolean testRequestCreateMail;

    @Value("${message-configuration.test-request.create.notification}")
    private boolean testRequestCreateNotification;

    @Value("${message-configuration.test-request.accept.mail}")
    private boolean testRequestAcceptMail;

    @Value("${message-configuration.test-request.accept.notification}")
    private boolean testRequestAcceptNotification;

    @Value("${message-configuration.test-request.reject.mail}")
    private boolean testRequestRejectMail;

    @Value("${message-configuration.test-request.reject.notification}")
    private boolean testRequestRejectNotification;

    @Value("${message-configuration.test-request.publish.mail}")
    private boolean testRequestPublishMail;

    @Value("${message-configuration.test-request.publish.notification}")
    private boolean testRequestPublishNotification;

    @Value("${message-configuration.test-request.unpublish.mail}")
    private boolean testRequestUnpublishMail;

    @Value("${message-configuration.test-request.unpublish.notification}")
    private boolean testRequestUnpublishNotification;



    private GradeEvaluator gradeEvaluator;


    @Autowired
    public void setTestcaseResultRepository(TestcaseResultRepository testcaseResultRepository) {
        this.testcaseResultRepository = testcaseResultRepository;
    }

    @Autowired
    public void setTestRequestRepository(TestRequestRepository testRequestRepository) {
        this.testRequestRepository = testRequestRepository;
    }

    @Autowired
    public void setTestcaseResultService(@Lazy TestcaseResultService testcaseResultService) {
        this.testcaseResultService = testcaseResultService;
    }

    @Autowired
    public void setComponentService(ComponentService componentService) {
        this.componentService = componentService;
    }

    @Autowired
    public void setSpecificationService(SpecificationService specificationService) {
        this.specificationService = specificationService;
    }

    @Autowired
    public void setTestcaseVariableService(TestcaseVariableService testcaseVariableService) {
        this.testcaseVariableService = testcaseVariableService;
    }

    @Autowired
    public void setTestcaseService(TestcaseService testcaseService) {
        this.testcaseService = testcaseService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setTestcaseExecutioner(TestcaseExecutioner testcaseExecutioner) {
        this.testcaseExecutioner = testcaseExecutioner;
    }

    @Autowired
    public void setTestResultRelationService(TestResultRelationService testResultRelationService) {
        this.testResultRelationService = testResultRelationService;
    }

    @Autowired
    public void setTestcaseOptionService(TestcaseOptionService testcaseOptionService) {
        this.testcaseOptionService = testcaseOptionService;
    }

    @Autowired
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Autowired
    public void setTestRequestValueService(@Lazy TestRequestValueService testRequestValueService) {
        this.testRequestValueService = testRequestValueService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setGradeEvaluator(GradeEvaluator gradeEvaluator) {
        this.gradeEvaluator = gradeEvaluator;
    }

    @Override
    public void stopTestingProcess(
            String testRequestId,
            String refObjUri,
            String refId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            Boolean changeOnlyIfNotSubmitted,
            ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException {
        TestRequestValidator.validateTestRequestStartReinitializeProcess(
                testRequestId,
                refObjUri,
                refId,
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                Constant.STOP_PROCESS_VALIDATION,
                testcaseResultService,
                contextInfo);

        testcaseExecutioner.stopTestingProcess(
                testRequestId,
                refObjUri,
                refId,
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                changeOnlyIfNotSubmitted,
                contextInfo);
    }

    @Override
    public void startTestingProcess(
            String testRequestId,
            String refObjUri,
            String refId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            ContextInfo contextInfo)
            throws InvalidParameterException,
            OperationFailedException,
            DataValidationErrorException, DoesNotExistException {

        TestRequestEntity testRequestEntity = this.getTestRequestById(testRequestId, contextInfo);

        TestRequestValidator.validateTestRequestStartReinitializeProcess(
                testRequestId,
                refObjUri,
                refId,
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                Constant.START_PROCESS_VALIDATION,
                testcaseResultService,
                contextInfo);

        List<TestRequestValueEntity> testRequestValueEntities = new ArrayList<>();
        for(TestRequestValueEntity testRequestValueEntity : testRequestEntity.getTestRequestValues()) {
            TestcaseVariableEntity testcaseVariableEntity = testcaseVariableService.getTestcaseVariableById(testRequestValueEntity.getTestcaseVariableId(), contextInfo);
            TestcaseEntity testcaseEntity = testcaseService.getTestcaseById(testcaseVariableEntity.getTestcase().getId(), contextInfo);
            if((Boolean.TRUE.equals(isManual) && testcaseEntity.getManual()) || (Boolean.TRUE.equals(isAutomated) && !testcaseEntity.getManual())){
                testRequestValueEntities.add(testRequestValueEntity);
            }
        }

        TestRequestValidator.validateRequiredTestRequestValues(testRequestValueEntities);

        testcaseExecutioner.executeTestingProcess(
                testRequestId,
                refObjUri,
                refId,
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                contextInfo);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public TestRequestEntity createTestRequest(TestRequestEntity testRequestEntity,
                                               ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException, DoesNotExistException {

        if (testRequestEntity == null) {
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestRequestServiceServiceImpl.class.getSimpleName());
            throw new InvalidParameterException("TestRequestEntity is missing");
        }

        defaultValueCreateTestRequest(testRequestEntity, contextInfo);

        TestRequestValidator.validateCreateUpdateTestRequest(Constant.CREATE_VALIDATION,
                testRequestEntity,
                this,
                userService,
                componentService,
                contextInfo);

        messageAdminsIfTestRequestCreated(contextInfo);

        //Create state change API to make this as Accepted or Rejected
        testRequestEntity = testRequestRepository.saveAndFlush(testRequestEntity);
        return testRequestEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public TestRequestEntity updateTestRequest(TestRequestEntity testRequestEntity,
                                               ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException, DoesNotExistException {

        if (testRequestEntity == null) {
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestRequestServiceServiceImpl.class.getSimpleName());
            throw new InvalidParameterException("TestRequestEntity is missing");
        }

        TestRequestValidator.validateCreateUpdateTestRequest(Constant.UPDATE_VALIDATION,
                testRequestEntity,
                this,
                userService,
                componentService,
                contextInfo);

        Set<TestRequestValueEntity> testRequestValueEntities = new HashSet<>();
        for(TestRequestValueEntity testRequestValueEntity : testRequestEntity.getTestRequestValues()){
            try{
                TestRequestValueEntity originalEntity = testRequestValueService.getTestRequestValueById(testRequestValueEntity.getId(), contextInfo);
                originalEntity.setTestRequestValueInput(testRequestValueEntity.getTestRequestValueInput());
                originalEntity.setTestRequest(testRequestValueEntity.getTestRequest());
                originalEntity.setTestcaseVariableId(testRequestValueEntity.getTestcaseVariableId());
                testRequestValueEntity = originalEntity;
            }
            catch(Exception e){
                TestRequestValueEntity newEntity = new TestRequestValueEntity();
                newEntity.setId(UUID.randomUUID().toString());
                newEntity.setTestRequest(testRequestEntity);
                newEntity.setTestcaseVariableId(testRequestValueEntity.getTestcaseVariableId());
                newEntity.setTestRequestValueInput(testRequestValueEntity.getTestRequestValueInput());
                testRequestValueEntity = newEntity;
            }
            testRequestValueEntities.add(testRequestValueEntity);
        }

        testRequestEntity.setTestRequestValues(testRequestValueEntities);

        testRequestEntity = testRequestRepository.saveAndFlush(testRequestEntity);
        return testRequestEntity;
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public Page<TestRequestEntity> searchTestRequests(
            TestRequestCriteriaSearchFilter testRequestSearchFilter,
            Pageable pageable,
            ContextInfo contextInfo)
            throws InvalidParameterException {

        Specification<TestRequestEntity> testRequestEntitySpecification = testRequestSearchFilter.buildSpecification(pageable, contextInfo);
        return testRequestRepository.findAll(testRequestEntitySpecification, CommonUtil.getPageable(pageable));
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public Page<TestRequestEntity> searchLikeTestRequests(
            TestRequestCriteriaSearchFilter testRequestSearchFilter,
            Pageable pageable,
            ContextInfo contextInfo)
            throws InvalidParameterException {

        Specification<TestRequestEntity> testRequestEntitySpecification = testRequestSearchFilter.buildLikeSpecification(pageable, contextInfo);
        return testRequestRepository.findAll(testRequestEntitySpecification, CommonUtil.getPageable(pageable));
    }


    @Override
    public List<TestRequestEntity> searchTestRequests(
            TestRequestCriteriaSearchFilter testRequestSearchFilter,
            ContextInfo contextInfo)
            throws InvalidParameterException {

        Specification<TestRequestEntity> testRequestEntitySpecification = testRequestSearchFilter.buildSpecification(contextInfo);
        return testRequestRepository.findAll(testRequestEntitySpecification);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @Override
    public TestRequestEntity getTestRequestById(String testRequestId,
                                                ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException {
        if (!StringUtils.hasLength(testRequestId)) {
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestRequestServiceServiceImpl.class.getSimpleName());
            throw new InvalidParameterException("TestRequestId is missing");
        }
        TestRequestCriteriaSearchFilter testRequestCriteriaSearchFilter = new TestRequestCriteriaSearchFilter(testRequestId);

        List<TestRequestEntity> testRequestEntities = this.searchTestRequests(testRequestCriteriaSearchFilter, contextInfo);
        return testRequestEntities.stream()
                .findFirst()
                .orElseThrow(() -> new DoesNotExistException("TestRequest does not found with id : " + testRequestId));
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public List<ValidationResultInfo> validateTestRequest(
            String validationTypeKey,
            TestRequestEntity testRequestEntity,
            ContextInfo contextInfo)
            throws InvalidParameterException,
            OperationFailedException {
        if (testRequestEntity == null) {
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestRequestServiceServiceImpl.class.getSimpleName());
            throw new InvalidParameterException("TestRequestEntity is missing");
        }
        return TestRequestValidator.validateTestRequest(validationTypeKey, testRequestEntity, this, userService, componentService, contextInfo);
    }

    @Override
    public List<ValidationResultInfo> validateChangeState(String testRequestId, String message, String stateKey, ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {
        List<ValidationResultInfo> errors = new ArrayList<>();
        try {
            TestRequestEntity testRequestEntity = this.getTestRequestById(testRequestId, contextInfo);
            if(stateKey.equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_REJECTED)){
                testRequestEntity.setMessage(message);
            } else {
                testRequestEntity.setMessage(null);
            }
            TestRequestValidator.validateChangeState(testRequestEntity, stateKey, componentService, specificationService, testcaseService, testcaseOptionService, errors, contextInfo);
            CommonStateChangeValidator.validateStateChangeByMap(TestRequestServiceConstants.TEST_REQUEST_STATUS, TestRequestServiceConstants.TEST_REQUEST_STATUS_MAP, testRequestEntity.getState(), stateKey, errors);
        } catch (DoesNotExistException ex) {
            errors.add(
                    new ValidationResultInfo("id",
                            ErrorLevel.ERROR,
                            ValidateConstant.ID_SUPPLIED + "update" + ValidateConstant.DOES_NOT_EXIST));
        }
        return errors;
    }

    @Override
    public List<TestRequestViewInfo> getApplicationsStats(ContextInfo contextInfo) throws OperationFailedException, InvalidParameterException, RuntimeException{

        //Fetch all test requests
        List<TestRequestEntity> testRequestEntities = testRequestRepository.findAll();

        List<TestRequestViewInfo> testRequestViewInfos = new ArrayList<>();

        //Add TestRequestViewInfo for every test request entity found
        testRequestEntities.forEach(testRequestEntity -> {

            //TestRequestInfo Model to DTO
            TestRequestViewInfo testRequestViewInfo = new TestRequestViewInfo();
            testRequestViewInfo.setId(testRequestEntity.getId());
            testRequestViewInfo.setState(testRequestEntity.getState());
            testRequestViewInfo.setName(testRequestEntity.getName());
            testRequestViewInfo.setAssesseeName(testRequestEntity.getAssessee().getName());
            testRequestViewInfo.setAssesseeEmail(testRequestEntity.getAssessee().getEmail());
            MetaInfo meta = new MetaInfo();
            meta.setCreatedAt(testRequestEntity.getCreatedAt());
            meta.setUpdatedAt(testRequestEntity.getUpdatedAt());
            meta.setCreatedBy(testRequestEntity.getCreatedBy());
            meta.setUpdatedBy(testRequestEntity.getUpdatedBy());
            meta.setVersion(testRequestEntity.getVersion());
            testRequestViewInfo.setMeta(meta);

            //Search for all testcases on this test request
            TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
            testcaseResultCriteriaSearchFilter.setTestRequestId(testRequestEntity.getId());
            testcaseResultCriteriaSearchFilter.setRefObjUri(ComponentServiceConstants.COMPONENT_REF_OBJ_URI);
            List<TestcaseResultEntity> testcaseResultEntities;

            try {
                testcaseResultEntities = testcaseResultService.searchTestcaseResults(
                        testcaseResultCriteriaSearchFilter,
                        contextInfo);
            } catch (OperationFailedException | InvalidParameterException e) {
                throw new RuntimeException(e);
            }

            List<ComponentEntity> components = testRequestEntity.getTestRequestUrls().stream().map(TestRequestUrlEntity::getComponent).collect(Collectors.toList());



            List<TestcaseResultViewInfo> testResultOfComponents = new ArrayList<>();

            testcaseResultEntities
                    .forEach(componentResult -> {

//                        components = components.stream()
//                                                .filter(componentEntity -> !(componentEntity.getId().equals(componentResult.getId())))
//                                                .collect(Collectors.toList());

                        components.removeIf(componentEntity -> componentEntity.getId().equals(componentResult.getRefId()));

                        TestcaseResultViewInfo componentResultViewInfo = new TestcaseResultViewInfo();
                        componentResultViewInfo.setComponentId(componentResult.getId());
                        componentResultViewInfo.setComponentName(componentResult.getName());
                        String testRequestState = testRequestEntity.getState();
                        if(TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED.equals(testRequestState) || TestRequestServiceConstants.TEST_REQUEST_STATUS_INPROGRESS.equals(testRequestState) || TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED.equals(testRequestState)){
                            componentResultViewInfo.setTestcaseResultState(componentResult.getState());
                        }
                        if(TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED.equals(testRequestState)){
                            componentResultViewInfo.setSuccess(componentResult.getSuccess());
                            componentResultViewInfo.setGrade(componentResult.getGrade());
                        }
                        testResultOfComponents.add(componentResultViewInfo);
                    });
            //add components to testResultOfComponents
            for(ComponentEntity component: components){
                TestcaseResultViewInfo inactiveComponent = new TestcaseResultViewInfo();
                inactiveComponent.setComponentId(component.getId());
                inactiveComponent.setComponentName(component.getName());


                testResultOfComponents.add(inactiveComponent);
            }

            testRequestViewInfo.setTestResultOfComponents(testResultOfComponents);

            //Search for all testcases on this test request
            testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
            testcaseResultCriteriaSearchFilter.setTestRequestId(testRequestEntity.getId());
            testcaseResultCriteriaSearchFilter.setRefObjUri(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI);
            List<TestcaseResultEntity> testRequestTestcaseResultEntity;

            try {
                testRequestTestcaseResultEntity = testcaseResultService.searchTestcaseResults(
                        testcaseResultCriteriaSearchFilter,
                        contextInfo);
            } catch (OperationFailedException | InvalidParameterException e) {
                throw new RuntimeException(e);
            }

            if(TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED.equals(testRequestEntity.getState())){
                TestcaseResultEntity testRequestTestcaseResult =testRequestTestcaseResultEntity.stream().filter(testcaseResultEntity -> testcaseResultEntity.getRefObjUri().equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)).findFirst().get();
                testRequestViewInfo.setSuccess(testRequestTestcaseResult.getSuccess());
                testRequestViewInfo.setGrade(testRequestTestcaseResult.getGrade());
            }

            testRequestViewInfos.add(testRequestViewInfo);
        });

        return testRequestViewInfos;
    }

    @Override
    public TestRequestEntity changeState(String testRequestId, String message, String stateKey, ContextInfo contextInfo) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, OperationFailedException, VersionMismatchException {

        List<ValidationResultInfo> errors = new ArrayList<>();

        TestRequestEntity testRequestEntity = this.getTestRequestById(testRequestId, contextInfo);


        if(stateKey.equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_REJECTED)){
            testRequestEntity.setMessage(message);
        } else {
            testRequestEntity.setMessage(null);
        }

        defaultValueChangeState(testRequestEntity, stateKey, contextInfo);

        if(!contextInfo.isPublisher()) {
            if(testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_PUBLISHED) || stateKey.equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_PUBLISHED)) {
                ValidationResultInfo validationResultInfo = new ValidationResultInfo();
                validationResultInfo.setElement("state");
                validationResultInfo.setLevel(ErrorLevel.ERROR);
                validationResultInfo.setMessage("Given transition "+testRequestEntity.getState()+" -> "+stateKey+" can only be performed by the publisher");
                errors.add(validationResultInfo);
                LOGGER.error("Given transition "+testRequestEntity.getState()+" -> "+stateKey+" can only be performed by the publisher");
                throw new DataValidationErrorException(ValidateConstant.ERRORS, errors);
            }
        }

        TestRequestValidator.validateChangeState(testRequestEntity, stateKey, componentService, specificationService, testcaseService, testcaseOptionService, errors, contextInfo);

        CommonStateChangeValidator.validateStateChange(TestRequestServiceConstants.TEST_REQUEST_STATUS, TestRequestServiceConstants.TEST_REQUEST_STATUS_MAP, testRequestEntity.getState(), stateKey, errors);

        String oldState = testRequestEntity.getState();

        testRequestEntity.setState(stateKey);

        testRequestEntity = testRequestRepository.saveAndFlush(testRequestEntity);

        UserEntity requestingUser = testRequestEntity.getAssessee();

        sendMailToTheUserOnChangeState(oldState,  message, stateKey, requestingUser, testRequestEntity.getName(), testRequestId, contextInfo);

        changeStateCallback(testRequestEntity, contextInfo);
        return testRequestEntity;
    }

    private void sendMailToTheUserOnChangeState(String oldState, String message, String newState, UserEntity requestingUser, String testRequestName, String testRequestId, ContextInfo contextInfo) {
        if (TestRequestServiceConstants.TEST_REQUEST_STATUS_PENDING.equals(oldState) && TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED.equals(newState)) {
            messageAssesseeIfTestRequestAccepted(requestingUser, testRequestName, contextInfo);
        } else if (TestRequestServiceConstants.TEST_REQUEST_STATUS_PENDING.equals(oldState) && TestRequestServiceConstants.TEST_REQUEST_STATUS_REJECTED.equals(newState)) {
            messageAssesseeIfTestRequestRejected(requestingUser, message, testRequestName, contextInfo);
        } else if (TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED.equals(oldState) && TestRequestServiceConstants.TEST_REQUEST_STATUS_PUBLISHED.equals(newState)) {
            messageAssesseeIfTestRequestPublished(requestingUser, testRequestName, baseUrl + "/application-report/" + testRequestId, contextInfo);
        }else if(TestRequestServiceConstants.TEST_REQUEST_STATUS_PUBLISHED.equals(oldState) && TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED.equals(newState)){
            messageAssesseeIfTestRequestUnPublished(requestingUser, testRequestName, baseUrl + "/application-report/" + testRequestId, contextInfo);
        }
    }

    private void changeStateCallback(TestRequestEntity testRequestEntity, ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, DataValidationErrorException, OperationFailedException, VersionMismatchException {
        if (testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED)) {
            createDraftTestcaseResultsByTestRequestAndProcessKey(testRequestEntity, contextInfo);
        } else if (testRequestEntity.getState().equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED)) {
            updateIsSuccessAndGradeForReport(testRequestEntity, contextInfo);
        }
    }

    private void createDraftTestcaseResultsByTestRequestAndProcessKey(TestRequestEntity testRequestEntity, ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException, DataValidationErrorException, DoesNotExistException, VersionMismatchException {
            List<ComponentEntity> activeComponents = fetchActiveComponents(contextInfo)
                .stream().filter(componentEntity ->
                        testRequestEntity.getTestRequestUrls().stream().anyMatch(testRequestUrlEntity -> testRequestUrlEntity.getComponent().getId().equals(componentEntity.getId()))
                                && componentEntity.getSpecifications().stream().anyMatch(
                                specificationEntity -> {
                                    return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                                            && specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                                        return testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                                    });
                                })
                ).collect(Collectors.toList());

        Integer counter = 1;
        if (!activeComponents.isEmpty()) {
            Boolean isManual = activeComponents.stream().anyMatch(componentEntity -> {
                return componentEntity.getSpecifications().stream().anyMatch(specificationEntity -> {
                    return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                            && specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                        return testcaseEntity.getManual()
                                && testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                    });
                });
            });
            Boolean isAutomated = activeComponents.stream().anyMatch(componentEntity -> {
                return componentEntity.getSpecifications().stream().anyMatch(specificationEntity -> {
                    return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                            && specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                        return !testcaseEntity.getManual()
                                && testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                    });
                });
            });
            Boolean isRequired = activeComponents.stream().anyMatch(componentEntity -> {
                return componentEntity.getSpecifications().stream().anyMatch(specificationEntity -> {
                    return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                            && specificationEntity.getRequired();
                });
            });
            Boolean isRecommended = activeComponents.stream().anyMatch(componentEntity -> {
                return componentEntity.getSpecifications().stream().anyMatch(specificationEntity -> {
                    return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                            && !specificationEntity.getRequired();
                });
            });
            Boolean isFunctional = activeComponents.stream().anyMatch(componentEntity -> {
                return componentEntity.getSpecifications().stream().anyMatch(specificationEntity -> {
                    return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                            && specificationEntity.getFunctional();
                });
            });
            Boolean isWorkflow = activeComponents.stream().anyMatch(componentEntity -> {
                return componentEntity.getSpecifications().stream().anyMatch(specificationEntity -> {
                    return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                            && !specificationEntity.getFunctional();
                });
            });

            TestcaseResultEntity testRequestTestcaseResult = createDraftTestCaseResultIfNotExists(
                    TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI,
                    testRequestEntity.getId(),
                    testRequestEntity.getId(),
                    testRequestEntity.getName(),
                    counter,
                    isManual,
                    isAutomated,
                    isRequired,
                    isRecommended,
                    isFunctional,
                    isWorkflow,
                    null,
                    null, contextInfo);
            counter++;

            for (ComponentEntity componentEntity : activeComponents) {
                List<SpecificationEntity> activeSpecifications = componentEntity.getSpecifications().stream().filter(specificationEntity -> {
                            return specificationEntity.getState().equals(SpecificationServiceConstants.SPECIFICATION_STATUS_ACTIVE)
                                    && specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                                return testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                            });
                        })
                        .sorted(Comparator.comparing(SpecificationEntity::getRank))
                        .collect(Collectors.toList());

                if (!activeSpecifications.isEmpty()) {
                    isManual = activeSpecifications.stream().anyMatch(specificationEntity -> {
                        return specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                            return testcaseEntity.getManual() && testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                        });
                    });
                    isAutomated = activeSpecifications.stream().anyMatch(specificationEntity -> {
                        return specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                            return !testcaseEntity.getManual() && testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                        });
                    });
                    isRequired = activeSpecifications.stream().anyMatch(specificationEntity -> {
                        return specificationEntity.getRequired();
                    });
                    isRecommended = activeSpecifications.stream().anyMatch(specificationEntity -> {
                        return !specificationEntity.getRequired();
                    });
                    isFunctional = activeSpecifications.stream().anyMatch(specificationEntity -> {
                        return specificationEntity.getFunctional();
                    });
                    isWorkflow = activeSpecifications.stream().anyMatch(specificationEntity -> {
                        return !specificationEntity.getFunctional();
                    });

                    TestcaseResultEntity componentTestcaseResult = createDraftTestCaseResultIfNotExists(
                            ComponentServiceConstants.COMPONENT_REF_OBJ_URI,
                            componentEntity.getId(),
                            testRequestEntity.getId(),
                            componentEntity.getName(),
                            counter,
                            isManual,
                            isAutomated,
                            isRequired,
                            isRecommended,
                            isFunctional,
                            isWorkflow,
                            testRequestTestcaseResult.getId(),
                            null, contextInfo);
                    counter++;

                    for (SpecificationEntity specificationEntity : activeSpecifications) {
                        List<TestcaseEntity> filteredTestcases = specificationEntity.getTestcases().stream().filter(testcaseEntity -> {
                                    return testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                                })
                                .sorted(Comparator.comparing(TestcaseEntity::getRank))
                                .collect(Collectors.toList());

                        if (!filteredTestcases.isEmpty()) {
                            isManual = specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                                return testcaseEntity.getManual() && testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                            });
                            isAutomated = specificationEntity.getTestcases().stream().anyMatch(testcaseEntity -> {
                                return !testcaseEntity.getManual() && testcaseEntity.getState().equals(TestcaseServiceConstants.TESTCASE_STATUS_ACTIVE);
                            });
                            isRequired = specificationEntity.getRequired();
                            isRecommended = !specificationEntity.getRequired();
                            isFunctional = specificationEntity.getFunctional();
                            isWorkflow = !specificationEntity.getFunctional();
                            TestcaseResultEntity specificationTestcaseResult = createDraftTestCaseResultIfNotExists(
                                    SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI,
                                    specificationEntity.getId(),
                                    testRequestEntity.getId(),
                                    specificationEntity.getName(),
                                    counter,
                                    isManual,
                                    isAutomated,
                                    isRequired,
                                    isRecommended,
                                    isFunctional,
                                    isWorkflow,
                                    componentTestcaseResult.getId(),
                                    null, contextInfo);
                            counter++;
                            for (TestcaseEntity testcaseEntity : filteredTestcases) {
                                TestcaseResultEntity testcaseResult = createDraftTestCaseResultIfNotExists(
                                        TestcaseServiceConstants.TESTCASE_REF_OBJ_URI,
                                        testcaseEntity.getId(),
                                        testRequestEntity.getId(),
                                        testcaseEntity.getName(),
                                        counter,
                                        testcaseEntity.getManual(),
                                        !testcaseEntity.getManual(),
                                        isRequired,
                                        isRecommended,
                                        isFunctional,
                                        isWorkflow,
                                        specificationTestcaseResult.getId(),
                                        testcaseEntity, contextInfo);


                                // create TestResultRelation For Manual
                                createTestResultRelationsForManualTestcase(testcaseEntity, testcaseResult, contextInfo);

                                counter++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateIsSuccessAndGradeForReport(TestRequestEntity testRequestEntity, ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException, DoesNotExistException {

        TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
        testcaseResultCriteriaSearchFilter.setTestRequestId(testRequestEntity.getId());

        List<TestcaseResultEntity> testcaseResultEntities = testcaseResultService.searchTestcaseResults(
                testcaseResultCriteriaSearchFilter,
                contextInfo);

        Optional<TestcaseResultEntity> testcaseResultOptional = testcaseResultEntities.stream().filter(testcaseResultEntity -> testcaseResultEntity.getRefObjUri().equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)).findFirst();

        if (testcaseResultOptional.isEmpty()) {
            throw new DoesNotExistException("Testcase Result Does not exists with id :" + testRequestEntity.getId());
        }

        TestcaseResultEntity testcaseResult = testcaseResultOptional.get();

        recalculateTestcaseResultEntity(testcaseResult, testcaseResultEntities, contextInfo);
    }   

    private void recalculateTestcaseResultEntity(TestcaseResultEntity testcaseResultEntity, List<TestcaseResultEntity> testcaseResultEntities, ContextInfo contextInfo) {
        if (testcaseResultEntity.getRefObjUri().equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI)) {
            //FOR SPECIFICATION
            //set success
            List<TestcaseResultEntity> filteredTestcaseResults = getFilteredChileTestcaseResultsForTestResult(testcaseResultEntity, testcaseResultEntities);
            testcaseResultEntity.setSuccess(filteredTestcaseResults.stream().allMatch(testcaseResultEntity1 ->
                    testcaseResultEntity1.getState()
                            .equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP)
                            || Boolean.TRUE.equals(testcaseResultEntity1.getSuccess())));
            //set duration
            Long duration = 0L;
            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                if (tcr.getDuration() != null) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);
            //Set message
            List<String> failedTestcaseResultName = filteredTestcaseResults.stream().filter(tcre -> tcre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !tcre.getSuccess()).map(IdStateNameMetaEntity::getName).toList();
            String message;
            if (failedTestcaseResultName.isEmpty()) {
                message = "Passed";
            } else {
                message = getMessage("Specification <b>", testcaseResultEntity, "</b> has been failed due to failing following testcase failure: ", failedTestcaseResultName);
            }
            testcaseResultEntity.setMessage(message);

            //Set compliance of specification
            int compliance = GradeEvaluator.getComplianceForSpecification(filteredTestcaseResults);
            testcaseResultEntity.setCompliant(compliance);
            testcaseResultEntity.setNonCompliant(filteredTestcaseResults.size()-compliance);

        } else if (testcaseResultEntity.getRefObjUri().equals(ComponentServiceConstants.COMPONENT_REF_OBJ_URI)) {
            //FOR COMPONENT
            //update specifications
            List<TestcaseResultEntity> filteredTestcaseResults = getFilteredChileTestcaseResultsForTestResult(testcaseResultEntity, testcaseResultEntities);
            for(TestcaseResultEntity testcaseResult: filteredTestcaseResults) {
                recalculateTestcaseResultEntity(testcaseResult, testcaseResultEntities, contextInfo);
            }
            //set success
            testcaseResultEntity.setSuccess(filteredTestcaseResults.stream().allMatch(testcaseResultEntity1 ->
                    !testcaseResultEntity1.getRequired() || Boolean.TRUE.equals(testcaseResultEntity1.getSuccess())));
            //set duration
            Long duration = 0L;
            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                if (tcr.getDuration() != null) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);
            //set message
            List<String> failedTestcaseResultName = filteredTestcaseResults.stream().filter(tcre -> tcre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !tcre.getSuccess()).map(IdStateNameMetaEntity::getName).toList();
            String message;
            if (failedTestcaseResultName.isEmpty()) {
                message = "Passed";
            } else {
                message = getMessage("Component <b>", testcaseResultEntity, "</b> has been failed due to failing following specification failure: ", failedTestcaseResultName);
            }
            testcaseResultEntity.setMessage(message);

            //Set Compliance of component
            testcaseResultEntity.setCompliant(GradeEvaluator.getCompliance(filteredTestcaseResults));
            testcaseResultEntity.setNonCompliant(GradeEvaluator.getNonCompliance(filteredTestcaseResults));


            //set grade
            testcaseResultEntity.setGrade(gradeEvaluator.evaluate(
                    filteredTestcaseResults.stream().filter(tcre -> !tcre.getRequired())
                            .collect(Collectors.toList()),
                    contextInfo)
            );
        } else if (testcaseResultEntity.getRefObjUri().equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)) {
            //FOR TestRequest
            //update components
            List<TestcaseResultEntity> filteredTestcaseResults = getFilteredChileTestcaseResultsForTestResult(testcaseResultEntity, testcaseResultEntities);
            for(TestcaseResultEntity testcaseResult: filteredTestcaseResults) {
                recalculateTestcaseResultEntity(testcaseResult, testcaseResultEntities, contextInfo);
            }
            //set success
            testcaseResultEntity.setSuccess(filteredTestcaseResults.stream().allMatch(testcaseResultEntity1 ->
                    !testcaseResultEntity1.getRequired() || Boolean.TRUE.equals(testcaseResultEntity1.getSuccess())));
            //set duration
            Long duration = 0L;
            for (TestcaseResultEntity tcr : filteredTestcaseResults) {
                if (tcr.getDuration() != null) {
                    duration = duration + tcr.getDuration();
                }
            }
            testcaseResultEntity.setDuration(duration);
            //set message
            List<String> failedTestcaseResultName = filteredTestcaseResults.stream().filter(tcre -> tcre.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED) && !tcre.getSuccess()).map(IdStateNameMetaEntity::getName).toList();
            String message;
            if (failedTestcaseResultName.isEmpty()) {
                message = "Passed";
            } else {
                message = getMessage("Test Request <b>", testcaseResultEntity, "</b> has been failed due to failing following component failure: ", failedTestcaseResultName);
            }
            testcaseResultEntity.setMessage(message);

            //Set Compliance of Test Request
            if(testcaseResultEntity.getRefObjUri().equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)){
                testcaseResultEntity.setCompliant(GradeEvaluator.getCompliance(filteredTestcaseResults));
                testcaseResultEntity.setNonCompliant(GradeEvaluator.getNonCompliance(filteredTestcaseResults));
            }

            //set grade
            testcaseResultEntity.setGrade(gradeEvaluator.evaluate(
                    testcaseResultEntities.stream().filter(tcre -> tcre.getRefObjUri().equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI) && !tcre.getRequired())
                            .collect(Collectors.toList()),
                    contextInfo)
            );
        }
    }

    private static List<TestcaseResultEntity> getFilteredChileTestcaseResultsForTestResult(TestcaseResultEntity testcaseResultEntity, List<TestcaseResultEntity> testcaseResultEntities) {
        return testcaseResultEntities.stream()
                .filter(tcre -> {
                    return tcre.getParentTestcaseResult() != null
                            && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                }).collect(Collectors.toList());
    }

    private void createTestResultRelationsForManualTestcase(TestcaseEntity testcaseEntity, TestcaseResultEntity testcaseResult, ContextInfo contextInfo) throws InvalidParameterException, DataValidationErrorException, OperationFailedException {

        // create for testcase
        TestResultRelationEntity testResultRelationEntity = createTestResultRelationEntity(
                TestcaseServiceConstants.TESTCASE_REF_OBJ_URI,
                testcaseEntity.getId(),
                testcaseEntity.getVersion(),
                testcaseResult
        );

        testResultRelationService.createTestcaseResult(testResultRelationEntity, contextInfo);

        // create for documents related to question
        DocumentCriteriaSearchFilter documentCriteriaSearchFilter = new DocumentCriteriaSearchFilter();
        documentCriteriaSearchFilter.setRefObjUri(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI);
        documentCriteriaSearchFilter.setRefId(testcaseEntity.getId());
        documentCriteriaSearchFilter.setState(Collections.singletonList(DocumentServiceConstants.DOCUMENT_STATUS_ACTIVE));

        List<DocumentEntity> documentEntities = documentService.searchDocument(documentCriteriaSearchFilter, contextInfo);

        for (DocumentEntity documentEntity : documentEntities) {
            testResultRelationEntity = createTestResultRelationEntity(
                    DocumentServiceConstants.DOCUMENT_REF_OBJ_URI,
                    documentEntity.getId(),
                    documentEntity.getVersion(),
                    testcaseResult
            );

            testResultRelationService.createTestcaseResult(testResultRelationEntity, contextInfo);
        }


        if (Boolean.TRUE.equals(testcaseEntity.getManual())) {
            // create for options
            TestcaseOptionCriteriaSearchFilter testcaseOptionCriteriaSearchFilter = new TestcaseOptionCriteriaSearchFilter();
            testcaseOptionCriteriaSearchFilter.setTestcaseId(testcaseEntity.getId());
            testcaseOptionCriteriaSearchFilter.setState(Collections.singletonList(TestcaseOptionServiceConstants.TESTCASE_OPTION_STATUS_ACTIVE));

            List<TestcaseOptionEntity> testcaseOptionEntities = testcaseOptionService.searchTestcaseOptions(testcaseOptionCriteriaSearchFilter, contextInfo);

            for (TestcaseOptionEntity testcaseOptionEntity : testcaseOptionEntities) {
                testResultRelationEntity = createTestResultRelationEntity(
                        TestcaseOptionServiceConstants.TESTCASE_OPTION_REF_OBJ_URI,
                        testcaseOptionEntity.getId(),
                        testcaseOptionEntity.getVersion(),
                        testcaseResult
                );

                testResultRelationService.createTestcaseResult(testResultRelationEntity, contextInfo);
            }
        }
    }

    public TestResultRelationEntity createTestResultRelationEntity(String refObjUri, String refId, Long versionOfRefEntity, TestcaseResultEntity testcaseResult) {
        TestResultRelationEntity testResultRelationEntity = new TestResultRelationEntity();
        testResultRelationEntity.setRefObjUri(refObjUri);
        testResultRelationEntity.setRefId(refId);
        testResultRelationEntity.setVersionOfRefEntity(versionOfRefEntity);
        testResultRelationEntity.setTestcaseResultEntity(testcaseResult);

        return testResultRelationEntity;
    }


    private List<ComponentEntity> fetchActiveComponents(ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {
        ComponentCriteriaSearchFilter componentCriteriaSearchFilter = new ComponentCriteriaSearchFilter();
        componentCriteriaSearchFilter.setState(Collections.singletonList(ComponentServiceConstants.COMPONENT_STATUS_ACTIVE));
        return componentService.searchComponents(componentCriteriaSearchFilter, Constant.FULL_PAGE_SORT_BY_RANK, contextInfo).getContent();
    }

    private TestcaseResultEntity createDraftTestCaseResultIfNotExists(String refObjUri,
                                                                      String refId,
                                                                      String testRequestId,
                                                                      String name,
                                                                      Integer counter,
                                                                      Boolean isManual,
                                                                      Boolean isAutomated,
                                                                      Boolean isRequired,
                                                                      Boolean isRecommended,
                                                                      Boolean isFunctional,
                                                                      Boolean isWorkflow,
                                                                      String parentTestcaseResultId,
                                                                      TestcaseEntity testcaseEntity, ContextInfo contextInfo) throws InvalidParameterException, DataValidationErrorException, OperationFailedException, DoesNotExistException, VersionMismatchException {

        TestcaseResultCriteriaSearchFilter searchFilter = new TestcaseResultCriteriaSearchFilter();
        searchFilter.setRefObjUri(refObjUri);
        searchFilter.setRefId(refId);



        searchFilter.setTestRequestId(testRequestId);
        searchFilter.setManual(isManual);

        List<TestcaseResultEntity> testcaseResultEntities = testcaseResultService.searchTestcaseResults(
                searchFilter,
                Constant.FULL_PAGE,
                contextInfo).getContent();

        if (!testcaseResultEntities.isEmpty()) {
            return testcaseResultEntities.get(0);
        }

        TestcaseResultEntity testcaseResultEntity = new TestcaseResultEntity();
        testcaseResultEntity.setRefObjUri(refObjUri);
        testcaseResultEntity.setRefId(refId);
        testcaseResultEntity.setState(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT);

        TestRequestEntity testRequestEntity = new TestRequestEntity();
        testRequestEntity.setId(testRequestId);
        testcaseResultEntity.setTestRequest(testRequestEntity);
        testcaseResultEntity.setTestcase(testcaseEntity);

        testcaseResultEntity.setRank(counter);
        testcaseResultEntity.setName(name);
        testcaseResultEntity.setManual(isManual);
        testcaseResultEntity.setAutomated(isAutomated);
        testcaseResultEntity.setRequired(isRequired);
        testcaseResultEntity.setRecommended(isRecommended);
        testcaseResultEntity.setFunctional(isFunctional);
        testcaseResultEntity.setWorkflow(isWorkflow);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(contextInfo.getUsername());
        testcaseResultEntity.setTester(userEntity);
        if (StringUtils.hasLength(parentTestcaseResultId)) {
            TestcaseResultEntity parentTestcaseResult = new TestcaseResultEntity();
            parentTestcaseResult.setId(parentTestcaseResultId);
            testcaseResultEntity.setParentTestcaseResult(parentTestcaseResult);
        }
        return testcaseResultService.createTestcaseResult(testcaseResultEntity, contextInfo);
    }

    private void defaultValueCreateTestRequest(TestRequestEntity testRequestEntity, ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, OperationFailedException {
        testRequestEntity.setState(TestRequestServiceConstants.TEST_REQUEST_STATUS_PENDING);
        UserEntity principalUser = userService.getPrincipalUser(contextInfo);
        if (principalUser.getRoles().stream().anyMatch(roleEntity -> UserServiceConstants.ROLE_ID_ASSESSEE.equals(roleEntity.getId()))) {
            testRequestEntity.setAssessee(principalUser);
        }
    }

    private void defaultValueChangeState(TestRequestEntity testRequestEntity, String stateKey, ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, OperationFailedException {
        if (stateKey.equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED) || stateKey.equals(TestRequestServiceConstants.TEST_REQUEST_STATUS_REJECTED)) {
            testRequestEntity.setApprover(userService.getPrincipalUser(contextInfo));
        }
    }

    private void messageAdminsIfTestRequestCreated(ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException {
        //Notify each admin that a test request is created
        List<UserEntity> admins = userService.getUsersByRole("role.admin", contextInfo);
        for (UserEntity admin : admins) {
            if (testRequestCreateMail) {
                emailService.testRequestCreatedMessage(admin.getEmail(), admin.getName(), contextInfo.getEmail());
            }
            if (testRequestCreateNotification) {
                NotificationEntity notificationEntity = new NotificationEntity("A new Test Request has been created by "+contextInfo.getEmail(), admin);
                applicationEventPublisher.publishEvent(new NotificationCreationEvent(notificationEntity, contextInfo));
            }
        }
    }

    private void messageAssesseeIfTestRequestAccepted(UserEntity requestingUser, String testRequestName, ContextInfo contextInfo) {
        if(testRequestAcceptMail) {
            emailService.testRequestAcceptedMessage(requestingUser.getEmail(), requestingUser.getName(), testRequestName);
        }
        if(testRequestAcceptNotification) {
            NotificationEntity notificationEntity = new NotificationEntity("Your Test Request with name "+testRequestName+" has been accepted.",requestingUser);
            applicationEventPublisher.publishEvent(new NotificationCreationEvent(notificationEntity, contextInfo));
        }
    }

    private void messageAssesseeIfTestRequestRejected(UserEntity requestingUser, String message, String testRequestName, ContextInfo contextInfo) {
        if(testRequestRejectMail) {
            emailService.testRequestRejectedMessage(requestingUser.getEmail(), requestingUser.getName(), testRequestName, message);
        }
        if(testRequestRejectNotification) {
            NotificationEntity notificationEntity = new NotificationEntity("Your Test Request with name "+testRequestName+" has been rejected.\nRejection Message : "+message,requestingUser);
            applicationEventPublisher.publishEvent(new NotificationCreationEvent(notificationEntity, contextInfo));
        }
    }

    private void messageAssesseeIfTestRequestPublished(UserEntity requestingUser, String testRequestName, String reportLink, ContextInfo contextInfo) {
        if(testRequestPublishMail) {
            emailService.testRequestPublishedMessage(requestingUser.getEmail(), requestingUser.getName(), testRequestName, reportLink);
        }
        if(testRequestPublishNotification) {
            NotificationEntity notificationEntity = new NotificationEntity("Your Test Request with name "+testRequestName+" has been Published.",requestingUser);
            applicationEventPublisher.publishEvent(new NotificationCreationEvent(notificationEntity, contextInfo));
        }
    }

    private void messageAssesseeIfTestRequestUnPublished(UserEntity requestingUser, String testRequestName, String reportLink, ContextInfo contextInfo){
        if(testRequestUnpublishMail) {
            emailService.testRequestUnpublishedMessage(requestingUser.getEmail(), requestingUser.getName(), testRequestName, reportLink);
        }
        if(testRequestUnpublishNotification) {
            NotificationEntity notificationEntity = new NotificationEntity("Your Test Request with name "+testRequestName+" has been Unpublished.",requestingUser);
            applicationEventPublisher.publishEvent(new NotificationCreationEvent(notificationEntity, contextInfo));
        }
    }

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

    @Async
    public int getAllTestRequestResults() {

        // Search for all test Request Results count
        return testcaseResultService.countTestcaseResultsOfTestRequest();

    }

    @Async
    public int noOfAssesseesRegistered() {

        return userService.searchActiveAssessees();

    }

    @Async
    public float complianceRate() {

        long now = System.currentTimeMillis();

        List<Object[]> complianceAndNonCompliance = testcaseResultService.complianceAndNonComplianceOfAllTestRequestResults();

        long compliance = 0;
        long nonCompliance = 0;
        if (complianceAndNonCompliance.get(0)[0] != null && complianceAndNonCompliance.get(0)[1] != null) {
            compliance = (long) complianceAndNonCompliance.get(0)[0];
            nonCompliance = (long) complianceAndNonCompliance.get(0)[1];
        }


        if((compliance+nonCompliance)!=0){
            now = System.currentTimeMillis() - now;
            System.out.println("Testing Time For Compliance Rate : " + now);
            return ((float) compliance /(compliance + nonCompliance))*100.0F;
        } else {
            now = System.currentTimeMillis() - now;
            System.out.println("Testing Time For Compliance Rate : " + now);
            return 0;
        }

    }

    @Async
    public float testingRate() {

        long now = System.currentTimeMillis();

        List<Object[]> getFinishedSkippedAndAllTestRequestResults = testcaseResultService.getFinishedSkippedAndAllTestRequestResults();

        long finishedAndSkippedTestRequestResults = (long) getFinishedSkippedAndAllTestRequestResults.get(0)[0];
        long allTestRequestResults = (long) getFinishedSkippedAndAllTestRequestResults.get(0)[1];

        if (allTestRequestResults != 0) {

            now = System.currentTimeMillis() - now;
            System.out.println("Testing Time For Testing Rate : " + now);

            return (finishedAndSkippedTestRequestResults/(float)allTestRequestResults)*100.0F;
        }
        else {
            now = System.currentTimeMillis() - now;
            System.out.println("Testing Time For Testing Rate : " + now);
            return 0;
        }

    }

    @Async
    public List<ApplicationRequests> applicationRequestsByMonth(){
        long now = System.currentTimeMillis();

        List<Object[]> maxmindate = testcaseResultRepository.maxMinDate(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI);

        Date maxDate = (Date) maxmindate.get(0)[0];
        Date minDate = (Date) maxmindate.get(0)[1];

        int max=-1;
        int min=0;

        if(minDate!=null && maxDate!=null) {
            min = minDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().getYear();
            max = maxDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().getYear();
        }

        List<ApplicationRequests> applicationRequests = new ArrayList<>();
        for(int year = max; year >=min; year--){

            List<ApplicationRequestDataByMonth> applicationRequestDataByMonthList = new ArrayList<>();

            for(int month = 1 ; month <= 12 ; month++){

                int monthlyTestRequestResultsCount = testcaseResultRepository.findResultsPerMonthCount(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI, year,month);

                ApplicationRequestDataByMonth applicationRequestDataByMonth = new ApplicationRequestDataByMonth();

                applicationRequestDataByMonth.setMonth(month);

                int compliant = testcaseResultRepository.findResultsPerMonthBySuccess(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI, year,month);

                applicationRequestDataByMonth.setCompliant(compliant);
                applicationRequestDataByMonth.setNonCompliant(monthlyTestRequestResultsCount - compliant);

                applicationRequestDataByMonthList.add(applicationRequestDataByMonth);
            }
            ApplicationRequests applicationRequest = new ApplicationRequests();
            applicationRequest.setYear(year);

            applicationRequest.setApplicationRequestDataByMonthList(applicationRequestDataByMonthList);

            applicationRequests.add(applicationRequest);

        }

        now = System.currentTimeMillis() - now;
        System.out.println("Testing Time For Application Requests By Month : " + now);
        return applicationRequests;
    }

    @Async
    public List<CompliantApplication> compliantApplications() {

        long now = System.currentTimeMillis();

        // Store returnable data of compliant application
        List<CompliantApplication> compliantApplications = new ArrayList<>();

        // Get Top five test Request Results according to compliant/non-compliant ratio
        List<TestcaseResultEntity> topFiveTestRequestsResult = testcaseResultService.findTopFiveTestRequestsResult();

        // Calculate and set compliant applications in compliant applications list
        for(int i = 0 ; i < topFiveTestRequestsResult.size() ; i++){

            CompliantApplication compliantApplication = new CompliantApplication();

            int compliantApplicationCompliant, compliantApplicationNonCompliant;

            try {
                compliantApplicationCompliant = topFiveTestRequestsResult.get(i).getCompliant();
            } catch(Exception e) {
                compliantApplicationCompliant = 0;
            }

            try {
                compliantApplicationNonCompliant = topFiveTestRequestsResult.get(i).getNonCompliant();
            } catch(Exception e) {
                compliantApplicationNonCompliant = 0;
            }

            compliantApplication.setApplicationName(topFiveTestRequestsResult.get(i).getName());
            compliantApplication.setTestcasesPassed(compliantApplicationCompliant);
            compliantApplication.setRank(i+1);
            compliantApplication.setTotalTestcases(compliantApplicationCompliant + compliantApplicationNonCompliant);

            // Search for Component Testcase Results of the current application of the loop
            TestcaseResultCriteriaSearchFilter searchFilter = new TestcaseResultCriteriaSearchFilter();
            searchFilter.setParentTestcaseResultId(topFiveTestRequestsResult.get(i).getId());

            /* QUESTION : Database call to get all components and then use java logic to match component OR Call database each time to get specific component */

            List<Object[]> componentEntities = componentService.searchComponentPartsByTestRequest(topFiveTestRequestsResult.get(i).getTestRequest().getId());

            /* QUESTION : Which Rank to be used?? */
            List<Component> components = getComponents(componentEntities);

            compliantApplication.setComponents(components);

            compliantApplications.add(compliantApplication);

        }
        now = System.currentTimeMillis() - now;
        System.out.println("Testing Time For Compliant Applications : " + now);
        return compliantApplications;
    }

    @Async
    public Map<String, Integer> pieChart() {

        long now = System.currentTimeMillis();

        Map<String, Integer> pieChart = new HashMap<>();

        // Put counts of each status of test requests in the pieChart variable
        pieChart.put(TestRequestServiceConstants.TEST_REQUEST_STATUS_INPROGRESS, testRequestRepository.findCountByState(TestRequestServiceConstants.TEST_REQUEST_STATUS_INPROGRESS));
        pieChart.put(TestRequestServiceConstants.TEST_REQUEST_STATUS_PENDING, testRequestRepository.findCountByState(TestRequestServiceConstants.TEST_REQUEST_STATUS_PENDING));
        pieChart.put(TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED, testRequestRepository.findCountByState(TestRequestServiceConstants.TEST_REQUEST_STATUS_FINISHED));
        pieChart.put(TestRequestServiceConstants.TEST_REQUEST_STATUS_SKIPPED, testRequestRepository.findCountByState(TestRequestServiceConstants.TEST_REQUEST_STATUS_SKIPPED));
        pieChart.put(TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED, testRequestRepository.findCountByState(TestRequestServiceConstants.TEST_REQUEST_STATUS_ACCEPTED));
        pieChart.put(TestRequestServiceConstants.TEST_REQUEST_STATUS_REJECTED, testRequestRepository.findCountByState(TestRequestServiceConstants.TEST_REQUEST_STATUS_REJECTED));

        now = System.currentTimeMillis() - now;
        System.out.println("Testing Time For PieChart : " + now);

        return pieChart;
    }

    @Async
    public List<AwardGraph> awardGraphs() {

        long now = System.currentTimeMillis();

        List<AwardGraph> awardGraphs = new ArrayList<>();

        List<Object[]> allComponents = componentService.findAllIdName();

        for (Object[] componentEntity : allComponents) {

            List<Object[]> bestFiveTestcaseResultPerComponent = this.findBestFiveTestcaseResultPerComponent((String) componentEntity[0]);

            List<AwardApplication> awardApplicationList = new ArrayList<>();

            AwardGraph awardGraph = new AwardGraph();

            awardGraph.setComponentName((String)componentEntity[1]);

            if(bestFiveTestcaseResultPerComponent.isEmpty()){


                awardGraph.setAwardApplicationList(new ArrayList<>());


            } else {


                for(Object[] testcaseResultPerComponent : bestFiveTestcaseResultPerComponent){
                    AwardApplication awardApplication = new AwardApplication();

                    awardApplication.setPassedTestcases((int)testcaseResultPerComponent[0]);
                    awardApplication.setTotalTestcases((int) testcaseResultPerComponent[0] + (int) testcaseResultPerComponent[1]);
                    awardApplication.setAppName(((TestRequestEntity)testcaseResultPerComponent[2]).getName());

                    awardApplicationList.add(awardApplication);
                }
                Comparator<AwardApplication> comparator = Comparator.comparing(
                        awardApplication -> {
                            if(awardApplication.getTotalTestcases()!=0){
                                return (awardApplication.getPassedTestcases() / awardApplication.getTotalTestcases());
                            } else {
                                return awardApplication.getPassedTestcases();
                            }
                        });

                awardApplicationList.sort(comparator.reversed());

                awardGraph.setAwardApplicationList(awardApplicationList);


            }


            awardGraphs.add(awardGraph);

        }

        Comparator<AwardGraph> comparator = Comparator.comparing(
                awardGraph -> {
                    if(Boolean.FALSE.equals(awardGraph.getAwardApplicationList().isEmpty())){
                        if(awardGraph.getAwardApplicationList().get(0).getTotalTestcases()!=0){
                            return (awardGraph.getAwardApplicationList().get(0).getPassedTestcases()/awardGraph.getAwardApplicationList().get(0).getTotalTestcases());
                        } else {
                            return 0;
                        }
                    } else {
                        return 0;
                    }

                });

        awardGraphs.sort(comparator.reversed());

        for(AwardGraph awardGraph : awardGraphs){

            awardGraph.setComponentRank(awardGraphs.indexOf(awardGraph)+1);
        }

        now = System.currentTimeMillis() - now;
        System.out.println("Testing Time For Award Graphs : " + now);

        return awardGraphs;
    }

    @Async
    public List<PercentageCumulativeGraph> percentageCumulativeGraphs() {

        long now = System.currentTimeMillis();

        List<PercentageCumulativeGraph> percentageCumulativeGraphs = new ArrayList<>();

        // Find all components
        List<String> componentEntities = componentService.findAllName();

        List<Object[]> filteredComponentsResults = testcaseResultRepository.nameComplianceAndNonCompliance(ComponentServiceConstants.COMPONENT_REF_OBJ_URI, TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED);

        for(Object[] componentResult: filteredComponentsResults){
            PercentageCumulativeGraph percentageCumulativeGraph = new PercentageCumulativeGraph();

            percentageCumulativeGraph.setCompliantTestRequests((Long)componentResult[0]);
            percentageCumulativeGraph.setTotalTestRequests((Long)componentResult[1] + (Long)componentResult[0]);
            percentageCumulativeGraph.setComponentName(String.valueOf(componentResult[2]));
            componentEntities = componentEntities.stream().filter(componentEntity -> !(componentEntity.equals(String.valueOf(componentResult[2])))).collect(Collectors.toList());

            percentageCumulativeGraphs.add(percentageCumulativeGraph);
        }
        for(String component : componentEntities){
            PercentageCumulativeGraph percentageCumulativeGraph = new PercentageCumulativeGraph();

            percentageCumulativeGraph.setComponentName(component);
            percentageCumulativeGraph.setTotalTestRequests(0L);
            percentageCumulativeGraph.setCompliantTestRequests(0L);

            percentageCumulativeGraphs.add(percentageCumulativeGraph);
        }

        // Sort percentageCumulativeGraph using percentage of compliant test Requests
        percentageCumulativeGraphs.sort(Comparator.comparing(percentageCumulativeGraph -> {
            if(percentageCumulativeGraph.getTotalTestRequests()!=0){
                return percentageCumulativeGraph.getCompliantTestRequests() / percentageCumulativeGraph.getTotalTestRequests();
            } else {
                return percentageCumulativeGraph.getCompliantTestRequests();
            }
        }));

        // Set component rank based in List index as list is sorted
        for( int i = 0 ; i < percentageCumulativeGraphs.size() ; i++ ){
            percentageCumulativeGraphs.get(i).setComponentRank( i + 1 );
        }

        now = System.currentTimeMillis() - now;
        System.out.println("Testing Time For Percentage Cumulative Graphs : " + now);

        return percentageCumulativeGraphs;
    }

    @Override
    public GraphInfo getDashboard(ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {

        GraphInfo graphInfo = new GraphInfo();

        // Get All Test Request Results

        CompletableFuture<Integer> futureTestRequestResults = CompletableFuture.supplyAsync(this::getAllTestRequestResults);


        // Total number of assessees registered

        CompletableFuture<Integer> futureNoOfAssesseesRegistered = CompletableFuture.supplyAsync(this::noOfAssesseesRegistered);


        // Compliance Rate

        CompletableFuture<Float> futureComplianceRate = CompletableFuture.supplyAsync(this::complianceRate);





        // Testing Rate


        CompletableFuture<Float> futureTestingRate = CompletableFuture.supplyAsync(this::testingRate);


        // Set ApplicationRequestsByMonth


        CompletableFuture<List<ApplicationRequests>> futureApplicationRequestsByMonth = CompletableFuture.supplyAsync(this::applicationRequestsByMonth);



        // Compliant Application

        CompletableFuture<List<CompliantApplication>> futureCompliantApplications = CompletableFuture.supplyAsync(this::compliantApplications);


        //PIE CHART

        CompletableFuture<Map<String, Integer>> futurePieChart = CompletableFuture.supplyAsync(this::pieChart);


        //AWARD GRAPH

        CompletableFuture<List<AwardGraph>> futureAwardGraphs = CompletableFuture.supplyAsync(this::awardGraphs);


        //Percentage Cumulative Graph

        CompletableFuture<List<PercentageCumulativeGraph>> futurePercentageCumulativeGraph = CompletableFuture.supplyAsync(this::percentageCumulativeGraphs);

        CompletableFuture<Void> combinedFuture
                = CompletableFuture.allOf(futureTestRequestResults, futureNoOfAssesseesRegistered, futureComplianceRate, futureTestingRate, futureApplicationRequestsByMonth, futureCompliantApplications, futurePieChart, futureAwardGraphs, futurePercentageCumulativeGraph);

        try {
            combinedFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new OperationFailedException("Operation Failed", e);
        }


        // Extract results from futures and set them into graphInfo
        try {
            graphInfo.setTotalApplications(futureTestRequestResults.get());
            graphInfo.setAssesseeRegistered(futureNoOfAssesseesRegistered.get());
            graphInfo.setComplianceRate(futureComplianceRate.get());
            graphInfo.setTestingRate(futureTestingRate.get());
            graphInfo.setApplicationRequestsByMonth(futureApplicationRequestsByMonth.get());
            graphInfo.setCompliantApplications(futureCompliantApplications.get());
            graphInfo.setPieChart(futurePieChart.get());
            graphInfo.setAwardGraph(futureAwardGraphs.get());
            graphInfo.setPercentageCumulativeGraph(futurePercentageCumulativeGraph.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new OperationFailedException("Operation Failed", e);
        }


        // Return Graph Info
        return graphInfo;
    }

    private static List<Component> getComponents(List<Object[]> componentEntities) {
        List<Component> components = new ArrayList<>();
        for(int j = 0; j < componentEntities.size() ; j++){

            Component component = new Component();
            component.setComponentRank((Integer)componentEntities.get(j)[0]);
            component.setComponentName((String)componentEntities.get(j)[1]);
            component.setTestcasesPassed((Integer)componentEntities.get(j)[2]);
            component.setTotalTestcases((Integer)componentEntities.get(j)[2] + (Integer)componentEntities.get(j)[3
                    ]);

            components.add(component);
        }
        return components;
    }

    private List<Object[]> findBestFiveTestcaseResultPerComponent(String componentId){
        return testcaseResultService.findBestFiveTestcaseResultPerComponent(componentId);
    }

    @Override
    public List<String> getPendingTestRequests(){
        return testRequestRepository.getPendingTestRequests(TestRequestServiceConstants.TEST_REQUEST_STATUS_PENDING);
    }


}

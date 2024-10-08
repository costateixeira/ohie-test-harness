package com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.util;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.argusoft.path.tht.reportmanagement.constant.TestcaseResultServiceConstants;
import com.argusoft.path.tht.reportmanagement.filter.TestcaseResultCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.models.entity.TestcaseResultEntity;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultAttributesService;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultService;
import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.constant.Module;
import com.argusoft.path.tht.systemconfiguration.constant.ValidateConstant;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.models.entity.IdStateNameMetaEntity;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.testcasemanagement.constant.ComponentServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.SpecificationServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.filter.ComponentCriteriaSearchFilter;
import com.argusoft.path.tht.testcasemanagement.models.entity.ComponentEntity;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseEntity;
import com.argusoft.path.tht.testcasemanagement.service.ComponentService;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseService;
import com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.TestCase;
import com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.event.TestcaseExecutionStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This TestcaseExecutioner can start automation process by running testcases
 * based on the testRequest.
 *
 * @author Dhruv
 */
@Component
public class TestcaseExecutioner {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestcaseExecutioner.class);

    protected static final Map<String, String> map = new HashMap<>();

    private ApplicationContext applicationContext;

    private ComponentService componentService;

    private TestcaseService testcaseService;

    private TestcaseResultService testcaseResultService;


    private TestcaseResultAttributesService testcaseResultAttributesService;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setComponentService(ComponentService componentService) {
        this.componentService = componentService;
    }

    @Autowired
    public void setTestcaseService(TestcaseService testcaseService) {
        this.testcaseService = testcaseService;
    }

    @Autowired
    public void setTestcaseResultService(TestcaseResultService testcaseResultService) {
        this.testcaseResultService = testcaseResultService;
    }


    @Autowired
    public void setTestcaseResultAttributesService(TestcaseResultAttributesService testcaseResultAttributesService) {
        this.testcaseResultAttributesService = testcaseResultAttributesService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeTestingProcess(
            String testRequestId,
            String refObjUri,
            String refId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            ContextInfo contextInfo) throws OperationFailedException {

        List<TestcaseResultEntity> testcaseResultEntities = changeStateAndPrepareIgenericClient(
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
        if (testcaseResultEntities == null) {
            return;
        }

        try {
            ChangeTestcaseResultAttributeUsingCriteriaSearchFilter(testRequestId, contextInfo);
        } catch (DoesNotExistException | InvalidParameterException e) {
            LOGGER.error(ValidateConstant.OPERATION_FAILED_EXCEPTION + TestcaseExecutioner.class.getSimpleName(), e);
            throw new OperationFailedException(e.getMessage(), e);
        }

        applicationEventPublisher.publishEvent(new TestcaseExecutionStartEvent(testRequestId,
                testcaseResultEntities.stream().map(IdStateNameMetaEntity::getId).toList(),
                refId,
                refObjUri,
                isWorkflow,
                isFunctional,
                isRequired,
                isRecommended,
                Constant.SUPER_USER_CONTEXT));

    }

    public void ChangeTestcaseResultAttributeUsingCriteriaSearchFilter(String testRequestId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, OperationFailedException {
        try {
            if (testRequestId.isEmpty()) {
                LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestcaseExecutioner.class.getSimpleName());
                throw new InvalidParameterException("TestRequest Id is not present");
            }
            TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
            testcaseResultCriteriaSearchFilter.setRefId(testRequestId);
            List<TestcaseResultEntity> testcaseResultEntity = testcaseResultService.searchTestcaseResults(testcaseResultCriteriaSearchFilter, contextInfo);
            if (testcaseResultEntity.isEmpty()) {
                LOGGER.error("{}{}", ValidateConstant.DOES_NOT_EXIST_EXCEPTION, TestcaseExecutioner.class.getSimpleName());
                throw new DoesNotExistException("testcaseResultEntity list is empty");
            }
            testcaseResultAttributesService.createAndChangeTestcaseResultAttributes(testcaseResultEntity.get(0), "is_Interrupted", "false", contextInfo);
        } catch (Exception ex) {
            LOGGER.error(ValidateConstant.EXCEPTION + TestcaseExecutioner.class.getSimpleName(), ex);
            throw new RuntimeException(ex);
        }
    }

    public List<TestcaseResultEntity> changeStateAndPrepareIgenericClient(
            String testRequestId,
            String refObjUri,
            String refId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            ContextInfo contextInfo) throws OperationFailedException {
        try {
            List<TestcaseResultEntity> testcaseResultEntities
                    = fetchTestcaseResultsByInputs(
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

            changeTestcaseResultsState(
                    testcaseResultEntities,
                    TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_PENDING,
                    Boolean.FALSE,
                    contextInfo);

            if (!Objects.equals(Boolean.TRUE, isAutomated)) {
                return null;
            }

            return testcaseResultEntities;
            /*TestRequestEntity testRequestEntity = testRequestService.getTestRequestById(testRequestId, contextInfo);
            List<ComponentEntity> activeComponents = fetchActiveComponents(contextInfo).stream().filter(componentEntity
                    -> testRequestEntity.getTestRequestUrls().stream().anyMatch(testRequestUrlEntity -> testRequestUrlEntity.getComponent().getId().equals(componentEntity.getId()))
            ).collect(Collectors.toList());

            Map<String, IGenericClient> iGenericClientMap = new HashMap<>();
            for (ComponentEntity componentEntity : activeComponents) {
                Optional<TestRequestUrlEntity> testRequestUrlOptional = testRequestEntity.getTestRequestUrls().stream().filter(testRequestUrl -> testRequestUrl.getComponent().getId().equals(componentEntity.getId())).findFirst();
                if (testRequestUrlOptional.isPresent()) {
                    TestRequestUrlEntity testRequestUrlEntity = testRequestUrlOptional.get();
                    IGenericClient client = getClient(testRequestUrlEntity.getFhirVersion(), testRequestUrlEntity.getFhirApiBaseUrl(), testRequestUrlEntity.getUsername(), testRequestUrlEntity.getPassword());
                    iGenericClientMap.put(componentEntity.getId(), client);
                } else {
                    LOGGER.error("Unable to find testRequestUrl for {}", componentEntity.getId());
                    throw new OperationFailedException("Unable to find testRequestUrl for " + componentEntity.getId());
                }
            }
            TestcaseResultEntitiesAndIgenericClient testcaseResultEntitiesAndIgenericClient = new TestcaseResultEntitiesAndIgenericClient(testcaseResultEntities, iGenericClientMap);

            ChangeTestcaseResultAttributeUsingCriteriaSearchFilter(testRequestId, contextInfo);

            return testcaseResultEntitiesAndIgenericClient;*/
        } catch (DataValidationErrorException e) {
            LOGGER.error(ValidateConstant.DOES_NOT_EXIST_EXCEPTION + TestcaseExecutioner.class.getSimpleName(), e);
            throw new OperationFailedException(e);
        } catch (InvalidParameterException | OperationFailedException | VersionMismatchException
                 | DoesNotExistException e) {
            LOGGER.error(ValidateConstant.OPERATION_FAILED_EXCEPTION + TestcaseExecutioner.class.getSimpleName(), e);
            throw new OperationFailedException(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
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
            Boolean reset,
            ContextInfo contextInfo) throws OperationFailedException {
        try {
            //Try to interrupt running process.
            boolean threadGroupFlag = false;
            if (contextInfo.getModule() != Module.SYSTEM
                    && !Objects.equals(isManual, Boolean.TRUE)
                    && !Objects.equals(isAutomated, Boolean.FALSE)) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                while (threadGroup != null) {
                    Thread[] threads = new Thread[threadGroup.activeCount()];
                    threadGroup.enumerate(threads);
                    for (Thread thread : threads) {
                        if (thread.getName().equals(testRequestId + refId + refObjUri + (isWorkflow == null ? "null" : isWorkflow.toString()) + (isFunctional == null ? "null" : isFunctional.toString()) + (isRequired == null ? "null" : isRequired.toString()) + (isRecommended == null ? "null" : isRecommended.toString()))) {
                            thread.interrupt();
                            threadGroupFlag = true;
                        }
                    }
                    if (threadGroupFlag) {
                        TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
                        testcaseResultCriteriaSearchFilter.setTestRequestId(testRequestId);
                        testcaseResultCriteriaSearchFilter.setRefId(refId);
                        testcaseResultCriteriaSearchFilter.setRefObjUri(refObjUri);
                        List<TestcaseResultEntity> testcaseResultEntityTwo = testcaseResultService.searchTestcaseResults(testcaseResultCriteriaSearchFilter, contextInfo);
                        TestcaseResultEntity testcaseResult = testcaseResultEntityTwo.get(0);
                        testcaseResultAttributesService.createAndChangeTestcaseResultAttributes(testcaseResult, "is_Interrupted", "true", contextInfo);
                        testcaseResultAttributesService.createAndChangeTestcaseResultAttributes(testcaseResult, "reset", reset.toString(), contextInfo);
                        break;
                    }
                    threadGroup = threadGroup.getParent();
                }
            }
            //If process found and tried to interrupt then following operation will happen when process will actually get stopped.
            if (!threadGroupFlag) {
                List<TestcaseResultEntity> testcaseResultEntities
                        = fetchTestcaseResultsByInputsForReinitialize(
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
                changeTestcaseResultsState(
                        testcaseResultEntities,
                        TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT,
                        reset,
                        contextInfo);
            }
        } catch (DoesNotExistException | InvalidParameterException | OperationFailedException
                 | VersionMismatchException ex) {
            LOGGER.error(ValidateConstant.OPERATION_FAILED_EXCEPTION + TestcaseExecutioner.class.getSimpleName(), ex);
            throw new OperationFailedException("Operation failed while updating testcaseResults", ex);
        } catch (DataValidationErrorException ex) {
            LOGGER.error(ValidateConstant.DATA_VALIDATION_EXCEPTION + TestcaseExecutioner.class.getSimpleName(), ex);
            throw new OperationFailedException(ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void executeTestcase(String testcaseResultId, Map<String, IGenericClient> iGenericClientMap, ContextInfo contextInfo) {
        try {
            TestcaseResultEntity testcaseResult = testcaseResultService.getTestcaseResultById(testcaseResultId, contextInfo);
            // start date for test case
            long startDateForTestCase = System.currentTimeMillis();
            try {
                TestcaseEntity testcaseEntity = testcaseService.getTestcaseById(testcaseResult.getRefId(), contextInfo);

                TestCase testCaseExecutionService = (TestCase) applicationContext.getBean(testcaseEntity.getBeanName());
                // Overwrite the date to be more accurate
                startDateForTestCase = System.currentTimeMillis();
                ValidationResultInfo validationResultInfo = testCaseExecutionService.test(iGenericClientMap, contextInfo);

                updateTestCaseResultByValidationResult(testcaseResult, validationResultInfo, startDateForTestCase, contextInfo);
            } catch (Exception e) {
                LOGGER.error(ValidateConstant.EXCEPTION + TestcaseExecutioner.class.getSimpleName(), e);


                try {
                    String errorMessage = e.getMessage();
                    if (errorMessage.length() > 2000) {
                        errorMessage = errorMessage.substring(0, 2000);
                    }
                    updateTestCaseResultForSystemError(testcaseResult, startDateForTestCase, errorMessage, contextInfo);
                } catch (InvalidParameterException | DataValidationErrorException | OperationFailedException
                         | VersionMismatchException | DoesNotExistException ex) {
                    LOGGER.error(ValidateConstant.EXCEPTION + TestcaseExecutioner.class.getSimpleName(), e);

                }
            }
        } catch (Exception e) {
            LOGGER.error(ValidateConstant.EXCEPTION + TestcaseExecutioner.class.getSimpleName(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markTestcaseResultInProgress(String testcaseResultId, ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, DataValidationErrorException, OperationFailedException, VersionMismatchException {
        testcaseResultService.changeState(testcaseResultId, TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_INPROGRESS, contextInfo);
    }

    private void updateTestCaseResultForSystemError(TestcaseResultEntity testcaseResultEntity, long startDate, String errorMessage, ContextInfo contextInfo) throws InvalidParameterException, DataValidationErrorException, OperationFailedException, VersionMismatchException, DoesNotExistException {
        testcaseResultEntity = testcaseResultService.getTestcaseResultById(testcaseResultEntity.getId(), contextInfo);
        testcaseResultEntity.setSuccess(Boolean.FALSE);
        testcaseResultEntity.setMessage(errorMessage);
        testcaseResultEntity.setHasSystemError(true);
        // Store total duration in milliseconds
        testcaseResultEntity.setDuration(System.currentTimeMillis() - startDate);
        testcaseResultService.updateTestcaseResult(testcaseResultEntity, contextInfo);

        testcaseResultService.changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED, contextInfo);
    }

    private void updateTestCaseResultByValidationResult(TestcaseResultEntity testcaseResultEntity, ValidationResultInfo validationResultInfo, long startDate, ContextInfo contextInfo) throws InvalidParameterException, DataValidationErrorException, OperationFailedException, VersionMismatchException, DoesNotExistException {
        testcaseResultEntity = testcaseResultService.getTestcaseResultById(testcaseResultEntity.getId(), contextInfo);
        testcaseResultEntity.setSuccess(Objects.equals(validationResultInfo.getLevel(), ErrorLevel.OK));
        testcaseResultEntity.setMessage(validationResultInfo.getMessage());
        // Store total duration in milliseconds
        testcaseResultEntity.setDuration(System.currentTimeMillis() - startDate);
        if (!Thread.currentThread().isInterrupted()) {
            testcaseResultService.updateTestcaseResult(testcaseResultEntity, contextInfo);
        }

        testcaseResultService.changeState(testcaseResultEntity.getId(), TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED, contextInfo);
    }

    public void changeTestcaseResultsState(
            List<TestcaseResultEntity> testcaseResultEntities,
            String newState,
            Boolean reset,
            ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException, DataValidationErrorException, DoesNotExistException, VersionMismatchException {
        for (TestcaseResultEntity testcaseResult : testcaseResultEntities) {

            if (Boolean.TRUE.equals(reset) || (!testcaseResult.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED)) && (!testcaseResult.getState().equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_SKIP))) {
                testcaseResultService.changeState(testcaseResult.getId(), newState, contextInfo);
            }
        }
    }

    public List<TestcaseResultEntity> fetchTestcaseResultsByInputsForReinitialize(
            String testRequestId,
            String refObjUri,
            String refId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {
        List<TestcaseResultEntity> filteredTestcaseResults;

        TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
        testcaseResultCriteriaSearchFilter.setTestRequestId(testRequestId);
        testcaseResultCriteriaSearchFilter.setManual(isManual);
        testcaseResultCriteriaSearchFilter.setAutomated(isAutomated);
        testcaseResultCriteriaSearchFilter.setRequired(isRequired);
        testcaseResultCriteriaSearchFilter.setRecommended(isRecommended);
        testcaseResultCriteriaSearchFilter.setFunctional(isFunctional);
        testcaseResultCriteriaSearchFilter.setWorkflow(isWorkflow);

        List<TestcaseResultEntity> testcaseResultEntities = testcaseResultService.searchTestcaseResults(testcaseResultCriteriaSearchFilter, Constant.FULL_PAGE_SORT_BY_RANK, contextInfo).getContent();

        Optional<TestcaseResultEntity> optionalTestcaseResultEntity = testcaseResultEntities.stream().filter(testcaseResultEntity -> {
            return testcaseResultEntity.getRefObjUri().equals(refObjUri) && testcaseResultEntity.getRefId().equals(refId);
        }).findFirst();

        if (!optionalTestcaseResultEntity.isPresent()) {
            LOGGER.error("{}{}", ValidateConstant.OPERATION_FAILED_EXCEPTION, TestcaseExecutioner.class.getSimpleName());
            throw new OperationFailedException("No TestRequest doesn't have testcases for selected inputs.");
        }

        TestcaseResultEntity testcaseResultEntity = optionalTestcaseResultEntity.get();
        if (refObjUri.equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            filteredTestcaseResults = Arrays.asList(testcaseResultEntity);
        } else if (refObjUri.equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI)) {
            filteredTestcaseResults
                    = testcaseResultEntities.stream()
                    .filter(tcre -> {
                        return tcre.getParentTestcaseResult() != null
                                && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                    }).collect(Collectors.toList());
        } else if (refObjUri.equals(ComponentServiceConstants.COMPONENT_REF_OBJ_URI)) {
            List<String> specificationTestcaseResultIds = testcaseResultEntities.stream()
                    .filter(tcre -> {
                        return tcre.getParentTestcaseResult() != null
                                && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                    }).map(tcre -> tcre.getId()).collect(Collectors.toList());
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

        return filteredTestcaseResults;
    }

    private List<TestcaseResultEntity> fetchTestcaseResultsByInputs(
            String testRequestId,
            String refObjUri,
            String refId,
            Boolean isManual,
            Boolean isAutomated,
            Boolean isRequired,
            Boolean isRecommended,
            Boolean isWorkflow,
            Boolean isFunctional,
            ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {
        List<TestcaseResultEntity> filteredTestcaseResults;

        TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter = new TestcaseResultCriteriaSearchFilter();
        testcaseResultCriteriaSearchFilter.setTestRequestId(testRequestId);
        testcaseResultCriteriaSearchFilter.setManual(isManual);
        testcaseResultCriteriaSearchFilter.setAutomated(isAutomated);
        testcaseResultCriteriaSearchFilter.setRequired(isRequired);
        testcaseResultCriteriaSearchFilter.setRecommended(isRecommended);
        testcaseResultCriteriaSearchFilter.setWorkflow(isWorkflow);
        testcaseResultCriteriaSearchFilter.setFunctional(isFunctional);

        List<TestcaseResultEntity> testcaseResultEntities = testcaseResultService.searchTestcaseResults(testcaseResultCriteriaSearchFilter, Constant.FULL_PAGE_SORT_BY_RANK, contextInfo).getContent();

        Optional<TestcaseResultEntity> optionalTestcaseResultEntity = testcaseResultEntities.stream().filter(testcaseResultEntity -> {
            return testcaseResultEntity.getRefObjUri().equals(refObjUri) && testcaseResultEntity.getRefId().equals(refId);
        }).findFirst();

        if (!optionalTestcaseResultEntity.isPresent()) {
            LOGGER.error("{}{}", ValidateConstant.OPERATION_FAILED_EXCEPTION, TestcaseExecutioner.class.getSimpleName());
            throw new OperationFailedException("No TestRequest doesn't have testcases for selected inputs.");
        }

        TestcaseResultEntity testcaseResultEntity = optionalTestcaseResultEntity.get();
        if (refObjUri.equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
            filteredTestcaseResults = Arrays.asList(testcaseResultEntity);
        } else if (refObjUri.equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI)) {
            filteredTestcaseResults
                    = testcaseResultEntities.stream()
                    .filter(tcre -> {
                        return tcre.getParentTestcaseResult() != null
                                && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                    }).collect(Collectors.toList());
        } else if (refObjUri.equals(ComponentServiceConstants.COMPONENT_REF_OBJ_URI)) {
            List<String> specificationTestcaseResultIds = testcaseResultEntities.stream()
                    .filter(tcre -> {
                        return tcre.getParentTestcaseResult() != null
                                && tcre.getParentTestcaseResult().getId().equals(testcaseResultEntity.getId());
                    }).map(tcre -> tcre.getId()).collect(Collectors.toList());
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

        return filteredTestcaseResults.stream()
                .filter(testcaseResultEntity1
                        -> testcaseResultEntity1.getState()
                        .equals(TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_DRAFT))
                .collect(Collectors.toList());
    }

    private List<ComponentEntity> fetchActiveComponents(ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {
        ComponentCriteriaSearchFilter componentCriteriaSearchFilter = new ComponentCriteriaSearchFilter();
        componentCriteriaSearchFilter.setState(Collections.singletonList(ComponentServiceConstants.COMPONENT_STATUS_ACTIVE));
        return componentService.searchComponents(componentCriteriaSearchFilter, Constant.FULL_PAGE_SORT_BY_RANK, contextInfo).getContent();
    }

    private record TestcaseResultEntitiesAndIgenericClient(List<TestcaseResultEntity> testcaseResultEntities,
                                                           Map<String, IGenericClient> iGenericClientMap) {

    }

}

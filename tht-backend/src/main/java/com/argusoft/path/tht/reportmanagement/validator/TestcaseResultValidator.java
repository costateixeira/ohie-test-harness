package com.argusoft.path.tht.reportmanagement.validator;

import com.argusoft.path.tht.reportmanagement.filter.TestResultRelationCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.filter.TestcaseResultCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.models.entity.TestResultRelationEntity;
import com.argusoft.path.tht.reportmanagement.models.entity.TestcaseResultEntity;
import com.argusoft.path.tht.reportmanagement.service.TestResultRelationService;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultService;
import com.argusoft.path.tht.systemconfiguration.audit.service.AuditService;
import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.constant.ValidateConstant;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.DataValidationErrorException;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.DoesNotExistException;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.InvalidParameterException;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.OperationFailedException;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.models.entity.IdMetaEntity;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.utils.RefObjectUriAndRefIdValidator;
import com.argusoft.path.tht.systemconfiguration.utils.ValidationUtils;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseOptionServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseEntity;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseOptionEntity;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseOptionService;
import com.argusoft.path.tht.testprocessmanagement.constant.TestRequestServiceConstants;
import com.argusoft.path.tht.testprocessmanagement.service.TestRequestService;
import com.argusoft.path.tht.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Validation methods for TestcaseResult
 *
 * @author Aastha
 */

@Component
public class TestcaseResultValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestcaseResultValidator.class);

    private static RefObjectUriAndRefIdValidator refObjectUriAndRefIdValidator;

    public static List<ValidationResultInfo> validateCreateUpdateTestCaseResult(String validationTypeKey, TestcaseResultService testcaseResultService, UserService userService, TestRequestService testRequestService, TestcaseResultEntity testcaseResultEntity, ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException, DataValidationErrorException {
        List<ValidationResultInfo> validationResultEntities
                = validateTestCaseResult(validationTypeKey,
                testcaseResultEntity,
                userService,
                testcaseResultService,
                testRequestService,
                contextInfo);
        if (ValidationUtils.containsErrors(validationResultEntities, ErrorLevel.ERROR)) {
            LOGGER.error("{}{}", ValidateConstant.DATA_VALIDATION_EXCEPTION, TestcaseResultValidator.class.getSimpleName());
            throw new DataValidationErrorException(
                    ValidateConstant.ERRORS,
                    validationResultEntities);
        }
        return validationResultEntities;
    }

    public static void validateSubmitTestcaseResult(String testcaseResultId, Set<String> selectedTestcaseOptionIds, TestcaseResultService testcaseResultService,
                                                    TestResultRelationService testResultRelationService,
                                                    ContextInfo contextInfo) throws DataValidationErrorException{
        List<ValidationResultInfo> validationResultEntitys
                = validateTestcaseResultSubmit(
                testcaseResultId,
                selectedTestcaseOptionIds,
                testcaseResultService,
                testResultRelationService,
                contextInfo);
        if (ValidationUtils.containsErrors(validationResultEntitys, ErrorLevel.ERROR)) {
            LOGGER.error("{}{}", ValidateConstant.DATA_VALIDATION_EXCEPTION, TestcaseResultValidator.class.getSimpleName());
            throw new DataValidationErrorException(
                    ValidateConstant.ERRORS,
                    validationResultEntitys);
        }
    }

    public static List<ValidationResultInfo> validateTestCaseResult(String validationTypeKey, TestcaseResultEntity testcaseResultEntity, UserService userService, TestcaseResultService testcaseResultService, TestRequestService testRequestService, ContextInfo contextInfo) throws InvalidParameterException, OperationFailedException {

        if (!StringUtils.hasLength(validationTypeKey)) {
            LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestcaseResultValidator.class.getSimpleName());
            throw new InvalidParameterException("validationTypeKey is missing");
        }
        // VALIDATE
        List<ValidationResultInfo> errors = new ArrayList<>();
        trimTestcaseResult(testcaseResultEntity);

        // check Common Required
        validateCommonRequired(testcaseResultEntity, errors);

        // check Common ForeignKey
        validateCommonForeignKey(testcaseResultEntity, userService, testcaseResultService, testRequestService, errors, contextInfo);

        // check Common Unique
        validateCommonUnique(testcaseResultEntity,
                validationTypeKey,
                testcaseResultService,
                errors,
                contextInfo);

        switch (validationTypeKey) {
            case Constant.UPDATE_VALIDATION:
                // get the info
                if (testcaseResultEntity.getId() != null) {
                    try {
                        TestcaseResultEntity originalEntity = testcaseResultService.getTestcaseResultById(testcaseResultEntity.getId(), contextInfo);
                        validateUpdateTestcaseResult(errors, testcaseResultEntity, originalEntity);
                    } catch (DoesNotExistException | InvalidParameterException ex) {
                        LOGGER.error(ValidateConstant.DOES_NOT_EXIST_EXCEPTION + TestcaseResultValidator.class.getSimpleName(), ex);
                        String fieldName = "id";
                        errors.add(
                                new ValidationResultInfo(fieldName,
                                        ErrorLevel.ERROR,
                                        ValidateConstant.ID_SUPPLIED + "update" + ValidateConstant.DOES_NOT_EXIST));
                        return errors;
                    }
                }
                break;
            case Constant.CREATE_VALIDATION:
                validateCreateTestcaseResult(errors, testcaseResultEntity, testcaseResultService, contextInfo);
                break;
            default:
                LOGGER.error("{}{}", ValidateConstant.INVALID_PARAM_EXCEPTION, TestcaseResultValidator.class.getSimpleName());
                throw new InvalidParameterException(ValidateConstant.INVALID_VALIDATION_TYPE_KEY);
        }

        //for refId and refObjUri
        refObjectUriAndRefIdValidator.refObjectUriAndRefIdValidation(testcaseResultEntity.getRefObjUri(), testcaseResultEntity.getRefId(), contextInfo, errors);

        // For : Id
        validateTestcaseResultEntityId(testcaseResultEntity,
                errors);
        // For :Name
        validateTestcaseResultEntityName(testcaseResultEntity,
                errors);
        // For :Order
        validateTestcaseResultEntityOrder(testcaseResultEntity,
                errors);
        // For :Description
        validateTestcaseResultEntityDescription(testcaseResultEntity,
                errors);
        // For :Message
        validateTestcaseResultEntityMessage(testcaseResultEntity,
                errors);
        // For :FailureMessage
        validateTestcaseResultEntityFailureMessage(testcaseResultEntity,
                errors);

        return errors;
    }

    private static void validateCommonForeignKey(TestcaseResultEntity testcaseResultEntity,
                                                 UserService userService,
                                                 TestcaseResultService testcaseResultService,
                                                 TestRequestService testRequestService,
                                                 List<ValidationResultInfo> errors,
                                                 ContextInfo contextInfo)
            throws
            InvalidParameterException {
        //validate TestcaseResult foreignKey.
        if (testcaseResultEntity.getTester() != null) {
            try {
                testcaseResultEntity.setTester(
                        userService.getUserById(testcaseResultEntity.getTester().getId(), contextInfo)
                );
            } catch (DoesNotExistException | InvalidParameterException ex) {
                LOGGER.error(ValidateConstant.DOES_NOT_EXIST_EXCEPTION + TestcaseResultValidator.class.getSimpleName(), ex);
                String fieldName = "tester";
                errors.add(
                        new ValidationResultInfo(fieldName,
                                ErrorLevel.ERROR,
                                ValidateConstant.ID_SUPPLIED + fieldName + ValidateConstant.DOES_NOT_EXIST));
            }
        }
        //validate TestcaseResult parentTestcaseResult.
        if (testcaseResultEntity.getParentTestcaseResult() != null) {
            try {
                testcaseResultEntity.setParentTestcaseResult(
                        testcaseResultService.getTestcaseResultById(testcaseResultEntity.getParentTestcaseResult().getId(), contextInfo)
                );
            } catch (DoesNotExistException | InvalidParameterException ex) {
                LOGGER.error(ValidateConstant.DOES_NOT_EXIST_EXCEPTION + TestcaseResultValidator.class.getSimpleName(), ex);
                String fieldName = "parentTestcaseResult";
                errors.add(
                        new ValidationResultInfo(fieldName,
                                ErrorLevel.ERROR,
                                ValidateConstant.ID_SUPPLIED + fieldName + ValidateConstant.DOES_NOT_EXIST
                        ));
            }
        }

        //validate testRequest foreignKey.
        if (testcaseResultEntity.getTestRequest() != null) {
            try {
                testcaseResultEntity.setTestRequest(
                        testRequestService.getTestRequestById(testcaseResultEntity.getTestRequest().getId(), contextInfo)
                );
            } catch (DoesNotExistException | InvalidParameterException ex) {
                LOGGER.error(ValidateConstant.DOES_NOT_EXIST_EXCEPTION + TestcaseResultValidator.class.getSimpleName(), ex);
                String fieldName = "testRequest";
                errors.add(
                        new ValidationResultInfo(fieldName,
                                ErrorLevel.ERROR,
                                ValidateConstant.ID_SUPPLIED + fieldName + ValidateConstant.DOES_NOT_EXIST));
            }
        }
    }

    //validate update
    private static void validateUpdateTestcaseResult(List<ValidationResultInfo> errors,
                                                     TestcaseResultEntity testcaseResultEntity,
                                                     TestcaseResultEntity originalEntity) {
        // required validation
        ValidationUtils.validateRequired(testcaseResultEntity.getId(), "id", errors);
        //check the meta required
        if (testcaseResultEntity.getVersion() == null) {
            String fieldName = "meta.version";
            errors.add(new ValidationResultInfo(fieldName,
                    ErrorLevel.ERROR,
                    fieldName + ValidateConstant.MUST_PROVIDED));
        } // check meta version id
        else if (!testcaseResultEntity.getVersion()
                .equals(originalEntity.getVersion())) {
            String fieldName = "meta.version";
            errors.add(new ValidationResultInfo(fieldName,
                    ErrorLevel.ERROR,
                    ValidateConstant.SOMEONE_UPDATED + " TestcaseResult " + ValidateConstant.REFRESH_COPY));
        }
        // check not updatable fields
        validateNotUpdatable(errors, testcaseResultEntity, originalEntity);
    }

    //validate not update
    private static void validateNotUpdatable(List<ValidationResultInfo> errors,
                                             TestcaseResultEntity testcaseResultEntity,
                                             TestcaseResultEntity originalEntity) {
        // state can't be updated
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getState(), originalEntity.getState(), "state", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getParentTestcaseResult(), originalEntity.getParentTestcaseResult(), "parent_test_case_result_id", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getRefObjUri(), originalEntity.getRefObjUri(), "ref_obj_uri", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getRefId(), originalEntity.getRefId(), "ref_id", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getTestRequest(), originalEntity.getTestRequest(), "test_request_id", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getManual(), originalEntity.getManual(), "is_manual", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getRequired(), originalEntity.getRequired(), "is_required", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getAutomated(), originalEntity.getAutomated(), "is_automated", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getRecommended(), originalEntity.getRecommended(), "is_recommended", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getWorkflow(), originalEntity.getWorkflow(), "is_workflow", errors);
        ValidationUtils.validateNotUpdatable(testcaseResultEntity.getFunctional(), originalEntity.getFunctional(), "is_functional", errors);
    }

    //validate create
    private static void validateCreateTestcaseResult(
            List<ValidationResultInfo> errors,
            TestcaseResultEntity testcaseResultEntity,
            TestcaseResultService testcaseResultService,
            ContextInfo contextInfo) {
        if (testcaseResultEntity.getId() != null) {
            try {
                testcaseResultService.getTestcaseResultById(testcaseResultEntity.getId(),
                        contextInfo);
                // if info found with same id than
                String fieldName = "id";
                errors.add(
                        new ValidationResultInfo(fieldName,
                                ErrorLevel.ERROR,
                                ValidateConstant.ID_SUPPLIED + "create" + ValidateConstant.ALREADY_EXIST));
            } catch (DoesNotExistException | InvalidParameterException ex) {
//                LOGGER.error(ValidateConstant.DOES_NOT_EXIST_EXCEPTION + TestcaseResultValidator.class.getSimpleName(), ex);
                // This is ok because created id should be unique
            }
        }
    }

    //Validate Required
    private static void validateCommonRequired(TestcaseResultEntity testcaseResultEntity,
                                               List<ValidationResultInfo> errors) {
        ValidationUtils
                .validateRequired(testcaseResultEntity.getName(), "name", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getRank(), "rank", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getState(), "state", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getAutomated(), "automated", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getManual(), "manual", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getFunctional(), "functional", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getRefObjUri(), "refObjUri", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getRefId(), "refId", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getTestRequest(), "testRequest", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getRecommended(), "recommended", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getRequired(), "required", errors);
        ValidationUtils
                .validateRequired(testcaseResultEntity.getWorkflow(), "workflow", errors);
        /* if (TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_FINISHED.equals(testcaseResultEntity.getState())
                && TestcaseServiceConstants.TESTCASE_REF_OBJ_URI.equals(testcaseResultEntity.getRefObjUri())
                && Objects.equals(Boolean.TRUE, testcaseResultEntity.getManual())
                && Objects.equals(Boolean.FALSE, testcaseResultEntity.getHasSystemError())) {
            ValidationUtils.validateRequired(testcaseResultEntity.getTestcaseOption(), "testcaseOption", errors);
        }*/
        if (!(StringUtils.hasLength(testcaseResultEntity.getRefObjUri()) && (testcaseResultEntity.getRefObjUri().equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)))) {
            ValidationUtils
                    .validateRequired(testcaseResultEntity.getParentTestcaseResult().getId(), "parent testcase result id", errors);
        }
    }

    //Validate Common Unique
    private static void validateCommonUnique(TestcaseResultEntity testcaseResultEntity,
                                             String validationTypeKey,
                                             TestcaseResultService testcaseResultService,
                                             List<ValidationResultInfo> errors,
                                             ContextInfo contextInfo)
            throws OperationFailedException, InvalidParameterException {
        // check unique field
        if ((validationTypeKey.equals(Constant.CREATE_VALIDATION) || testcaseResultEntity.getId() != null)) {
            TestcaseResultCriteriaSearchFilter searchFilter = new TestcaseResultCriteriaSearchFilter();
            if (testcaseResultEntity.getTestRequest() != null) {
                searchFilter.setTestRequestId(testcaseResultEntity.getTestRequest().getId());
            }
            searchFilter.setRefId(testcaseResultEntity.getRefId());
            searchFilter.setRefObjUri(testcaseResultEntity.getRefObjUri());
            searchFilter.setManual(Objects.equals(Boolean.TRUE, testcaseResultEntity.getManual()));
            Page<TestcaseResultEntity> testcaseResultEntities = testcaseResultService
                    .searchTestcaseResults(
                            searchFilter,
                            Constant.TWO_VALUE_PAGE,
                            contextInfo);

            // if info found with same testRequestId, refObjUri, refId than and not current id
            boolean flag
                    = testcaseResultEntities.stream().anyMatch(c -> (validationTypeKey.equals(Constant.CREATE_VALIDATION)
                    || !c.getId().equals(testcaseResultEntity.getId()))
            );
            if (flag) {
                String fieldName = "testRequestId, refObjUri and refId";
                errors.add(
                        new ValidationResultInfo(fieldName,
                                ErrorLevel.ERROR,
                                "Given TestcaseResult with same testRequestId, refObjUri and refId already exists."));
            }
        }
    }

    //Validation For :Id
    private static void validateTestcaseResultEntityId(TestcaseResultEntity testcaseResultEntity,
                                                       List<ValidationResultInfo> errors) {
        ValidationUtils.validateLength(testcaseResultEntity.getId(),
                "id",
                0,
                255,
                errors);
    }

    //Validation For :Name
    private static void validateTestcaseResultEntityName(TestcaseResultEntity testcaseResultEntity,
                                                         List<ValidationResultInfo> errors) {
        ValidationUtils.validateLength(testcaseResultEntity.getName(),
                "name",
                3,
                1000,
                errors);
    }

    //Validation For :Order
    private static void validateTestcaseResultEntityOrder(TestcaseResultEntity testcaseResultEntity,
                                                          List<ValidationResultInfo> errors) {
        ValidationUtils.validateIntegerRange(testcaseResultEntity.getRank(),
                "rank",
                1,
                null,
                errors);
    }

    //Validation For :Description
    private static void validateTestcaseResultEntityDescription(TestcaseResultEntity testcaseResultEntity,
                                                                List<ValidationResultInfo> errors) {
        ValidationUtils.validateLength(testcaseResultEntity.getDescription(),
                "description",
                0,
                1000,
                errors);
    }

    //Validation For :Message
    private static void validateTestcaseResultEntityMessage(TestcaseResultEntity testcaseResultEntity,
                                                            List<ValidationResultInfo> errors) {
        ValidationUtils.validateLength(testcaseResultEntity.getMessage(),
                "message",
                0,
                2000,
                errors);
    }

    //Validation For :FailureMessage
    private static void validateTestcaseResultEntityFailureMessage(TestcaseResultEntity testcaseResultEntity,
                                                            List<ValidationResultInfo> errors) {
        ValidationUtils.validateLength(testcaseResultEntity.getFailureMessage(),
                "failureMessage",
                0,
                2000,
                errors);
    }

    //trim all TestcaseResult field
    private static void trimTestcaseResult(TestcaseResultEntity testcaseResultEntity) {
        if (testcaseResultEntity.getId() != null) {
            testcaseResultEntity.setId(testcaseResultEntity.getId().trim());
        }
        if (testcaseResultEntity.getName() != null) {
            testcaseResultEntity.setName(testcaseResultEntity.getName().trim());
        }
        if (testcaseResultEntity.getDescription() != null) {
            testcaseResultEntity.setDescription(testcaseResultEntity.getDescription().trim());
        }
        if (testcaseResultEntity.getMessage() != null) {
            testcaseResultEntity.setMessage(testcaseResultEntity.getMessage().trim());
        }
        if (testcaseResultEntity.getFailureMessage() != null) {
            testcaseResultEntity.setFailureMessage(testcaseResultEntity.getFailureMessage().trim());
        }
        if (testcaseResultEntity.getRefObjUri() != null) {
            testcaseResultEntity.setRefObjUri(testcaseResultEntity.getRefObjUri().trim());
        }
        if (testcaseResultEntity.getRefId() != null) {
            testcaseResultEntity.setRefId(testcaseResultEntity.getRefId().trim());
        }
    }

    private static List<ValidationResultInfo> validateTestcaseResultSubmit(
            String testcaseResultId,
            Set<String> selectedTestcaseOptionIds,
            TestcaseResultService testcaseResultService,
            TestResultRelationService testResultRelationService,
            ContextInfo contextInfo) {
        List<ValidationResultInfo> errors = new ArrayList<>();

        if(selectedTestcaseOptionIds == null || selectedTestcaseOptionIds.isEmpty() ){
            String fieldName = "selectedTestcaseOptionIds";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "The selectedTestcaseOptionIds is required."));
            return errors;
        }
        try {
            testcaseResultService
                    .getTestcaseResultById(testcaseResultId,
                            contextInfo);
        } catch (DoesNotExistException | InvalidParameterException ex) {
            String fieldName = "testcaseResultId";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "The testcaseResultId supplied for the submit does not exists"));
            return errors;
        }
        try {

            TestcaseEntity testcase = validateAndGetManualQuestionInAudit(testcaseResultId, testResultRelationService, contextInfo, errors);

            // validate selectedTestcaseOptionIds size based on question type
            if (TestcaseServiceConstants.QuestionType.SINGLE_SELECT.name().equals(testcase.getQuestionType()) && selectedTestcaseOptionIds.size() > 1) {
                String fieldName = "selectedTestcaseOptionIds";
                errors.add(
                        new ValidationResultInfo(fieldName,
                                ErrorLevel.ERROR,
                                "selectedTestcaseOptionIds cannot be more than one, as the question is type of SINGLE_SELECT only."));
            }

            validateTestResultRelationsForSelectedTestcaseOptionIds(testcaseResultId, selectedTestcaseOptionIds, testResultRelationService, contextInfo, errors);

        } catch (InvalidParameterException | DoesNotExistException | OperationFailedException |
                 DataValidationErrorException ex) {
            LOGGER.error(ValidateConstant.EXCEPTION + TestcaseResultValidator.class.getSimpleName(), ex);
            String fieldName = "selectedTestcaseOptionId";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "The selectedTestcaseOptionId supplied for the submit does not exists"));
        }
        return errors;
    }

    private static TestcaseEntity validateAndGetManualQuestionInAudit(String testcaseResultId, TestResultRelationService testResultRelationService, ContextInfo contextInfo, List<ValidationResultInfo> errors) throws InvalidParameterException, DataValidationErrorException, OperationFailedException {
        TestcaseEntity testcase = new TestcaseEntity();

        try {
            List<Object> testResultRelationEntitiesFromAuditMapping = testResultRelationService.getTestResultRelationEntitiesFromAuditMapping(testcaseResultId, TestcaseServiceConstants.TESTCASE_REF_OBJ_URI, contextInfo);

            Optional<TestcaseEntity> testcaseEntity = testResultRelationEntitiesFromAuditMapping.stream().findFirst().map(TestcaseEntity.class::cast);

            if (testcaseEntity.isPresent()) {
                testcase = testcaseEntity.get();
            } else {
                LOGGER.error("TestcaseEntity not found for testcaseResultId: {}", testcaseResultId);
                String fieldName = "testcaseResultId";
                errors.add(
                        new ValidationResultInfo(fieldName,
                                ErrorLevel.ERROR,
                                "Failed to get testResultRelationEntities for Testcase"));
            }
        } catch (DoesNotExistException e) {
            LOGGER.error("caught exception in TestcaseResultValidator ", e);
            String fieldName = "testcaseResultId";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "Failed to get testResultRelationEntities for Testcase"));
        }
        return testcase;
    }

    private static void validateTestResultRelationsForSelectedTestcaseOptionIds(String testcaseResultId, Set<String> selectedTestcaseOptionIds, TestResultRelationService testResultRelationService, ContextInfo contextInfo, List<ValidationResultInfo> errors) throws InvalidParameterException, DoesNotExistException, OperationFailedException, DataValidationErrorException {
        // validate for TestResultRelation
        TestResultRelationCriteriaSearchFilter testResultRelationCriteriaSearchFilter = new TestResultRelationCriteriaSearchFilter();
        testResultRelationCriteriaSearchFilter.setTestcaseResultId(testcaseResultId);
        testResultRelationCriteriaSearchFilter.setRefObjUri(TestcaseOptionServiceConstants.TESTCASE_OPTION_REF_OBJ_URI);
        testResultRelationCriteriaSearchFilter.setRefId(selectedTestcaseOptionIds);

        List<TestResultRelationEntity> testResultRelationEntities = testResultRelationService.searchTestResultRelation(testResultRelationCriteriaSearchFilter, contextInfo);

        if (selectedTestcaseOptionIds != null && selectedTestcaseOptionIds.size() != testResultRelationEntities.size()) {
            throw new DoesNotExistException("Failed to find all the test result related records from the database ");
        }

        List<String> resultRelationIds = testResultRelationEntities.stream().map(IdMetaEntity::getId).toList();

        try {
            testResultRelationService.getTestResultRelationEntitiesFromAuditMapping(resultRelationIds, contextInfo);
        } catch (DoesNotExistException | InvalidParameterException e) {
            String fieldName = "selectedTestcaseOptionId";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "Failed to find the selectedTestcaseOptionId supplied for the submit"));
        }
    }

    @Autowired
    public void setRefObjectUriAndRefIdValidator(RefObjectUriAndRefIdValidator refObjectUriAndRefIdValidatorIdValidator) {
        TestcaseResultValidator.refObjectUriAndRefIdValidator = refObjectUriAndRefIdValidatorIdValidator;
    }

}

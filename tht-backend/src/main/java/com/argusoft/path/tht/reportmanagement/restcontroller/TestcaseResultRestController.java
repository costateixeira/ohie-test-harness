package com.argusoft.path.tht.reportmanagement.restcontroller;

import com.argusoft.path.tht.reportmanagement.constant.TestcaseResultServiceConstants;
import com.argusoft.path.tht.reportmanagement.filter.TestcaseResultCriteriaSearchFilter;
import com.argusoft.path.tht.reportmanagement.models.dto.TestcaseResultAnswerInfo;
import com.argusoft.path.tht.reportmanagement.models.dto.TestcaseResultInfo;
import com.argusoft.path.tht.reportmanagement.models.entity.TestcaseResultEntity;
import com.argusoft.path.tht.reportmanagement.models.mapper.TestcaseResultMapper;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultService;
import com.argusoft.path.tht.systemconfiguration.constant.ValidateConstant;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.testcasemanagement.testbed.dto.start.request.StartRequest;
import com.argusoft.path.tht.testcasemanagement.testbed.dto.status.request.StatusRequest;
import com.argusoft.path.tht.testcasemanagement.testbed.dto.status.response.StatusResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.common.collect.Multimap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.List;

/**
 * This TestcaseResultServiceRestController maps end points with standard
 * service.
 *
 * @author Dhruv
 */
@RestController
@RequestMapping("/testcase-result")
@Api(value = "REST API for TestcaseResult services", tags = {"TestcaseResult API"})
public class TestcaseResultRestController {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestcaseResultRestController.class);

    private TestcaseResultService testcaseResultService;
    private TestcaseResultMapper testcaseResultMapper;

    @Autowired
    public void setTestcaseResultService(TestcaseResultService testcaseResultService) {
        this.testcaseResultService = testcaseResultService;
    }

    @Autowired
    public void setTestcaseResultMapper(TestcaseResultMapper testcaseResultMapper) {
        this.testcaseResultMapper = testcaseResultMapper;
    }

    /**
     * We can expose this API in future if needed. {@inheritdoc}
     *
     * @return
     */
//    @ApiOperation(value = "Create new TestcaseResult", response = TestcaseResultInfo.class)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successfully created TestcaseResult"),
//            @ApiResponse(code = 401, message = "You are not authorized to create the resource"),
//            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
//    })
//    @PostMapping("")
    @Transactional(rollbackFor = Exception.class)
    public TestcaseResultInfo createTestcaseResult(
            @RequestBody TestcaseResultInfo testcaseResultInfo,
            @RequestAttribute(name = "contextInfo") ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException,
            DataValidationErrorException, DoesNotExistException, VersionMismatchException {

        TestcaseResultEntity testcaseResultEntity = testcaseResultMapper.dtoToModel(testcaseResultInfo);
        testcaseResultEntity = testcaseResultService.createTestcaseResult(testcaseResultEntity, contextInfo);
        return testcaseResultMapper.modelToDto(testcaseResultEntity);

    }

    /**
     * We can expose this API in future if needed. {@inheritdoc}
     *
     * @return
     */
//    @ApiOperation(value = "Update existing TestcaseResult", response = TestcaseResultInfo.class)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successfully updated TestcaseResult"),
//            @ApiResponse(code = 401, message = "You are not authorized to create the resource"),
//            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
//
//    })
//    @PutMapping("")
    @Transactional(rollbackFor = Exception.class)
    public TestcaseResultInfo updateTestcaseResult(
            @RequestBody TestcaseResultInfo testcaseResultInfo,
            @RequestAttribute(name = "contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException,
            OperationFailedException,
            InvalidParameterException,
            VersionMismatchException,
            DataValidationErrorException {

        TestcaseResultEntity testcaseResultEntity = testcaseResultMapper.dtoToModel(testcaseResultInfo);
        testcaseResultEntity = testcaseResultService.updateTestcaseResult(testcaseResultEntity, contextInfo);
        return testcaseResultMapper.modelToDto(testcaseResultEntity);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @ApiOperation(value = "View a page of available filtered TestcaseResults", response = Page.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved page"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("")
    public Page<TestcaseResultInfo> searchTestcaseResults(
            TestcaseResultCriteriaSearchFilter testcaseResultCriteriaSearchFilter,
            Pageable pageable,
            @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws OperationFailedException,
            InvalidParameterException {
        Page<TestcaseResultEntity> testcaseResultEntities = testcaseResultService.searchTestcaseResults(testcaseResultCriteriaSearchFilter, pageable, contextInfo);
        return testcaseResultMapper.pageEntityToDto(testcaseResultEntities);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @ApiOperation(value = "Submit manual TestcaseResults", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Submitted TestcaseResult"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })

    @PatchMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize(value = "hasAnyAuthority('role.admin','role.tester')")
    public List<TestcaseResultInfo> submitTestcaseResult(
            @RequestBody List<TestcaseResultAnswerInfo> testcaseResultAnswerInfos,
            @RequestAttribute("contextInfo") ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, DataValidationErrorException, OperationFailedException, VersionMismatchException {
        List<TestcaseResultEntity> testcaseResultEntities = testcaseResultService
                .submitTestcaseResult(
                        testcaseResultAnswerInfos,
                        contextInfo);
        return testcaseResultMapper.modelToDto(testcaseResultEntities);
    }

    @ApiOperation(value = "Patch Update For TestcaseResultEntity", response = TestcaseResultInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Updated TestcaseResult"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @PatchMapping(value = "/{testcaseResultId}", consumes = "application/json-patch+json")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize(value = "hasAnyAuthority('role.admin','role.tester')")
    public TestcaseResultInfo submitTestcaseResult(
            @PathVariable("testcaseResultId") String testcaseResultId,
            @RequestBody JsonPatch jsonPatch,
            @RequestAttribute("contextInfo") ContextInfo contextInfo) throws InvalidParameterException, DoesNotExistException, DataValidationErrorException, OperationFailedException, VersionMismatchException {

        TestcaseResultInfo testcaseResultById = this.getTestcaseResultById(testcaseResultId, contextInfo);

        TestcaseResultInfo testcaseResultPatched = applyPatchToTestcaseInfo(jsonPatch, testcaseResultById);

        // update and return
        TestcaseResultEntity testcaseResultEntity = testcaseResultMapper.dtoToModel(testcaseResultPatched);
        testcaseResultEntity = testcaseResultService.updateTestcaseResult(testcaseResultEntity, contextInfo);
        return testcaseResultMapper.modelToDto(testcaseResultEntity);
    }

    private TestcaseResultInfo applyPatchToTestcaseInfo(JsonPatch jsonPatch, TestcaseResultInfo testcaseResultInfo) throws OperationFailedException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode patched = jsonPatch.apply(objectMapper.convertValue(testcaseResultInfo, JsonNode.class));
            return objectMapper.treeToValue(patched, TestcaseResultInfo.class);
        } catch (JsonPatchException | JsonProcessingException e) {
            LOGGER.error(ValidateConstant.OPERATION_FAILED_EXCEPTION + TestcaseResultRestController.class.getSimpleName(), e);
            throw new OperationFailedException("Caught Exception while processing Json ", e);
        }
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @ApiOperation(value = "View available TestcaseResult with supplied id", response = TestcaseResultInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved TestcaseResult"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/{testcaseResultId}")
    public TestcaseResultInfo getTestcaseResultById(
            @PathVariable("testcaseResultId") String testcaseResultId,
            @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException {

        TestcaseResultEntity testcaseResultById = testcaseResultService.getTestcaseResultById(testcaseResultId, contextInfo);
        return testcaseResultMapper.modelToDto(testcaseResultById);
    }

    /**
     * We can expose this API in future if needed. {@inheritdoc}
     */
//    @ApiOperation(value = "View a list of validation errors for TestcaseResult", response = List.class)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successfully retrieved Validation errors"),
//            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
//            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
//    })
//    @PostMapping("/validate")
    public List<ValidationResultInfo> validateTestcaseResult(
            @RequestParam(name = "validationTypeKey",
                    required = true) String validationTypeKey,
            @RequestBody(required = true) TestcaseResultInfo testcaseResultInfo,
            @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws InvalidParameterException,
            OperationFailedException, DataValidationErrorException {
        TestcaseResultEntity testcaseResultEntity = testcaseResultMapper.dtoToModel(testcaseResultInfo);
        return testcaseResultService
                .validateTestcaseResult(validationTypeKey, testcaseResultEntity, contextInfo);
    }

    //    @ApiOperation(value = "To change status of TestcaseResult", response = DocumentInfo.class)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successfully updated TestcaseResult"),
//            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
//            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
//    })
//    @PatchMapping("/state/{testcaseResultId}/{changeState}")
//    @Transactional
    public TestcaseResultInfo updateTestcaseResultState(@PathVariable("testcaseResultId") String testcaseResultId,
                                                        @PathVariable("changeState") String changeState,
                                                        @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, OperationFailedException, VersionMismatchException {
        TestcaseResultEntity testcaseResultEntity = testcaseResultService.changeState(testcaseResultId, changeState, contextInfo);
        return testcaseResultMapper.modelToDto(testcaseResultEntity);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @ApiOperation(value = "Retrieves multiple TestcaseResult status corresponding to the given filters", response = TestcaseResultInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved TestcaseResults"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/status")
    public List<TestcaseResultInfo> getMultipleTestcaseResultStatus(
            @RequestParam(value = "testRequestId", required = false) String testRequestId,
            @RequestParam(value = "manual", required = false) Boolean isManual,
            @RequestParam(value = "automated", required = false) Boolean isAutomated,
            @RequestParam(value = "required", required = false) Boolean isRequired,
            @RequestParam(value = "recommended", required = false) Boolean isRecommended,
            @RequestParam(value = "workflow", required = false) Boolean isWorkflow,
            @RequestParam(value = "functional", required = false) Boolean isFunctional,
            @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException, OperationFailedException, AccessDeniedException {

        List<TestcaseResultEntity> testcaseResults = testcaseResultService.getMultipleTestcaseResultStatus(
                testRequestId,
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                contextInfo);

        return testcaseResultMapper.modelToDto(testcaseResults);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @ApiOperation(value = "Retrieves a TestcaseResult corresponding to the given filters", response = TestcaseResultInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved TestcaseResult"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/status/{testcaseResultId}")
    public TestcaseResultInfo getTestcaseResultStatus(
            @PathVariable("testcaseResultId") String testcaseResultId,
            @RequestParam(value = "manual", required = false) Boolean isManual,
            @RequestParam(value = "automated", required = false) Boolean isAutomated,
            @RequestParam(value = "required", required = false) Boolean isRequired,
            @RequestParam(value = "recommended", required = false) Boolean isRecommended,
            @RequestParam(value = "workflow", required = false) Boolean isWorkflow,
            @RequestParam(value = "functional", required = false) Boolean isFunctional,
            @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException,
            InvalidParameterException, OperationFailedException {

        TestcaseResultEntity testcaseResultById = testcaseResultService.getTestcaseResultStatus(
                testcaseResultId,
                isManual,
                isAutomated,
                isRequired,
                isRecommended,
                isWorkflow,
                isFunctional,
                contextInfo);
        return testcaseResultMapper.modelToDto(testcaseResultById);
    }

    /**
     * {@inheritdoc}
     *
     * @return
     */
    @ApiOperation(value = "Retrieves classes extending Test case class.", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved classes name"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/sub-classes")
    public List<String> getSubClassesNameForTestCase() {
        return testcaseResultService.getSubClassesNameForTestCase();
    }

    @ApiOperation(value = "Retrieves all status of test case result.", response = Multimap.class)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/status/mapping")
    public List<String> getStatusMapping(@RequestParam("sourceStatus") String sourceStatus) {
        Collection<String> strings = TestcaseResultServiceConstants.TESTCASE_RESULT_STATUS_MAP.get(sourceStatus);
        return strings.parallelStream().toList();
    }

    @GetMapping("/start-test-case/{testSuiteId}")
    public StatusResponse startTestcaseAndStatusResponse(@PathVariable(value = "testSuiteId") String testSuiteId, @RequestAttribute("contextInfo") ContextInfo contextInfo) throws Exception {
        return testcaseResultService.startTestcaseAndStatusResponse(testSuiteId, contextInfo);
    }

}

package com.argusoft.path.tht.systemconfiguration.utils;

import com.argusoft.path.tht.fileservice.constant.DocumentServiceConstants;
import com.argusoft.path.tht.fileservice.service.DocumentService;
import com.argusoft.path.tht.reportmanagement.constant.TestcaseResultServiceConstants;
import com.argusoft.path.tht.reportmanagement.service.TestcaseResultService;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.DoesNotExistException;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.InvalidParameterException;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.OperationFailedException;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.testcasemanagement.constant.ComponentServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.SpecificationServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseOptionServiceConstants;
import com.argusoft.path.tht.testcasemanagement.constant.TestcaseServiceConstants;
import com.argusoft.path.tht.testcasemanagement.service.ComponentService;
import com.argusoft.path.tht.testcasemanagement.service.SpecificationService;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseOptionService;
import com.argusoft.path.tht.testcasemanagement.service.TestcaseService;
import com.argusoft.path.tht.testprocessmanagement.constant.TestRequestServiceConstants;
import com.argusoft.path.tht.testprocessmanagement.service.TestRequestService;
import com.argusoft.path.tht.usermanagement.constant.UserServiceConstants;
import com.argusoft.path.tht.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * validate ref obj uri and refId
 *
 * @author Rohit
 */

@Component
public class RefObjectUriAndRefIdValidator {

    List<String> refObjUriList = List.of(
            ComponentServiceConstants.COMPONENT_REF_OBJ_URI,
            DocumentServiceConstants.DOCUMENT_REF_OBJ_URI,
            SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI,
            TestcaseServiceConstants.TESTCASE_REF_OBJ_URI,
            TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI,
            TestcaseOptionServiceConstants.TESTCASE_OPTION_REF_OBJ_URI,
            TestcaseResultServiceConstants.TESTCASE_RESULT_REF_OBJ_URI,
            UserServiceConstants.USER_REF_OBJ_URI
    );

    private ComponentService componentService;
    private SpecificationService specificationService;
    private TestcaseService testcaseService;
    private TestcaseOptionService testcaseOptionService;
    private TestcaseResultService testcaseResultService;
    private DocumentService documentService;
    private TestRequestService testRequestService;
    private UserService userService;

    @Autowired
    public void setComponentService(ComponentService componentService) {
        this.componentService = componentService;
    }

    @Autowired
    public void setSpecificationService(SpecificationService specificationService) {
        this.specificationService = specificationService;
    }

    @Autowired
    public void setTestcaseService(TestcaseService testcaseService) {
        this.testcaseService = testcaseService;
    }

    @Autowired
    public void setTestcaseOptionService(TestcaseOptionService testcaseOptionService) {
        this.testcaseOptionService = testcaseOptionService;
    }

    @Autowired
    public void setTestcaseResultService(TestcaseResultService testcaseResultService) {
        this.testcaseResultService = testcaseResultService;
    }

    @Autowired
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Autowired
    public void setTestRequestService(TestRequestService testRequestService) {
        this.testRequestService = testRequestService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void refObjectUriAndRefIdValidation(String refObjUri, String refId,
                                               ContextInfo contextInfo, List<ValidationResultInfo> errors) throws InvalidParameterException, OperationFailedException {

        if (!StringUtils.hasLength(refObjUri)) {
            String fieldName = "refObjUri";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "input parameter refObjUri is empty"));
            return;
        }

        if (!refObjUriList.contains(refObjUri)) {
            String fieldName = "refObjUri";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "The refObjUri provided does not exists"));
            return;
        }

        try {
            if (refObjUri.equals(ComponentServiceConstants.COMPONENT_REF_OBJ_URI)) {
                componentService.getComponentById(refId, contextInfo);
            } else if (refObjUri.equals(SpecificationServiceConstants.SPECIFICATION_REF_OBJ_URI)) {
                specificationService.getSpecificationById(refId, contextInfo);
            } else if (refObjUri.equals(TestcaseServiceConstants.TESTCASE_REF_OBJ_URI)) {
                testcaseService.getTestcaseById(refId, contextInfo);
            } else if (refObjUri.equals(TestcaseOptionServiceConstants.TESTCASE_OPTION_REF_OBJ_URI)) {
                testcaseOptionService.getTestcaseOptionById(refId, contextInfo);
            } else if (refObjUri.equals(TestcaseResultServiceConstants.TESTCASE_RESULT_REF_OBJ_URI)) {
                testcaseResultService.getTestcaseResultById(refId, contextInfo);
            } else if (refObjUri.equals(DocumentServiceConstants.DOCUMENT_REF_OBJ_URI)) {
                documentService.getDocument(refId, contextInfo);
            } else if (refObjUri.equals(TestRequestServiceConstants.TEST_REQUEST_REF_OBJ_URI)) {
                testRequestService.getTestRequestById(refId, contextInfo);
            } else if (refObjUri.equals(UserServiceConstants.USER_REF_OBJ_URI)) {
                userService.getUserById(refId, contextInfo);
            }
        } catch (DoesNotExistException ex) {
            int startIndex = refObjUri.lastIndexOf(".") + 1;
            int endIndex = refObjUri.indexOf("Info");
            String entity = refObjUri.substring(startIndex, endIndex);
            String fieldName = "refId";
            errors.add(
                    new ValidationResultInfo(fieldName,
                            ErrorLevel.ERROR,
                            "The refId supplied for " + entity + " does not exists"));
        }
    }
}

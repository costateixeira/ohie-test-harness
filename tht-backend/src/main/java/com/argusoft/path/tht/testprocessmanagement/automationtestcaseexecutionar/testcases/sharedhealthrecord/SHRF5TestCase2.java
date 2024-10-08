//package com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.testcases.sharedhealthrecord;
//
//import ca.uhn.fhir.rest.api.MethodOutcome;
//import ca.uhn.fhir.rest.client.api.IGenericClient;
//import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
//import com.argusoft.path.tht.systemconfiguration.constant.ValidateConstant;
//import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.OperationFailedException;
//import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
//import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
//import com.argusoft.path.tht.systemconfiguration.utils.FHIRUtils;
//import com.argusoft.path.tht.testcasemanagement.constant.ComponentServiceConstants;
//import com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.TestCase;
//import org.hl7.fhir.r4.model.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * Testcase For SHRF5TestCase2
// *
// * @author Bhavi
// */
//
//@Component
//public class SHRF5TestCase2 implements TestCase {
//
//    public static final Logger LOGGER = LoggerFactory.getLogger(SHRF5TestCase1.class);
//
//    @Override
//    public ValidationResultInfo test(Map<String, IGenericClient> iGenericClientMap,
//                                     ContextInfo contextInfo) throws OperationFailedException {
//        try {
//            String testCaseName = this.getClass().getSimpleName();
//            LOGGER.info("Start testing {}", testCaseName);
//
//            IGenericClient client = iGenericClientMap.get(ComponentServiceConstants.COMPONENT_SHARED_HEALTH_RECORD_REGISTRY_ID);
//
//            if (client == null) {
//                LOGGER.error("{} Failed to get IGenericClient", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to get IGenericClient");
//            }
//
//            Patient patient = FHIRUtils.createPatient("Doe", "John", "male", "1990-01-01", "urn:oid:1.3.6.1.4.1.21367.13.20.1000", "IHERED-994", true, "9414473", "555-555-5555", "john.doe@example.com", client);
//            MethodOutcome patientOutcome = client.create().resource(patient).execute();
//
//            if (Boolean.FALSE.equals(patientOutcome.getCreated())) {
//                LOGGER.error("{} Testcase Failed when creating Patient", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create Patient");
//            }
//
//            String patientReference = "Patient/" + patientOutcome.getId().getIdPart();
//
//            Practitioner practitionerOne = FHIRUtils.createPractitioner("Walter", "male", "12-05-2001", "9414", "555-555-5555");
//            MethodOutcome practitionerOneOutcome = client.create().resource(practitionerOne).execute();
//            if (Boolean.FALSE.equals(practitionerOneOutcome.getCreated())) {
//                LOGGER.error("{} Testcase Failed when creating Practitioner", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create Practitioner");
//            }
//            String practitionerReference = "Practitioner/" + practitionerOneOutcome.getId().getIdPart();
//
//            String patientId = patientOutcome.getId().getIdPart();
//
//            Organization organizationOne = FHIRUtils.createOrganization("Good Health Clinic", "India", "Gandhinagar", "111-111-111");
//            MethodOutcome organizationOneOutcome = client.create().resource(organizationOne).execute();
//            if (Boolean.FALSE.equals(organizationOneOutcome.getCreated())) {
//                LOGGER.error("{} Testcase Failed when creating organization", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create organization");
//            }
//
//            Composition admissionNoteOne = FHIRUtils.createAdmissionNote(patientId, organizationOneOutcome.getId().getIdPart(), practitionerOneOutcome.getId().getIdPart(), "Good Health Clinic");
//            MethodOutcome admissionOneNoteOutcome = client.create().resource(admissionNoteOne).execute();
//            if (Boolean.FALSE.equals(admissionOneNoteOutcome.getCreated())) {
//                LOGGER.error("{} Testcase Failed when creating Composition", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create Admission Composition");
//            }
//
//            Composition operativeNoteOne = FHIRUtils.createOperativeNote(patientId, organizationOneOutcome.getId().getIdPart(), practitionerOneOutcome.getId().getIdPart(), "Good Health Clinic");
//            MethodOutcome operativeNoteOneOutcome = client.create().resource(operativeNoteOne).execute();
//            if (Boolean.FALSE.equals(operativeNoteOneOutcome.getCreated())) {
//                LOGGER.error("{} Testcase Failed when creating Composition", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create operative Composition");
//            }
//
//            Composition progressNoteOne = FHIRUtils.createProgressNotes(patientId, organizationOneOutcome.getId().getIdPart(), practitionerOneOutcome.getId().getIdPart(), "Good Health Clinic");
//            MethodOutcome progressNoteOneOutcome = client.create().resource(progressNoteOne).execute();
//
//            if (Boolean.FALSE.equals(progressNoteOneOutcome.getCreated())) {
//                LOGGER.error("{} Testcase Failed when creating Composition", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create progress Composition");
//            }
//
//            Composition dischargeSummaryOne = FHIRUtils.createDischargeSummary(patientId, organizationOneOutcome.getId().getIdPart(), practitionerOneOutcome.getId().getIdPart(), "Good Health Clinic");
//            MethodOutcome dischargeSummaryOneOutcome = client.create().resource(dischargeSummaryOne).execute();
//            if (Boolean.FALSE.equals(dischargeSummaryOneOutcome.getCreated())) {
//                LOGGER.error("{} Testcase Failed when creating Composition", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create Discharge Composition");
//            }
//
//            int expectedTotal = 4;
//            Bundle bundle = client.search().forResource(Composition.class).where(Composition.PATIENT.hasId(patientId)).returnBundle(Bundle.class).execute();
//            int actualTotal = bundle.getTotal();
//            if ((!bundle.hasEntry()) || actualTotal != expectedTotal) {
//                LOGGER.error("{} Test case failed because Composition has no Entry or not as expected entry", testCaseName);
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Composition has no entry or not as expected entry");
//            }
//            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
//                Resource resource = entry.getResource();
//
//                // Check if the resource is a Composition
//                if (resource instanceof Composition composition) {
//                    String title = composition.getTitle();
//                    if (!title.equals("Admission Note") && !title.equals("Operative Note") && !title.equals("Progress Note") && !title.equals("Discharge Summary")) {
//                        LOGGER.error("{} Test case failed because of wrong title", testCaseName);
//                        return new ValidationResultInfo(ErrorLevel.ERROR, "Test case failed because of wrong title");
//                    }
//                    Reference subjectRef = composition.getSubject();
//                    if (!subjectRef.getReference().equals(patientReference)) {
//                        LOGGER.error("{} Test case failed because of Invalid subject reference", testCaseName);
//                        return new ValidationResultInfo(ErrorLevel.ERROR, "Test case failed because of Invalid subject reference" + subjectRef.getReference());
//                    }
//                    List<Reference> authorRefs = composition.getAuthor();
//                    boolean authorFound = false;
//                    for (Reference authorRef : authorRefs) {
//                        if (authorRef.getReference().startsWith(practitionerReference)) {
//                            authorFound = true;
//                            break;
//                        }
//                    }
//                    if (!authorFound) {
//                        LOGGER.error("{} Test case failed because Author reference not found", testCaseName);
//                        return new ValidationResultInfo(ErrorLevel.ERROR, "Test case failed because Author reference not found");
//                    }
//                } else {
//                    LOGGER.error("{} Test case failed because Resource type is not of Composition", testCaseName);
//                    return new ValidationResultInfo(ErrorLevel.ERROR, "Test case failed because Resource type is not of Composition");
//                }
//            }
//            LOGGER.info("{} Testcase successfully passed!", testCaseName);
//            return new ValidationResultInfo(ErrorLevel.OK, "Passed");
//        } catch (Exception ex) {
//            LOGGER.error(ValidateConstant.OPERATION_FAILED_EXCEPTION + SHRF5TestCase2.class.getSimpleName(), ex);
//            throw new OperationFailedException(ex.getMessage(), ex);
//        }
//    }
//}

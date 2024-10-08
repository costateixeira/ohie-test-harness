//package com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.testcases.clientregistry;
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
//import org.hl7.fhir.r4.model.Bundle;
//import org.hl7.fhir.r4.model.Patient;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Map;
//
///**
// * Testcase For CRF8TestCase1
// *
// * @author Bhavi
// */
//
//@Component
//public class CRF8TestCase1 implements TestCase {
//
//    public static final Logger LOGGER = LoggerFactory.getLogger(CRF8TestCase1.class);
//
//    @Override
//    public ValidationResultInfo test(Map<String, IGenericClient> iGenericClientMap, ContextInfo contextInfo) throws OperationFailedException {
//        try {
//            IGenericClient client = iGenericClientMap.get(ComponentServiceConstants.COMPONENT_CLIENT_REGISTRY_ID);
//            if (client == null) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to get IGenericClient");
//            }
//
////            Initial birthdate
//            String birthDate = "1990-01-01";
//            Patient patient = FHIRUtils.createPatient("Doe", "John", "M", birthDate, "urn:oid:1.3.6.1.4.1.21367.13.20.1000", "IHERED-994", true, "", "555-555-5555", "john.doe@example.com", client);
//            MethodOutcome outcome = client.create().resource(patient).execute();
//            if (Boolean.FALSE.equals(outcome.getCreated())) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create patient");
//            }
//            String patientId = outcome.getId().getIdPart();
////          URL for retrieving patient history
//            String historyUrl = client.getServerBase() + "/Patient/" + patientId + "/_history";
//
//            patient = client.read().resource(Patient.class).withId(patientId).execute();
//            Date changedbirthDate = FHIRUtils.parseDate("1999-07-26");
//            patient.setBirthDate(changedbirthDate);
//
//            outcome = client.update().resource(patient).withId(patientId).execute();
//            if (!patientId.equals(outcome.getResource().getIdElement().getIdPart())) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed because instead of update it has created patient");
//            }
//            Bundle patientHistoryBundleAfterBirthDateChange = client.search().byUrl(historyUrl).returnBundle(Bundle.class).execute();
////          In each entry we are checking whether it contain previous birthdate
//            for (Bundle.BundleEntryComponent entry : patientHistoryBundleAfterBirthDateChange.getEntry()) {
//                Patient historicalPatient = (Patient) entry.getResource();
//                Date historybirthDate = historicalPatient.getBirthDate();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                String formattedBirthDate = sdf.format(historybirthDate);
//                if (birthDate.equals(formattedBirthDate)) {
//                    return new ValidationResultInfo(ErrorLevel.OK, "Passed");
//                }
//
//            }
//            return new ValidationResultInfo(ErrorLevel.ERROR, "Failed");
//
//        } catch (Exception ex) {
//            LOGGER.error(ValidateConstant.OPERATION_FAILED_EXCEPTION + CRF8TestCase1.class.getSimpleName(), ex);
//            throw new OperationFailedException(ex.getMessage(), ex);
//        }
//    }
//}

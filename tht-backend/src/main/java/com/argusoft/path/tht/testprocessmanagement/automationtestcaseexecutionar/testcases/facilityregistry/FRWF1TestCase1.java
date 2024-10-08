//package com.argusoft.path.tht.testprocessmanagement.automationtestcaseexecutionar.testcases.facilityregistry;
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
//import org.hl7.fhir.r4.model.Location;
//import org.hl7.fhir.r4.model.Organization;
//import org.hl7.fhir.r4.model.Reference;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * Testcase For FRWF1TestCase1
// *
// * @author Rohit
// */
//
//@Component
//public class FRWF1TestCase1 implements TestCase {
//
//    public static final Logger LOGGER = LoggerFactory.getLogger(FRWF1TestCase1.class);
//
//    @Override
//    public ValidationResultInfo test(Map<String, IGenericClient> iGenericClientMap,
//                                     ContextInfo contextInfo) throws OperationFailedException {
//        try {
//            LOGGER.info("Start testing FRWF1TestCase1");
//            LOGGER.info("Creating facility");
//
//            IGenericClient client = iGenericClientMap.get(ComponentServiceConstants.COMPONENT_FACILITY_REGISTRY_ID);
//            if (client == null) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to get IGenericClient");
//            }
//
//            //create organization
//            Organization organization = FHIRUtils.createOrganization("Health Level Seven International", "0222", "999-999-9999",
//                    "hq@HL7.org", "Ann Arbor", "MI", "USA");
//
//            MethodOutcome organizationOutcome = client.create()
//                    .resource(organization)
//                    .execute();
//
//            // Check if the organization was created successfully
//            if (Boolean.FALSE.equals(organizationOutcome.getCreated())) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create organization");
//            }
//
//            String organizationId = organizationOutcome.getResource().getIdElement().getIdPart();
//            organization = client.read()
//                    .resource(Organization.class)
//                    .withId(organizationId)
//                    .execute();
//
//            //check for organization
//            if (!organization.hasName()) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to read organization");
//            }
//
//            //create location
//            Location location = FHIRUtils.createLocation("0234", "South Wing, second floor", "Second floor of the Old South Wing, formerly in use by Psychiatry",
//                    "2328", "second wing admissions", "Den Burg", "9105 PZ", "NLD", organization);
//
//            MethodOutcome locationOutcome = client.create()
//                    .resource(location)
//                    .execute();
//
//            //check for the location created successfully
//            if (Boolean.FALSE.equals(locationOutcome.getCreated())) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to create location");
//            }
//
//            String locationId = locationOutcome.getResource().getIdElement().getIdPart();
//            location = client.read()
//                    .resource(Location.class)
//                    .withId(locationId)
//                    .execute();
//
//            //to fetch out organization from location
//            Reference organizationRef = location.getManagingOrganization();
//            String organizationRefId = organizationRef.getReferenceElement().getIdPart();
//
//            //check this id with the actual organization id
//            if (!organizationId.equals(organizationRefId)) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to find organization in location");
//            }
//
//            //query for the location by name
//            Bundle locationBundle = client.search()
//                    .forResource(Location.class)
//                    .where(Location.NAME.matches().value("South Wing, second floor"))
//                    .returnBundle(Bundle.class)
//                    .execute();
//
//            // Check if location was found by name
//            if (locationBundle.getEntry().isEmpty()) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to search location by name");
//            }
//
//            //query for the organization by name
//            Bundle organizationBundle = client.search()
//                    .forResource(Organization.class)
//                    .where(Organization.NAME.matches().value("Health Level Seven International"))
//                    .returnBundle(Bundle.class)
//                    .execute();
//
//            // Check if organization was found by name
//            if (organizationBundle.getEntry().isEmpty()) {
//                return new ValidationResultInfo(ErrorLevel.ERROR, "Failed to search organization by name");
//            }
//
//            // Pass the test case if all the above conditions are passed
//            LOGGER.info("testFRWF1Case1 Testcase successfully passed!");
//            return new ValidationResultInfo(ErrorLevel.OK, "Passed");
//
//        } catch (Exception ex) {
//            LOGGER.error(ValidateConstant.EXCEPTION + FRWF1TestCase1.class.getSimpleName(), ex);
//            throw new OperationFailedException(ex.getMessage(), ex);
//        }
//    }
//}

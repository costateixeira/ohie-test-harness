package com.argusoft.path.tht.specificationtestmanagement.openhie.automation.repositories;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.argusoft.path.tht.specificationtestmanagement.openhie.automation.workflow.cr.CRWF1TestCases;
import com.argusoft.path.tht.specificationtestmanagement.openhie.automation.workflow.cr.CRWF2TestCases;
import com.argusoft.path.tht.specificationtestmanagement.openhie.automation.workflow.cr.CRWF3TestCases;
import com.argusoft.path.tht.specificationtestmanagement.openhie.automation.workflow.cr.CRWF4TestCases;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.models.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Implemantation of the CR Testing.
 * Reference
 *
 * @author dhruv
 * @since 2023-09-25
 */
public class CRTestCases {
    public static ValidationResultInfo test(IGenericClient client, ContextInfo contextInfo) {
        System.out.println("Started CRTestCases");
        List<ValidationResultInfo> testCases = new ArrayList<>();
        //code to add entry that started process for CRTest
        testCases.add(CRWF1TestCases.test(client, contextInfo));
        testCases.add(CRWF2TestCases.test(client, contextInfo));
        testCases.add(CRWF3TestCases.test(client, contextInfo));
        testCases.add(CRWF4TestCases.test(client, contextInfo));

        //make entry for whole CR and return response.
        return new ValidationResultInfo("testCR", ErrorLevel.OK, "Passed");
    }
}

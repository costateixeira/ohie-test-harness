/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argusoft.path.tht.specificationtestmanagement.openhie.automation.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.argusoft.path.tht.specificationtestmanagement.openhie.automation.repositories.CRTestCases;
import com.argusoft.path.tht.specificationtestmanagement.openhie.automation.service.AutomationTestProcessService;
import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.models.dto.ContextInfo;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.utils.ValidationUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This AutomationServiceImpl contains implementation for Automation Test Process service.
 *
 * @author dhruv
 * @since 2023-09-13
 */
@Service
public class AutomationTestProcessServiceImpl implements AutomationTestProcessService {

    public void startAutomationTestingProcess(ContextInfo contextInfo) {

        //get these parameters from the UI.
        //and check that process is not started for these parameters before starting process.
        String contextType = "D3"; //create enum for this
        String serverBaseURL = "https://hapi.fhir.org/baseDstu3";
        //http://hapi.fhir.org/baseR4
        //https://hapi.fhir.org/baseDstu2
        //https://hapi.fhir.org/baseDstu3
        //http://hapi.fhir.org/search?serverId=home_r4&pretty=true&_summary=&resource=Patient&param.0.0=&param.0.1=&param.0.2=&param.0.3=&param.0.name=birthdate&param.0.type=date&sort_by=&sort_direction=&resource-search-limit=
        //http://localhost:8080/fhir

        FhirContext context;
        switch (contextType) {
            case "D2":
                context = FhirContext.forDstu2();
                break;
            default:
                context = FhirContext.forDstu3();
        }

        context.getRestfulClientFactory().setConnectTimeout(60 * 1000); //fix configuration
        context.getRestfulClientFactory().setSocketTimeout(60 * 1000); //fix configuration
        IGenericClient client = context.newRestfulGenericClient(serverBaseURL);

        //Add entry that started automation testing.
        //start testcases
        List<CompletableFuture<ValidationResultInfo>> testCases = new ArrayList<>();
        testCases.add(CRTestCases.test(client, contextInfo));
        //same way add test case for other repositories.

        CompletableFuture<Void> allTestCases = CompletableFuture.allOf(
                testCases.toArray(new CompletableFuture[testCases.size()])
        );

        CompletableFuture<List<ValidationResultInfo>> allTestCasesJoins = allTestCases.thenApply(v -> {
            return testCases.stream()
                    .map(pageContentFuture -> pageContentFuture.join())
                    .collect(Collectors.toList());
        });

        allTestCasesJoins.thenAccept(validationResultInfos -> {
            //make entry for automation test.
            if (ValidationUtils.containsErrors(validationResultInfos, ErrorLevel.ERROR)) {
                System.out.println("Failed");
//                new ValidationResultInfo("testAutomation", ErrorLevel.OK,"Failed");
            } else {
                System.out.println("Passed");
//                new ValidationResultInfo("testAutomation", ErrorLevel.OK,"Passed");
            }
        });
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argusoft.path.tht.specificationtestmanagement.openhie.automation.service;

import com.argusoft.path.tht.systemconfiguration.models.dto.ContextInfo;

/**
 * This interface provides contract for Automation Test Process API.
 *
 * @author dhruv
 * @since 2023-09-13
 */
public interface AutomationTestProcessService {

    /**
     * start automation testing process.
     *
     * @param contextInfo information containing the principalId and locale
     *                    information about the caller of service operation
     * @return message
     */
    public void startAutomationTestingProcess(ContextInfo contextInfo);

}

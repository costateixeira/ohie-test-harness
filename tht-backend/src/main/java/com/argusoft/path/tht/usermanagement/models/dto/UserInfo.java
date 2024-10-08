package com.argusoft.path.tht.usermanagement.models.dto;

import com.argusoft.path.tht.systemconfiguration.models.dto.IdStateMetaInfo;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * This info is for User DTO that contains all the User model's data.
 *
 * @author Dhruv
 */
public class UserInfo extends IdStateMetaInfo {

    @ApiModelProperty(notes = "The name for User model",
            allowEmptyValue = false,
            example = "quick user",
            dataType = "String",
            required = true)
    private String name;

    @ApiModelProperty(notes = "The companyName for User model",
            allowEmptyValue = false,
            example = "Argusoft",
            dataType = "String",
            required = true)
    private String companyName;

    @ApiModelProperty(notes = "The email for User model",
            allowEmptyValue = false,
            example = "abc@egov.com",
            dataType = "String",
            required = true)
    private String email;

    @ApiModelProperty(notes = "The password for User model",
            allowEmptyValue = false,
            example = "Qwerty@123",
            dataType = "String",
            required = true)
    private String password;

    @ApiModelProperty(notes = "The associatedRoleIds for model",
            allowEmptyValue = false,
            example = "['1','2','3']",
            dataType = "Set<String>",
            required = true)
    private Set<String> roleIds;

    @ApiModelProperty(notes = "The rejection message for model",
            allowEmptyValue = false,
            example = "Rejected",
            dataType = "String",
            required = true)
    private String message;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Set<String> getRoleIds() {
        if (this.roleIds == null) {
            this.roleIds = new HashSet<>();
        }
        return this.roleIds;
    }

    public void setRoleIds(Set<String> roleIds) {
        this.roleIds = roleIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

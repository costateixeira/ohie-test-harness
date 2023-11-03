/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argusoft.path.tht.usermanagement.models.dto;

import com.argusoft.path.tht.systemconfiguration.models.dto.IdMetaInfo;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Set;

/**
 * This info is for User DTO that contains all the User model's data.
 *
 * @author dhruv
 * @since 2023-09-13
 */
public class UserInfo extends IdMetaInfo implements Serializable {

    @ApiModelProperty(notes = "The name for User model",
            allowEmptyValue = false,
            example = "quick user",
            dataType = "String",
            required = true)
    private String name;

    @ApiModelProperty(notes = "The userName for User model",
            allowEmptyValue = false,
            example = "quickuser",
            dataType = "String",
            required = true)
    private String userName;

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

    @ApiModelProperty(notes = "The provider for User model",
            allowEmptyValue = false,
            example = "[google,local]",
            dataType = "String",
            required = true,
            hidden = true)
    private String provider;

    @ApiModelProperty(notes = "The associatedRoleIds for model",
            allowEmptyValue = false,
            example = "['1','2','3']",
            dataType = "List<UserRoleInfo>",
            required = true)
    private Set<String> roleIds;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<String> roleIds) {
        this.roleIds = roleIds;
    }
}

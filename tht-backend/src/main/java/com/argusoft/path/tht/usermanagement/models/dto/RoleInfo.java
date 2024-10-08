package com.argusoft.path.tht.usermanagement.models.dto;

import com.argusoft.path.tht.systemconfiguration.models.dto.IdInfo;
import io.swagger.annotations.ApiModelProperty;

/**
 * This info is for User DTO that contains all the Role model's data.
 *
 * @author Dhruv
 */
public class RoleInfo extends IdInfo {

    @ApiModelProperty(notes = "The name for User model",
            allowEmptyValue = false,
            example = "quick user",
            dataType = "String",
            required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.argusoft.path.tht.systemconfiguration.models.dto;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * This class is provides implementation for HasMeta.
 *
 * @author Dhruv
 */
public class HasMetaInfo implements Serializable {

    @ApiModelProperty(notes = "The meta of the model",
            allowEmptyValue = true,
            example = "{\n"
                    + "\t\"createdAt\":2014-02-28 ,\n"
                    + "\t\"createdBy\":\"userId\" ,\n"
                    + "\t\"updatedAt\":2014-02-28 ,\n"
                    + "\t\"updatedBy\":\"userId\" ,\n"
                    + "\t\"version\":1\"\n"
                    + "}",
            dataType = "MetaInfo",
            required = false,
            accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private MetaInfo meta;

    public MetaInfo getMeta() {
        if (this.meta == null) {
            this.meta = new MetaInfo();
        }
        return this.meta;
    }

    public void setMeta(MetaInfo meta) {
        this.meta = meta;
    }

    public HasMetaInfo(MetaInfo metaInfo) {
        this.meta = metaInfo;
    }

    public HasMetaInfo() {

    }


}

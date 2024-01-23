package com.argusoft.path.tht.testcasemanagement.constant;

import com.argusoft.path.tht.testcasemanagement.models.dto.ComponentInfo;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constant for ComponentService.
 *
 * @author Dhruv
 */
public class ComponentServiceConstants {

    public static final String COMPONENT_REF_OBJ_URI = ComponentInfo.class.getName();

    //Component states
    public static final String COMPONENT_STATUS_DRAFT = "component.status.draft";
    public static final String COMPONENT_STATUS_ACTIVE = "component.status.active";

    public static final String COMPONENT_STATUS_INACTIVE = "component.status.inactive";

    //Component Ids
    public static final String COMPONENT_CLIENT_REGISTRY_ID = "component.client.registry";
    public static final String COMPONENT_FACILITY_REGISTRY_ID = "component.facility.registry";

    public static List<String> COMPONENT_STATUS = new ArrayList<>();

    static {
        COMPONENT_STATUS.add(COMPONENT_STATUS_INACTIVE);
        COMPONENT_STATUS.add(COMPONENT_STATUS_ACTIVE);
        COMPONENT_STATUS.add(COMPONENT_STATUS_DRAFT);
    }

    public static final Multimap<String, String> COMPONENT_STATUS_MAP = ArrayListMultimap.create();

    static {
        COMPONENT_STATUS_MAP.put(COMPONENT_STATUS_DRAFT, COMPONENT_STATUS_ACTIVE);
        COMPONENT_STATUS_MAP.put(COMPONENT_STATUS_ACTIVE, COMPONENT_STATUS_INACTIVE);
        COMPONENT_STATUS_MAP.put(COMPONENT_STATUS_INACTIVE, COMPONENT_STATUS_ACTIVE);
    }

}

package com.argusoft.path.tht.testcasemanagement.models.mapper;

import com.argusoft.path.tht.systemconfiguration.models.mapper.ModelDtoMapper;
import com.argusoft.path.tht.testcasemanagement.models.dto.SpecificationInfo;
import com.argusoft.path.tht.testcasemanagement.models.entity.ComponentEntity;
import com.argusoft.path.tht.testcasemanagement.models.entity.SpecificationEntity;
import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper to covert DTO <-> Entity for the Specification.
 *
 * @author Dhruv
 */
@Mapper(componentModel = "spring")
public interface SpecificationMapper extends ModelDtoMapper<SpecificationEntity, SpecificationInfo> {

    SpecificationMapper INSTANCE = Mappers.getMapper(SpecificationMapper.class);

    @Mapping(source = "component", target = "componentId")
    @Mapping(source = "testcases", target = "testcaseIds")
    SpecificationInfo modelToDto(SpecificationEntity specificationEntity);

    @InheritInverseConfiguration
    SpecificationEntity dtoToModel(SpecificationInfo specificationInfo);

    List<SpecificationInfo> modelToDto(List<SpecificationEntity> specificationEntities);

    List<SpecificationEntity> dtoToModel(List<SpecificationInfo> specificationInfos);

    default Set<String> setToTestcaseIds(Set<TestcaseEntity> testcaseEntities) {
        if (testcaseEntities == null) {
            return null;
        }
        return testcaseEntities.stream()
                .map(TestcaseEntity::getId)
                .collect(Collectors.toSet());
    }

    default Set<TestcaseEntity> setToTestcases(Set<String> testcaseIds) {
        if (testcaseIds == null) {
            return null;
        }
        return testcaseIds.stream()
                .map(id -> {
                    TestcaseEntity testcaseEntity = new TestcaseEntity();
                    testcaseEntity.setId(id);
                    return testcaseEntity;
                })
                .collect(Collectors.toSet());
    }

    default String setToComponentId(ComponentEntity componentEntity) {
        if (componentEntity == null) {
            return null;
        }
        return componentEntity.getId();
    }

    default ComponentEntity setToComponent(String componentId) {
        if (!StringUtils.hasLength(componentId)) {
            return null;
        }
        ComponentEntity componentEntity = new ComponentEntity();
        componentEntity.setId(componentId);
        return componentEntity;
    }

}

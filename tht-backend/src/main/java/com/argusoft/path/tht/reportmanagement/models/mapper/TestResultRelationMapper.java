package com.argusoft.path.tht.reportmanagement.models.mapper;

import com.argusoft.path.tht.reportmanagement.models.dto.TestResultRelationInfo;
import com.argusoft.path.tht.reportmanagement.models.entity.TestResultRelationEntity;
import com.argusoft.path.tht.reportmanagement.models.entity.TestcaseResultEntity;
import com.argusoft.path.tht.systemconfiguration.models.mapper.ModelDtoMapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper to covert DTO <-> Entity for the TestResultRelation.
 *
 * @author Hardik
 */

@Mapper(componentModel = "spring")
public interface TestResultRelationMapper extends ModelDtoMapper<TestResultRelationEntity, TestResultRelationInfo> {

    TestResultRelationMapper INSTANCE = Mappers.getMapper(TestResultRelationMapper.class);

    @Mapping(source = "version", target = "versionOfRefEntity")
    @Mapping(source = "id", target = "testcaseResultEntityId")
    TestResultRelationInfo modelToDto(TestcaseResultEntity testcaseResultEntity);

    @InheritInverseConfiguration
    @Override
    TestResultRelationEntity dtoToModel(TestResultRelationInfo testResultRelationInfo);

    @Override
    List<TestResultRelationInfo> modelToDto(List<TestResultRelationEntity> testcaseResultEntities);

    @Override
    List<TestResultRelationEntity> dtoToModel(List<TestResultRelationInfo> testResultRelationInfos);

}

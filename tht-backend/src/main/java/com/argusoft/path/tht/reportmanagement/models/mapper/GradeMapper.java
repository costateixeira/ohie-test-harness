package com.argusoft.path.tht.reportmanagement.models.mapper;

import com.argusoft.path.tht.reportmanagement.models.dto.GradeInfo;
import com.argusoft.path.tht.reportmanagement.models.entity.GradeEntity;
import com.argusoft.path.tht.systemconfiguration.models.mapper.ModelDtoMapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper to covert DTO <-> Entity for the Grade.
 *
 * @author Dhruv
 */

@Mapper(componentModel = "spring")
public interface GradeMapper extends ModelDtoMapper<GradeEntity, GradeInfo> {

    GradeMapper INSTANCE = Mappers.getMapper(GradeMapper.class);

    @Override
    GradeInfo modelToDto(GradeEntity gradeEntity);

    @InheritInverseConfiguration
    @Override
    GradeEntity dtoToModel(GradeInfo gradeInfo);

    @Override
    List<GradeInfo> modelToDto(List<GradeEntity> gradeEntities);

    @Override
    List<GradeEntity> dtoToModel(List<GradeInfo> gradeEntities);

}

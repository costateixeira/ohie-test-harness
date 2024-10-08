package com.argusoft.path.tht.usermanagement.models.mapper;

import com.argusoft.path.tht.systemconfiguration.models.mapper.ModelDtoMapper;
import com.argusoft.path.tht.usermanagement.models.dto.UserInfo;
import com.argusoft.path.tht.usermanagement.models.entity.RoleEntity;
import com.argusoft.path.tht.usermanagement.models.entity.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper to covert DTO <-> Entity for the User.
 *
 * @author Dhruv
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends ModelDtoMapper<UserEntity, UserInfo> {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "roles", target = "roleIds")
    @Mapping(source = "password", target = "password", qualifiedByName = "setToPassword")
    UserInfo modelToDto(UserEntity userEntity);

    @InheritInverseConfiguration
    @Mapping(source = "password", target = "password", qualifiedByName = "setToPasswordForModel")
    UserEntity dtoToModel(UserInfo userInfo);

    List<UserInfo> modelToDto(List<UserEntity> userEntities);

    List<UserEntity> dtoToModel(List<UserInfo> userInfos);

    default Set<String> setToRoleIds(Set<RoleEntity> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(RoleEntity::getId)
                .collect(Collectors.toSet());
    }

    default Set<RoleEntity> setToRoles(Set<String> roleIds) {
        if (roleIds == null) {
            return null;
        }
        return roleIds.stream()
                .map(id -> {
                    RoleEntity roleEntity = new RoleEntity();
                    roleEntity.setId(id);
                    return roleEntity;
                })
                .collect(Collectors.toSet());
    }

    @Named("setToPassword")
    default String setToPassword(String password) {
        return null;
    }

    @Named("setToPasswordForModel")
    default String setToPasswordForModel(String password) {
        return password;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argusoft.path.tht.testcasemanagement.repository;

import com.argusoft.path.tht.testcasemanagement.models.entity.ComponentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This repository is for making queries on the Component model.
 *
 * @author dhruv
 * @since 2023-09-13
 */
@Repository
public interface ComponentRepository
        extends JpaRepository<ComponentEntity, String>, ComponentCustomRepository {

    @Query("SELECT DISTINCT entity FROM ComponentEntity entity \n")
    public Page<ComponentEntity> findComponents(Pageable pageable);

    @Query("SELECT DISTINCT entity FROM ComponentEntity entity \n"
            + " WHERE entity.id IN (:ids)")
    public List<ComponentEntity> findComponentsByIds(@Param("ids") List<String> ids);

}

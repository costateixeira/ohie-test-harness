package com.argusoft.path.tht.testcasemanagement.repository;

import com.argusoft.path.tht.testcasemanagement.models.entity.TestcaseOptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * This repository is for making queries on the TestcaseOption model.
 *
 * @author Dhruv
 */
@Repository
public interface TestcaseOptionRepository
        extends JpaRepository<TestcaseOptionEntity, String>, JpaSpecificationExecutor<TestcaseOptionEntity> {

    @Query("SELECT DISTINCT entity FROM TestcaseOptionEntity entity \n")
    public Page<TestcaseOptionEntity> findTestcaseOptions(Pageable pageable);

}

package com.argusoft.path.tht.testcasemanagement.models.entity;

import com.argusoft.path.tht.systemconfiguration.models.entity.IdStateNameMetaEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This model is mapped to specification table in database.
 *
 * @author Dhruv
 */
@Entity
@Audited
@Table(name = "specification")
public class SpecificationEntity extends IdStateNameMetaEntity {

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "is_functional")
    private Boolean isFunctional;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    @JsonIgnore
    private ComponentEntity component;

    @JsonIgnore
    @OneToMany(mappedBy = "specification", cascade = {})
    private Set<TestcaseEntity> testcases;

    public SpecificationEntity() {
    }

    public SpecificationEntity(SpecificationEntity specificationEntity) {
        super(specificationEntity);
        this.setRank(specificationEntity.getRank());
        this.setFunctional(specificationEntity.getFunctional());
        if(specificationEntity.getComponent()!=null){
            this.setComponent(new ComponentEntity(specificationEntity.getComponent()));
        }
        this.setTestcases(specificationEntity.getTestcases().stream().map(TestcaseEntity::new).collect(Collectors.toSet()));
        this.setRequired(specificationEntity.getRequired());
    }

    public Boolean getFunctional() {
        return isFunctional;
    }

    public void setFunctional(Boolean functional) {
        isFunctional = functional;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public void setRequired(Boolean required) {
        isRequired = required;
    }

    @Column(name = "is_required")
    private Boolean isRequired;


    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public ComponentEntity getComponent() {
        return component;
    }

    public void setComponent(ComponentEntity component) {
        this.component = component;
    }

    public Set<TestcaseEntity> getTestcases() {
        if (testcases == null) {
            testcases = new HashSet<>();
        }
        return testcases;
    }

    public void setTestcases(Set<TestcaseEntity> testcases) {
        this.testcases = testcases;
    }

    @Override
    public String toString() {
        return "SpecificationEntity{" +
                "rank=" + rank +
                ", isFunctional=" + isFunctional +
                '}';
    }

}

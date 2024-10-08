package com.argusoft.path.tht.usermanagement.filter;

import com.argusoft.path.tht.systemconfiguration.examplefilter.AbstractCriteriaSearchFilter;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.usermanagement.constant.UserServiceConstants;
import com.argusoft.path.tht.usermanagement.models.entity.RoleEntity;
import com.argusoft.path.tht.usermanagement.models.entity.UserEntity;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Criteria Search filter for User
 *
 * @author Hardik
 */

public class UserSearchCriteriaFilter extends AbstractCriteriaSearchFilter<UserEntity> {

    private String id;

    @ApiParam(
            value = "name of the user"
    )
    private String name;

    @ApiParam(
            value = "name of the user"
    )
    private List<String> states;

    @ApiParam(
            value = "email of the user"
    )
    private String email;

    @ApiParam(
            value = "role of the user"
    )
    private List<String> role;

    @ApiParam(
            value = "company name of the user"
    )
    private String companyName;

    @ApiParam(
            value = "requested date of the user"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date requestDate;

    private Root<UserEntity> userEntityRoot;

    private Join<UserEntity, RoleEntity> userEntityRoleEntityJoin;

    public UserSearchCriteriaFilter(String id) {
        this.id = id;
    }

    public UserSearchCriteriaFilter() {
    }

    @Override
    protected void modifyCriteriaQuery(CriteriaBuilder criteriaBuilder, Root<UserEntity> root, CriteriaQuery<?> query, Pageable pageable) {

        if(!CollectionUtils.isEmpty(getRole())){
            query.groupBy(root.get("id"));
        }

        Sort.Order order = pageable.getSort().getOrderFor("default");
        if (order == null) {
            return;
        }
        Expression<Object> stateWiseDefaultOrder = criteriaBuilder.selectCase()
                .when(criteriaBuilder.equal(root.get("state"), UserServiceConstants.USER_STATUS_APPROVAL_PENDING), 1)
                .when(criteriaBuilder.equal(root.get("state"), UserServiceConstants.USER_STATUS_VERIFICATION_PENDING), 2)
                .when(criteriaBuilder.equal(root.get("state"), UserServiceConstants.USER_STATUS_ACTIVE), 3)
                .when(criteriaBuilder.equal(root.get("state"), UserServiceConstants.USER_STATUS_INACTIVE), 4)
                .otherwise(6);
        if (order.isAscending()) {
            query.orderBy(criteriaBuilder.asc(stateWiseDefaultOrder));
        } else {
            query.orderBy(criteriaBuilder.desc(stateWiseDefaultOrder));
        }
    }

    @Override
    protected List<Predicate> buildPredicates(Root<UserEntity> root, CriteriaBuilder criteriaBuilder, ContextInfo contextInfo) {
        setUserEntityRoot(root);
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasLength(getPrimaryId())) {
            predicates.add(criteriaBuilder.equal(getUserEntityRoot().get("id"), getPrimaryId()));
        }

        if (StringUtils.hasLength(getName())) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(getUserEntityRoot().get("name")), getNameBasedOnSearchType(getName())));

        }

        if (!CollectionUtils.isEmpty(getState())) {
            predicates.add(criteriaBuilder.in(getUserEntityRoot().get("state")).value(getState()));
        }

        if (StringUtils.hasLength(getEmail())) {
            predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(getUserEntityRoot().get("email")), getEmail().toLowerCase()));
        }

        if (!CollectionUtils.isEmpty(getRole())) {
            predicates.add(criteriaBuilder.in(this.getUserEntityRoleEntityJoin().get("id")).value(getRole()));
        }


        return predicates;
    }

    @Override
    protected List<Predicate> buildLikePredicates(Root<UserEntity> root, CriteriaBuilder criteriaBuilder, ContextInfo contextInfo){
        setUserEntityRoot(root);
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasLength(getName())) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(getUserEntityRoot().get("name")), "%" + name.toLowerCase() + "%"));
        }

        if (StringUtils.hasLength(getCompanyName())) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(getUserEntityRoot().get("companyName")), "%" + companyName.toLowerCase() + "%"));
        }

        if (getRequestDate() != null) {
            // Truncate the time part of the requestDate
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getRequestDate());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date truncatedRequestDate = new java.sql.Date(calendar.getTime().getTime());

            // Add the predicate to compare the date part only
            predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.function("DATE", Date.class, root.get("createdAt")),
                    truncatedRequestDate
            ));
        }


        if (!CollectionUtils.isEmpty(getState())) {
            predicates.add(criteriaBuilder.in(getUserEntityRoot().get("state")).value(getState()));
        }

        if (!CollectionUtils.isEmpty(getRole())) {
            predicates.add(criteriaBuilder.in(this.getUserEntityRoleEntityJoin().get("id")).value(getRole()));
        }

        if (StringUtils.hasLength(getEmail())) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(getUserEntityRoot().get("email")), "%" + email.toLowerCase() + "%"));
        }

        return predicates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getState() {
        return states;
    }

    public void setState(List<String> states) {
        this.states = states;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrimaryId() {
        return id;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    private Root<UserEntity> getUserEntityRoot() {
        return userEntityRoot;
    }

    private void setUserEntityRoot(Root<UserEntity> userEntityRoot) {
        this.userEntityRoot = userEntityRoot;
        this.userEntityRoleEntityJoin = null;
    }

    private Join<UserEntity, RoleEntity> getUserEntityRoleEntityJoin() {
        if (userEntityRoleEntityJoin == null) {
            userEntityRoleEntityJoin = getUserEntityRoot().join("roles");
        }
        return userEntityRoleEntityJoin;
    }

}

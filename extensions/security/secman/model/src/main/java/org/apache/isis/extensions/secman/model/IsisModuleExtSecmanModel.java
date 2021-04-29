/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.extensions.secman.model;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationFeatureViewModels;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationOrphanedPermissionManager;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationOrphanedPermissionManager_relocateSelected;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermissionMenu;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_allow;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_changing;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_delete;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_updateRole;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_veto;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_viewing;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRoleMenu;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_addPermission;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_addUser;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_delete;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_removePermissions;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_removeUsers;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_updateDescription;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_updateName;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancyMenu;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_addChild;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_addUser;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_delete;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_removeChild;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_removeUser;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_updateName;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_updateParent;
import org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_users;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUserMenu;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_addRole;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_delete;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_duplicate;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_filterPermissions;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_lock;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_permissions;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_removeRoles;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_resetPassword;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_unlock;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updateAccountType;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updateAtPath;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updateEmailAddress;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updateFaxNumber;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updateName;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updatePassword;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updatePhoneNumber;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_updateUsername;
import org.apache.isis.extensions.secman.model.dom.user.HasUsername_open;
import org.apache.isis.extensions.secman.model.dom.user.MeService;
import org.apache.isis.extensions.secman.model.facets.TenantedAuthorizationFacetFactory;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // Module
        IsisModuleExtSecmanApi.class,

        // @DomainService
        ApplicationFeatureViewModels.class,
        ApplicationOrphanedPermissionManager.class,
        ApplicationPermissionMenu.class,
        ApplicationRoleMenu.class,
        ApplicationTenancyMenu.class,
        ApplicationUserMenu.class,
        
        //ImpersonateMenuAdvisorForSecman.class, //not activated by default yet
        MeService.class,

        // @Component
        TenantedAuthorizationFacetFactory.Register.class,
        
        // -- Mixins
/*TODO[2619] when launching the demo there should be 
 *  - introspecting 43 value types
 *  - introspecting 464 mixins
 *  - introspecting 52 managed beans contributing (aka domain services)
 *  - introspecting 59 entities (JDO+JPA)
 *  - introspecting 166 view models
 *  so it seems this list is missing one mixin ...     
 *        
        //ApplicationOrphanedPermissionManager
        ApplicationOrphanedPermissionManager_relocateSelected.class,

        //ApplicationPermission
        ApplicationPermission_allow.class,
        ApplicationPermission_changing.class,
        ApplicationPermission_delete.class,
        ApplicationPermission_updateRole.class,
        ApplicationPermission_veto.class,
        ApplicationPermission_viewing.class,

        //ApplicationRole
        ApplicationRole_addPermission.class,
        ApplicationRole_addUser.class,
        ApplicationRole_delete.class,
        ApplicationRole_removePermissions.class,
        ApplicationRole_removeUsers.class,
        ApplicationRole_updateDescription.class,
        ApplicationRole_updateName.class,

        //ApplicationTenancy
        ApplicationTenancy_users.class,
        ApplicationTenancy_addChild.class,
        ApplicationTenancy_addUser.class,
        ApplicationTenancy_delete.class,
        ApplicationTenancy_removeChild.class,
        ApplicationTenancy_removeUser.class,
        ApplicationTenancy_updateName.class,
        ApplicationTenancy_updateParent.class,

        //ApplicationUser
        ApplicationUser_permissions.class,
        ApplicationUser_addRole.class,
        ApplicationUser_delete.class,
        ApplicationUser_duplicate.class,
        ApplicationUser_filterPermissions.class,
        ApplicationUser_lock.class,
        HasUsername_open.class,
        ApplicationUser_removeRoles.class,
        ApplicationUser_resetPassword.class,
        ApplicationUser_unlock.class,
        ApplicationUser_updateAccountType.class,
        ApplicationUser_updateAtPath.class,
        ApplicationUser_updateEmailAddress.class,
        ApplicationUser_updateFaxNumber.class,
        ApplicationUser_updateName.class,
        ApplicationUser_updatePassword.class,
        ApplicationUser_updatePhoneNumber.class,
        ApplicationUser_updateUsername.class,

        //ApplicationUserManager (abstract, concrete classes are in JDO/JPA)
        //ApplicationUserManager_allUsers.class,
        //ApplicationUserManager_newDelegateUser.class,
        //ApplicationUserManager_newLocalUser.class,
*/
    })        
//TODO[2619] remove once we have the complete list of @Components
@ComponentScan(
        basePackageClasses= {
                IsisModuleExtSecmanModel.class
        })
public class IsisModuleExtSecmanModel {

}

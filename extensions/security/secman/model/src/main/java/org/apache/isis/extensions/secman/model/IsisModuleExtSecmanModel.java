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

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationFeatureViewModels;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationNamespace;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationType;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationTypeAction;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationTypeCollection;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationTypeMember;
import org.apache.isis.extensions.secman.model.dom.feature.ApplicationTypeProperty;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationOrphanedPermissionManager;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationOrphanedPermissionManager_relocateSelected;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermissionMenu;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_allow;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_changing;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_delete;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_feature;
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
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUserManager;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUserMenu;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_addRole;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_delete;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_duplicate;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_filterPermissions;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_lock;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_permissions;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_removeRoles;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_resetPassword;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_unlock;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateAccountType;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateAtPath;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateEmailAddress;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateFaxNumber;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateName;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updatePassword;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updatePhoneNumber;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateUsername;
import org.apache.isis.extensions.secman.model.dom.user.HasUsername_open;
import org.apache.isis.extensions.secman.model.dom.user.MeService;
import org.apache.isis.extensions.secman.model.dom.user.UserPermissionViewModel;
import org.apache.isis.extensions.secman.model.facets.TenantedAuthorizationPostProcessor;
import org.apache.isis.extensions.secman.model.spiimpl.TableColumnHidingService;

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
        TenantedAuthorizationPostProcessor.Register.class,
        TableColumnHidingService.class,

        // -- ViewModels

        ApplicationNamespace.class,
        ApplicationType.class,
        ApplicationTypeAction.class,
        ApplicationTypeCollection.class,
        ApplicationTypeMember.class,
        ApplicationTypeProperty.class,
        ApplicationUserManager.class,
        UserPermissionViewModel.class,

        // -- Mixins

        //ApplicationOrphanedPermissionManager
        ApplicationOrphanedPermissionManager_relocateSelected.class,

        //ApplicationPermission
        ApplicationPermission_allow.class,
        ApplicationPermission_changing.class,
        ApplicationPermission_delete.class,
        ApplicationPermission_feature.class,
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
        ApplicationTenancy_addChild.class,
        ApplicationTenancy_addUser.class,
        ApplicationTenancy_delete.class,
        ApplicationTenancy_removeChild.class,
        ApplicationTenancy_removeUser.class,
        ApplicationTenancy_updateName.class,
        ApplicationTenancy_updateParent.class,
        ApplicationTenancy_users.class,

        //ApplicationUser
        ApplicationUser_addRole.class,
        ApplicationUser_delete.class,
        ApplicationUser_duplicate.class,
        ApplicationUser_filterPermissions.class,
        ApplicationUser_lock.class,
        ApplicationUser_permissions.class,
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
        HasUsername_open.class,

        //ApplicationUserManager (abstract, concrete classes are in JDO/JPA)
        //ApplicationUserManager_allUsers.class,
        //ApplicationUserManager_newDelegateUser.class,
        //ApplicationUserManager_newLocalUser.class,
    })
public class IsisModuleExtSecmanModel {

}

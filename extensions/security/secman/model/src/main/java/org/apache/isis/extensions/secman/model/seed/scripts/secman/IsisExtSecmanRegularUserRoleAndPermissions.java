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
package org.apache.isis.extensions.secman.model.seed.scripts.secman;

import org.apache.isis.applib.domain.DomainObjectList;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.layout.LayoutServiceMenu;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.inspect.model.ActionNode;
import org.apache.isis.core.metamodel.inspect.model.CollectionNode;
import org.apache.isis.core.metamodel.inspect.model.FacetAttrNode;
import org.apache.isis.core.metamodel.inspect.model.FacetGroupNode;
import org.apache.isis.core.metamodel.inspect.model.FacetNode;
import org.apache.isis.core.metamodel.inspect.model.ParameterNode;
import org.apache.isis.core.metamodel.inspect.model.PropertyNode;
import org.apache.isis.core.metamodel.inspect.model.TypeNode;
import org.apache.isis.core.security.authentication.logout.LogoutMenu;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.fixtures.AbstractRoleAndPermissionsFixtureScript;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.menu.MeService;

import lombok.val;

/**
 * Role for regular users of the security module, providing the ability to lookup their user account using the
 * {@link MeService}, and for viewing and maintaining their user details.
 *
 * @since 2.0 {@index}
 */
public class IsisExtSecmanRegularUserRoleAndPermissions extends AbstractRoleAndPermissionsFixtureScript {

    public IsisExtSecmanRegularUserRoleAndPermissions(SecmanConfiguration configBean) {
        super(configBean.getRegularUserRoleName(), "Regular user of the security module");
    }

    @Override
    protected void execute(ExecutionContext executionContext) {

        val allowViewing = Can.of(
                ApplicationFeatureId.newType(ApplicationUser.OBJECT_TYPE),
                ApplicationFeatureId.newMember(ApplicationRole.OBJECT_TYPE, "name"),
                ApplicationFeatureId.newMember(ApplicationRole.OBJECT_TYPE, "description"),
                ApplicationFeatureId.newType(ActionNode.OBJECT_TYPE),
                ApplicationFeatureId.newType(CollectionNode.OBJECT_TYPE),
                ApplicationFeatureId.newType(FacetAttrNode.OBJECT_TYPE),
                ApplicationFeatureId.newType(FacetGroupNode.OBJECT_TYPE),
                ApplicationFeatureId.newType(FacetNode.OBJECT_TYPE),
                ApplicationFeatureId.newType(ParameterNode.OBJECT_TYPE),
                ApplicationFeatureId.newType(PropertyNode.OBJECT_TYPE),
                ApplicationFeatureId.newType(TypeNode.OBJECT_TYPE)
                );

        val allowChanging = Can.of(
                ApplicationFeatureId.newType(MeService.OBJECT_TYPE),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updateName"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updatePassword"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updateEmailAddress"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updatePhoneNumber"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "updateFaxNumber"),
                ApplicationFeatureId.newType(LogoutMenu.OBJECT_TYPE),
                ApplicationFeatureId.newType(UserMemento.OBJECT_TYPE),
                ApplicationFeatureId.newType(RoleMemento.OBJECT_TYPE),
                ApplicationFeatureId.newType(DomainObjectList.OBJECT_TYPE),
                ApplicationFeatureId.newType(LayoutServiceMenu.OBJECT_TYPE)
                );

        val vetoViewing = Can.of(
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "filterPermissions"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "resetPassword"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "lock"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "unlock"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "addRole"),
                ApplicationFeatureId.newMember(ApplicationUser.OBJECT_TYPE, "removeRoles")
                );

        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.VIEWING,
                allowViewing);

        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                allowChanging);

        newPermissions(
                ApplicationPermissionRule.VETO,
                ApplicationPermissionMode.VIEWING,
                vetoViewing);

    }

}

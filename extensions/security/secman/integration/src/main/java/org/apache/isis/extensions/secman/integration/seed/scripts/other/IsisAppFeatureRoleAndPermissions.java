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
package org.apache.isis.extensions.secman.integration.seed.scripts.other;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeatui.ApplicationFeatureMenu;
import org.apache.isis.applib.services.appfeatui.ApplicationFeatureViewModel;
import org.apache.isis.applib.services.appfeatui.ApplicationNamespace;
import org.apache.isis.applib.services.appfeatui.ApplicationType;
import org.apache.isis.applib.services.appfeatui.ApplicationTypeAction;
import org.apache.isis.applib.services.appfeatui.ApplicationTypeCollection;
import org.apache.isis.applib.services.appfeatui.ApplicationTypeMember;
import org.apache.isis.applib.services.appfeatui.ApplicationTypeProperty;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.fixtures.AbstractRoleAndPermissionsFixtureScript;

/**
 * @since 2.0 {@index}
 */
public class IsisAppFeatureRoleAndPermissions
extends AbstractRoleAndPermissionsFixtureScript {

    public static final String ROLE_NAME = ApplicationFeatureMenu.LOGICAL_TYPE_NAME.replace(".","-");

    public IsisAppFeatureRoleAndPermissions() {
        super(ROLE_NAME, "Access application features");
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        newPermissions(
                ApplicationPermissionRule.ALLOW,
                ApplicationPermissionMode.CHANGING,
                Can.of(
                        ApplicationFeatureId.newType(ApplicationFeatureMenu.LOGICAL_TYPE_NAME),
                        ApplicationFeatureId.newType(ApplicationFeatureViewModel.LOGICAL_TYPE_NAME),
                        ApplicationFeatureId.newType(ApplicationNamespace.LOGICAL_TYPE_NAME),
                        ApplicationFeatureId.newType(ApplicationType.LOGICAL_TYPE_NAME),
                        ApplicationFeatureId.newType(ApplicationTypeMember.LOGICAL_TYPE_NAME),
                        ApplicationFeatureId.newType(ApplicationTypeAction.LOGICAL_TYPE_NAME),
                        ApplicationFeatureId.newType(ApplicationTypeProperty.LOGICAL_TYPE_NAME),
                        ApplicationFeatureId.newType(ApplicationTypeCollection.LOGICAL_TYPE_NAME)
                        )
        );
    }
}

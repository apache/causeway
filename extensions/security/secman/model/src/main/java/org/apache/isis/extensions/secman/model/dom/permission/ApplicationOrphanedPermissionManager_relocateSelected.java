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
package org.apache.isis.extensions.secman.model.dom.permission;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission.RelocateNamespaceDomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        associateWith = "orphanedPermissions",
        domainEvent = RelocateNamespaceDomainEvent.class, 
        semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
@RequiredArgsConstructor
public class ApplicationOrphanedPermissionManager_relocateSelected {

    @Inject private ApplicationFeatureRepositoryDefault applicationFeatureRepository;
    
    private final ApplicationOrphanedPermissionManager holder;
    
    public ApplicationOrphanedPermissionManager act(
            final Collection<ApplicationPermission> permissions,
            
            @Parameter(optionality = Optionality.MANDATORY)
            final String targetNamespace) {
        
        permissions.forEach(perm->relocate(perm, targetNamespace));
        return holder;
    }

    public Collection<String> choices1Act() {
        return applicationFeatureRepository.packageNames();
    }
    
    private void relocate(
            final ApplicationPermission permission, 
            final String targetNamespace) {
        
        val appFeatureId = ApplicationFeatureId.newFeature(
                permission.getFeatureType(), 
                permission.getFeatureFqn());
        
        val relocatedFqn = appFeatureId
                .withNamespace(targetNamespace)
                .getFullyQualifiedName();
        
        permission.setFeatureFqn(relocatedFqn);
    }
    
}

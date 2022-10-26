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
package org.apache.causeway.extensions.secman.integration.spiimpl;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.user.menu.MeService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named(CausewayModuleExtSecmanApplib.NAMESPACE + ".TableColumnVisibilityServiceForSecman")
@javax.annotation.Priority(PriorityPrecedence.LATE - 10)
@Qualifier("Secman")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TableColumnVisibilityServiceForSecman implements TableColumnVisibilityService {

    final MeService meService;
    final FactoryService factoryService;
    final SpecificationLoader specificationLoader;

    @Override
    public boolean hides(final Class<?> elementType, final String memberId) {
        val me = factoryService.mixin(MeService.me.class, meService).act();
        val permissionSet = me.getPermissionSet();

        final boolean granted = specificationLoader.specForType(elementType)
            .map(ObjectSpecification::getLogicalTypeName)
            .map(logicalTypeName->{
                //XXX lombok val issue with lambda
                val featureId = ApplicationFeatureId.newMember(logicalTypeName, memberId);
                return permissionSet.evaluate(featureId, ApplicationPermissionMode.VIEWING).isGranted();
            })
            .orElse(false); // do not grant if elementType has no logicalTypeName

        return !granted;

    }

}

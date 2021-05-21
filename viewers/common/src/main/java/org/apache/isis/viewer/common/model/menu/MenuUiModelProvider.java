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
package org.apache.isis.viewer.common.model.menu;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

@Service
public class MenuUiModelProvider {

    @Inject private MetaModelContext metaModelContext;

    public MenuUiModel getMenu(final DomainServiceLayout.MenuBar menuBarSelect) {
        return MenuUiModel.of(menuBarSelect, select(menuBarSelect));
    }

    // -- HELPER

    private List<String> select(final DomainServiceLayout.MenuBar menuBarSelect) {
        return metaModelContext.streamServiceAdapters()
                .filter(with(menuBarSelect))
                .map(ManagedObject::getSpecification)
                .map(ObjectSpecification::getLogicalTypeName)
                .collect(Collectors.toList());
    }

    private static Predicate<ManagedObject> with(final DomainServiceLayout.MenuBar menuBarSelect) {
        return (ManagedObject adapter) -> {
            val domainServiceLayoutFacet = adapter.getSpecification()
                    .getFacet(DomainServiceLayoutFacet.class);
            return (domainServiceLayoutFacet != null
                        && domainServiceLayoutFacet.getMenuBar() == menuBarSelect)
                    || (domainServiceLayoutFacet == null
                        && menuBarSelect == DomainServiceLayout.MenuBar.PRIMARY);
        };
    }

}

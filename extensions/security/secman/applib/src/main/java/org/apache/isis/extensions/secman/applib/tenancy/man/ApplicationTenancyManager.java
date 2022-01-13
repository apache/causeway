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
package org.apache.isis.extensions.secman.applib.tenancy.man;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        logicalTypeName = ApplicationTenancyManager.LOGICAL_TYPE_NAME
)
public class ApplicationTenancyManager {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationTenancyManager";

    @ObjectSupport public String title() {
        return "Application Tenancy Manager";
    }

    // -- INFORMAL METADATA

    @Inject private SpecificationLoader specLoader;

    @Property @PropertyLayout(fieldSetId = "metadata")
    public String getTenancyType() {
        return specLoader.specForLogicalTypeName(ApplicationTenancy.LOGICAL_TYPE_NAME)
                .map(ObjectSpecification::getCorrespondingClass)
                .map(Class::getName)
                .orElse("not found");
    }

    // --

    // behaviour provided by mixins

}

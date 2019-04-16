/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.metamodel;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.schema.metamodel.v1.DomainClassDto;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

@Mixin(method="act")
public class Object_downloadMetaModelXml {

    private final Object object;

    public Object_downloadMetaModelXml(final Object object) {
        this.object = object;
    }

    public static class ActionDomainEvent extends org.apache.isis.applib.IsisApplibModule.ActionDomainEvent<Object_downloadMetaModelXml> {}

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            contributed = Contributed.AS_ACTION,
            cssClassFa = "fa-download",
            position = ActionLayout.Position.PANEL_DROPDOWN
    )
    @MemberOrder(name = "datanucleusIdLong", sequence = "700.2")
    public Object act(
            @ParameterLayout(named = "File name")
            final String fileName) {

        MetaModelService6.Config config =
                new MetaModelService6.Config()
                        .withIgnoreNoop()
                        .withIgnoreAbstractClasses()
                        .withIgnoreInterfaces()
                        .withIgnoreBuiltInValueTypes();
        final String pkg = object.getClass().getPackage().getName();
        config = config.withPackagePrefix(pkg);
        final MetamodelDto metamodelDto = metaModelService.exportMetaModel(config);

        final List<DomainClassDto> domainClassDtos = metamodelDto.getDomainClassDto();

        final String className = object.getClass().getName();
        for (Iterator<DomainClassDto> iterator = domainClassDtos.iterator(); iterator.hasNext(); ) {
            final DomainClassDto classDto = iterator.next();
            final String id = classDto.getId();
            if(!Objects.equals(id, className)) {
                iterator.remove();
            }
        }
        final String asXml = jaxbService.toXml(metamodelDto);

        return new Clob(
                Util.withSuffix(fileName, "xml"),
                metaModelServicesMenu.mimeTypeTextXml, asXml);
    }

    public String default0Act() {
        return Util.withSuffix(object.getClass().getSimpleName(), "xml");
    }


    @javax.inject.Inject
    MetaModelService6 metaModelService;
    @Inject
    JaxbService jaxbService;
    @Inject
    MetaModelServicesMenu metaModelServicesMenu;
}

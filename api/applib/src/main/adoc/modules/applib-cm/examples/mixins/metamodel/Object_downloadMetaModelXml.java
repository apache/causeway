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
package org.apache.isis.applib.mixins.metamodel;

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
import org.apache.isis.applib.mixins.MixinConstants;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.metamodel.MetaModelServicesMenu;
import org.apache.isis.applib.value.BlobClobFactory;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Mixin(method="act")
@RequiredArgsConstructor
public class Object_downloadMetaModelXml {

    private final Object holder;

    public static class ActionDomainEvent 
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_downloadMetaModelXml> {}

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
    @MemberOrder(name = MixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "700.2")
    public Object act(

            // PARAM 0
            @ParameterLayout(
                    named = MixinConstants.FILENAME_PROPERTY_NAME,
                    describedAs = MixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName) {

        val pkg = holder.getClass().getPackage().getName();

        val config =
                new MetaModelService.Config()
                .withIgnoreNoop()
                .withIgnoreAbstractClasses()
                .withIgnoreInterfaces()
                .withIgnoreBuiltInValueTypes()
                .withPackagePrefix(pkg);

        val metamodelDto = metaModelService.exportMetaModel(config);

        val className = holder.getClass().getName();

        val domainClassDtos = metamodelDto.getDomainClassDto();
        domainClassDtos.removeIf(classDto->!Objects.equals(classDto.getId(), className));

        val xmlString = jaxbService.toXml(metamodelDto);

        return BlobClobFactory.clobXml(fileName, xmlString);

    }

    // -- PARAM 0

    public String default0Act() {
        return holder.getClass().getSimpleName();
    }

    // -- DEPENDENCIES

    @Inject MetaModelService metaModelService;
    @Inject JaxbService jaxbService;
    @Inject MetaModelServicesMenu metaModelServicesMenu;



}

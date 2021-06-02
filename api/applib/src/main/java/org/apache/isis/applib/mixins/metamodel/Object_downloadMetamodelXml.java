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
import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.mixins.dto.DtoMixinConstants;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.metamodel.Config;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.metamodel.MetaModelServiceMenu;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides the ability to download the framework's internal
 * metamodel for any domain entity or view model, in XML format.
 *
 * @since 1.x {@index}
 */
@Action(
        domainEvent = Object_downloadMetamodelXml.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-download",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        sequence = "700.2"
)
@DomainObject(logicalTypeName = IsisModuleApplib.NAMESPACE + ".mixins.metamodel.Object_downloadMetamodelXml")
@RequiredArgsConstructor
public class Object_downloadMetamodelXml {

    private final Object holder;

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_downloadMetamodelXml> {}

    @MemberSupport
    public Object act(
            @ParameterLayout(
                    named = DtoMixinConstants.FILENAME_PROPERTY_NAME,
                    describedAs = DtoMixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName) {


        final Optional<LogicalType> logicalTypeIfAny = metaModelService.lookupLogicalTypeByClass(holder.getClass());
        if(!logicalTypeIfAny.isPresent()) {
            messageService.warnUser("Unknown class, unable to export");
            return null;
        }
        final String namespace = logicalTypeIfAny.get().getNamespace();

        val config =
                new Config()
                .withIgnoreNoop()
                .withIgnoreAbstractClasses()
                .withIgnoreInterfaces()
                .withIgnoreBuiltInValueTypes()
                .withNamespacePrefix(namespace);

        val metamodelDto = metaModelService.exportMetaModel(config);

        val className = holder.getClass().getName();

        val domainClassDtos = metamodelDto.getDomainClassDto();
        domainClassDtos.removeIf(classDto->!Objects.equals(classDto.getId(), className));

        val xmlString = jaxbService.toXml(metamodelDto);

        return Clob.of(fileName, CommonMimeType.XML, xmlString);
    }


    /**
     * Defaults to the simple name of the domain object's class.
     */
    @MemberSupport public String default0Act() {
        return holder.getClass().getSimpleName();
    }



    @Inject MetaModelService metaModelService;
    @Inject MessageService messageService;
    @Inject JaxbService jaxbService;



}

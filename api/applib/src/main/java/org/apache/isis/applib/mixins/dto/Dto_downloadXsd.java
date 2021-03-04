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
package org.apache.isis.applib.mixins.dto;

import java.util.Map;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.jaxb.IsisSchemas;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.util.ZipWriter;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Mixin that provides the ability to download the XSD schema for a view model
 * can be downloaded as XML.
 *
 * <p>
 *  Requires that the view model is a JAXB view model, and implements the
 *  {@link Dto} marker interface.
 * </p>
 *
 * <p>
 * If the domain object's JAXB annotations reference only a single XSD schema
 * then this will return that XML text as a {@link Clob} of that XSD.
 * If there are multiple XSD schemas referenced then the action will return a
 * zip of those schemas, wrapped up in a {@link Blob}.
 * </p>
 *
 * @since 1.x {@index}
 */
@Action(
        domainEvent = Dto_downloadXsd.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        restrictTo = RestrictTo.PROTOTYPING
        )
@ActionLayout(
        cssClassFa = "fa-download"
        )
@RequiredArgsConstructor
public class Dto_downloadXsd {

    private final Dto holder;

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Dto_downloadXsd> {}

    /**
     * The {@link IsisSchemas} parameter can be used to optionally ignore the
     * common Apache Isis schemas; useful if there is only one other XSD schema
     * referenced by the DTO.
     */
    @MemberOrder(sequence = "500.2")
    public Object act(

            @ParameterLayout(
                    named = DtoMixinConstants.FILENAME_PROPERTY_NAME,
                    describedAs = DtoMixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName,

            final IsisSchemas isisSchemas) {

        val schemaMap = jaxbService.toXsd(holder, isisSchemas);

        if(schemaMap.isEmpty()) {
            val msg = String.format(
                    "No schemas were generated for %s; programming error?",
                    holder.getClass().getName());
            messageService.warnUser(msg);
            return null;
        }

        if(schemaMap.size() == 1) {
            val xmlString = schemaMap.values().iterator().next();
            return Clob.of(fileName, CommonMimeType.XSD, xmlString);
        }

        val zipWriter = ZipWriter.newInstance();

        for (Map.Entry<String, String> entry : schemaMap.entrySet()) {
            val namespaceUri = entry.getKey();
            val schemaText = entry.getValue();
            zipWriter.nextEntry(zipEntryNameFor(namespaceUri), writer->{
                writer.write(schemaText);
            });
        }

        return Blob.of(fileName, CommonMimeType.ZIP, zipWriter.toBytes());

    }

    public String default0Act() {
        return holder.getClass().getName();
    }

    public IsisSchemas default1Act() {
        return IsisSchemas.IGNORE;
    }

    private static String zipEntryNameFor(final String namespaceUri) {
        return namespaceUri + ".xsd";
    }

    @Inject MessageService messageService;
    @Inject JaxbService jaxbService;
}


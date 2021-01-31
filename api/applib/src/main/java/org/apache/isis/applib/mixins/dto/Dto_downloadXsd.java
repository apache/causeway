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
import org.apache.isis.applib.mixins.MixinConstants;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.util.ZipWriter;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
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

    @MemberOrder(sequence = "500.2")
    public Object act(

            // PARAM 0
            @ParameterLayout(
                    named = MixinConstants.FILENAME_PROPERTY_NAME,
                    describedAs = MixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName,

            // PARAM 1
            final JaxbService.IsisSchemas isisSchemas) {

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

    // -- PARAM 0

    public String default0Act() {
        return holder.getClass().getName();
    }

    // -- PARAM 1

    public JaxbService.IsisSchemas default1Act() {
        return JaxbService.IsisSchemas.IGNORE;
    }

    // -- HELPER

    private static String zipEntryNameFor(final String namespaceUri) {
        return namespaceUri + ".xsd";
    }

    // -- DEPENDENCIES

    @Inject MessageService messageService;
    @Inject JaxbService jaxbService;
}


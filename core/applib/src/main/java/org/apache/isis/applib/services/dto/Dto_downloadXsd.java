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
package org.apache.isis.applib.services.dto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

@Mixin(method="act")
public class Dto_downloadXsd {

    private final Dto dto;

    private final MimeType mimeTypeApplicationZip;

    public Dto_downloadXsd(final Dto dto) {
        this.dto = dto;
        try {
            mimeTypeApplicationZip = new MimeType("application", "zip");
        } catch (final MimeTypeParseException ex) {
            throw new FatalException(ex);
        }
    }

    public static class ActionDomainEvent extends org.apache.isis.applib.IsisApplibModule.ActionDomainEvent<Dto_downloadXsd> {}

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            contributed = Contributed.AS_ACTION,
            cssClassFa = "fa-download"
    )
    @MemberOrder(sequence = "500.2")
    public Object act(
            @ParameterLayout(named = "File name")
            final String fileName,
            final JaxbService.IsisSchemas isisSchemas) {

        final Map<String, String> map = jaxbService.toXsd(dto, isisSchemas);

        if(map.isEmpty()) {
            messageService.warnUser(String.format(
                    "No schemas were generated for %s; programming error?", dto.getClass().getName()));
            return null;
        }

        if(map.size() == 1) {
            final Map.Entry<String, String> entry = map.entrySet().iterator().next();
            return new Clob(Util.withSuffix(fileName, "xsd"), "text/xml", entry.getValue());
        }

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            final OutputStreamWriter writer = new OutputStreamWriter(zos);

            for (Map.Entry<String, String> entry : map.entrySet()) {
                final String namespaceUri = entry.getKey();
                final String schemaText = entry.getValue();
                zos.putNextEntry(new ZipEntry(zipEntryNameFor(namespaceUri)));
                writer.write(schemaText);
                writer.flush();
                zos.closeEntry();
            }

            writer.close();
            return new Blob(Util.withSuffix(fileName, "zip"), mimeTypeApplicationZip, baos.toByteArray());
        } catch (final IOException ex) {
            throw new FatalException("Unable to create zip", ex);
        }
    }

    public String default0Act() {
        return Util.withSuffix(dto.getClass().getName(), "xsd");
    }

    public JaxbService.IsisSchemas default1Act() {
        return JaxbService.IsisSchemas.IGNORE;
    }

    private static String zipEntryNameFor(final String namespaceUri) {
        return Util.withSuffix(namespaceUri, "xsd");
    }


    @javax.inject.Inject
    MessageService messageService;

    @javax.inject.Inject
    JaxbService jaxbService;
}


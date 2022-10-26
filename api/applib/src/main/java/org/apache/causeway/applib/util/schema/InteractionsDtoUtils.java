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
package org.apache.causeway.applib.util.schema;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.causeway.applib.util.JaxbUtil;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.InteractionsDto;

import lombok.val;

/**
 * @since 1.x {@index}
 */
public final class InteractionsDtoUtils {

    public static void init() {
        getJaxbContext();
    }


    // -- marshalling

    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            jaxbContext = JaxbUtil.jaxbContextFor(InteractionsDto.class);
        }
        return jaxbContext;
    }


    public static InteractionsDto fromXml(final Reader reader) {
        try {
            final Unmarshaller un = getJaxbContext().createUnmarshaller();
            return (InteractionsDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static InteractionsDto fromXml(final String xml) {
        return fromXml(new StringReader(xml));
    }

    public static InteractionsDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {

        final String s = _Resources.loadAsString(contextClass, resourceName, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final InteractionsDto interactionDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(interactionDto, caw);
        return caw.toString();
    }

    public static void toXml(final InteractionsDto interactionsDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(interactionsDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }



    // -- other

    public static List<InteractionDto> split(InteractionsDto interactionsDto) {
        List<InteractionDto> interactionDtos = new ArrayList<>();
        interactionsDto.getInteractionDto().forEach(interactionDto -> {
            copyVersion(interactionsDto, interactionDto);
            interactionDtos.add(interactionDto);
        });
        return interactionDtos;
    }

    private static void copyVersion(
            final InteractionsDto from,
            final InteractionDto to) {
        val majorVersion = from.getMajorVersion();
        val minorVersion = from.getMinorVersion();
        if (!_Strings.isNullOrEmpty(majorVersion) && !_Strings.isNullOrEmpty(minorVersion)) {
            to.setMajorVersion(majorVersion);
            to.setMinorVersion(minorVersion);
        }
    }

    public static InteractionsDto join(
            final List<InteractionDto> interactionDtos) {
        val interactionsDto = new InteractionsDto();
        interactionDtos.forEach(interactionDto -> {
            copyVersion(interactionDto, interactionsDto);
            interactionsDto.getInteractionDto().add(interactionDto);
        });
        return interactionsDto;
    }

    private static void copyVersion(
            final InteractionDto from,
            final InteractionsDto dto) {
        val majorVersion = from.getMajorVersion();
        val minorVersion = from.getMinorVersion();
        if (!_Strings.isNullOrEmpty(majorVersion) && !_Strings.isNullOrEmpty(minorVersion)) {
            dto.setMajorVersion(majorVersion);
            dto.setMinorVersion(minorVersion);
        }
        from.setMajorVersion(null);
        from.setMinorVersion(null);
    }


}

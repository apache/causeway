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
package org.apache.isis.applib.util.schema;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.ParamsDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;
import org.apache.isis.schema.ixn.v2.InteractionDto;
import org.apache.isis.schema.ixn.v2.InteractionsDto;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

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

    public static InteractionsDto combine(List<InteractionDto> interactionDtos) {
        InteractionsDto dto = new InteractionsDto();
        interactionDtos.forEach(x -> {
            promoteVersionToTop(dto, x);
            dto.getInteractionDto().add(x);
        });
        return dto;
    }

    private static void promoteVersionToTop(InteractionsDto dto, InteractionDto x) {
        val majorVersion = x.getMajorVersion();
        val minorVersion = x.getMinorVersion();
        if (!_Strings.isNullOrEmpty(majorVersion) && !_Strings.isNullOrEmpty(minorVersion)) {
            dto.setMajorVersion(majorVersion);
            dto.setMinorVersion(minorVersion);
        }
        x.setMajorVersion(null);
        x.setMinorVersion(null);
    }


}

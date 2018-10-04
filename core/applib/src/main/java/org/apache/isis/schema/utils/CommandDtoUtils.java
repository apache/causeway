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
package org.apache.isis.schema.utils;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.MapDto;
import org.apache.isis.schema.cmd.v1.ParamsDto;
import org.apache.isis.schema.common.v1.OidsDto;
import org.apache.isis.schema.common.v1.PeriodDto;

public final class CommandDtoUtils {

    public static void init() {
        getJaxbContext();
    }

    // -- marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            jaxbContext = JaxbUtil.jaxbContextFor(CommandDto.class);
        }
        return jaxbContext;
    }

    public static CommandDto fromXml(final Reader reader) {
        try {
            final Unmarshaller un = getJaxbContext().createUnmarshaller();
            return (CommandDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static CommandDto fromXml(final String xml) {
        return fromXml(new StringReader(xml));
    }

    public static CommandDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {

        final String s = _Resources.loadAsString(contextClass, resourceName, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final CommandDto commandDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(commandDto, caw);
        return caw.toString();
    }

    public static void toXml(final CommandDto commandDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(commandDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }



    public static OidsDto targetsFor(final CommandDto dto) {
        OidsDto targets = dto.getTargets();
        if(targets == null) {
            targets = new OidsDto();
            dto.setTargets(targets);
        }
        return targets;
    }

    public static ParamsDto parametersFor(final ActionDto actionDto) {
        ParamsDto parameters = actionDto.getParameters();
        if(parameters == null) {
            parameters = new ParamsDto();
            actionDto.setParameters(parameters);
        }
        return parameters;
    }

    public static PeriodDto timingsFor(final CommandDto commandDto) {
        PeriodDto timings = commandDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            commandDto.setTimings(timings);
        }
        return timings;
    }

    public static String getUserData(final CommandDto dto, final String key) {
        if(dto == null || key == null) {
            return null;
        }
        return CommonDtoUtils.getMapValue(dto.getUserData(), key);
    }

    public static void setUserData(
            final CommandDto dto, final String key, final String value) {
        if(dto == null || key == null) {
            return;
        }
        final MapDto userData = userDataFor(dto);
        CommonDtoUtils.putMapKeyValue(userData, key, value);
    }

    private static MapDto userDataFor(final CommandDto commandDto) {
        MapDto userData = commandDto.getUserData();
        if(userData == null) {
            userData = new MapDto();
            commandDto.setUserData(userData);
        }
        return userData;
    }

}

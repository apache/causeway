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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.io.Resources;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandMementoDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;

public final class CommandMementoDtoUtils {

    //region > marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(CommandMementoDto.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return jaxbContext;
    }

    public static CommandMementoDto fromXml(final Reader reader) {
        try {
            final Unmarshaller un = getJaxbContext().createUnmarshaller();
            return (CommandMementoDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static CommandMementoDto fromXml(final String xml) {
        return fromXml(new StringReader(xml));
    }

    public static CommandMementoDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final CommandMementoDto aimDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(aimDto, caw);
        return caw.toString();
    }

    public static void toXml(final CommandMementoDto aimDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(aimDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    public static CommandMementoDto newDto(
            final UUID transactionId, final String user) {
        CommandMementoDto dto = new CommandMementoDto();

        dto.setMajorVersion("1");
        dto.setMinorVersion("0");

        dto.setTransactionId(transactionId.toString());

        ActionDto actionDto = new ActionDto();
        dto.setAction(actionDto);


        dto.setUser(user);

        return dto;
    }


    static ValueDto argumentFor(final ParamDto paramDto) {
        ValueDto valueDto = paramDto.getArgument();
        if(valueDto == null) {
            valueDto = new ValueDto();
            paramDto.setArgument(valueDto);
        }
        return valueDto;
    }

    /**
     *
     * @param params
     * @param parameterName
     * @param parameterType - to determine the value type (if any)
     * @param arg - either a value type (possibly boxed primitive), or a reference type
     * @param bookmarkService - used if not a value type
     */
    public static void addParamArg(
            final List<ParamDto> params,
            final String parameterName,
            final Class<?> parameterType,
            final Object arg,
            final BookmarkService bookmarkService) {
        boolean isValueType = addParamArgValue(params, parameterName, parameterType, arg);
        if(!isValueType) {
            addParamArgReference(
                    params, parameterName,
                    arg instanceof Bookmark
                            ? (Bookmark)arg
                            : bookmarkService.bookmarkFor(arg));
        }
    }

    private static boolean addParamArgValue(
            final List<ParamDto> params,
            final String parameterName,
            final Class<?> parameterType, final Object arg) {
        ParamDto paramDto = null;
        if(parameterType == String.class) {
            paramDto = newParamDto(params, parameterName, ValueType.STRING, arg);
        } else
        if(parameterType == byte.class || parameterType == Byte.class) {
            paramDto = newParamDto(params, parameterName, ValueType.BYTE, arg);
        } else
        if(parameterType == short.class || parameterType == Short.class) {
            paramDto = newParamDto(params, parameterName, ValueType.SHORT, arg);
        }else
        if(parameterType == int.class || parameterType == Integer.class) {
            paramDto = newParamDto(params, parameterName, ValueType.INT, arg);
        }else
        if(parameterType == long.class || parameterType == Long.class) {
            paramDto = newParamDto(params, parameterName, ValueType.LONG, arg);
        }else
        if(parameterType == char.class || parameterType == Character.class) {
            paramDto = newParamDto(params, parameterName, ValueType.CHAR, arg);
        }else
        if(parameterType == boolean.class || parameterType == Boolean.class) {
            paramDto = newParamDto(params, parameterName, ValueType.BOOLEAN, arg);
        }else
        if(parameterType == float.class || parameterType == Float.class) {
            paramDto = newParamDto(params, parameterName, ValueType.FLOAT, arg);
        }else
        if(parameterType == double.class || parameterType == Double.class) {
            paramDto = newParamDto(params, parameterName, ValueType.DOUBLE, arg);
        }else
        if(parameterType == BigInteger.class) {
            paramDto = newParamDto(params, parameterName, ValueType.BIG_INTEGER, arg);
        }else
        if(parameterType == BigDecimal.class) {
            paramDto = newParamDto(params, parameterName, ValueType.BIG_DECIMAL, arg);
        }else
        if(parameterType == DateTime.class) {
            paramDto = newParamDto(params, parameterName, ValueType.JODA_DATE_TIME, arg);
        }else
        if(parameterType == LocalDateTime.class) {
            paramDto =
                    newParamDto(params, parameterName, ValueType.JODA_LOCAL_DATE_TIME, arg);
        }else
        if(parameterType == LocalDate.class) {
            paramDto = newParamDto(params, parameterName, ValueType.JODA_LOCAL_DATE, arg);
        }else
        if(parameterType == LocalTime.class) {
            paramDto = newParamDto(params, parameterName, ValueType.JODA_LOCAL_TIME, arg);
        }

        if(paramDto != null) {
            final ValueDto valueDto = argumentFor(paramDto);

            CommonDtoUtils.setValue(valueDto, parameterType, arg);
            return true;
        }

        // none of the supported value types
        return false;
    }

    private static void addParamArgReference(
            final List<ParamDto> params,
            final String parameterName,
            final Bookmark bookmark) {
        final ParamDto paramDto =
                newParamDto(params, parameterName, ValueType.REFERENCE, bookmark);
        final ValueDto valueDto = argumentFor(paramDto);
        OidDto argValue = CommonDtoUtils.asOidDto(bookmark);
        valueDto.setReference(argValue);
    }

    private static ParamDto newParamDto(
            final List<ParamDto> params,
            final String parameterName,
            final ValueType parameterType,
            final Object value) {
        final ParamDto paramDto = newParamDto(parameterName, parameterType);
        paramDto.setNull(value == null);
        params.add(paramDto);
        return paramDto;
    }

    private static ParamDto newParamDto(
            final String parameterName,
            final ValueType parameterType) {
        final ParamDto argDto = new ParamDto();
        argDto.setParameterName(parameterName);
        argDto.setParameterType(parameterType);
        return argDto;
    }

    public static Object paramArgOf(final ParamDto paramDto) {
        if(paramDto.isNull()) {
            return null;
        }
        final ValueType parameterType = paramDto.getParameterType();
        final ValueDto argument = paramDto.getArgument();
        return CommonDtoUtils.getValue(argument, parameterType);
    }

}

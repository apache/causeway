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
import org.apache.isis.schema.cmd.v1.CommandDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;

public final class CommandDtoUtils {

    //region > marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(CommandDto.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
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
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final CommandDto aimDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(aimDto, caw);
        return caw.toString();
    }

    public static void toXml(final CommandDto aimDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(aimDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion


    static ValueDto argumentFor(final ParamDto paramDto) {
        ValueDto valueDto = paramDto.getArgument();
        if(valueDto == null) {
            valueDto = new ValueDto();
            paramDto.setArgument(valueDto);
        }
        return valueDto;
    }

    /**
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

        ParamDto paramDto = newParamDto(parameterName, parameterType, arg, bookmarkService);
        params.add(paramDto);
    }

    public static ParamDto newParamDto(
            final String parameterName,
            final Class<?> parameterType,
            final Object arg,
            final BookmarkService bookmarkService) {

        ParamDto paramDto = newParamDto(parameterName, parameterType, arg);
        if(paramDto != null) {

            if (arg != null) {
                final ValueDto valueDto = argumentFor(paramDto);
                CommonDtoUtils.setValue(valueDto, parameterType, arg);
            }

        } else {

            // none of the supported value types
            final Bookmark bookmark = arg instanceof Bookmark
                    ? (Bookmark)arg
                    : bookmarkService.bookmarkFor(arg);

            paramDto = newParamDto(parameterName, ValueType.REFERENCE, bookmark);

            if (bookmark != null) {
                final ValueDto valueDto = argumentFor(paramDto);

                OidDto argValue = CommonDtoUtils.asOidDto(bookmark);
                valueDto.setReference(argValue);
            }
        }
        return paramDto;
    }

    private static ParamDto newParamDto(final String parameterName, final Class<?> parameterType, final Object arg) {
        ParamDto paramDto = null;
        if(parameterType == String.class) {
            paramDto = newParamDto(parameterName, ValueType.STRING, arg);
        } else
        if(parameterType == byte.class || parameterType == Byte.class) {
            paramDto = newParamDto(parameterName, ValueType.BYTE, arg);
        } else
        if(parameterType == short.class || parameterType == Short.class) {
            paramDto = newParamDto(parameterName, ValueType.SHORT, arg);
        }else
        if(parameterType == int.class || parameterType == Integer.class) {
            paramDto = newParamDto(parameterName, ValueType.INT, arg);
        }else
        if(parameterType == long.class || parameterType == Long.class) {
            paramDto = newParamDto(parameterName, ValueType.LONG, arg);
        }else
        if(parameterType == char.class || parameterType == Character.class) {
            paramDto = newParamDto(parameterName, ValueType.CHAR, arg);
        }else
        if(parameterType == boolean.class || parameterType == Boolean.class) {
            paramDto = newParamDto(parameterName, ValueType.BOOLEAN, arg);
        }else
        if(parameterType == float.class || parameterType == Float.class) {
            paramDto = newParamDto(parameterName, ValueType.FLOAT, arg);
        }else
        if(parameterType == double.class || parameterType == Double.class) {
            paramDto = newParamDto(parameterName, ValueType.DOUBLE, arg);
        }else
        if(parameterType == BigInteger.class) {
            paramDto = newParamDto(parameterName, ValueType.BIG_INTEGER, arg);
        }else
        if(parameterType == BigDecimal.class) {
            paramDto = newParamDto(parameterName, ValueType.BIG_DECIMAL, arg);
        }else
        if(parameterType == DateTime.class) {
            paramDto = newParamDto(parameterName, ValueType.JODA_DATE_TIME, arg);
        }else
        if(parameterType == LocalDateTime.class) {
            paramDto = newParamDto(parameterName, ValueType.JODA_LOCAL_DATE_TIME, arg);
        }else
        if(parameterType == LocalDate.class) {
            paramDto = newParamDto(parameterName, ValueType.JODA_LOCAL_DATE, arg);
        }else
        if(parameterType == LocalTime.class) {
            paramDto = newParamDto(parameterName, ValueType.JODA_LOCAL_TIME, arg);
        }else
        if(parameterType.isEnum()) {
            paramDto = newParamDto(parameterName, ValueType.ENUM, arg);
        }
        return paramDto;
    }

    private static ParamDto newParamDto(final String parameterName, final ValueType parameterType, final Object value) {
        final ParamDto paramDto = newParamDto(parameterName, parameterType);
        paramDto.setNull(value == null);
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

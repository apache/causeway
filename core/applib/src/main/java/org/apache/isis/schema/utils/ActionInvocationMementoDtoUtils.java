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
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.schema.aim.v2.ActionInvocationDto;
import org.apache.isis.schema.aim.v2.ActionInvocationMementoDto;
import org.apache.isis.schema.aim.v2.ReturnDto;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.PeriodDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

public final class ActionInvocationMementoDtoUtils {

    //region > marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(ActionInvocationMementoDto.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return jaxbContext;
    }

    public static ActionInvocationMementoDto fromXml(final Reader reader) {
        try {
            final Unmarshaller un = getJaxbContext().createUnmarshaller();
            return (ActionInvocationMementoDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static ActionInvocationMementoDto fromXml(final String xml) {
        return fromXml(new StringReader(xml));
    }

    public static ActionInvocationMementoDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final ActionInvocationMementoDto aimDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(aimDto, caw);
        return caw.toString();
    }

    public static void toXml(final ActionInvocationMementoDto aimDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(aimDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region > actionIdentifier, target

    public static ActionInvocationMementoDto newDto(
            final UUID transactionId,
            final int sequence,
            final String actionIdentifier,
            final Bookmark targetBookmark,
            final String title,
            final String user,
            final Timestamp timestamp) {

        final ActionInvocationMementoDto aim = new ActionInvocationMementoDto();

        aim.setMajorVersion("2");
        aim.setMinorVersion("0");

        aim.setTransactionId(transactionId.toString());

        ActionInvocationDto invocation = invocationFor(aim);

        invocation.setSequence(sequence);
        invocation.setId(aim.getTransactionId() + "." + invocation.getSequence());

        final OidDto target = new OidDto();
        target.setObjectType(targetBookmark.getObjectType());
        target.setObjectIdentifier(target.getObjectIdentifier());
        invocation.setTarget(target);

        invocation.setTitle(title);
        invocation.setUser(user);

        ActionDto action = actionFor(invocation);
        action.setActionIdentifier(actionIdentifier);

        PeriodDto timings = timingsFor(invocation);
        timings.setStart(JavaSqlTimestampXmlGregorianCalendarAdapter.print(timestamp));

        return aim;
    }

    //endregion

    //region > invocationFor, actionFor, timingsFor

    private static ActionInvocationDto invocationFor(
            final ActionInvocationMementoDto aim) {
        ActionInvocationDto invocation = aim.getInvocation();
        if(invocation == null) {
            invocation = new ActionInvocationDto();
            aim.setInvocation(invocation);
        }
        return invocation;
    }

    private static ActionDto actionFor(final ActionInvocationDto invocation) {
        ActionDto action = invocation.getAction();
        if(action == null) {
            action = new ActionDto();
            invocation.setAction(action);
        }
        return action;
    }

    private static List<ParamDto> parametersFor(final ActionInvocationMementoDto aim) {
        return parametersFor(invocationFor(aim));
    }

    private static List<ParamDto> parametersFor(final ActionInvocationDto invocationDto) {
        final ActionDto actionDto = actionFor(invocationDto);
        return actionDto.getParameters();
    }

    private static PeriodDto timingsFor(final ActionInvocationDto invocation) {
        PeriodDto timings = invocation.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            invocation.setTimings(timings);
        }
        return timings;
    }

    //endregion

    //region > addParamArg

    public static void addParamArg(
            final ActionInvocationMementoDto aim,
            final String parameterName,
            final Class<?> parameterType,
            final Object arg,
            final BookmarkService bookmarkService) {

        final List<ParamDto> params = parametersFor(aim);
        CommandMementoDtoUtils.addParamArg(params, parameterName, parameterType, arg, bookmarkService);
    }

    //region > addReturn

    /**
     *
     * @param aim
     * @param returnType - to determine the value type (if any)
     * @param result - either a value type (possibly boxed primitive), or a reference type
     * @param bookmarkService - used if not a value type
     */
    public static void addReturn(
            final ActionInvocationMementoDto aim,
            final Class<?> returnType,
            final Object result,
            final BookmarkService bookmarkService) {
        boolean isValueType = ActionInvocationMementoDtoUtils.addReturnValue(aim, returnType, result);
        if(!isValueType) {
            ActionInvocationMementoDtoUtils.addReturnReference(aim, bookmarkService.bookmarkFor(result));
        }
    }

    public static boolean addReturnValue(
            final ActionInvocationMementoDto aim,
            final Class<?> returnType,
            final Object returnVal) {
        final ReturnDto returnDto = returnValueDtoFor(aim);
        return setValue(returnDto, returnType, returnVal);
    }

    public static void addReturnReference(
            final ActionInvocationMementoDto aim,
            final Bookmark bookmark) {
        final ReturnDto returnedDto = returnValueDtoFor(aim);
        OidDto oidDto = CommonDtoUtils.asOidDto(bookmark);
        ValueDto value = new ValueDto();
        value.setReference(oidDto);
        returnedDto.setValue(value);
        returnedDto.setReturnType(ValueType.REFERENCE);
    }

    private static ReturnDto returnValueDtoFor(final ActionInvocationMementoDto aim) {
        ActionInvocationDto invocationDto = invocationFor(aim);
        ReturnDto returned = invocationDto.getReturned();
        if(returned == null) {
            returned = new ReturnDto();
            invocationDto.setReturned(returned);
        }
        return returned;
    }

    //endregion


    //region > getParameters, getParameterNames, getParameterTypes
    public static List<ParamDto> getParameters(final ActionInvocationMementoDto aim) {
        final List<ParamDto> params = parametersFor(aim);
        final int parameterNumber = getNumberOfParameters(aim);
        final List<ParamDto> paramDtos = Lists.newArrayList();
        for (int i = 0; i < parameterNumber; i++) {
            final ParamDto paramDto = params.get(i);
            paramDtos.add(paramDto);
        }
        return Collections.unmodifiableList(paramDtos);
    }

    private static int getNumberOfParameters(final ActionInvocationMementoDto aim) {
        final List<ParamDto> params = parametersFor(aim);
        return params != null ? params.size() : 0;
    }

    public static List<String> getParameterNames(final ActionInvocationMementoDto aim) {
        return immutableList(Iterables.transform(getParameters(aim), CommonDtoUtils.PARAM_DTO_TO_NAME));
    }
    public static List<ValueType> getParameterTypes(final ActionInvocationMementoDto aim) {
        return immutableList(Iterables.transform(getParameters(aim), CommonDtoUtils.PARAM_DTO_TO_TYPE));
    }
    //endregion

    //region > getParameter, getParameterName, getParameterType
    public static ParamDto getParameter(final ActionInvocationMementoDto aim, final int paramNum) {
        final int parameterNumber = getNumberOfParameters(aim);
        if(paramNum > parameterNumber) {
            throw new IllegalArgumentException(String.format("No such parameter %d (the memento has %d parameters)", paramNum, parameterNumber));
        }
        final List<ParamDto> parameters = getParameters(aim);
        return parameters.get(paramNum);
    }

    public static ValueDto getParameterArg(final ActionInvocationMementoDto aim, final int paramNum) {
        final ParamDto paramDto = getParameter(aim, paramNum);
        return CommandMementoDtoUtils.argumentFor(paramDto);
    }


    public static String getParameterName(final ActionInvocationMementoDto aim, final int paramNum) {
        return CommonDtoUtils.PARAM_DTO_TO_NAME.apply(getParameter(aim, paramNum));
    }
    public static ValueType getParameterType(final ActionInvocationMementoDto aim, final int paramNum) {
        return CommonDtoUtils.PARAM_DTO_TO_TYPE.apply(getParameter(aim, paramNum));
    }
    public static boolean isNull(final ActionInvocationMementoDto aim, int paramNum) {
        final ParamDto paramDto = getParameter(aim, paramNum);
        return paramDto.isNull();
    }
    //endregion

    //region > getArg
    public static <T> T getArg(final ActionInvocationMementoDto aim, int paramNum, Class<T> cls) {
        final ParamDto paramDto = getParameter(aim, paramNum);
        if(paramDto.isNull()) {
            return null;
        }
        final ValueDto valueDto = CommandMementoDtoUtils.argumentFor(paramDto);
        final ValueType parameterType = paramDto.getParameterType();
        return CommonDtoUtils.getValue(valueDto, parameterType);
    }

    //endregion


    private static <T> List<T> immutableList(final Iterable<T> iterable) {
        return Collections.unmodifiableList(Lists.newArrayList(iterable));
    }

    private static boolean setValue(
            final ReturnDto returnDto,
            final Class<?> type,
            final Object val) {
        ValueDto valueDto = new ValueDto();
        returnDto.setValue(valueDto);
        setValueType(returnDto, type);
        return CommonDtoUtils.setValue(valueDto, type, val);
    }

    private static boolean setValueType(
            final ReturnDto returnDto,
            final Class<?> type) {
        if(type == String.class) {
            returnDto.setReturnType(ValueType.STRING);
        } else
        if(type == byte.class || type == Byte.class) {
            returnDto.setReturnType(ValueType.BYTE);
        } else
        if(type == short.class || type == Short.class) {
            returnDto.setReturnType(ValueType.SHORT);
        }else
        if(type == int.class || type == Integer.class) {
            returnDto.setReturnType(ValueType.INT);
        }else
        if(type == long.class || type == Long.class) {
            returnDto.setReturnType(ValueType.LONG);
        }else
        if(type == char.class || type == Character.class) {
            returnDto.setReturnType(ValueType.CHAR);
        }else
        if(type == boolean.class || type == Boolean.class) {
            returnDto.setReturnType(ValueType.BOOLEAN);
        }else
        if(type == float.class || type == Float.class) {
            returnDto.setReturnType(ValueType.FLOAT);
        }else
        if(type == double.class || type == Double.class) {
            returnDto.setReturnType(ValueType.DOUBLE);
        }else
        if(type == BigInteger.class) {
            returnDto.setReturnType(ValueType.BIG_INTEGER);
        }else
        if(type == BigDecimal.class) {
            returnDto.setReturnType(ValueType.BIG_DECIMAL);
        }else
        if(type == DateTime.class) {
            returnDto.setReturnType(ValueType.JODA_DATE_TIME);
        }else
        if(type == LocalDateTime.class) {
            returnDto.setReturnType(ValueType.JODA_LOCAL_DATE_TIME);
        }else
        if(type == LocalDate.class) {
            returnDto.setReturnType(ValueType.JODA_LOCAL_DATE);
        }else
        if(type == LocalTime.class) {
            returnDto.setReturnType(ValueType.JODA_LOCAL_TIME);
        }else
        {
            // none of the supported value types
            return false;
        }
        return true;
    }


    //endregion

    //region > debugging
    public static void dump(final ActionInvocationMementoDto aim, final PrintStream out) throws JAXBException {
        out.println(toXml(aim));
    }
    //endregion


}

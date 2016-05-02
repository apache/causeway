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
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.PeriodDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.mim.v1.ActionInvocationDto;
import org.apache.isis.schema.mim.v1.MemberInteractionDto;
import org.apache.isis.schema.mim.v1.MemberInteractionMementoDto;
import org.apache.isis.schema.mim.v1.PropertyModificationDto;
import org.apache.isis.schema.mim.v1.ReturnDto;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;

public final class MemberInteractionMementoDtoUtils {

    //region > marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(MemberInteractionMementoDto.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return jaxbContext;
    }

    public static MemberInteractionMementoDto fromXml(final Reader reader) {
        try {
            final Unmarshaller un = getJaxbContext().createUnmarshaller();
            return (MemberInteractionMementoDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static MemberInteractionMementoDto fromXml(final String xml) {
        return fromXml(new StringReader(xml));
    }

    public static MemberInteractionMementoDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final MemberInteractionMementoDto aimDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(aimDto, caw);
        return caw.toString();
    }

    public static void toXml(final MemberInteractionMementoDto aimDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(aimDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region > newDto

    enum Type {
        ACTION_INVOCATION,
        PROPERTY_MODIFICATION
    }

    /**
     *
     * @param newValueDto - will be null if clearing the property
     */
    public static MemberInteractionMementoDto newPropertyDto(
            final UUID transactionId,
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String propertyIdentifier,
            final ParamDto newValueDto,
            final String user,
            final Timestamp startedAt, final Timestamp completedAt
    ) {

        return newDto(Type.PROPERTY_MODIFICATION, transactionId, sequence, targetBookmark, targetTitle,
                propertyIdentifier, null, null, newValueDto,
                user, startedAt, completedAt);

    }

    public static MemberInteractionMementoDto newActionDto(
            final UUID transactionId,
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String actionIdentifier,
            final List<ParamDto> parameterDtos,
            final ReturnDto returnDto,
            final String user,
            final Timestamp startedAt, final Timestamp completedAt) {

        return newDto(Type.ACTION_INVOCATION, transactionId, sequence, targetBookmark, targetTitle, actionIdentifier,
                parameterDtos, returnDto, null,
                user, startedAt, completedAt);
    }

    /**
     * @param parameterDtos - populated only for actions
     * @param returnDto     - populated only for actions (could be null)
     * @param newValueDto   - populated only for property modificaitons
     */
    private static MemberInteractionMementoDto newDto(
            final Type type,
            final UUID transactionId,
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String memberIdentifier,
            final List<ParamDto> parameterDtos,
            final ReturnDto returnDto,
            final ParamDto newValueDto,
            final String user,
            final Timestamp startedAt, final Timestamp completedAt) {
        final MemberInteractionMementoDto mim = new MemberInteractionMementoDto();

        mim.setMajorVersion("1");
        mim.setMinorVersion("0");

        mim.setTransactionId(transactionId.toString());

        final MemberInteractionDto memberInteraction;
        if(type == Type.ACTION_INVOCATION) {

            final ActionInvocationDto invocation = actionInvocationFor(mim);
            final ActionInvocationDto.Parameters parameters = invocation.getParameters();
            parameters.getParameter().addAll(parameterDtos);
            invocation.setReturned(returnDto);

            memberInteraction = invocation;
        } else {
            final PropertyModificationDto modification = propertyModificationFor(mim);
            // modification.

            memberInteraction = modification;
        }

        memberInteraction.setSequence(sequence);
        memberInteraction.setId(mim.getTransactionId() + "." + sequence);

        final OidDto target = new OidDto();
        target.setObjectType(targetBookmark.getObjectType());
        target.setObjectIdentifier(target.getObjectIdentifier());
        memberInteraction.setTarget(target);

        memberInteraction.setTitle(targetTitle);
        memberInteraction.setUser(user);

        memberInteraction.setMemberIdentifier(memberIdentifier);

        final PeriodDto timings = timingsFor(memberInteraction);
        timings.setStart(JavaSqlTimestampXmlGregorianCalendarAdapter.print(startedAt));
        timings.setComplete(JavaSqlTimestampXmlGregorianCalendarAdapter.print(completedAt));

        return mim;
    }

    //endregion

    //region > invocationFor, actionFor, timingsFor

    private static ActionInvocationDto actionInvocationFor(
            final MemberInteractionMementoDto mim) {
        ActionInvocationDto invocation = (ActionInvocationDto) mim.getInteraction();
        if(invocation == null) {
            invocation = new ActionInvocationDto();
            mim.setInteraction(invocation);
        }
        return invocation;
    }

    private static PropertyModificationDto propertyModificationFor(
            final MemberInteractionMementoDto mim) {
        PropertyModificationDto modification = (PropertyModificationDto) mim.getInteraction();
        if(modification == null) {
            modification = new PropertyModificationDto();
            mim.setInteraction(modification);
        }
        return modification;
    }

    private static List<ParamDto> parametersFor(final MemberInteractionMementoDto mim) {
        return parametersFor(actionInvocationFor(mim));
    }

    private static List<ParamDto> parametersFor(final ActionInvocationDto invocationDto) {
        return invocationDto.getParameters().getParameter();
    }

    private static PeriodDto timingsFor(final MemberInteractionDto invocation) {
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
            final MemberInteractionMementoDto mim,
            final String parameterName,
            final Class<?> parameterType,
            final Object arg,
            final BookmarkService bookmarkService) {

        final List<ParamDto> params = parametersFor(mim);
        CommandMementoDtoUtils.addParamArg(params, parameterName, parameterType, arg, bookmarkService);
    }

    //region > addReturn

    /**
     *
     * @param mim
     * @param returnType - to determine the value type (if any)
     * @param result - either a value type (possibly boxed primitive), or a reference type
     * @param bookmarkService - used if not a value type
     */
    public static void addReturn(
            final MemberInteractionMementoDto mim,
            final Class<?> returnType,
            final Object result,
            final BookmarkService bookmarkService) {
        boolean isValueType = MemberInteractionMementoDtoUtils.addReturnValue(mim, returnType, result);
        if(!isValueType) {
            MemberInteractionMementoDtoUtils.addReturnReference(mim, bookmarkService.bookmarkFor(result));
        }
    }

    public static boolean addReturnValue(
            final MemberInteractionMementoDto mim,
            final Class<?> returnType,
            final Object returnVal) {
        final ReturnDto returnDto = returnValueDtoFor(mim);
        return setValue(returnDto, returnType, returnVal);
    }

    public static void addReturnReference(
            final MemberInteractionMementoDto aim,
            final Bookmark bookmark) {
        final ReturnDto returnedDto = returnValueDtoFor(aim);
        OidDto oidDto = CommonDtoUtils.asOidDto(bookmark);
        ValueDto value = new ValueDto();
        value.setReference(oidDto);
        returnedDto.setValue(value);
        returnedDto.setReturnType(ValueType.REFERENCE);
    }

    private static ReturnDto returnValueDtoFor(final MemberInteractionMementoDto mim) {
        ActionInvocationDto invocationDto = actionInvocationFor(mim);
        ReturnDto returned = invocationDto.getReturned();
        if(returned == null) {
            returned = new ReturnDto();
            invocationDto.setReturned(returned);
        }
        return returned;
    }

    //endregion


    //region > getParameters, getParameterNames, getParameterTypes
    public static List<ParamDto> getParameters(final ActionInvocationDto ai) {
        final List<ParamDto> params = parametersFor(ai);
        final int parameterNumber = getNumberOfParameters(ai);
        final List<ParamDto> paramDtos = Lists.newArrayList();
        for (int i = 0; i < parameterNumber; i++) {
            final ParamDto paramDto = params.get(i);
            paramDtos.add(paramDto);
        }
        return Collections.unmodifiableList(paramDtos);
    }

    private static int getNumberOfParameters(final ActionInvocationDto ai) {
        final List<ParamDto> params = parametersFor(ai);
        return params != null ? params.size() : 0;
    }

    public static List<String> getParameterNames(final ActionInvocationDto ai) {
        return immutableList(Iterables.transform(getParameters(ai), CommonDtoUtils.PARAM_DTO_TO_NAME));
    }
    public static List<ValueType> getParameterTypes(final ActionInvocationDto ai) {
        return immutableList(Iterables.transform(getParameters(ai), CommonDtoUtils.PARAM_DTO_TO_TYPE));
    }
    //endregion

    //region > getParameter, getParameterName, getParameterType
    public static ParamDto getParameter(final ActionInvocationDto ai, final int paramNum) {
        final int parameterNumber = getNumberOfParameters(ai);
        if(paramNum > parameterNumber) {
            throw new IllegalArgumentException(String.format("No such parameter %d (the memento has %d parameters)", paramNum, parameterNumber));
        }
        final List<ParamDto> parameters = getParameters(ai);
        return parameters.get(paramNum);
    }

    public static ValueDto getParameterArg(final ActionInvocationDto ai, final int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return CommandMementoDtoUtils.argumentFor(paramDto);
    }


    public static String getParameterName(final ActionInvocationDto ai, final int paramNum) {
        return CommonDtoUtils.PARAM_DTO_TO_NAME.apply(getParameter(ai, paramNum));
    }
    public static ValueType getParameterType(final ActionInvocationDto ai, final int paramNum) {
        return CommonDtoUtils.PARAM_DTO_TO_TYPE.apply(getParameter(ai, paramNum));
    }
    public static boolean isNull(final ActionInvocationDto ai, int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.isNull();
    }
    //endregion

    //region > getArg
    public static <T> T getArg(final ActionInvocationDto ai, int paramNum, Class<T> cls) {
        final ParamDto paramDto = getParameter(ai, paramNum);
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

    public static boolean setValue(
            final ReturnDto returnDto,
            final Class<?> type,
            final Object val) {
        if(val == null) {
            returnDto.setNull(true);
            return true;
        } else {
            returnDto.setNull(false);
            final ValueDto valueDto = new ValueDto();
            returnDto.setValue(valueDto);
            setValueType(returnDto, type);
            return CommonDtoUtils.setValue(valueDto, type, val);
        }
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
    public static void dump(final MemberInteractionMementoDto mim, final PrintStream out) throws JAXBException {
        out.println(toXml(mim));
    }
    //endregion


}

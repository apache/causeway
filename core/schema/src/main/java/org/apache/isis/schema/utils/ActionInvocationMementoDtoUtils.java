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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.aim.v1.ActionInvocationMementoDto;
import org.apache.isis.schema.aim.v1.ParamDto;
import org.apache.isis.schema.common.v1.BookmarkObjectState;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.utils.jaxbadapters.JavaSqlTimestampXmlGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaDateTimeXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalDateTimeXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalDateXMLGregorianCalendarAdapter;
import org.apache.isis.schema.utils.jaxbadapters.JodaLocalTimeXMLGregorianCalendarAdapter;

public final class ActionInvocationMementoDtoUtils {

    //region > static
    private static final Function<ParamDto, String> PARAM_DTO_TO_NAME = new Function<ParamDto, String>() {
        @Override public String apply(final ParamDto input) {
            return input.getParameterName();
        }
    };
    private static final Function<ParamDto, ValueType> PARAM_DTO_TO_TYPE = new Function<ParamDto, ValueType>() {
        @Override public ValueType apply(final ParamDto input) {
            return input.getParameterType();
        }
    };
    private static JAXBContext jaxbContext;
    private static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(ActionInvocationMementoDto.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return jaxbContext;
    }
    //endregion

    public static ActionInvocationMementoDto newDto() {
        return new ActionInvocationMementoDto();
    }

    //region > actionIdentifier, target

    public static void setMetadata(
            final ActionInvocationMementoDto aim,
            final UUID transactionId,
            final int sequence,
            final Timestamp timestamp,
            final String targetClass,
            final String targetAction,
            final String actionIdentifier,
            final String targetObjectType,
            final String targetObjectIdentifier,
            final String title,
            final String user) {
        ActionInvocationMementoDto.Metadata metadata = aim.getMetadata();
        if(metadata == null) {
            metadata = new ActionInvocationMementoDto.Metadata();
            aim.setMetadata(metadata);
        }

        metadata.setTransactionId(transactionId.toString());
        metadata.setSequence(sequence);
        metadata.setTimestamp(JavaSqlTimestampXmlGregorianCalendarAdapter.print(timestamp));

        metadata.setTargetClass(targetClass);
        metadata.setTargetAction(targetAction);
        metadata.setActionIdentifier(actionIdentifier);

        final OidDto target = new OidDto();
        target.setObjectType(targetObjectType);
        target.setObjectIdentifier(targetObjectIdentifier);
        metadata.setTarget(target);

        metadata.setTitle(title);
        metadata.setUser(user);
   }


    //endregion

    //region > addArgValue, addArgReference
    public static boolean addArgValue(
            final ActionInvocationMementoDto aim,
            final String parameterName,
            final Class<?> parameterType,
            final Object arg) {

        ParamDto paramDto = null;
        if(parameterType == String.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.STRING, arg);
        } else
        if(parameterType == byte.class || parameterType == Byte.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.BYTE, arg);
        } else
        if(parameterType == short.class || parameterType == Short.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.SHORT, arg);
        }else
        if(parameterType == int.class || parameterType == Integer.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.INT, arg);
        }else
        if(parameterType == long.class || parameterType == Long.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.LONG, arg);
        }else
        if(parameterType == char.class || parameterType == Character.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.CHAR, arg);
        }else
        if(parameterType == boolean.class || parameterType == Boolean.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.BOOLEAN, arg);
        }else
        if(parameterType == float.class || parameterType == Float.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.FLOAT, arg);
        }else
        if(parameterType == double.class || parameterType == Double.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.DOUBLE, arg);
        }else
        if(parameterType == BigInteger.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.BIG_INTEGER, arg);
        }else
        if(parameterType == BigDecimal.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.BIG_DECIMAL, arg);
        }else
        if(parameterType == DateTime.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.JODA_DATE_TIME, arg);
        }else
        if(parameterType == LocalDateTime.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.JODA_LOCAL_DATE_TIME, arg);
        }else
        if(parameterType == LocalDate.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.JODA_LOCAL_DATE, arg);
        }else
        if(parameterType == LocalTime.class) {
            paramDto = newParamDto(aim, parameterName, ValueType.JODA_LOCAL_TIME, arg);
        }

        if(paramDto != null) {
            final ValueDto valueDto = valueDtoFor(paramDto);
            setValue(valueDto, parameterType, arg);
            return true;
        }

        // none of the supported value types
        return false;
    }


    public static void addArgReference(
            final ActionInvocationMementoDto aim,
            final String parameterName,
            final Bookmark bookmark) {
        final ParamDto paramDto = newParamDto(aim, parameterName, ValueType.REFERENCE, bookmark);
        final ValueDto valueDto = valueDtoFor(paramDto);
        OidDto argValue = asOidDto(bookmark);
        valueDto.setReference(argValue);
    }


    private static OidDto asOidDto(final Bookmark reference) {
        OidDto argValue;
        if(reference != null) {
            argValue = new OidDto();
            argValue.setObjectType(reference.getObjectType());
            argValue.setObjectState(bookmarkObjectStateOf(reference));
            argValue.setObjectIdentifier(reference.getIdentifier());
        } else {
            argValue = null;
        }
        return argValue;
    }

    private static BookmarkObjectState bookmarkObjectStateOf(final Bookmark reference) {
        switch(reference.getObjectState()) {
        case PERSISTENT: return BookmarkObjectState.PERSISTENT;
        case TRANSIENT: return BookmarkObjectState.TRANSIENT;
        case VIEW_MODEL: return BookmarkObjectState.VIEW_MODEL;
        }
        throw new IllegalArgumentException(String.format("reference.objectState '%s' not recognized", reference.getObjectState()));
    }

    private static ParamDto newParamDto(
            final ActionInvocationMementoDto aim,
            final String parameterName,
            final ValueType parameterType, final Object value) {
        final ActionInvocationMementoDto.Payload.Parameters params = getParametersHolderAutoCreate(aim);
        final ParamDto paramDto = newParamDto(parameterName, parameterType);
        paramDto.setNull(value == null);
        addParamNum(params, paramDto);
        return paramDto;
    }

    // lazily creates if required
    private static ValueDto valueDtoFor(final ParamDto paramDto) {
        ValueDto valueDto = paramDto.getValue();
        if(valueDto == null) {
            valueDto = new ValueDto();
        }
        paramDto.setValue(valueDto);
        return valueDto;
    }

    private static ParamDto newParamDto(final String parameterName, final ValueType parameterType) {
        final ParamDto argDto = new ParamDto();
        argDto.setParameterName(parameterName);
        argDto.setParameterType(parameterType);
        return argDto;
    }

    //endregion

    //region > addReturnValue, addReturnReference
    public static boolean addReturnValue(
            final ActionInvocationMementoDto aim,
            final Class<?> returnType,
            final Object returnVal) {
        final ValueDto valueDto = returnValueDtoFor(aim);
        return setValue(valueDto, returnType, returnVal);
    }

    public static void addReturnReference(
            final ActionInvocationMementoDto aim,
            final Bookmark bookmark) {
        final ValueDto valueDto = returnValueDtoFor(aim);
        OidDto argValue = asOidDto(bookmark);
        valueDto.setReference(argValue);
    }

    //endregion




    //region > getNumberOfParameters, getParameters, getParameterNames, getParameterTypes
    public static int getNumberOfParameters(final ActionInvocationMementoDto aim) {
        final ActionInvocationMementoDto.Payload.Parameters params = getParametersHolderElseThrow(aim);
        if(params == null) {
            return 0;
        }
        return params.getNum();
    }
    public static List<ParamDto> getParameters(final ActionInvocationMementoDto aim) {
        final ActionInvocationMementoDto.Payload.Parameters params = getParametersHolderElseThrow(aim);
        final int parameterNumber = getNumberOfParameters(aim);
        final List<ParamDto> paramDtos = Lists.newArrayList();
        for (int i = 0; i < parameterNumber; i++) {
            final ParamDto paramDto = params.getParam().get(i);
            paramDtos.add(paramDto);
        }
        return Collections.unmodifiableList(paramDtos);
    }
    public static List<String> getParameterNames(final ActionInvocationMementoDto aim) {
        return immutableList(Iterables.transform(getParameters(aim), PARAM_DTO_TO_NAME));
    }
    public static List<ValueType> getParameterTypes(final ActionInvocationMementoDto aim) {
        return immutableList(Iterables.transform(getParameters(aim), PARAM_DTO_TO_TYPE));
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

    public static ValueDto getParameterValue(final ActionInvocationMementoDto aim, final int paramNum) {
        final ParamDto paramDto = getParameter(aim, paramNum);
        return valueDtoFor(paramDto);
    }


    public static String getParameterName(final ActionInvocationMementoDto aim, final int paramNum) {
        return PARAM_DTO_TO_NAME.apply(getParameter(aim, paramNum));
    }
    public static ValueType getParameterType(final ActionInvocationMementoDto aim, final int paramNum) {
        return PARAM_DTO_TO_TYPE.apply(getParameter(aim, paramNum));
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
        final ValueDto valueDto = valueDtoFor(paramDto);
        switch(paramDto.getParameterType()) {
        case STRING:
            return (T) valueDto.getString();
        case BYTE:
            return (T) valueDto.getByte();
        case SHORT:
            return (T) valueDto.getShort();
        case INT:
            return (T) valueDto.getInt();
        case LONG:
            return (T) valueDto.getLong();
        case FLOAT:
            return (T) valueDto.getFloat();
        case DOUBLE:
            return (T) valueDto.getDouble();
        case BOOLEAN:
            return (T) valueDto.isBoolean();
        case CHAR:
            final String aChar = valueDto.getChar();
            if(Strings.isNullOrEmpty(aChar)) { return null; }
            return (T) (Object)aChar.charAt(0);
        case BIG_DECIMAL:
            return (T) valueDto.getBigDecimal();
        case BIG_INTEGER:
            return (T) valueDto.getBigInteger();
        case JODA_DATE_TIME:
            return (T) JodaDateTimeXMLGregorianCalendarAdapter.parse(valueDto.getDateTime());
        case JODA_LOCAL_DATE:
            return (T) JodaLocalDateXMLGregorianCalendarAdapter.parse(valueDto.getLocalDate());
        case JODA_LOCAL_DATE_TIME:
            return (T) JodaLocalDateTimeXMLGregorianCalendarAdapter.parse(valueDto.getLocalDateTime());
        case JODA_LOCAL_TIME:
            return (T) JodaLocalTimeXMLGregorianCalendarAdapter.parse(valueDto.getLocalTime());
        case REFERENCE:
            return (T) valueDto.getReference();
        }
        throw new IllegalStateException("Parameter type was not recognised (possible bug)");
    }
    //endregion

    //region > marshalling
    public static ActionInvocationMementoDto fromXml(Reader reader) {
        Unmarshaller un = null;
        try {
            un = getJaxbContext().createUnmarshaller();
            return (ActionInvocationMementoDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static ActionInvocationMementoDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final ActionInvocationMementoDto aim) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(aim, caw);
        return caw.toString();
    }

    public static void toXml(final ActionInvocationMementoDto aim, final Writer writer) {
        Marshaller m = null;
        try {
            m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(aim, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region > debugging
    public static void dump(final ActionInvocationMementoDto aim, final PrintStream out) throws JAXBException {
        out.println(toXml(aim));
    }
    //endregion

    //region > helpers
    private static void addParamNum(final ActionInvocationMementoDto.Payload.Parameters params, final ParamDto arg) {
        params.getParam().add(arg);
        Integer num = params.getNum();
        if(num == null) {
            num = 0;
        }
        params.setNum(num +1);
    }

    private static ActionInvocationMementoDto.Payload.Parameters getParametersHolderElseThrow(final ActionInvocationMementoDto aim) {
        final ActionInvocationMementoDto.Payload payload = getPayloadElseThrow(aim);
        final ActionInvocationMementoDto.Payload.Parameters parameters = payload.getParameters();
        if(parameters == null) {
            throw new IllegalStateException("No parameters have been added");
        }
        return parameters;
    }

    private static ActionInvocationMementoDto.Payload.Parameters getParametersHolderAutoCreate(final ActionInvocationMementoDto aim) {
        final ActionInvocationMementoDto.Payload payload = getPayloadAutoCreate(aim);
        ActionInvocationMementoDto.Payload.Parameters params = payload.getParameters();
        if(params == null) {
            params = new ActionInvocationMementoDto.Payload.Parameters();
            payload.setParameters(params);
        }
        return params;
    }

    private static ValueDto returnValueDtoFor(final ActionInvocationMementoDto aim) {
        final ActionInvocationMementoDto.Payload payload = getPayloadAutoCreate(aim);
        ValueDto valueDto = payload.getReturn();
        if(valueDto == null) {
            valueDto = new ValueDto();
            payload.setReturn(valueDto);
        }
        return valueDto;
    }

    private static ActionInvocationMementoDto.Payload getPayloadAutoCreate(final ActionInvocationMementoDto aim) {
        ActionInvocationMementoDto.Payload payload = aim.getPayload();
        if(payload == null) {
            payload = new ActionInvocationMementoDto.Payload();
            aim.setPayload(payload);
        }
        return payload;
    }

    private static ActionInvocationMementoDto.Payload getPayloadElseThrow(final ActionInvocationMementoDto aim) {
        ActionInvocationMementoDto.Payload payload = aim.getPayload();
        if(payload == null) {
            throw new IllegalStateException("No payload has been added");
        }
        return payload;
    }

    private static <T> List<T> immutableList(final Iterable<T> transform) {
        return Collections.unmodifiableList(
                Lists.newArrayList(
                        transform
                )
        );
    }


    private static boolean setValue(final ValueDto valueDto, final Class<?> type, final Object returnVal) {
        if(type == String.class) {
            final String argValue = (String) returnVal;
            valueDto.setString(argValue);
        } else
        if(type == byte.class || type == Byte.class) {
            final Byte argValue = (Byte) returnVal;
            valueDto.setByte(argValue);
        } else
        if(type == short.class || type == Short.class) {
            final Short argValue = (Short) returnVal;
            valueDto.setShort(argValue);
        }else
        if(type == int.class || type == Integer.class) {
            final Integer argValue = (Integer) returnVal;
            valueDto.setInt(argValue);
        }else
        if(type == long.class || type == Long.class) {
            final Long argValue = (Long) returnVal;
            valueDto.setLong(argValue);
        }else
        if(type == char.class || type == Character.class) {
            final Character argValue = (Character) returnVal;
            valueDto.setChar("" + argValue);
        }else
        if(type == boolean.class || type == Boolean.class) {
            final Boolean argValue = (Boolean) returnVal;
            valueDto.setBoolean(argValue);
        }else
        if(type == float.class || type == Float.class) {
            final Float argValue = (Float) returnVal;
            valueDto.setFloat(argValue);
        }else
        if(type == double.class || type == Double.class) {
            final Double argValue = (Double) returnVal;
            valueDto.setDouble(argValue);
        }else
        if(type == BigInteger.class) {
            final BigInteger argValue = (BigInteger) returnVal;
            valueDto.setBigInteger(argValue);
        }else
        if(type == BigDecimal.class) {
            final BigDecimal argValue = (BigDecimal) returnVal;
            valueDto.setBigDecimal(argValue);
        }else
        if(type == DateTime.class) {
            final DateTime argValue = (DateTime) returnVal;
            valueDto.setDateTime(JodaDateTimeXMLGregorianCalendarAdapter.print(argValue));
        }else
        if(type == LocalDateTime.class) {
            final LocalDateTime argValue = (LocalDateTime) returnVal;
            valueDto.setLocalDateTime(JodaLocalDateTimeXMLGregorianCalendarAdapter.print(argValue));
        }else
        if(type == LocalDate.class) {
            final LocalDate argValue = (LocalDate) returnVal;
            valueDto.setLocalDate(JodaLocalDateXMLGregorianCalendarAdapter.print(argValue));
        }else
        if(type == LocalTime.class) {
            final LocalTime argValue = (LocalTime) returnVal;
            valueDto.setLocalTime(JodaLocalTimeXMLGregorianCalendarAdapter.print(argValue));
        }else
        {
            // none of the supported value types
            return false;
        }
        return true;
    }

    //endregion


}

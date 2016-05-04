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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.cmd.v1.ParamsDto;
import org.apache.isis.schema.common.v1.InteractionType;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.PeriodDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.common.v1.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v1.ActionInvocationDto;
import org.apache.isis.schema.ixn.v1.InteractionDto;
import org.apache.isis.schema.ixn.v1.MemberExecutionDto;
import org.apache.isis.schema.ixn.v1.PropertyEditDto;

public final class InteractionDtoUtils {

    //region > marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(InteractionDto.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return jaxbContext;
    }

    public static InteractionDto fromXml(final Reader reader) {
        try {
            final Unmarshaller un = getJaxbContext().createUnmarshaller();
            return (InteractionDto) un.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static InteractionDto fromXml(final String xml) {
        return fromXml(new StringReader(xml));
    }

    public static InteractionDto fromXml(
            final Class<?> contextClass,
            final String resourceName,
            final Charset charset) throws IOException {
        final URL url = Resources.getResource(contextClass, resourceName);
        final String s = Resources.toString(url, charset);
        return fromXml(new StringReader(s));
    }

    public static String toXml(final InteractionDto interactionDto) {
        final CharArrayWriter caw = new CharArrayWriter();
        toXml(interactionDto, caw);
        return caw.toString();
    }

    public static void toXml(final InteractionDto interactionDto, final Writer writer) {
        try {
            final Marshaller m = getJaxbContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(interactionDto, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region > newInteractionDto, newInteractionDtoWithActionExecution

    public static InteractionDto newInteractionDto(final String transactionId) {
        final InteractionDto interactionDto = new InteractionDto();

        interactionDto.setMajorVersion("1");
        interactionDto.setMinorVersion("0");

        interactionDto.setTransactionId(transactionId);
        return interactionDto;
    }

    public static InteractionDto newInteractionDtoWithActionInvocation(
            final String transactionId,
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String actionIdentifier,
            final List<ParamDto> parameterDtos,
            final String user) {

        final InteractionDto interactionDto = newInteractionDto(transactionId);

        final MemberExecutionDto executionDto = newActionInvocation(
                sequence, targetBookmark, targetTitle,
                actionIdentifier, parameterDtos,
                user, transactionId);

        addExecution(interactionDto, executionDto);

        return interactionDto;
    }

    public static InteractionDto newInteractionDtoWithPropertyModification(
            final String transactionId,
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String propertyIdentifier,
            final ValueWithTypeDto newValueDto,
            final String user
    ) {

        final InteractionDto interactionDto = newInteractionDto(transactionId);

        final MemberExecutionDto executionDto = newPropertyEdit(
                sequence, targetBookmark, targetTitle,
                propertyIdentifier, newValueDto,
                user, transactionId);

        addExecution(interactionDto, executionDto);

        return interactionDto;
    }

    //endregion

    //region > newActionInvocation, newPropertyModification

    public static ActionInvocationDto newActionInvocation(
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String actionIdentifier,
            final List<ParamDto> parameterDtos,
            final String user,
            final String transactionId) {

        return (ActionInvocationDto) newMemberExecutionDto(
                InteractionType.ACTION_INVOCATION, transactionId, sequence,
                targetBookmark, targetTitle, actionIdentifier,
                parameterDtos, null,
                user);
    }

    public static PropertyEditDto newPropertyEdit(
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String propertyIdentifier,
            final ValueWithTypeDto newValueDto,
            final String user,
            final String transactionId) {
        return (PropertyEditDto) newMemberExecutionDto(
                InteractionType.PROPERTY_EDIT, transactionId, sequence,
                targetBookmark, targetTitle, propertyIdentifier,
                null, newValueDto,
                user);
    }

    private static MemberExecutionDto newMemberExecutionDto(
            final InteractionType type,
            final String transactionId,
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String memberId,
            final List<ParamDto> parameterDtos,
            final ValueWithTypeDto newValueDto,
            final String user) {

        final MemberExecutionDto executionDto;
        if(type == InteractionType.ACTION_INVOCATION) {

            final ActionInvocationDto invocation = new ActionInvocationDto();
            final ParamsDto invocationParameters = parametersFor(invocation);
            invocation.setParameters(invocationParameters);
            invocationParameters.getParameter().addAll(parameterDtos);

            executionDto = invocation;
        } else {
            final PropertyEditDto edit = new PropertyEditDto();
            edit.setNewValue(newValueDto);

            executionDto = edit;
        }

        executionDto.setSequence(sequence);
        executionDto.setId(transactionId + "." + sequence);

        final OidDto target = new OidDto();
        target.setObjectType(targetBookmark.getObjectType());
        target.setObjectIdentifier(target.getObjectIdentifier());
        executionDto.setTarget(target);

        executionDto.setTitle(targetTitle);
        executionDto.setUser(user);

        executionDto.setMemberIdentifier(memberId);
        return executionDto;
    }

    //endregion

    //region > addExecution

    public static void addExecution(
            final InteractionDto interactionDto,
            final MemberExecutionDto executionDto) {
        interactionDto.setExecution(executionDto);

        executionDto.setInteractionType(
                executionDto instanceof ActionInvocationDto
                        ? InteractionType.ACTION_INVOCATION
                        : InteractionType.PROPERTY_EDIT);
    }


    //endregion

    //region > invocationFor, actionFor, timingsFor

    private static ActionInvocationDto actionInvocationFor(final InteractionDto interactionDto) {
        ActionInvocationDto invocation = (ActionInvocationDto) interactionDto.getExecution();
        if(invocation == null) {
            invocation = new ActionInvocationDto();
            interactionDto.setExecution(invocation);
            invocation.setInteractionType(InteractionType.ACTION_INVOCATION);
        }
        return invocation;
    }

    private static PropertyEditDto propertyEditFor(final InteractionDto interactionDto) {
        PropertyEditDto edit = (PropertyEditDto) interactionDto.getExecution();
        if(edit == null) {
            edit = new PropertyEditDto();
            interactionDto.setExecution(edit);
            edit.setInteractionType(InteractionType.PROPERTY_EDIT);
        }
        return edit;
    }

    private static List<ParamDto> parameterListFor(final InteractionDto ixnDto) {
        return parameterListFor(actionInvocationFor(ixnDto));
    }

    private static ParamsDto parametersFor(final ActionInvocationDto invocationDto) {
        ParamsDto parameters = invocationDto.getParameters();
        if(parameters == null) {
            parameters = new ParamsDto();
            invocationDto.setParameters(parameters);
        }
        return parameters;
    }

    private static List<ParamDto> parameterListFor(final ActionInvocationDto invocationDto) {
        return parametersFor(invocationDto).getParameter();
    }

    private static PeriodDto timingsFor(final MemberExecutionDto executionDto) {
        PeriodDto timings = executionDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            executionDto.setTimings(timings);
        }
        return timings;
    }

    //endregion

    //region > addParamArg

    public static void addParamArg(
            final InteractionDto interactionDto,
            final String parameterName,
            final Class<?> parameterType,
            final Object arg,
            final BookmarkService bookmarkService) {

        final List<ParamDto> params = parameterListFor(interactionDto);
        ParamDto paramDto = CommonDtoUtils.newParamDto(parameterName, parameterType, arg, bookmarkService);
        params.add(paramDto);
    }
    //endregion

    //region > addReturn

    /**
     *
     * @param returnType - to determine the value type (if any)
     * @param result - either a value type (possibly boxed primitive), or a reference type
     * @param bookmarkService - used if not a value type
     */
    public static void addReturn(
            final ActionInvocationDto invocationDto,
            final Class<?> returnType,
            final Object result, final BookmarkService bookmarkService) {
        final ValueWithTypeDto returned = CommonDtoUtils
                .newValueWithTypeDto(returnType, result, bookmarkService);
        invocationDto.setReturned(returned);
    }
    //endregion

    //region > getParameters, getParameterNames, getParameterTypes
    public static List<ParamDto> getParameters(final ActionInvocationDto ai) {
        final List<ParamDto> params = parameterListFor(ai);
        final int parameterNumber = getNumberOfParameters(ai);
        final List<ParamDto> paramDtos = Lists.newArrayList();
        for (int i = 0; i < parameterNumber; i++) {
            final ParamDto paramDto = params.get(i);
            paramDtos.add(paramDto);
        }
        return Collections.unmodifiableList(paramDtos);
    }

    private static int getNumberOfParameters(final ActionInvocationDto ai) {
        final List<ParamDto> params = parameterListFor(ai);
        return params != null ? params.size() : 0;
    }

    public static List<String> getParameterNames(final ActionInvocationDto ai) {
        return immutableList(Iterables.transform(getParameters(ai), CommonDtoUtils.PARAM_DTO_TO_NAME));
    }
    public static List<ValueType> getParameterTypes(final ActionInvocationDto ai) {
        return immutableList(Iterables.transform(getParameters(ai), CommonDtoUtils.PARAM_DTO_TO_TYPE));
    }

    private static <T> List<T> immutableList(final Iterable<T> iterable) {
        return Collections.unmodifiableList(Lists.newArrayList(iterable));
    }

    //endregion

    //region > getParameter, getParameterName, getParameterType, getParameterArgument
    public static ParamDto getParameter(final ActionInvocationDto ai, final int paramNum) {
        final int parameterNumber = getNumberOfParameters(ai);
        if(paramNum > parameterNumber) {
            throw new IllegalArgumentException(String.format("No such parameter %d (the memento has %d parameters)", paramNum, parameterNumber));
        }
        final List<ParamDto> parameters = getParameters(ai);
        return parameters.get(paramNum);
    }

    public static ValueDto getParameterArgument(final ActionInvocationDto ai, final int paramNum) {
        return getParameter(ai, paramNum);
    }

    public static String getParameterName(final ActionInvocationDto ai, final int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.getName();
    }

    public static ValueType getParameterType(final ActionInvocationDto ai, final int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.getType();
    }
    public static boolean isNull(final ActionInvocationDto ai, int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.isNull();
    }
    //endregion

    //region > getParameterArgValue
    public static <T> T getParameterArgValue(final ActionInvocationDto ai, int paramNum, Class<T> inferClass) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return CommonDtoUtils.getValue(paramDto);
    }
    public static <T> T getParameterArgValue(final ActionInvocationDto ai, int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return CommonDtoUtils.getValue(paramDto);
    }
    //endregion

    //region > debugging
    public static void dump(final InteractionDto ixnDto, final PrintStream out) throws JAXBException {
        out.println(toXml(ixnDto));
    }

    //endregion

}

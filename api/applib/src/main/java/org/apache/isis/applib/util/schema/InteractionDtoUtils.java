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
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.ParamsDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;
import org.apache.isis.schema.ixn.v2.InteractionDto;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

/**
 * @since 1.x {@index}
 */
public final class InteractionDtoUtils {

    public static void init() {
        getJaxbContext();
    }

    // -- marshalling
    static JAXBContext jaxbContext;
    static JAXBContext getJaxbContext() {
        if(jaxbContext == null) {
            jaxbContext = JaxbUtil.jaxbContextFor(InteractionDto.class);
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

        final String s = _Resources.loadAsString(contextClass, resourceName, charset);
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


    // -- newInteractionDto

    /**
     * Encapsulates the mechanism for obtaining a {@link MemberExecutionDto} DTO (XML memento)
     * of the provided in-memory
     * {@link Execution}.
     */
    public enum Strategy {
        FLAT {

            @Override
            public MemberExecutionDto dtoFor(final Execution<?, ?> execution) {
                return execution.getDto();
            }
        },
        DEEP {
            @Override
            public MemberExecutionDto dtoFor(final Execution<?, ?> execution) {
                return traverse(execution);
            }

            private MemberExecutionDto traverse(final Execution<?, ?> parentExecution) {

                final MemberExecutionDto parentDto = clone(parentExecution.getDto());

                final List<Execution<?, ?>> children = parentExecution.getChildren();
                for (Execution<?, ?> childExecution : children) {
                    final MemberExecutionDto childDto = clone(childExecution.getDto());
                    final MemberExecutionDto.ChildExecutions childExecutions =
                            InteractionDtoUtils.childExecutionsOf(parentDto);
                    childExecutions.getExecution().add(childDto);
                    traverse(childExecution);
                }

                return parentDto;
            }

            private MemberExecutionDto clone(final MemberExecutionDto memberExecutionDto) {
                return MemberExecutionDtoUtils.clone(memberExecutionDto);
            }
        };


        public abstract MemberExecutionDto dtoFor(final Execution<?, ?> execution);

    }

    private static MemberExecutionDto.ChildExecutions childExecutionsOf(final MemberExecutionDto dto) {
        MemberExecutionDto.ChildExecutions childExecutions = dto.getChildExecutions();
        if(childExecutions == null) {
            childExecutions = new MemberExecutionDto.ChildExecutions();
            dto.setChildExecutions(childExecutions);
        }
        return childExecutions;
    }

    /**
     * Creates a {@link InteractionDto} (serializable  to XML) for the provided
     * {@link Execution}
     * (the applib object).
     */
    public static InteractionDto newInteractionDto(final Execution<?, ?> execution) {
        return newInteractionDto(execution, Strategy.FLAT);
    }

    /**
     * Creates a {@link InteractionDto} (serializable  to XML) for the provided
     * {@link Execution}
     * (the applib object).
     */
    public static InteractionDto newInteractionDto(
            final Execution<?, ?> execution,
            final Strategy strategy) {

        final MemberExecutionDto memberExecutionDto = strategy.dtoFor(execution);
        return newInteractionDto(execution, memberExecutionDto);
    }

    private static InteractionDto newInteractionDto(
            final Execution<?, ?> execution,
            final MemberExecutionDto executionDto) {
        final Interaction interaction = execution.getInteraction();
        final String interactionId = interaction.getInteractionId().toString();

        return InteractionDtoUtils.newInteractionDto(interactionId, executionDto);
    }

    private static InteractionDto newInteractionDto(
            final String interactionId,
            final MemberExecutionDto executionDto) {
        final InteractionDto interactionDto = new InteractionDto();

        interactionDto.setMajorVersion("2");
        interactionDto.setMinorVersion("0");

        interactionDto.setInteractionId(interactionId);
        interactionDto.setExecution(executionDto);

        executionDto.setInteractionType(
                executionDto instanceof ActionInvocationDto
                    ? InteractionType.ACTION_INVOCATION
                    : InteractionType.PROPERTY_EDIT);

        return interactionDto;
    }

    // -- newActionInvocation, newPropertyModification

    public static ActionInvocationDto newActionInvocation(
            final int sequence,
            final Bookmark targetBookmark,
            final String targetTitle,
            final String actionIdentifier,
            final List<ParamDto> parameterDtos,
            final String user) {

        return (ActionInvocationDto) newMemberExecutionDto(
                InteractionType.ACTION_INVOCATION, sequence,
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
            final String user) {
        return (PropertyEditDto) newMemberExecutionDto(
                InteractionType.PROPERTY_EDIT, sequence,
                targetBookmark, targetTitle, propertyIdentifier,
                null, newValueDto,
                user);
    }

    private static MemberExecutionDto newMemberExecutionDto(
            final InteractionType type,
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

        final OidDto target = targetBookmark.toOidDto();
        executionDto.setTarget(target);

        executionDto.setTitle(targetTitle);
        executionDto.setUser(user);

        final String logicalMemberId = deriveLogicalMemberId(targetBookmark, memberId);
        executionDto.setLogicalMemberIdentifier(logicalMemberId);
        executionDto.setMemberIdentifier(memberId);
        return executionDto;
    }

    static String deriveLogicalMemberId(final Bookmark bookmark, final String memberId) {
        final String logicalTypeName = bookmark.getLogicalTypeName();
        final int hashAt = memberId.lastIndexOf("#");
        final String localMemberId = hashAt >= 0 && hashAt < memberId.length()
                ? memberId.substring(hashAt + 1)
                : memberId;
        return logicalTypeName + "#" + localMemberId;
    }

    // -- invocationFor, actionFor, timingsFor

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


    // -- getParameters, getParameterNames, getParameterTypes
    public static List<ParamDto> getParameters(final ActionInvocationDto ai) {
        final List<ParamDto> params = parameterListFor(ai);
        final int parameterNumber = getNumberOfParameters(ai);
        final List<ParamDto> paramDtos = _Lists.newArrayList();
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
        return Collections.unmodifiableList(
                _NullSafe.stream(getParameters(ai))
                .map(ParamDto::getName)
                .collect(Collectors.toList())
                );
    }
    public static List<ValueType> getParameterTypes(final ActionInvocationDto ai) {
        return Collections.unmodifiableList(
                _NullSafe.stream(getParameters(ai))
                .map(ParamDto::getType)
                .collect(Collectors.toList())
                );
    }

    // -- getParameter, getParameterName, getParameterType, getParameterArgument

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

    public static boolean isNull(final ActionInvocationDto ai, final int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.isNull();
    }

    // -- DEBUGGING (DUMP)

    public static void dump(final InteractionDto ixnDto, final PrintStream out) throws JAXBException {
        out.println(toXml(ixnDto));
    }


}

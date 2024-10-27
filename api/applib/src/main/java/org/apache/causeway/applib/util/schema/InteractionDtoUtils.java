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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.io.DtoMapper;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.ValueDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class InteractionDtoUtils {

    public void init() {
        dtoMapper.get();
    }

    private _Lazy<DtoMapper<InteractionDto>> dtoMapper = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(InteractionDto.class));

    public DtoMapper<InteractionDto> dtoMapper() {
        return dtoMapper.get();
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
                return MemberExecutionDtoUtils.dtoMapper(memberExecutionDto.getClass()).clone(memberExecutionDto);
            }
        };

        public abstract MemberExecutionDto dtoFor(final Execution<?, ?> execution);

    }

    private MemberExecutionDto.ChildExecutions childExecutionsOf(final MemberExecutionDto dto) {
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
    public InteractionDto newInteractionDto(final Execution<?, ?> execution) {
        return newInteractionDto(execution, Strategy.FLAT);
    }

    /**
     * Creates a {@link InteractionDto} (serializable  to XML) for the provided
     * {@link Execution}
     * (the applib object).
     */
    public InteractionDto newInteractionDto(
            final Execution<?, ?> execution,
            final Strategy strategy) {

        final MemberExecutionDto memberExecutionDto = strategy.dtoFor(execution);
        return newInteractionDto(execution, memberExecutionDto);
    }

    private InteractionDto newInteractionDto(
            final Execution<?, ?> execution,
            final MemberExecutionDto executionDto) {
        final Interaction interaction = execution.getInteraction();
        final String interactionId = interaction.getInteractionId().toString();

        return InteractionDtoUtils.newInteractionDto(interactionId, executionDto);
    }

    private InteractionDto newInteractionDto(
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

    public ActionInvocationDto newActionInvocation(
            final int sequence,
            final Bookmark targetBookmark,
            final String actionIdentifier,
            final List<ParamDto> parameterDtos,
            final String user) {

        return (ActionInvocationDto) newMemberExecutionDto(
                InteractionType.ACTION_INVOCATION, sequence,
                targetBookmark, actionIdentifier,
                parameterDtos, null,
                user);
    }

    public PropertyEditDto newPropertyEdit(
            final int sequence,
            final Bookmark targetBookmark,
            final String propertyIdentifier,
            final ValueWithTypeDto newValueDto,
            final String user) {
        return (PropertyEditDto) newMemberExecutionDto(
                InteractionType.PROPERTY_EDIT, sequence,
                targetBookmark, propertyIdentifier,
                null, newValueDto,
                user);
    }

    private MemberExecutionDto newMemberExecutionDto(
            final InteractionType type,
            final int sequence,
            final Bookmark targetBookmark,
            final String memberId,
            final List<ParamDto> parameterDtos,
            final ValueWithTypeDto newValueDto,
            final String username) {

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

        executionDto.setUsername(username);

        final String logicalMemberId = deriveLogicalMemberId(targetBookmark, memberId);
        executionDto.setLogicalMemberIdentifier(logicalMemberId);
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

    private ParamsDto parametersFor(final ActionInvocationDto invocationDto) {
        ParamsDto parameters = invocationDto.getParameters();
        if(parameters == null) {
            parameters = new ParamsDto();
            invocationDto.setParameters(parameters);
        }
        return parameters;
    }

    private List<ParamDto> parameterListFor(final ActionInvocationDto invocationDto) {
        return parametersFor(invocationDto).getParameter();
    }

    // -- getParameters, getParameterNames, getParameterTypes
    public List<ParamDto> getParameters(final ActionInvocationDto ai) {
        final List<ParamDto> params = parameterListFor(ai);
        final int parameterNumber = getNumberOfParameters(ai);
        final List<ParamDto> paramDtos = _Lists.newArrayList();
        for (int i = 0; i < parameterNumber; i++) {
            final ParamDto paramDto = params.get(i);
            paramDtos.add(paramDto);
        }
        return Collections.unmodifiableList(paramDtos);
    }

    private int getNumberOfParameters(final ActionInvocationDto ai) {
        final List<ParamDto> params = parameterListFor(ai);
        return params != null ? params.size() : 0;
    }

    public List<String> getParameterNames(final ActionInvocationDto ai) {
        return Collections.unmodifiableList(
                _NullSafe.stream(getParameters(ai))
                .map(ParamDto::getName)
                .collect(Collectors.toList())
                );
    }
    public List<ValueType> getParameterTypes(final ActionInvocationDto ai) {
        return Collections.unmodifiableList(
                _NullSafe.stream(getParameters(ai))
                .map(ParamDto::getType)
                .collect(Collectors.toList())
                );
    }

    // -- getParameter, getParameterName, getParameterType, getParameterArgument

    public ParamDto getParameter(final ActionInvocationDto ai, final int paramNum) {
        final int parameterNumber = getNumberOfParameters(ai);
        if(paramNum > parameterNumber) {
            throw new IllegalArgumentException(String.format("No such parameter %d (the memento has %d parameters)", paramNum, parameterNumber));
        }
        final List<ParamDto> parameters = getParameters(ai);
        return parameters.get(paramNum);
    }

    public ValueDto getParameterArgument(final ActionInvocationDto ai, final int paramNum) {
        return getParameter(ai, paramNum);
    }

    public String getParameterName(final ActionInvocationDto ai, final int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.getName();
    }

    public ValueType getParameterType(final ActionInvocationDto ai, final int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.getType();
    }

    public boolean isNull(final ActionInvocationDto ai, final int paramNum) {
        final ParamDto paramDto = getParameter(ai, paramNum);
        return paramDto.isNull();
    }

}

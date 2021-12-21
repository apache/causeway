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
package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

/**
 * Groups together all domain-specific metadata for an invokable action on a
 * domain object or domain service.
 *
 * @see Property
 * @see Collection
 * @see DomainObject
 * @see ActionLayout
 *
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE }
)
@Retention(RetentionPolicy.RUNTIME)
@DomainObject(nature=Nature.MIXIN, mixinMethod = "act") // meta annotation, only applies at class level
@Domain.Include // meta annotation, in support of meta-model validation
public @interface Action {

    /**
     * References a collection of a certain element type, for an action with a collection parameter of that
     * element type; the action's choices will be automatically populated from checkboxes rendered in the collection.
     *
     * <p>
     *     This will also result in the action being rendered near to the collection, similar to the way in
     *     which {@link ActionLayout#associateWith()} does.
     * </p>
     *
     * <p>
     *     If there are multiple actions associated with a collection, either by way of {@link Action#choicesFrom()} or
     *     using {@link ActionLayout#associateWith()}, then their order in the UI can be influenced using
     *     {@link ActionLayout#sequence()}.
     * </p>
     *
     * @see ActionLayout#associateWith()
     * @see ActionLayout#sequence()
     */
    String choicesFrom()
            default "";

    /**
     * The {@link CommandDtoProcessor} to process this command's DTO.
     *
     * <p>
     *     The processor itself is used by {@link ContentMappingServiceForCommandDto} and
     *     {@link ContentMappingServiceForCommandsDto} to dynamically transform the DTOs.
     * </p>
     *
     * @see Property#commandDtoProcessor()
     * @see Action#commandPublishing()
     */
    Class<? extends CommandDtoProcessor> commandDtoProcessor()
            default CommandDtoProcessor.class;

    /**
     * Whether action invocations, captured as {@link Command}s,
     * should be published to {@link CommandSubscriber}s.
     *
     * @see Property#commandPublishing()
     * @see Action#commandDtoProcessor()
     */
    Publishing commandPublishing()
            default Publishing.NOT_SPECIFIED;

    /**
     * Indicates that an invocation of the action should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.domain.ActionDomainEvent}.
     *
     * <p>
     *     Subscribers of this event can interact with the business rule
     *     checking (hide, disable, validate) and its modification (before and
     *     after).
     * </p>
     *
     * <p>For example:
     * </p>
     *
     * <pre>
     * public class SomeObject{
     *     public static class ChangeStartDateDomainEvent extends ActionDomainEvent&lt;SomeObject&gt; { ... }
     *
     *     &#64;Action(domainEvent=ChangedStartDateDomainEvent.class)
     *     public void changeStartDate(final Date startDate) { ...}
     *     ...
     * }
     * </pre>
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see Property#domainEvent()
     * @see Collection#domainEvent()
     * @see DomainObject#actionDomainEvent()
     */
    Class<? extends ActionDomainEvent<?>> domainEvent()
            default ActionDomainEvent.Default.class;

    /**
     * Whether {@link Execution}s (triggered by action invocations), should
     * be published to {@link ExecutionSubscriber}s.
     *
     * @see Property#executionPublishing()
     */
    Publishing executionPublishing()
            default Publishing.NOT_SPECIFIED;

    /**
     * Indicates where (in the UI) the action is not visible to the user.
     *
     * <p>
     * It is also possible to suppress an action's visibility using {@link ActionLayout#hidden()}.
     *
     * <p>
     * For {@link DomainService domain service} actions, the action's visibility is dependent upon its
     * {@link DomainService#nature() nature}.
     *
     * @see Property#hidden()
     * @see Collection#hidden()
     */
    Where hidden()
            default Where.NOT_SPECIFIED;

    /**
     * Whether the action is restricted to prototyping, or whether it is
     * available also in production mode.
     *
     * <p>
     *     By default there are no restrictions, with the action being available in all environments.
     * </p>
     */
    RestrictTo restrictTo()
            default RestrictTo.NOT_SPECIFIED;

    /**
     * The action semantics, either
     * {@link SemanticsOf#SAFE_AND_REQUEST_CACHEABLE cached},
     * {@link SemanticsOf#SAFE safe} (query-only),
     * {@link SemanticsOf#IDEMPOTENT idempotent} or
     * {@link SemanticsOf#NON_IDEMPOTENT non-idempotent}.
     *
     * <p>
     * The action's semantics determine whether objects are modified as the
     * result of invoking this action (if not, the results can be cached for the
     * remainder of the request).  If the objects do cause a change in state,
     * they additionally determine whether re-invoking the action would result
     * in a further change.
     * </p>
     *
     * <p>
     *     There are also `...ARE_YOU_SURE` variants
     *     (@link {@link SemanticsOf#IDEMPOTENT_ARE_YOU_SURE} and
     *     (@link {@link SemanticsOf#NON_IDEMPOTENT_ARE_YOU_SURE} that cause a
     *     confirmation dialog to be displayed in the Wicket viewer.
     * </p>
     */
    SemanticsOf semantics()
            default SemanticsOf.NOT_SPECIFIED;

    /**
     * If the action returns a collection, then this hints as to the run-time
     * type of the objects within that collection.
     *
     * <p>
     *     This is only provided as a fallback; usually the framework can infer
     *     the element type of the collection from the action method's
     *     return type (eg if it returns <code>Collection</code> instead of <code>Collection&lt;Customer&gt;</code>)
     * </p>
     *
     * @see Collection#typeOf()
     */
    Class<?> typeOf()
            default void.class; // represents unspecified

    /**
     * For downloading {@link Blob} or {@link Clob}, optionally restrict the files accepted (eg <tt>.xslx</tt>).
     *
     * <p>
     * The value should be of the form "file_extension|audio/*|video/*|image/*|media_type".
     * </p>
     *
     * @see <a href="http://www.w3schools.com/tags/att_input_accept.asp">http://www.w3schools.com</a>
     * @see Parameter#fileAccept()
     * @see Property#fileAccept()
     */
    String fileAccept()
            default "";

}

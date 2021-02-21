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

import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

/**
 * Collects together all the domain semantics for the property of a domain
 * object.
 *
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@DomainObject(nature=Nature.MIXIN, mixinMethod = "prop") // meta annotation, only applies at class level
public @interface Property {

    /**
     * The {@link CommandDtoProcessor} to process this command's DTO.
     *
     * <p>
     *     The processor itself is used by {@link ContentMappingServiceForCommandDto} and
     *     {@link ContentMappingServiceForCommandsDto} to dynamically transform the DTOs.
     * </p>
     *
     * @see Action#commandDtoProcessor()
     * @see Property#commandPublishing()
     */
    Class<? extends CommandDtoProcessor> commandDtoProcessor()
            default CommandDtoProcessor.class;

    /**
     * Whether property edits, captured as {@link Command}s,
     * should be published to {@link CommandSubscriber}s.
     *
     * @see Action#commandPublishing()
     * @see Property#commandDtoProcessor()
     */
    Publishing commandPublishing()
            default Publishing.NOT_SPECIFIED;

    /**
     * Indicates that changes to the property that should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.domain.PropertyDomainEvent}.
     *
     * <p>For example:
     * </p>
     *
     * <pre>
     * public static class StartDateChanged extends PropertyDomainEvent { ... }
     *
     * &#64;Property(domainEvent=StartDateChanged.class)
     * public LocalDate getStartDate() { ...}
     * </pre>
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see Action#domainEvent()
     * @see Collection#domainEvent()
     * @see DomainObject#propertyDomainEvent()
     */
    Class<? extends PropertyDomainEvent<?,?>> domainEvent()
            default PropertyDomainEvent.Default.class;

    /**
     * Whether the properties of this domain object can be edited, or collections of this object be added to/removed from.
     *
     * <p>
     *     Note that non-editable objects can nevertheless have actions invoked upon them.
     * </p>
     *
     * @see Property#editingDisabledReason()
     * @see DomainObject#editing()
     */
    Editing editing()
            default Editing.NOT_SPECIFIED;

    /**
     * If {@link #editing()} is set to {@link Editing#DISABLED},
     * then the reason to provide to the user as to why this property cannot be edited.
     *
     * @see Property#editing()
     */
    String editingDisabledReason()
            default "";

    /**
     * Whether
     * {@link Execution}s
     * (triggered property edits), should be dispatched to
     * {@link ExecutionSubscriber}s.
     *
     * @see Action#executionPublishing()
     */
    Publishing executionPublishing()
            default Publishing.NOT_SPECIFIED;

    /**
     * For uploading {@link Blob} or {@link Clob}, optionally restrict the files accepted (eg <tt>.xslx</tt>).
     *
     * <p>
     * The value should be of the form "file_extension|audio/*|video/*|image/*|media_type".
     * </p>
     *
     * @see <a href="http://www.w3schools.com/tags/att_input_accept.asp">http://www.w3schools.com</a>
     * @see Parameter#fileAccept()
     * @see Action#fileAccept()
     */
    String fileAccept()
            default "";

    /**
     * Indicates where the property is not visible to the user.
     *
     * @see Action#hidden()
     * @see Collection#hidden()
     */
    Where hidden()
            default Where.NOT_SPECIFIED;

    /**
     * The maximum entry length of a field.
     *
     * <p>
     *     The default value (<code>-1</code>) indicates that no maxLength has been specified.
     * </p>
     *
     * @see Parameter#maxLength()
     */
    int maxLength()
            default -1;

    /**
     * Indicates whether the property should be included or excluded from mementos.
     *
     * <p>
     *     To ensure that the property is actually not persisted in the objectstore, also annotate with the JDO annotation
     *     <code>javax.jdo.annotations.NotPersistent</code>
     * </p>
     */
    Snapshot snapshot()
            default Snapshot.NOT_SPECIFIED;

    /**
     * The {@link org.apache.isis.applib.spec.Specification}(s) to be satisfied by this property.
     *
     * <p>
     * If more than one is provided, then all must be satisfied (in effect &quot;AND&quot;ed together).
     * </p>
     *
     * @see Parameter#mustSatisfy()
     */
    Class<? extends Specification>[] mustSatisfy()
            default {};

    /**
     * Whether this property is optional or is mandatory (ie required).
     *
     * <p>
     *     For properties the default value, {@link org.apache.isis.applib.annotation.Optionality#DEFAULT}, usually
     *     means that the property is required unless it has been overridden by <code>javax.jdo.annotations.Column</code>
     *     with its <code>javax.jdo.annotations.Column#allowsNull()</code> attribute set to true.
     * </p>
     *
     * @see Parameter#optionality()
     */
    Optionality optionality()
            default Optionality.NOT_SPECIFIED;

    /**
     * If set to {@link Projecting#PROJECTED projected}, then indicates that the owner of this property is a view model
     * which is a projection of some other entity, and that the property holds a reference to that
     * &quot;underlying&quot;.
     *
     * <p>
     *     This is used to automatically redirect any bookmarks to the view model (projection) to instead be directed
     *     at the underlying entity.
     * </p>
     *
     * <p>
     *     Only one such property should be marked as being a projection with a view model.
     * </p>
     */
    Projecting projecting()
            default Projecting.NOT_SPECIFIED;

    /**
     * Regular expression pattern that a value should conform to, and can be formatted as.
     *
     * @see Parameter#regexPattern()
     * @see Property#regexPatternReplacement()
     * @see Property#regexPatternFlags()
     */
    String regexPattern()
            default "";

    /**
     * Pattern flags, as per {@link java.util.regex.Pattern#compile(String, int)} .
     *
     * <p>
     *     The default value, <code>0</code>, means that no flags have been specified.
     * </p>
     *
     * @see Property#regexPattern()
     */
    int regexPatternFlags()
            default 0;

    /**
     * Replacement text for the pattern in generated error message.
     *
     * @see Property#regexPattern()
     */
    String regexPatternReplacement()
            default "Doesn't match pattern";

}

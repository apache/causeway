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
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

/**
 * Domain semantics for domain object property.
 */
// tag::refguide[]
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Mixin(method = "prop")
public @interface Property {

    // end::refguide[]
    /**
     * Whether the property edit should be reified into a
     * {@link org.apache.isis.applib.services.command.Command} object.
     */
    // tag::refguide[]
    CommandReification command()                                // <.>
            default CommandReification.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * The {@link CommandDtoProcessor} to process this command's DTO.
     *
     * <p>
     *     The processor itself is used by {@link ContentMappingServiceForCommandDto} and
     *     {@link ContentMappingServiceForCommandsDto} to dynamically transform the DTOs.
     * </p>
     */
    // tag::refguide[]
    Class<? extends CommandDtoProcessor> commandDtoProcessor()  // <.>
            default CommandDtoProcessor.class;

    // end::refguide[]
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
     */
    // tag::refguide[]
    Class<? extends PropertyDomainEvent<?,?>> domainEvent()     // <.>
            default PropertyDomainEvent.Default.class;

    // end::refguide[]
    /**
     * Whether the properties of this domain object can be edited, or collections of this object be added to/removed from.
     *
     * <p>
     *     Note that non-editable objects can nevertheless have actions invoked upon them.
     * </p>
     */
    // tag::refguide[]
    Editing editing()                                           // <.>
            default Editing.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * If {@link #editing()} is set to {@link Editing#DISABLED},
     * then the reason to provide to the user as to why this property cannot be edited.
     */
    // tag::refguide[]
    String editingDisabledReason()                              // <.>
            default "";

    // end::refguide[]
    /**
     * For uploading {@link Blob} or {@link Clob}, optionally restrict the files accepted (eg <tt>.xslx</tt>).
     *
     * <p>
     * The value should be of the form "file_extension|audio/*|video/*|image/*|media_type".
     * </p>
     *
     * @see <a href="http://www.w3schools.com/tags/att_input_accept.asp">http://www.w3schools.com</a>
     */
    // tag::refguide[]
    String fileAccept()                                         // <.>
            default "";

    // end::refguide[]
    /**
     * Indicates where the property is not visible to the user.
     */
    // tag::refguide[]
    Where hidden()                                              // <.>
            default Where.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * The maximum entry length of a field.
     *
     * <p>
     *     The default value (<code>-1</code>) indicates that no maxLength has been specified.
     * </p>
     */
    // tag::refguide[]
    int maxLength()                                             // <.>
            default -1;

    // end::refguide[]
    /**
     * Indicates whether the property should be included or excluded from mementos.
     *
     * <p>
     *     To ensure that the property is actually not persisted in the objectstore, also annotate with the JDO annotation
     *     <code>javax.jdo.annotations.NotPersistent</code>
     * </p>
     */
    // tag::refguide[]
    MementoSerialization mementoSerialization()                 // <.>
            default MementoSerialization.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * The {@link org.apache.isis.applib.spec.Specification}(s) to be satisfied by this property.
     *
     * <p>
     * If more than one is provided, then all must be satisfied (in effect &quot;AND&quot;ed together).
     * </p>
     */
    // tag::refguide[]
    Class<? extends Specification>[] mustSatisfy()              // <.>
            default {};

    // end::refguide[]
    /**
     * Whether this property is optional or is mandatory (ie required).
     *
     * <p>
     *     For properties the default value, {@link org.apache.isis.applib.annotation.Optionality#DEFAULT}, usually
     *     means that the property is required unless it has been overridden by <code>javax.jdo.annotations.Column</code>
     *     with its <code>javax.jdo.annotations.Column#allowsNull()</code> attribute set to true.
     * </p>
     */
    // tag::refguide[]
    Optionality optionality()                                   // <.>
            default Optionality.NOT_SPECIFIED;

    // end::refguide[]
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
    // tag::refguide[]
    Projecting projecting()                                     // <.>
            default Projecting.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * Whether the property edit should be published.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.publish.PublisherService}
     * or {@link org.apache.isis.applib.services.publish.PublisherService} is registered with the framework.
     * </p>
     */
    // tag::refguide[]
    Publishing publishing()                                     // <.>
            default Publishing.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * Regular expression pattern that a value should conform to, and can be formatted as.
     */
    // tag::refguide[]
    String regexPattern()                                       // <.>
            default "";

    // end::refguide[]
    /**
     * Pattern flags, as per {@link java.util.regex.Pattern#compile(String, int)} .
     *
     * <p>
     *     The default value, <code>0</code>, means that no flags have been specified.
     * </p>
     */
    // tag::refguide[]
    int regexPatternFlags()                                     // <.>
            default 0;

    // end::refguide[]
    /**
     * Replacement text for the pattern in generated error message.
     */
    // tag::refguide[]
    String regexPatternReplacement()                            // <.>
            default "Doesn't match pattern";

}
// end::refguide[]

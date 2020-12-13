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

/**
 * Layout hints for properties.
 *
 * @since 1.x {@index}
 * @see org.apache.isis.applib.annotation.ParameterLayout
 */
// tag::refguide[]
@Inherited
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@DomainObject(nature=Nature.MIXIN, mixinMethod = "prop") // meta annotation, only applies at class level
public @interface PropertyLayout {

    // end::refguide[]
    /**
     * Indicates the css class that a property should have.
     */
    // tag::refguide[]
    String cssClass()                               // <.>
            default "";

    // end::refguide[]
    /**
     * Description of this property, eg to be rendered in a tooltip.
     */
    // tag::refguide[]
    String describedAs()                            // <.>
            default "";

    // end::refguide[]
    /**
     * Indicates where in the UI the property should <i>not</i>not be visible.
     */
    // tag::refguide[]
    Where hidden()                                  // <.>
            default Where.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * In forms, positioning of the label (left, top or none) relative to the property value.
     *
     * <p>
     * If not specified, the default depends upon the property value's datatype.
     * </p>
     */
    // tag::refguide[]
    LabelPosition labelPosition()                   // <.>
            default LabelPosition.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * For string properties (and parameters), render as a text area over specified number of lines.
     */
    // tag::refguide[]
    int multiLine()                                 // <.>
            default -1;

    // end::refguide[]
    /**
     * Name of this property (overriding the name derived from its name in code).
     */
    // tag::refguide[]
    String named()                                  // <.>
            default "";

    // end::refguide[]
    /**
     * A flag indicating whether the value of {@linkplain #named()} should be HTML escaped or not.
     */
    // tag::refguide[]
    boolean namedEscaped()                          // <.>
            default true;

    // end::refguide[]
    /**
     * Whether this property should be used to construct the navigable chain of breadcrumbs in the UI.
     *
     * <p>
     *     Only one property can be annotated as such per domain class.
     * </p>
     *
     * @return
     */
    // tag::refguide[]
    Navigable navigable()                           // <.>
            default Navigable.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * How the properties of this domain object are be edited, either {@link PromptStyle#DIALOG dialog} or {@link PromptStyle#INLINE inline}.
     */
    // tag::refguide[]
    PromptStyle promptStyle()                       // <.>
            default PromptStyle.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * For date properties (and parameters) only, instructs the viewer that the date should be rendered as one day
     * prior to the actually stored date.
     *
     * <p>
     * This is intended to be used so that an exclusive end date of an interval
     * can be rendered as 1 day before the actual value stored.
     * </p>
     *
     * <p>
     * For example:
     * </p>
     * <pre>
     * public LocalDate getStartDate() { ... }
     *
     * &#64;PropertyLayout(renderDay=RenderDay.AS_DAY_BEFORE)
     * public LocalDate getEndDate() { ... }
     * </pre>
     *
     * <p>
     * Here, the interval of the [1-may-2013,1-jun-2013) would be rendered as the dates
     * 1-may-2013 for the start date but using 31-may-2013 (the day before) for the end date.  What is stored
     * In the domain object, itself, however, the value stored is 1-jun-2013.
     * </p>
     */
    // tag::refguide[]
    RenderDay renderDay()                           // <.>
            default RenderDay.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * Indicates that the value held by the property never changes over time, even when other properties of the object
     * do change.
     *
     * <p>
     *     Setting this attribute to <tt>RepaintPolicy.NO_REPAINT</tt> is used as a hint to the viewer to not repaint
     *     the property after an AJAX update of some other property/ies of the object have changed.
     *     This is primarily for performance, eg can improve the user experience when rendering PDFs/blobs.
     * </p>
     *
     * <p>
     *     Note that for this to work, the viewer will also ensure that none of the property's parent component
     *     (such as a tab group panel) are re-rendered.
     * </p>
     *
     * <p>
     *     Design note: we considered implementing this an "immutable" flag on the {@link Property } annotation
     *     (because this flag is typically appropriate for immutable/unchanging properties of a domain object).
     *     However, we decided not to do that, on the basis that it might be interpreted as having a deeper impact
     *     within the framework than simply a hint for rendering.
     * </p>
     */
    // tag::refguide[]
    Repainting repainting()                         // <.>
            default Repainting.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * The typical entry length of a field, use to determine the optimum width for display
     */
    // tag::refguide[]
    int typicalLength()                             // <.>
            default -1;

}
// end::refguide[]


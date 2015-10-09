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

import java.lang.annotation.*;

/**
 * Layout hints for properties.
 *
 * @see org.apache.isis.applib.annotation.ParameterLayout
 */
@Inherited
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyLayout {

    /**
     * Indicates the css class that a property should have.
     */
    String cssClass() default "";

    /**
     * Description of this property, eg to be rendered in a tooltip.
     */
    String describedAs() default "";

    /**
     * Indicates where in the UI the property should <i>not</i>not be visible.
     */
    Where hidden() default Where.NOT_SPECIFIED;

    /**
     * In forms, positioning of the label (left, top or none) relative to the property value.
     *
     * <p>
     * If not specified, the default depends upon the property value's datatype.
     * </p>
     */
    LabelPosition labelPosition() default LabelPosition.LEFT;

    /**
     * For string properties (and parameters), render as a text area over specified number of lines.
     */
    int multiLine() default -1;

    /**
     * Name of this property (overriding the name derived from its name in code).
     */
    String named() default "";

    /**
     * A flag indicating whether the value of {@linkplain #named()} should be HTML escaped or not.
     */
    boolean namedEscaped() default true;

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
     * &#64;PropertyLayout(renderedAsDayBefore=true)
     * public LocalDate getEndDate() { ... }
     * </pre>
     *
     * <p>
     * Here, the interval of the [1-may-2013,1-jun-2013) would be rendered as the dates
     * 1-may-2013 for the start date but using 31-may-2013 (the day before) for the end date.  What is stored
     * In the domain object, itself, however, the value stored is 1-jun-2013.
     * </p>
     */
    boolean renderedAsDayBefore() default false;


    /**
     * The typical entry length of a field, use to determine the optimum width for display
     */
    int typicalLength() default -1;
}


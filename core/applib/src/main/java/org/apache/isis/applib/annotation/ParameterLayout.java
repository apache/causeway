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
 * Layout hints for action parameters.
 *
 * @see org.apache.isis.applib.annotation.PropertyLayout
 */
@Inherited
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterLayout {

    /**
     * Indicates the css class that a parameter should have.
     */
    String cssClass() default "";

    /**
     * Description of this property, eg to be rendered in a tooltip.
     */
    String describedAs() default "";

    /**
     * In forms, positioning of the label (left, top or none) relative to the parameter value.
     *
     * <p>
     * If not specified, the default depends upon the parameter value's datatype.
     * </p>
     */
    LabelPosition labelPosition() default LabelPosition.NOT_SPECIFIED;

    /**
     * Name of this action parameter.
     */
    String named() default "";

    /**
     * A flag indicating whether the value of {@linkplain #named()} should be HTML escaped or not.
     */
    boolean namedEscaped() default true;

    /**
     * For string parameters (and properties), whether to render as a text area over multiple lines.
     */
    int multiLine() default -1;

    /**
     * For date parameters (and properties) only, instructs the viewer that the date should be rendered as one day
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
     * public void updateDates(
     *     &#64;ParameterLayout(named="From") LocalDate startDate,
     *     &#64;ParameterLayout(named="To"), renderDay=RenderDay.AS_DAY_BEFORE) LocalDate endDate) { ... }
     * </pre>
     *
     * <p>
     * Here, the interval of the [1-may-2013,1-jun-2013) would be rendered as the dates
     * 1-may-2013 for the start date but using 31-may-2013 (the day before) for the end date.  What is stored
     * In the domain object, itself, however, the value stored is 1-jun-2013.
     * </p>
     */
    RenderDay renderDay() default RenderDay.NOT_SPECIFIED;

    /**
     * The typical entry length of a field, use to determine the optimum width for display
     */
    int typicalLength() default -1;

}


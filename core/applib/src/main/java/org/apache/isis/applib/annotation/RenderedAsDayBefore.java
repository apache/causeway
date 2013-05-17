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
 * A rendering hint, instructing the viewer that the date should as one day prior to
 * the actualy stored date.
 * 
 * <p>
 * This is intended to be used so that an exclusive end date of an interval
 * can be rendered as 1 day before the actual value stored.
 * 
 * <p>
 * For example:
 * <pre>
 * public LocalDate getStartDate() { ... }
 * 
 * &#64;RenderedAsDayBefore
 * public LocalDate getEndDate() { ... }
 * </pre>
 * 
 * <p>
 * Here, the interval of the [1-may-2013,1-jun-2013) would be rendered as the dates
 * 1-may-2013 for the start date but using 31-may-2013 (the day before) for the end date.  What is stored
 * In the domain object, itself, however, the value stored is 1-jun-2013.
 */
@Inherited
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface RenderedAsDayBefore {
}

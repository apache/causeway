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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A title annotation used to annotate methods used to construct the title of a
 * domain object instance. It is used as a marker.
 * @since 1.x {@index}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.FIELD
})
public @interface Title {

    /**
     * The order (in Dewey decimal notation) that the property annotated with
     * {@link Title} appears with respect to other properties also annotated
     * with {@link Title}.
     */
    String sequence() default "1.0";

    /**
     * The string to use to separate this property from any preceding properties
     * in the title.
     */
    String prepend() default " ";

    /**
     * The string to append to this property if non-empty.
     */
    String append() default "";

    /**
     * The length to abbreviate this title element to.
     */
    int abbreviatedTo() default Integer.MAX_VALUE;

}

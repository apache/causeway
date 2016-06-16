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

import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

import java.lang.annotation.*;

/**
 * Domain semantics for domain object collection.
 */
@Inherited
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

    /**
     * The minimum entry length of an autocomplete search argument.
     *
     * <p>
     *     The default value (<code>-1</code>) indicates that no minLength has been specified.
     * </p>
     *
     * @deprecated  - should use {@link MinLength} instead, because the autocomplete search argument is not, formally speaking, an action parameter (it is merely the parameter of one of the action's supporting methods).
     */
    @Deprecated
    int minLength() default -1;


    /**
     * The maximum entry length of a field.
     *
     * <p>
     *     The default value (<code>-1</code>) indicates that no maxLength has been specified.
     * </p>
     */
    int maxLength() default -1;



    /**
     * Whether this parameter is optional or is mandatory (ie required).
     *
     * <p>
     *     For parameters the default value, {@link org.apache.isis.applib.annotation.Optionality#DEFAULT}, is taken
     *     to mean that the parameter is required.
     * </p>
     */
    Optionality optionality() default Optionality.DEFAULT;



    /**
     * The {@link org.apache.isis.applib.spec.Specification}(s) to be satisfied by this parameter.
     *
     * <p>
     * If more than one is provided, then all must be satisfied (in effect &quot;AND&quot;ed together).
     * </p>
     */
    Class<? extends Specification>[] mustSatisfy() default {};



    /**
     * Regular expression pattern that a value should conform to, and can be formatted as.
     */
    String regexPattern() default "";

    /**
     * Pattern flags, as per {@link java.util.regex.Pattern#compile(String, int)} .
     *
     * <p>
     *     The default value, <code>0</code>, means that no flags have been specified.
     * </p>
     */
    int regexPatternFlags() default 0;

    /**
     * Replacement text for the pattern in generated error message.
     */
    String regexPatternReplacement() default "Doesn't match pattern";


    /**
     * For uploading {@link Blob} or {@link Clob}, optionally restrict the files accepted (eg <tt>.xslx</tt>).
     *
     * <p>
     * The value should be of the form "file_extension|audio/*|video/*|image/*|media_type".
     * </p>
     *
     * @see <a href="http://www.w3schools.com/tags/att_input_accept.asp">http://www.w3schools.com</a>
     */
    String fileAccept() default "";

}
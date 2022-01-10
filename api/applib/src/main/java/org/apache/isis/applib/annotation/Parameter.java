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

import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

/**
 * Collects together all domain semantics of an action parameter within a
 * single annotation.
 *
 * @see Action
 * @see Property
 * @see ParameterLayout
 *
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

    /**
     * For uploading {@link Blob} or {@link Clob}, optionally restrict the files accepted (eg <tt>.xslx</tt>).
     *
     * <p>
     * The value should be of the form "file_extension|audio/*|video/*|image/*|media_type".
     * </p>
     *
     * <p>
     *     Note that this does not prevent the user from uploading some other file type; rather it merely defaults the
     *     file type in the file open dialog.
     * </p>
     *
     * @see Action#fileAccept()
     * @see Property#fileAccept()
     * @see <a href="http://www.w3schools.com/tags/att_input_accept.asp">http://www.w3schools.com</a>
     */
    String fileAccept()
            default "";

    /**
     * The maximum entry length of a string parameter (it is ignored for other
     * types).
     *
     * <p>
     *     The default value (<code>-1</code>) indicates that no maxLength has
     *     been specified.
     * </p>
     *
     * @see Property#maxLength()
     */
    int maxLength()
            default -1;

    /**
     * The {@link org.apache.isis.applib.spec.Specification}(s) to be satisfied by this parameter.
     *
     * <p>
     * If more than one is provided, then all must be satisfied (in effect &quot;AND&quot;ed together).
     * </p>
     *
     * @see Property#mustSatisfy()
     */
    Class<? extends Specification>[] mustSatisfy()
            default {};

    /**
     * Whether this parameter is optional or is mandatory (ie required).
     *
     * <p>
     *     For parameters the default value, {@link org.apache.isis.applib.annotation.Optionality#DEFAULT}, is taken
     *     to mean that the parameter is required.
     * </p>
     *
     * @see Property#optionality()
     */
    Optionality optionality()
            default Optionality.NOT_SPECIFIED;

    /**
     * Regular expression pattern that a value should conform to, and can be formatted as.
     *
     * @see Property#regexPattern()
     * @see Parameter#regexPatternReplacement()
     * @see Parameter#regexPatternFlags()
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
     * @see Parameter#regexPattern()
     */
    int regexPatternFlags()
            default 0;

    /**
     * Replacement text for the pattern in generated error message.
     *
     * @see Parameter#regexPattern()
     */
    String regexPatternReplacement()
            default "Doesn't match pattern";


}

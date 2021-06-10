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
package org.apache.isis.subdomains.base.applib.types;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;

/**
 * Meta-annotation for a {@link String} property or parameter representing A
 * unique reference code of some sort.
 *
 * @since 2.0 {@index}
 */
@Property(maxLength = Reference.MAX_LEN, regexPattern = Reference.REGEX, regexPatternReplacement = Reference.REGEX_DESCRIPTION)
@Parameter(maxLength = Reference.MAX_LEN, regexPattern = Reference.REGEX, regexPatternReplacement = Reference.REGEX_DESCRIPTION)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Reference {

    int MAX_LEN = 24;

    String REGEX = "[ -/_A-Z0-9]+";
    String REGEX_DESCRIPTION = "Only capital letters, numbers and 3 symbols being: \"_\" , \"-\" and \"/\" are allowed";

}

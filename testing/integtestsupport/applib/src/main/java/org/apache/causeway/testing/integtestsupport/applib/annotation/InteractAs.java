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
package org.apache.causeway.testing.integtestsupport.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;

/**
 * Offers an alternative to {@link InteractionService}'s imperative approach to switch
 * the {@link InteractionContext} for a given block of <i>JUnit</i> test code.
 * <p>
 * Example:<br>
 * <pre>
 * &#64;Test
 * &#64;InteractAs(
 *     userName = "sven",
 *     localeName = "en",
 *     frozenDateTime = "2010-01-01 13:02:04 -03")
 * void test() {
 *     // ...
 * }
 * </pre>
 *
 * @since 2.0 {@index}
 */
@Inherited
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface InteractAs {

    /**
     * If empty, defaults to the system user (with elevated privileges).
     */
    String userName()
        default ""; // unspecified

    /**
     * The {@link Locale} language tag. Eg. "en", "fr", "de", ...
     * If empty, defaults to current system locale.
     * @see Locale#forLanguageTag(String)
     */
    String localeName()
        default ""; // unspecified

    /**
     * If empty, defaults to current system time.
     * <p>
     * Format: {@literal "yyyy-MM-dd HH:mm:ss[.SSS][' '][XXX][x]"}<br>
     * Examples:<br>
     * <ul>
     * <li>"2022-01-31 14:04:33.017 -03:30" (full form)</li>
     * <li>"2022-01-31 14:04:33 -03" (no millis, no offset minutes)</li>
     * <li>"2022-01-31 14:04:33 Z" (no millis, no offset = UTC)</li>
     * </ul>
     */
    String frozenDateTime()
        default ""; // unspecified

}

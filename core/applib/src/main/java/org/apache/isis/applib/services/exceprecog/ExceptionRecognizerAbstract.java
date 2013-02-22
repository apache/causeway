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
package org.apache.isis.applib.services.exceprecog;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Convenience implementation of {@link ExceptionRecognizer} that provides some
 * utility methods to subclasses.
 */
public abstract class ExceptionRecognizerAbstract implements ExceptionRecognizer {

    /**
     * Convenience for subclass implementations that always return a fixed message.
     */
    protected static Function<String, String> constant(final String message) {
        return new Function<String, String>() {

            @Override
            public String apply(String input) {
                return message;
            }
        };
    }

    /**
     * Convenience for subclass implementations that always prefixes the exception message
     * with the supplied text
     */
    protected static Function<String, String> prefix(final String prefix) {
        return new Function<String, String>() {

            @Override
            public String apply(String input) {
                return prefix + "<br/><br/>" + input;
            }
        };
    }

}

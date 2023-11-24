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
package org.apache.isis.viewer.wicket.ui.pages.accmngt.password_reset;

import java.util.regex.Pattern;

import org.apache.wicket.validation.validator.PatternValidator;

/**
 * Copy of {@link org.apache.wicket.validation.validator.EmailAddressValidator}, generalized a little.
 */
public class EmailAddressValidator extends PatternValidator {
    private static final long serialVersionUID = 1L;
    private static final EmailAddressValidator INSTANCE = new EmailAddressValidator();

    public static EmailAddressValidator getInstance() {
        return INSTANCE;
    }

    protected EmailAddressValidator() {
        super(
        "^[_A-Za-z0-9-]" +
               "[_A-Za-z0-9-+]+" +
                "(\\.[_A-Za-z0-9-]+)*" +
                "@[A-Za-z0-9-]+" +
                "(\\.[A-Za-z0-9-]+)*" +
                "(" +
                  "(\\.[A-Za-z]{2,}){1}" +
                "$)", Pattern.CASE_INSENSITIVE);
    }
}

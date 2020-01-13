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


package org.apache.isis.core.metamodel.examples.facets.jsr303;

import javax.validation.Constraint;


public class CustomPatternValidator implements Constraint<CustomPattern> {
    private java.util.regex.Pattern pattern;

    public void initialize(final CustomPattern params) {
        pattern = java.util.regex.Pattern.compile(params.regex(), params.flags());
    }

    public boolean isValid(final Object ovalue) {
        if (ovalue == null) {
            return true;
        }
        if (!(ovalue instanceof String)) {
            return false;
        }
        final String value = (String) ovalue;
        return pattern.matcher(value).matches();
    }
}

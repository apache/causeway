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

package org.apache.isis.core.metamodel.facets.members.disabled.layout;

import java.util.Properties;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstractImpl;

public class DisabledFacetFromProperties extends DisabledFacetAbstractImpl {

    public DisabledFacetFromProperties(final When when, Where where, String reason, final FacetHolder holder) {
        super(when, where, reason, holder);
    }

    public DisabledFacetFromProperties(Properties properties, FacetHolder holder) {
        this(disabledWhenFrom(properties), disabledWhereFrom(properties), disabledReasonFrom(properties), holder);
    }

    private static When disabledWhenFrom(Properties properties) {
        String value = properties.getProperty("when");
        // same default as in Disabled.when()
        return value != null? When.valueOf(value): When.ALWAYS;
    }

    private static Where disabledWhereFrom(Properties properties) {
        String value = properties.getProperty("where");
        // same default as in Disabled.where()
        return value != null? Where.valueOf(value): Where.ANYWHERE;
    }
    
    private static String disabledReasonFrom(Properties properties) {
        String value = properties.getProperty("reason");
        // same default as in Disabled.reason()
        return value != null? value: "";
    }

}

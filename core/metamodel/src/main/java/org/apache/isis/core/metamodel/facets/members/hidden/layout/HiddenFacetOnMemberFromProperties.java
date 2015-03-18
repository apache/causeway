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

package org.apache.isis.core.metamodel.facets.members.hidden.layout;

import java.util.Properties;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstractImpl;

public class HiddenFacetOnMemberFromProperties extends HiddenFacetAbstractImpl {

    public HiddenFacetOnMemberFromProperties(final When when, Where where, final FacetHolder holder) {
        super(HiddenFacetOnMemberFromProperties.class, when, where, holder);
    }

    public HiddenFacetOnMemberFromProperties(Properties properties, FacetHolder holder) {
        super(HiddenFacetOnMemberFromProperties.class, hiddenWhenFrom(properties), hiddenWhereFrom(properties), holder);
    }

    private static When hiddenWhenFrom(Properties properties) {
        String value = properties.getProperty("when");
        // same default as in Hidden.when()
        return value != null? When.valueOf(value): When.ALWAYS;
    }

    private static Where hiddenWhereFrom(Properties properties) {
        String value = properties.getProperty("where");
        // same default as in Hidden.where()
        return value != null? Where.valueOf(value): Where.ANYWHERE;
    }

}

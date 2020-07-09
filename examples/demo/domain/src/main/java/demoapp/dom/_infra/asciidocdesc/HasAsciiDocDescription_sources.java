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
package demoapp.dom._infra.asciidocdesc;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom._infra.resources.MarkupVariableResolverService;

@Property
@RequiredArgsConstructor @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class HasAsciiDocDescription_sources {

    private final HasAsciiDocDescription hasAsciiDocDescription;

    @PropertyLayout(labelPosition = LabelPosition.NONE, hidden = Where.ALL_TABLES)
    @MemberOrder(name = "sources", sequence = "1")
    public AsciiDoc prop() {
        val packageName = hasAsciiDocDescription.getClass().getPackage().getName();
        val sourceLocation = packageName.replace('.', '/');
        return AsciiDoc.valueOfAdoc(
                markupVariableResolverService.resolveVariables(
                        String.format("link:${SOURCES_DEMO}/%s[Sources] for this demo", sourceLocation)));
    }

    @Inject
    MarkupVariableResolverService markupVariableResolverService;

}

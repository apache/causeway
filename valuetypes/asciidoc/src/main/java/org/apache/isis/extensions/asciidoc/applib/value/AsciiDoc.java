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
package org.apache.isis.extensions.asciidoc.applib.value;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.extensions.asciidoc.ui.converter.AsciiDocConverter;

/**
 * Immutable value type holding pre-rendered HTML.
 *
 */
@Value(semanticsProviderName = 
        "org.apache.isis.metamodel.facets.value.markup.MarkupValueSemanticsProvider")
public class AsciiDoc extends Markup {

    private static final long serialVersionUID = 1L;

    public static AsciiDoc valueOfAdoc(String asciiDoc) {
        return valueOfHtml(AsciiDocConverter.adocToHtml(asciiDoc));
    }

    public static AsciiDoc valueOfHtml(String html) {
        return new AsciiDoc(html);
    }

    private AsciiDoc(String html) {
        super(html);
    }


}

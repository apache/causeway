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
package org.apache.causeway.valuetypes.asciidoc.applib.value;

import java.io.Serializable;

import org.apache.causeway.valuetypes.asciidoc.applib.CausewayModuleValAsciidocApplib.AdocToHtmlConverter;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;


@EqualsAndHashCode
public abstract class AsciiDocAbstract implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter final String adoc;

    @EqualsAndHashCode.Exclude
    private final String sourceLanguage;

    @EqualsAndHashCode.Exclude
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    private final String html = AdocToHtmlConverter.instance().adocToHtml(
            String.format("%s%s%s",
                    sourceLanguage != null ? String.format("[source,%s]\n====\n", sourceLanguage) : "",
                    adoc,
                    sourceLanguage != null ? "====\n" : ""
            ));

    public AsciiDocAbstract(String sourceLanguage) {
        this(null, sourceLanguage);
    }

    public AsciiDocAbstract(
            final String adoc,
            final String sourceLanguage
    ) {
        this.adoc = blankIfNull(adoc);
        this.sourceLanguage = sourceLanguage;
    }

    public String asHtml() {
        return html();
    }

    private static String blankIfNull(String str) {
        return str != null ? str : "";
    }
}

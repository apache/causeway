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
package org.apache.isis.valuetypes.markdown.applib.value;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.value.HasHtml;
import org.apache.isis.valuetypes.markdown.applib.jaxb.MarkdownJaxbAdapter;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Immutable value type holding pre-rendered HTML.
 *
 * @since 2.0 {@index}
 */
@org.apache.isis.applib.annotation.Value(
        logicalTypeName = IsisModuleApplib.NAMESPACE + ".value.Markdown")
@EqualsAndHashCode
@XmlJavaTypeAdapter(MarkdownJaxbAdapter.class)  // for JAXB view model support
public class Markdown implements HasHtml, Serializable {

    private static final long serialVersionUID = 1L;

    public static Markdown valueOf(final String markdown) {
        return new Markdown(markdown);
    }

    @Getter private final String markdown;

    @EqualsAndHashCode.Exclude
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    private final String html = Converter.mdToHtml(getMarkdown());

    public Markdown() {
        this(null);
    }

    public Markdown(final String markdown) {
        this.markdown = markdown !=null ? markdown : "";
    }

    @Override
    public String asHtml() {
        return html();
    }

    public boolean isEqualTo(final Markdown other) {
        return Objects.equals(this, other);
    }

    @Override
    public String toString() {
        return "Markdown[length=" + markdown.length() + "]";
    }

}

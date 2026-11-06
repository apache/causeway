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
package org.apache.causeway.valuetypes.markdown.applib.value;

import java.io.Serializable;
import java.util.Objects;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.commons.internal.hardening._Hardening;
import org.apache.causeway.commons.internal.hardening._Hardening.SafelistSelect;
import org.apache.causeway.valuetypes.markdown.applib.CausewayModuleValMarkdownApplib;
import org.apache.causeway.valuetypes.markdown.applib.jaxb.MarkdownJaxbAdapter;

/**
 * Immutable value type holding pre-rendered HTML.
 *
 * @since 2.0 {@index}
 */
@Named(CausewayModuleValMarkdownApplib.NAMESPACE + ".Markdown")
@org.apache.causeway.applib.annotation.Value
@XmlJavaTypeAdapter(MarkdownJaxbAdapter.class)  // for JAXB view model support
public record Markdown(String markdown, String asHtml) implements Serializable {

    public static Markdown valueOf(final String markdown) {
        return new Markdown(markdown);
    }

    public Markdown {
    	markdown = markdown != null
        		? markdown
				: "";
    	asHtml = _Hardening.toSafeHtml(
        		Converter.mdToHtml(markdown), SafelistSelect.MARKDOWN);
    }

    public Markdown() {
        this("", null);
    }

    public Markdown(final String markdown) {
        this(markdown, null);
    }

    @Override
    public final boolean equals(final Object o) {
    	return o instanceof Markdown other
			? Objects.equals(this.markdown, other.markdown)
			: false;
    }

    @Override
    public final int hashCode() {
    	return Objects.hashCode(markdown);
    }

    public boolean isEqualTo(final Markdown other) {
        return Objects.equals(this, other);
    }

    @Override
    public String toString() {
        return String.format("Markdown[length=%d,content=%s]",
                markdown.length(), Markup.summarizeHtmlAsTitle(markdown));
    }

    // -- DEPRECATIONS

    /**
     * @deprecated use {@link #markdown()} instead.
     */
    @Deprecated
    public String getMarkdown() {
		return markdown();
	}

}

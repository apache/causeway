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
package org.apache.causeway.commons.internal.hardening;

import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import org.apache.causeway.commons.io.JsonUtils;

/**
 * Various hardening utilities.
 *
 * <p> Introduced as a consequence of CAUSEWAY-3077, extended for CAUSEWAY-4079
 */
public class _Hardening {

	public enum SafelistSelect {
		ASCIIDOC,
		MARKDOWN,
		MARKUP
	}

    /**
     * @see "https://jsoup.org/cookbook/cleaning-html/safelist-sanitizer"
     */
    public static String toSafeHtml(final @Nullable String untrustedHtml, final @Nullable SafelistSelect select) {
    	return StringUtils.hasLength(untrustedHtml)
			? Jsoup.clean(untrustedHtml, safelist(select))
			: untrustedHtml;
    }

    /**
     * Makes JSON safe to inline into a {@code <script>} element, by escaping {@code '<'} such that a
     * literal {@code </script>} can never occur - this prevents breaking out of the script context
     * (see CAUSEWAY-4079).
     */
    public static Optional<String> toSafeJson(final @Nullable String untrustedJson) {
    	return (StringUtils.hasLength(untrustedJson)
				? Optional.of(untrustedJson.replace("<", "\\u003c"))
				: Optional.<String>empty())
			// checks whether given String can be parsed
			.filter(json->JsonUtils.tryRead(Map.class, json).getValue().isPresent());
    }

    // -- HELPER

    /**
     * A {@link Safelist} that is safe against script/event-handler injection, yet rich enough to
     * preserve the structural HTML and CSS classes emitted by markup languages such as AsciiDoc and
     * Markdown (headings, tables, images, code blocks, ...).
     * (see CAUSEWAY-4079)
     */
    private static final Safelist RICH_HTML_SAFELIST = Safelist.relaxed()
            .addAttributes(":all", "class", "id");

    private static Safelist safelist(final @Nullable SafelistSelect select) {
    	return switch (select) {
    		case null -> Safelist.basic();
			case MARKUP -> Safelist.basic();
			case ASCIIDOC, MARKDOWN -> RICH_HTML_SAFELIST;
		};
    }

}

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;

/**
 * Various hardening utilities.
 * <p>
 * Introduced as a consequence of CAUSEWAY-3077.
 */
public class _Hardening {

    // -- XSS GUARDS

    /**
     * @throws IllegalArgumentException - when an XSS attack is encountered, or the URL is not parseable
     * @implNote unfortunately has potential for false positives; but shall do for now
     */
    public static Optional<URL> toUrlWithXssGuard(final @Nullable String untrustedUrl) {
        if(_Strings.isEmpty(untrustedUrl)) {
            return Optional.empty();
        }
        if(_Strings.condenseWhitespaces(untrustedUrl.toLowerCase(), "").contains("javascript:")) {
            // simple guard against XSS attacks like javascript:alert(document)
            throw new IllegalArgumentException("Not parseable as an URL ('" + untrustedUrl + "').");
        }
        try {
            return Optional.of(new java.net.URL(untrustedUrl));
        } catch (final MalformedURLException ex) {
            throw new IllegalArgumentException("Not parseable as an URL ('" + untrustedUrl + "').", ex);
        }
    }

    /**
     * @see "https://jsoup.org/cookbook/cleaning-html/safelist-sanitizer"
     */
    public static String toSafeHtml(final @Nullable String untrustedHtml) {
        if(_Strings.isEmpty(untrustedHtml)) {
            return untrustedHtml;
        }
        return Jsoup.clean(untrustedHtml, Safelist.basic());
    }

}

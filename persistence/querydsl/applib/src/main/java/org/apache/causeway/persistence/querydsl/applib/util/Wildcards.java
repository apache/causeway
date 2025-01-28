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
 *
 */
package org.apache.causeway.persistence.querydsl.applib.util;

import java.util.regex.Pattern;

import org.springframework.lang.Nullable;

import org.jspecify.annotations.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utility methods to convert between UI &quot;wildcards&quot; (meaning &quot;*&quot; and &quot;?&quot;), ANSI SQL
 * wildcards (&quot;%&quot; and &quot;_&quot;) and Regex (&quot;.*&quot; and &quot;.&quot;).
 *
 * @since 2.1 {@index}
 */
@UtilityClass
public class Wildcards {

    public final static Pattern REGEX_PATTERN = Pattern.compile("\\(\\?i\\)"); // Pattern to recognize #wildcardToCaseInsensitiveRegex conversion

    public String toAnsiSqlWildcard(final String search) {
        if (REGEX_PATTERN.matcher(search).find()) {
            // Don't replace anything when regex is given
            return search;
        }
        String result = search.replace("*", "%").replace("?", "_");
        if (!result.contains("%") && !result.contains("_")) {
            result = "%" + result + "%";
        }
        return result;
    }

    public String wildcardToRegex(
            @Nullable final String searchPattern,
            final CaseSensitivity caseSensitivity
    ) {
        var searchPatternWithWildcards = withWildcards(searchPattern);
        var searchPatternAsRegex = wildToRegex(searchPatternWithWildcards);
        switch (caseSensitivity) {
            case INSENSITIVE:
                return "(?i)".concat(searchPatternAsRegex);
            case SENSITIVE:
            default:
                return searchPatternAsRegex;
        }
    }

    private String withWildcards(@Nullable final String searchPattern) {
        if(searchPattern == null || searchPattern.isEmpty()) {
            return "*";
        }
        return searchPattern.contains("*") || searchPattern.contains("?")
                ? searchPattern
                : "*" + searchPattern + "*";
    }

    private String wildToRegex(final @NonNull String pattern) {
        return pattern.replace("*", ".*").replace("?", ".");
    }

}
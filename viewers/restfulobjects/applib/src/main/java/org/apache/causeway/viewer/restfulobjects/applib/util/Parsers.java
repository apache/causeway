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
package org.apache.causeway.viewer.restfulobjects.applib.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Parsers {

    public record BooleanParser() implements Parser<Boolean> {
        @Override public Boolean valueOf(final String str) {
            if (str == null) return null;
            return "yes".equalsIgnoreCase(str) || "true".equalsIgnoreCase(str)
                ? Boolean.TRUE
                : Boolean.FALSE;
        }
        @Override public String asString(final Boolean t) {
            return t ? "yes" : "no";
        }
    }

    public record IntegerParser() implements Parser<Integer> {
        @Override public Integer valueOf(final String str) {
            if (str == null) return null;
            return Integer.valueOf(str);
        }
        @Override public String asString(final Integer t) {
            return t.toString();
        }
    }

    public record StringParser() implements Parser<String> {
        @Override public String valueOf(final String str) { return str; }
        @Override public String asString(final String t) { return t; }
    }

    public record DateParser() implements Parser<Date> {
        final static SimpleDateFormat RFC1123_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");
        @Override public Date valueOf(final String str) {
            if (!StringUtils.hasLength(str)) return null;
            try {
                return RFC1123_DATE_FORMAT.parse(str);
            } catch (final ParseException e) {
                return null;
            }
        }
        @Override public String asString(final Date t) {
            return t!=null
                    ? RFC1123_DATE_FORMAT.format(t)
                    : null;
        }
    }

    public record MediaTypeParser() implements Parser<MediaType> {
        @Override public MediaType valueOf(final String str) {
            if (!StringUtils.hasLength(str)) return null;
            return MediaType.valueOf(str);
        }
        @Override public String asString(final MediaType t) {
            return t!=null
                    ? t.toString()
                    : null;
        }
    }

    public record CacheControlParser() implements Parser<CacheControl> {
        @Override public CacheControl valueOf(String str) {
            //Cache-Control: no-cache, no-store, max-age=3600
            var directives = _Strings.splitThenStream(str.toLowerCase(Locale.US), ",")
                .map(String::trim)
                .toList();
            var builder = CacheControl.empty();
            // prime the builder
            for(String directive : directives) {
                switch (directive) {
                    case "no-cache" -> builder = CacheControl.noCache();
                    case "no-store" -> builder = CacheControl.noStore();
                    default -> {
                        if(directive.startsWith("max-age=")) {
                            builder = CacheControl.maxAge(Integer.parseInt(directive.substring("max-age=".length())), TimeUnit.SECONDS);
                        }
                    }
                };
            }
            // modifications
            for(String directive : directives) {
                if(directive.startsWith("s-maxage=")) {
                    builder = builder.sMaxAge(Integer.parseInt(directive.substring("s-maxage=".length())), TimeUnit.SECONDS);
                } else if(directive.startsWith("stale-while-revalidate=")) {
                    builder = builder.staleWhileRevalidate(Integer.parseInt(directive.substring("stale-while-revalidate=".length())), TimeUnit.SECONDS);
                } else if(directive.startsWith("stale-if-error=")) {
                    builder = builder.staleIfError(Integer.parseInt(directive.substring("stale-if-error=".length())), TimeUnit.SECONDS);
                } else switch (directive) {
                    case "must-revalidate" -> builder = builder.mustRevalidate();
                    case "no-transform" -> builder = builder.noTransform();
                    case "public" -> builder = builder.cachePublic();
                    case "private" -> builder = builder.cachePrivate();
                    case "proxy-revalidate" -> builder = builder.proxyRevalidate();
                    case "immutable" -> builder = builder.immutable();
                    default -> {}
                };
            }
            return builder;
        }
        @Override public String asString(CacheControl t) {
            return t.getHeaderValue();
        }
    }

    public record ETagParser() implements Parser<String> {
        @Override public String valueOf(String str) {
            return null;
        }
        @Override public String asString(String t) {
            return null;
        }
    }

    public record ListOfStringsParser() implements Parser<List<String>> {
        @Override public List<String> valueOf(final List<String> strings) {
            if (strings == null) return Collections.emptyList();
            if (strings.size() == 1) {
                // special case processing to handle comma-separated values
                return valueOf(strings.get(0));
            }
            return strings;
        }
        @Override public List<String> valueOf(final String[] strings) {
            if (strings == null) return Collections.emptyList();
            if (strings.length == 1) {
                // special case processing to handle comma-separated values
                return valueOf(strings[0]);
            }
            return Arrays.asList(strings);
        }
        @Override public List<String> valueOf(final String str) {
            if (str == null) {
                return Collections.emptyList();
            }
            return _Strings.splitThenStream(str, ",")
                    .collect(Collectors.toList());
        }
        @Override public String asString(final List<String> strings) {
            return _NullSafe.stream(strings)
                    .collect(Collectors.joining(","));
        }
    }

    public record ListOfListOfStringsParser() implements Parser<List<List<String>>> {
        @Override public List<List<String>> valueOf(final List<String> str) {
            if (str == null || str.size() == 0) return null;
            final List<List<String>> listOfLists = _Lists.newArrayList();
            for (final String s : str) {
                listOfLists.add(PathNode.split(s));
            }
            return listOfLists;
        }
        @Override public List<List<String>> valueOf(final String[] str) {
            if (str == null || str.length == 0) return null;
            return valueOf(Arrays.asList(str));
        }
        @Override public List<List<String>> valueOf(final String str) {
            if (str == null || str.isEmpty())  return Collections.emptyList();
            return _Strings.splitThenStream(str, ",")
                    .map(PathNode::split)
                    .collect(Collectors.toList());
        }
        @Override public String asString(final List<List<String>> listOfLists) {
            return _NullSafe.stream(listOfLists)
                    .map(listOfStrings->_NullSafe.stream(listOfStrings).collect(Collectors.joining(".")))
                    .collect(Collectors.joining(","));
        }
    }

    public record ArrayOfStringsParser() implements Parser<String[]> {
        @Override public String[] valueOf(final List<String> strings) {
            if (strings == null) return _Constants.emptyStringArray;
            if (strings.size() == 1) {
                // special case processing to handle comma-separated values
                return valueOf(strings.get(0));
            }
            return strings.toArray(new String[] {});
        }
        @Override public String[] valueOf(final String[] strings) {
            if (strings == null) return _Constants.emptyStringArray;
            if (strings.length == 1) {
                // special case processing to handle comma-separated values
                return valueOf(strings[0]);
            }
            return strings;
        }
        @Override public String[] valueOf(final String str) {
            if (str == null) return _Constants.emptyStringArray;
            return _Strings.splitThenStream(str, ",")
                    .collect(Collectors.toList())
                    .toArray(_Constants.emptyStringArray);
        }
        @Override public String asString(final String[] strings) {
            return _NullSafe.stream(strings)
                    .collect(Collectors.joining(","));
        }
    }

    public record ListOfMediaTypesParser() implements Parser<List<MediaType>> {
        @Override public List<MediaType> valueOf(final String str) {
            if (str == null) return Collections.emptyList();
            return _Strings.splitThenStream(str, ",")
                    .map(MediaType::valueOf)
                    .collect(Collectors.toList());
        }
        @Override public String asString(final List<MediaType> listOfMediaTypes) {
            return _NullSafe.stream(listOfMediaTypes)
                    .map(MediaType::toString)
                    .collect(Collectors.joining(","));
        }
    }

}

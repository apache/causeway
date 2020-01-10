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
package org.apache.isis.viewer.restfulobjects.applib.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;

import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

public abstract class Parser<T> {

    public T valueOf(final List<String> str) {
        if (str == null) {
            return null;
        }
        if (str.size() == 0) {
            return null;
        }
        return valueOf(str.get(0));
    }

    public T valueOf(final String[] str) {
        if (str == null) {
            return null;
        }
        if (str.length == 0) {
            return null;
        }
        return valueOf(str[0]);
    }

    public T valueOf(final JsonRepresentation jsonRepresentation) {
        if (jsonRepresentation == null) {
            return null;
        }
        return valueOf(jsonRepresentation.asString());
    }

    public JsonRepresentation asJsonRepresentation(final T t) {
        return JsonRepresentation.newMap("dummy", asString(t)).getRepresentation("dummy");
    }

    public abstract T valueOf(String str);

    public abstract String asString(T t);

    public final static Parser<String> forString() {
        return new Parser<String>() {
            @Override
            public String valueOf(final String str) {
                return str;
            }

            @Override
            public String asString(final String t) {
                return t;
            }
        };
    }

    public static Parser<Date> forDate() {

        return new Parser<Date>() {
            private final SimpleDateFormat RFC1123_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");

            @Override
            public Date valueOf(final String str) {
                if (str == null) {
                    return null;
                }
                try {
                    return RFC1123_DATE_FORMAT.parse(str);
                } catch (final ParseException e) {
                    return null;
                }
            }

            @Override
            public String asString(final Date t) {
                return RFC1123_DATE_FORMAT.format(t);
            }
        };
    }

    public static Parser<CacheControl> forCacheControl() {
        return new Parser<CacheControl>() {
            @Override
            public CacheControl valueOf(final String str) {
                if (str == null) {
                    return null;
                }
                final CacheControl cacheControl = CacheControl.valueOf(str);
                // workaround for bug in CacheControl's equals() method
                cacheControl.getCacheExtension();
                cacheControl.getNoCacheFields();
                return cacheControl;
            }

            @Override
            public String asString(final CacheControl cacheControl) {
                return cacheControl.toString();
            }
        };
    }

    public static Parser<MediaType> forJaxRsMediaType() {
        return new Parser<MediaType>() {
            @Override
            public MediaType valueOf(final String str) {
                if (str == null) {
                    return null;
                }
                return MediaType.valueOf(str);
            }

            @Override
            public String asString(final MediaType t) {
                return t.toString();
            }

        };
    }

    //    @Deprecated // [ahuber] MediaType is in standard API! proposing removal
    //    public static Parser<com.google.common.net.MediaType> forGuavaMediaType() {
    //        return new Parser<com.google.common.net.MediaType>() {
    //            @Override
    //            public com.google.common.net.MediaType valueOf(final String str) {
    //                if (str == null) {
    //                    return null;
    //                }
    //                return com.google.common.net.MediaType.parse(str);
    //            }
    //
    //            @Override
    //            public String asString(final com.google.common.net.MediaType t) {
    //                return t.toString();
    //            }
    //        };
    //    }

    public static Parser<Boolean> forBoolean() {
        return new Parser<Boolean>() {
            @Override
            public Boolean valueOf(final String str) {
                if (str == null) {
                    return null;
                }
                return "yes".equalsIgnoreCase(str) || "true".equalsIgnoreCase(str)
                        ? Boolean.TRUE
                                : Boolean.FALSE;
            }

            @Override
            public String asString(final Boolean t) {
                return t ? "yes" : "no";
            }

        };
    }

    public static Parser<Integer> forInteger() {
        return new Parser<Integer>() {

            @Override
            public Integer valueOf(final String str) {
                if (str == null) {
                    return null;
                }
                return Integer.valueOf(str);
            }

            @Override
            public String asString(final Integer t) {
                return t.toString();
            }
        };
    }

    public static Parser<List<String>> forListOfStrings() {
        return new Parser<List<String>>() {

            @Override
            public List<String> valueOf(final List<String> strings) {
                if (strings == null) {
                    return Collections.emptyList();
                }
                if (strings.size() == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings.get(0));
                }
                return strings;
            }

            @Override
            public List<String> valueOf(final String[] strings) {
                if (strings == null) {
                    return Collections.emptyList();
                }
                if (strings.length == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings[0]);
                }
                return Arrays.asList(strings);
            }

            @Override
            public List<String> valueOf(final String str) {
                if (str == null) {
                    return Collections.emptyList();
                }
                return _Strings.splitThenStream(str, ",")
                        .collect(Collectors.toList());
            }

            @Override
            public String asString(final List<String> strings) {
                return _NullSafe.stream(strings)
                        .collect(Collectors.joining(","));
            }
        };
    }

    public static Parser<List<List<String>>> forListOfListOfStrings() {
        return new Parser<List<List<String>>>() {

            @Override
            public List<List<String>> valueOf(final List<String> str) {
                if (str == null) {
                    return null;
                }
                if (str.size() == 0) {
                    return null;
                }
                final List<List<String>> listOfLists = _Lists.newArrayList();
                for (final String s : str) {
                    listOfLists.add(PathNode.split(s));
                }
                return listOfLists;
            }

            @Override
            public List<List<String>> valueOf(final String[] str) {
                if (str == null) {
                    return null;
                }
                if (str.length == 0) {
                    return null;
                }
                return valueOf(Arrays.asList(str));
            }

            @Override
            public List<List<String>> valueOf(final String str) {
                if (str == null || str.isEmpty()) {
                    return Collections.emptyList();
                }

                return _Strings.splitThenStream(str, ",")
                        .map(PathNode::split)
                        .collect(Collectors.toList());
            }

            @Override
            public String asString(final List<List<String>> listOfLists) {

                return _NullSafe.stream(listOfLists)
                        .map(listOfStrings->_NullSafe.stream(listOfStrings).collect(Collectors.joining(".")))
                        .collect(Collectors.joining(","));
            }
        };
    }

    public static Parser<String[]> forArrayOfStrings() {
        return new Parser<String[]>() {

            @Override
            public String[] valueOf(final List<String> strings) {
                if (strings == null) {
                    return _Constants.emptyStringArray;
                }
                if (strings.size() == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings.get(0));
                }
                return strings.toArray(new String[] {});
            }

            @Override
            public String[] valueOf(final String[] strings) {
                if (strings == null) {
                    return _Constants.emptyStringArray;
                }
                if (strings.length == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings[0]);
                }
                return strings;
            }

            @Override
            public String[] valueOf(final String str) {
                if (str == null) {
                    return _Constants.emptyStringArray;
                }

                return _Strings.splitThenStream(str, ",")
                        .collect(Collectors.toList())
                        .toArray(_Constants.emptyStringArray);
            }

            @Override
            public String asString(final String[] strings) {
                return _NullSafe.stream(strings)
                        .collect(Collectors.joining(","));
            }
        };
    }

    public static Parser<List<MediaType>> forListOfJaxRsMediaTypes() {
        return new Parser<List<MediaType>>() {

            @Override
            public List<MediaType> valueOf(final String str) {
                if (str == null) {
                    return Collections.emptyList();
                }

                return _Strings.splitThenStream(str, ",")
                        .map(MediaType::valueOf)
                        .collect(Collectors.toList());
            }

            @Override
            public String asString(final List<MediaType> listOfMediaTypes) {

                return _NullSafe.stream(listOfMediaTypes)
                        .map(MediaType::toString)
                        .collect(Collectors.joining(","));
            }
        };
    }

    public static Parser<String> forETag() {
        return new Parser<String>(){

            @Override
            public String valueOf(String str) {
                return null;
            }

            @Override
            public String asString(String t) {
                return null;
            }};
    }


}

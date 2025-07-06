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

import java.util.Date;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

/**
 * @since 1.x {@index}
 */
public interface Parser<T> {

    T valueOf(String str);
    String asString(T t);

    default T valueOf(final List<String> str) {
        if (str == null || str.size() == 0) return null;
        return valueOf(str.get(0));
    }

    default T valueOf(final String[] str) {
        if (str == null || str.length == 0) return null;
        return valueOf(str[0]);
    }

    default T valueOf(final JsonRepresentation jsonRepresentation) {
        if (jsonRepresentation == null) return null;
        return valueOf(jsonRepresentation.asString());
    }

    default JsonRepresentation asJsonRepresentation(final T t) {
        return JsonRepresentation.newMap("dummy", asString(t)).getRepresentation("dummy");
    }

    // -- FACTORIES

    static Parser<Boolean> forBoolean() { return new Parsers.BooleanParser(); }
    static Parser<Date> forDate() { return new Parsers.DateParser(); }
    static Parser<Integer> forInteger() { return new Parsers.IntegerParser(); }
    static Parser<String> forString() { return new Parsers.StringParser(); }
    static Parser<MediaType> forMediaType() { return new Parsers.MediaTypeParser(); }
    static Parser<String> forETag() { return new Parsers.ETagParser(); }
    static Parser<CacheControl> forCacheControl() { return new Parsers.CacheControlParser(); }
    static Parser<List<String>> forListOfStrings() { return new Parsers.ListOfStringsParser(); }
    static Parser<List<List<String>>> forListOfListOfStrings() { return new Parsers.ListOfListOfStringsParser(); }
    static Parser<String[]> forArrayOfStrings() { return new Parsers.ArrayOfStringsParser(); }
    static Parser<List<MediaType>> forListOfMediaTypes() { return new Parsers.ListOfMediaTypesParser(); }

}

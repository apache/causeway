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
package org.apache.causeway.viewer.restfulobjects.applib;

import java.util.Date;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;

import org.apache.causeway.viewer.restfulobjects.applib.util.Parser;

/**
 * @since 1.x {@index}
 */
public class RestfulResponse<T> {

    public record Header<X>(
        String name,
        Parser<X> parser) {

        public static final Header<String> WARNING = new Header<String>("Warning", Parser.forWarning());
        public static final Header<Date> LAST_MODIFIED = new Header<Date>("Last-Modified", Parser.forDate());
        public static final Header<CacheControl> CACHE_CONTROL = new Header<CacheControl>("Cache-Control", Parser.forCacheControl());
        public static final Header<MediaType> CONTENT_TYPE = new Header<MediaType>("Content-Type", Parser.forMediaType());
        public static final Header<Integer> CONTENT_LENGTH = new Header<Integer>("Content-Length", Parser.forInteger());
        //public static final Header<String> ETAG = new Header<String>("ETag", Parser.forETag());

        public X parse(final String value) {
            return value != null ? parser.valueOf(value): null;
        }

        public String render(X message) {
            return parser.asString(message);
        }

    }

}

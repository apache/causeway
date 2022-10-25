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
package org.apache.causeway.commons.internal.base;

import java.util.Collections;
import java.util.Iterator;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 *
 * package private mixin for utility class {@link _Strings}
 *
 */
final class _Strings_SplitIterator {

    public static Iterator<String> splitIterator(final @Nullable String x, final String delimiter){
        if(_Strings.isEmpty(delimiter)) {
            throw new IllegalArgumentException("a non empty delimiter is required");
        }
        if(_Strings.isEmpty(x)) {
            return Collections.<String>emptyIterator();
        }
        final int dlen = delimiter.length();
        return new Iterator<String>() {
            private int p=0, q=-1;

            private String next = _next();

            private String _next() {
                if(q==-2)
                    return null;
                q = x.indexOf(delimiter, p);
                if(q>-1) {
                    final int p0 = p; p=q+dlen;
                    return x.substring(p0, q);
                }
                q = -2; // terminal
                return x.substring(p, x.length());
            }

            @Override
            public boolean hasNext() {
                return next!=null;
            }

            @Override
            public String next() {

                if(!hasNext()) {
                    throw _Exceptions.noSuchElement("end of string already reached");
                }

                try {
                    return next;
                } finally {
                    next=_next();
                }
            }
        };
    }


}

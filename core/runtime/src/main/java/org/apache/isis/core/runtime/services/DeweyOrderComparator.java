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

package org.apache.isis.core.runtime.services;

import java.util.Comparator;
import java.util.List;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class DeweyOrderComparator implements Comparator<String> {

    public DeweyOrderComparator() {}

    @Override
    public int compare(String o1, String o2) {
        final Parsed<String> p1 = new Parsed<String>(o1);
        final Parsed<String> p2 = new Parsed<String>(o2);
        return p1.compareTo(p2);
    }

    private static class Parsed<V> implements Comparable<Parsed<V>> {
        private static final Function<String, Integer> PARSE = new Function<String, Integer>() {
            @Override
            public Integer apply(String input) {
                try {
                    return Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE;
                }
            }
        };
        private final List<Integer> parts;
        private final String key;
        Parsed(String key) {
            this.key = key;
            final Iterable<String> iter = Splitter.on(".").split(key);
            parts = Lists.newArrayList(Iterators.transform(iter.iterator(), PARSE));
        }

        @Override
        public int compareTo(Parsed<V> other) {
            for (int i = 0; i < parts.size(); i++) {
                Integer p = parts.get(i);
                if (other.parts.size() == i) {
                    // run out of parts for other, put it before us
                    return +1;
                }
                final Integer q = other.parts.get(i);
                final int comparison = p.compareTo(q);
                if(comparison != 0) {
                    return +comparison;
                }
            }
            if(other.parts.size() > parts.size()) {
                // run out of parts on our side, still more on others; put us before it
                return -1;
            }
            return 0;
        }
    }
}



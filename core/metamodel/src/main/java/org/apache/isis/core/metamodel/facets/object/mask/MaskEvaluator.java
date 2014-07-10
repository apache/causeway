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

package org.apache.isis.core.metamodel.facets.object.mask;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MaskEvaluator {

    interface Converter {
        void convert(String str, StringBuilder buf);
    }

    static class RegExConverter implements Converter {
        private final String mask;
        private final String regex;

        public RegExConverter(final String mask, final String regex) {
            this.mask = mask;
            this.regex = regex;
        }

        public String getMask() {
            return mask;
        }

        public String getRegex() {
            return regex;
        }

        @Override
        public void convert(final String str, final StringBuilder buf) {
            final String convert = str.replace(mask, regex);
            if (!convert.equals(str)) {
                buf.append(convert);
            }
        }
    }

    @SuppressWarnings("serial")
    private static List<Converter> converters = new ArrayList<Converter>() {
        {
            add("#", "[0-9]");
            // add(".", "[\\" +
            // DecimalFormatSymbols.getInstance().getDecimalSeparator()+"]");
            // add(",",
            // "["+DecimalFormatSymbols.getInstance().getGroupingSeparator()+"]");
            add("&", "[A-Za-z]");
            add("?", "[A-Za-z]");
            add("A", "[A-Za-z0-9]");
            add("a", "[ A-Za-z0-9]");
            add("9", "[ 0-9]");
            add("U", "[A-Z]");
            add("L", "[a-z]");

            add(new Converter() {
                @Override
                public void convert(final String str, final StringBuilder buf) {
                    if (buf.length() == 0) {
                        buf.append(str);
                    }
                }
            });
        }

        public void add(final String mask, final String regex) {
            add(new RegExConverter(mask, regex));
        }
    };

    private final Pattern pattern;

    public MaskEvaluator(final String mask) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            final String charAt = "" + mask.charAt(i);
            for (final Converter converter : converters) {
                converter.convert(charAt, buf);
            }
        }
        pattern = Pattern.compile(buf.toString());
    }

    public boolean evaluate(final String str) {
        return pattern.matcher(str).matches();
    }

}

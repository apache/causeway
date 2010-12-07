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


package org.apache.isis.core.commons.lang;

import java.util.List;


public final class StringUtils {
    private StringUtils() {}

    public static String naturalName(final String name) {

        int pos = 0;

        // find first upper case character
        while ((pos < name.length()) && Character.isLowerCase(name.charAt(pos))) {
            pos++;
        }

        if (pos == name.length()) {
            return "invalid name";
        }
        return naturalize(name, pos);
    }

    public static String naturalize(final String name) {
        return naturalize(name, 0);
    }

    private static String naturalize(final String name, final int startingPosition) {
        if (name.length() <= startingPosition) {
            throw new IllegalArgumentException("string shorter than starting position provided");
        }
        final StringBuffer s = new StringBuffer(name.length() - startingPosition);
        for (int j = startingPosition; j < name.length(); j++) { // process english name - add spaces
            if ((j > startingPosition) && isStartOfNewWord(name.charAt(j), name.charAt(j - 1))) {
                s.append(' ');
            }
            if (j == startingPosition) {
                s.append(Character.toUpperCase(name.charAt(j)));
            } else {
                s.append(name.charAt(j));
            }
        }
        final String str = s.toString();
        return str;
    }

    private static boolean isStartOfNewWord(final char c, final char previousChar) {
        return Character.isUpperCase(c) || Character.isDigit(c) && !Character.isDigit(previousChar);
    }

    public static String capitalize(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Reciprocal of {@link #isNullOrEmpty(String)}.
     */
    public static boolean isNotEmpty(final String str) {
        return !isNullOrEmpty(str);
    }

    public static String lowerFirst(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static boolean in(final String str, final String[] strings) {
        for (final String strCandidate : strings) {
            if (strCandidate.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static String commaSeparatedClassNames(final List<Object> objects) {
        final StringBuilder buf = new StringBuilder();
        int i = 0;
        for (final Object object : objects) {
            if (i++ > 0) {
                buf.append(',');
            }
            buf.append(object.getClass().getName());
        }
        return buf.toString();
    }

    public static String stripNewLines(final String str) {
        return str.replaceAll("[\r\n]", "");
    }

    public static String combine(List<String> list) {
        final StringBuffer buf = new StringBuffer();
        for (String message: list) {
            if (list.size() > 1) {
                buf.append("; ");
            }
            buf.append(message);
        }
        return buf.toString();
    }

    public static String firstWord(String line) {
        String[] split = line.split(" ");
        return split[0];
    }

	public static String stripLeadingSlash(String path) {
	    if (!path.startsWith("/")) {
	        return path;
	    }
	    if (path.length() < 2) {
	        return "";
	    }
	    return path.substring(1);
	}

	public static String removeTabs(final String text) {
	    // quick return - jvm java should always return here
	    if (text.indexOf('\t') == -1) {
	        return text;
	    }
	    final StringBuffer buf = new StringBuffer();
	    for (int i = 0; i < text.length(); i++) {
	        // a bit clunky to stay with j# api
	        if (text.charAt(i) != '\t') {
	            buf.append(text.charAt(i));
	        }
	    }
	    return buf.toString();
	}

	
    private static final char CARRIAGE_RETURN = '\n';
    private static final char LINE_FEED = '\r';


    /**
     * Converts any <tt>\n</tt> to <tt>line.separator</tt>
     * @param string
     * @return
     */
    public static String lineSeparated(String string) {
        StringBuilder buf = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        boolean lastWasLineFeed = false;
        for(char c: string.toCharArray()) {
            boolean isLineFeed = c == LINE_FEED;
            boolean isCarriageReturn = c == CARRIAGE_RETURN;
            if (isCarriageReturn) {
                buf.append(lineSeparator);
                lastWasLineFeed = false;
            } else {
                if(lastWasLineFeed) {
                    buf.append(LINE_FEED);
                }
                if(isLineFeed) {
                    lastWasLineFeed = true;
                } else {
                    buf.append(c);
                    lastWasLineFeed = false;    
                }
            }
        }
        if(lastWasLineFeed) {
            buf.append(LINE_FEED);
        }
        return buf.toString();
    }




}

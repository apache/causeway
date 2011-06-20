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


package org.apache.isis.application.valueholder;

import org.apache.isis.application.BusinessObject;


public class MultilineTextString extends TextString {
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public MultilineTextString() {
        super();
    }

    /**
     * @param text
     */
    public MultilineTextString(final String text) {
        super(text);
    }

    /**
     * @param textString
     */
    public MultilineTextString(final MultilineTextString textString) {
        super(textString);
    }

    /**
     * 
     */
    public MultilineTextString(final BusinessObject parent) {
        super(parent);
    }

    /**
     * @param text
     */
    public MultilineTextString(final BusinessObject parent, final String text) {
        super(parent, text);
    }

    /**
     * @param textString
     */
    public MultilineTextString(final BusinessObject parent, final MultilineTextString textString) {
        super(parent, textString);
    }

    /*
     * @see org.apache.isis.object.value.TextString#setValue(java.lang.String)
     */
    public void setValue(final String text) {
        super.setValue(convertLineEnding(text));
    }

    public void restoreFromEncodedString(final String data) {
        super.restoreFromEncodedString(convertLineEnding(data));
    }

    protected boolean isCharDisallowed(final char c) {
        return c == '\r';
    }

    private String convertLineEnding(final String original) {
        if (original == null)
            return null;
        /*
         * convert all line ending to LF e.g. CR -> LF CRLF -> LF
         */
        StringBuffer text = new StringBuffer(original.length());

        for (int i = 0; i < original.length(); i++) {
            if (original.charAt(i) == '\r') {
                text.append('\n');

                // skip next char if LF (ie is a CRLF sequence
                if (i + 1 < original.length() && original.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                text.append(original.charAt(i));
            }
        }

        return text.toString();
    }

    /*
     * int numberOfLine() int maxWidth();
     * 
     */

}

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

package org.apache.isis.viewer.dnd.view.text;

import org.apache.isis.viewer.dnd.drawing.Text;

public class TextUtils {

    private TextUtils() {
    }

    public static String limitText(final String xtext, final Text style, final int maxWidth) {
        String text = xtext;
        final int ellipsisWidth = style.stringWidth("...");
        if (maxWidth > 0 && style.stringWidth(text) > maxWidth) {
            int lastCharacterWithinAllowedWidth = 0;
            for (int textWidth = ellipsisWidth; textWidth <= maxWidth;) {
                final char character = text.charAt(lastCharacterWithinAllowedWidth);
                textWidth += style.charWidth(character);
                lastCharacterWithinAllowedWidth++;
            }

            int space = text.lastIndexOf(' ', lastCharacterWithinAllowedWidth - 1);
            if (space > 0) {
                while (space >= 0) {
                    final char character = text.charAt(space - 1);
                    if (Character.isLetterOrDigit(character)) {
                        break;
                    }
                    space--;
                }

                text = text.substring(0, space);
            } else {
                if (lastCharacterWithinAllowedWidth > 0) {
                    text = text.substring(0, lastCharacterWithinAllowedWidth - 1);
                } else {
                    text = "";
                }
            }
            text += "...";
        }
        return text;
    }

}

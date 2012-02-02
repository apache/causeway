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

package org.apache.isis.viewer.dnd.drawing;

public interface Text {

    /**
     * Returns the width, in pixels, of the specified character.
     */
    int charWidth(char c);

    /**
     * Returns the height, in pixels, of the distance from the baseline to top
     * of the tallest character (including accents that are not common in
     * english.
     */
    int getAscent();

    /**
     * Returns the height, in pixels, of the distance from bottom of the lowest
     * descending character to the baseline.
     */
    int getDescent();

    /**
     * Returns the mid point, in pixels, between the baseline and the top of the
     * characters.
     */
    int getMidPoint();

    /**
     * Return the name of this text style.
     */
    String getName();

    /**
     * Returns the height, in pixels, for a normal line of text - where there is
     * some space between two lines of text.
     */
    int getTextHeight();

    /**
     * Returns the sum of the text height and line spacing.
     * 
     * @see #getLineHeight()
     * @see #getLineSpacing()
     */
    int getLineHeight();

    /**
     * Returns the number of blank vertical pixels to add between adjacent lines
     * to give them additional spacing.
     */
    int getLineSpacing();

    /**
     * Returns the width of the specified in pixels.
     */
    int stringWidth(String text);

    /**
     * Returns the height in pixels when the specified text is wrapped at the
     * specified width
     */
    int stringHeight(String text, int maxWidth);

    int stringWidth(String message, int maxWidth);
}

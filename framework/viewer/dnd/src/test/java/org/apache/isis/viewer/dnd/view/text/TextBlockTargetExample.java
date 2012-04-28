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

final class TextBlockTargetExample implements TextBlockTarget {
    @Override
    public int getMaxFieldWidth() {
        return 200;
    }

    @Override
    public Text getText() {
        return new Text() {

            @Override
            public int charWidth(final char ch) {
                return 10;
            }

            @Override
            public int stringHeight(final String text, final int width) {
                return 0;
            }

            @Override
            public int stringWidth(final String string) {
                return 40;
            }

            @Override
            public int stringWidth(final String message, final int maxWidth) {
                return 0;
            }

            @Override
            public int getAscent() {
                return 10;
            }

            @Override
            public int getLineHeight() {
                return 15;
            }

            @Override
            public int getMidPoint() {
                return 7;
            }

            @Override
            public int getDescent() {
                return 0;
            }

            @Override
            public int getTextHeight() {
                return 15;
            }

            @Override
            public int getLineSpacing() {
                return 0;
            }

            @Override
            public String getName() {
                return null;
            }
        };
    }
}

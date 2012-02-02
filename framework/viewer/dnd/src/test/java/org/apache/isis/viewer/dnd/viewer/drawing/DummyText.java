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

package org.apache.isis.viewer.dnd.viewer.drawing;

import org.apache.isis.viewer.dnd.drawing.Text;

public class DummyText implements Text {

    public DummyText() {
        super();
    }

    @Override
    public int charWidth(final char c) {
        return 10;
    }

    @Override
    public int getAscent() {
        return 2;
    }

    @Override
    public int getDescent() {
        return 4;
    }

    @Override
    public int getMidPoint() {
        return 1;
    }

    @Override
    public int getTextHeight() {
        return 8;
    }

    @Override
    public int getLineHeight() {
        return getAscent() + getTextHeight() + getDescent();
    }

    @Override
    public int getLineSpacing() {
        return getLineHeight() + 5;
    }

    @Override
    public int stringHeight(final String text, final int width) {
        return getLineHeight();
    }

    @Override
    public int stringWidth(final String text) {
        return text.length() * charWidth('x');
    }

    @Override
    public int stringWidth(final String message, final int maxWidth) {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

}

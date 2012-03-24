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

public class TextFieldContentStub extends TextContent {

    public TextFieldContentStub() {
        super(null, 1, WRAPPING);
    }

    @Override
    public void alignDisplay(final int line) {
    }

    @Override
    public int getNoLinesOfContent() {
        return 16;
    }

    @Override
    public int getNoDisplayLines() {
        return 3;
    }

    @Override
    public String getText(final int forLine) {
        // 35 characters
        // 0 - 3 Now
        // 4 - 6 is
        // 7 - 10 the
        // 11 - 17 winter
        // 18 - 20 of
        // 21 - 25 our
        // 24 - 34 discontent
        return "Now is the winter of our discontent";
    }
}

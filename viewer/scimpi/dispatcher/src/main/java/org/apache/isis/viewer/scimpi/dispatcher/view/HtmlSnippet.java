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

package org.apache.isis.viewer.scimpi.dispatcher.view;

public class HtmlSnippet implements Snippet {
    private final StringBuffer html = new StringBuffer();
    private boolean containsVariable;
    private final String lineNumbers;
    private final String path;

    public HtmlSnippet(final String lineNumbers, final String path) {
        this.lineNumbers = lineNumbers;
        this.path = path;
    }

    public void append(final String html) {
        this.html.append(html);
        containsVariable |= html.indexOf("${") >= 0;
    }

    @Override
    public String getHtml() {
        return html.toString();
    }

    public boolean isContainsVariable() {
        return containsVariable;
    }

    @Override
    public String errorAt() {
        return path + ":" + lineNumbers;
    }
}

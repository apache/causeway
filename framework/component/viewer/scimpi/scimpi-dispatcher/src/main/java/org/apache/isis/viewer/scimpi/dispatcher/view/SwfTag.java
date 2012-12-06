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

import org.apache.isis.viewer.scimpi.dispatcher.action.Attributes;

public class SwfTag implements Snippet {

    public static final int END = 0;
    public static final int EMPTY = 1;
    public static final int START = 2;
    private final String tagName;
    private final int type;
    private final Attributes attributes;
    private final String lineNumbers;
    private final String path;

    public SwfTag(final String tagName, final Attributes attributes, final int type, final String lineNumbers, final String path) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.type = type;
        this.lineNumbers = lineNumbers;
        this.path = path;
    }

    @Override
    public String getHtml() {
        return tagName;
    }
    
    public String getPath() { 
        return path; 
    } 

    public int getType() {
        return type;
    }

    public String getName() {
        return tagName;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public String errorAt() {
        return path + ":" + lineNumbers;
    }

    public String debug() {
        return path + ":" + lineNumbers + " - " + getAttributes();
    }

    @Override
    public String toString() {
        String t = null;
        switch (type) {
        case EMPTY:
            t = "empty";
            break;
        case START:
            t = "start";
            break;
        case END:
            t = "end";
            break;
        }
        return "SwfTag[name=" + tagName + ",path=" + path + ",line=" + lineNumbers + ",type=" + t + "]";
    }

}

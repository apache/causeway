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


/**
 * A read-only text string. This class does support value listeners.
 */
public class Label extends TextString {
    private static final long serialVersionUID = 1L;

    public Label() {
        super();
    }

    public Label(final String text) {
        super(text);
    }

    public Label(final BusinessObject parent) {
        super(parent);
    }

    public Label(final BusinessObject parent, final String text) {
        super(parent, text);
    }

    public boolean userChangeable() {
        return false;
    }

    public String getObjectHelpText() {
        return "A Label object.";
    }
}

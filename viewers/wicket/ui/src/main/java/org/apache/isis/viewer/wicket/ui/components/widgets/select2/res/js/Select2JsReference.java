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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.res.js;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * A JavaScript reference that loads <a href="https://github.com/ivaynberg/select2/">Select2.js</a>
 */
public class Select2JsReference
extends JavaScriptResourceReference {
    private static final long serialVersionUID = 1L;

    public Select2JsReference() {
    	super(Select2JsReference.class, "select2.full.js");
    }

}

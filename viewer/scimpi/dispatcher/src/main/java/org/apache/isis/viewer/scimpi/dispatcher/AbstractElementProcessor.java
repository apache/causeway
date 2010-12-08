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


package org.apache.isis.viewer.scimpi.dispatcher;

import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public abstract class AbstractElementProcessor implements ElementProcessor, Names {

    /**
     * Return the Class for the class specified in the type attribute.  
     */
    protected Class<?> forClass(Request request) { 
        Class<?> cls = null; 
        String className = request.getOptionalProperty(TYPE);
        if (className != null) {
            try {
                cls = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ScimpiException("No class for " + className, e);
            }
        }
        return cls;
    }
    /*
    protected static final String COLLECTION = "collection";
    protected static final String CONFIRM = "confirm";
    protected static final String CLASS = "class";
    protected static final String DEFAULT = "default";
    protected static final String ELEMENT_NAME = "element-name";
    protected static final String EVEN_ROW_CLASS = "even-row";
    protected static final String ERRORS = "error";
    protected static final String FIELD = "field";
    protected static final String FIELD_NAME = "field-name";
    protected static final String FORMS = "show-forms";
    protected static final String HEADING = "heading";
    protected static final String ICON_CLASS = "icon";
    protected static final String HIDDEN = "hidden";
    protected static final String HEADER_LEVEL = "header";
    protected static final String ID = "id";
    protected static final String LEGEND = "legend";
    protected static final String LINK = "link";
    protected static final String METHOD = "method";
    protected static final String NAME = "name";
    protected static final String ODD_ROW_CLASS = "odd-row";
    protected static final String OBJECT = "object";
    protected static final String PARAMETER_NUMBER = "number";
    protected static final String PLURAL = "plural";
    protected static final String REFERENCE_NAME = "reference-name";
    protected static final String RESULT_NAME = "result-name";
    protected static final String RESULT_OVERRIDE = "result-override";
    protected static final String SCOPE = "scope";
    protected static final String SHOW_ICON = "icon";
    protected static final String SHOW_SELECT = "select";
    protected static final String SHOW_EDIT = "edit";
    protected static final String SHOW_DELETE = "delete";
    protected static final String TITLE = "title";
    protected static final String TRUNCATE = "truncate";
    protected static final String TYPE = "type";
    protected static final String VIEW = "view";
    protected static final String VALUE = "value";
    protected static final String VOID = "void";
    protected static final String WHEN = "when";
    */
}


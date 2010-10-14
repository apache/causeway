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


package org.apache.isis.webapp.action;

import java.util.Enumeration;
import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.nodes.TagNode;
import org.apache.isis.webapp.context.RequestContext;


public class Attributes {
    private static final String TRUE = " true yes on ";
    private static final String FALSE = " false no off ";
    private final TagNode tagNode;
    private final RequestContext context;

    public Attributes(TagNode tagNode, RequestContext context) {
        this.tagNode = tagNode;
        this.context = context;
    }

    public boolean isPropertySet(String name) {
        String attribute = tagNode.getAttribute(name);
        return attribute != null && !context.replaceVariables(attribute, false).equals("");
    }

    public boolean isPropertySpecified(String name) {
        String attribute = tagNode.getAttribute(name);
        return attribute != null;
    }
    
    public String getOptionalProperty(String name, boolean ensureVariablesExists) {
        return getOptionalProperty(name, null, ensureVariablesExists);
    }

    public String getOptionalProperty(String name, String defaultValue, boolean ensureVariablesExists) {
        String attribute = tagNode.getAttribute(name);
        return attribute == null ? defaultValue : context.replaceVariables(attribute, ensureVariablesExists);
    }

    public String getRequiredProperty(String name, boolean ensureVariablesExists) {
        String attribute = tagNode.getAttribute(name);
        if (attribute == null) {
            throw new RequiredPropertyException("Missing property: " + name);
        } else if (attribute.equals("")) {
            throw new RequiredPropertyException("Property not set: " + name);
       } else {
           return context.replaceVariables(attribute, ensureVariablesExists);
        }
    }
    
    public String[] getPropertyNames(String excluding[]) {
        Vector attributes = tagNode.getAttributesEx();
        String[] names = new String[attributes.size()];
        int i = 0;
        names: for (Enumeration e = attributes.elements(); e.hasMoreElements(); ) {
            String name = ((Attribute) e.nextElement()).getName();
            if (name == null) {
                continue;
            }
            for (int j = 0; j < excluding.length; j++) {
                if (name.equals(excluding[j])) {
                    continue names;
                }
            }
            if (tagNode.getAttribute(name) != null) {
                names[i++] = name;
            }
        }
        
        String[] array = new String[i];
        System.arraycopy(names, 0, array, 0, i);
        return array;
    }

    public boolean isRequested(String name) {
        return isRequested(name, false);
    }

    public boolean isRequested(String name, boolean defaultValue) {
        String flag = getOptionalProperty(name, true);
        if (flag == null) {
            return defaultValue;
        } else {
            String value = " " + flag.toLowerCase().trim() + " ";
            if (TRUE.indexOf(value) >= 0) {
                return true;
            } else if  (FALSE.indexOf(value) >= 0) {
                return false;
            } else {
                throw new PropertyException("Attribute " + name + " has illegal value " + flag);
            }
        }
    }

}


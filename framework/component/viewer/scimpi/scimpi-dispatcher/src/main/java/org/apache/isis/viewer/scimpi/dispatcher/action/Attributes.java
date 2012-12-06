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

package org.apache.isis.viewer.scimpi.dispatcher.action;

import java.util.Enumeration;
import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.nodes.TagNode;

import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;

public class Attributes {
    private static final String TRUE = " true yes on ";
    private static final String FALSE = " false no off ";
    private final TagNode tagNode;
    private final RequestContext context;

    public Attributes(final TagNode tagNode, final RequestContext context) {
        this.tagNode = tagNode;
        this.context = context;
    }

    public boolean isPropertySet(final String name) {
        final String attribute = tagNode.getAttribute(name);
        int end = attribute.length() - 1;
        final int pos = attribute.indexOf(':');
        end = pos == -1 ? end : pos;
        final String variabelName = attribute.substring(2, end);
        final Object value = context.getVariable(variabelName);
        return value != null;
        // return attribute != null &&
        // !context.replaceVariables(attribute).equals("");
    }

    public boolean isPropertySpecified(final String name) {
        final String attribute = tagNode.getAttribute(name);
        return attribute != null;
    }

    public String getOptionalProperty(final String name, final boolean ensureVariablesExists) {
        return getOptionalProperty(name, null, ensureVariablesExists);
    }

    public String getOptionalProperty(final String name, final String defaultValue, final boolean ensureVariablesExists) {
        final String attribute = tagNode.getAttribute(name);
        return attribute == null ? defaultValue : context.replaceVariables(attribute);
    }

    public String getRequiredProperty(final String name, final boolean ensureVariablesExists) {
        final String attribute = tagNode.getAttribute(name);
        if (attribute == null) {
            throw new RequiredPropertyException("Missing property: " + name);
        } else if (attribute.equals("")) {
            throw new RequiredPropertyException("Property not set: " + name);
        } else {
            return context.replaceVariables(attribute);
        }
    }

    public String[] getPropertyNames(final String excluding[]) {
        final Vector attributes = tagNode.getAttributesEx();
        final String[] names = new String[attributes.size()];
        int i = 0;
        names: for (final Enumeration e = attributes.elements(); e.hasMoreElements();) {
            final String name = ((Attribute) e.nextElement()).getName();
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

        final String[] array = new String[i];
        System.arraycopy(names, 0, array, 0, i);
        return array;
    }

    @Override
    public String toString() {
        return tagNode.toHtml(); // getAttributesEx().toString();
    }

    public boolean isRequested(final String name) {
        return isRequested(name, false);
    }

    public boolean isRequested(final String name, final boolean defaultValue) {
        final String flag = getOptionalProperty(name, true);
        if (flag == null) {
            return defaultValue;
        } else {
            return isTrue(flag);
        }
    }

    public static boolean isTrue(final String flag) {
        final String value = " " + flag.toLowerCase().trim() + " ";
        if (TRUE.indexOf(value) >= 0) {
            return true;
        } else if (FALSE.indexOf(value) >= 0) {
            return false;
        } else {
            throw new PropertyException("Illegal flag value: " + flag);
        }
    }

}

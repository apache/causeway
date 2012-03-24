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

package org.apache.isis.core.runtime.userprofile;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;

public class Options implements DebuggableWithTitle {

    private final Properties properties = new Properties();

    public void addOption(final String name, final String value) {
        properties.put(name, value);
    }

    public void addOptions(final String name, final Options options) {
        properties.put(name, options);
    }

    public Iterator<String> names() {
        final Enumeration<?> propertyNames = properties.propertyNames();
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return propertyNames.hasMoreElements();
            }

            @Override
            public String next() {
                return (String) propertyNames.nextElement();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String getString(final String name) {
        return properties.getProperty(name);
    }

    public String getString(final String name, final String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public int getInteger(final String name, final int defaultValue) {
        final String value = getString(name);
        if (value == null) {
            return defaultValue;
        } else {
            return Integer.valueOf(value).intValue();
        }
    }

    public Options getOptions(final String name) {
        Options options = (Options) properties.get(name);
        if (options == null) {
            options = new Options();
            addOptions(name, options);
        }
        return options;
    }

    public boolean isOptions(final String name) {
        return properties.get(name) instanceof Options;
    }

    public void copy(final Options options) {
        properties.putAll(options.properties);
    }

    // ///////////////////////////////
    // Debugging
    // ///////////////////////////////

    @Override
    public String debugTitle() {
        return "Options";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        final Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            final String name = (String) keys.nextElement();
            debug.appendln(name, properties.get(name));
        }
    }

}

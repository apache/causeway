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

    public void addOption(String name, String value) {
        properties.put(name, value);
    }
    
    public void addOptions(String name, Options options) {
        properties.put(name, options);
    }

    public void copy(Options options) {
        properties.putAll(options.properties);
    }

    public void debugData(DebugBuilder debug) {
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String name = (String) keys.nextElement();
            debug.appendln(name, properties.get(name));
        }
    }

    public Iterator<String>  names() {
    	final Enumeration<?> propertyNames = properties.propertyNames();
    	return new Iterator<String>() {
			public boolean hasNext() {
				return propertyNames.hasMoreElements();
			}
			public String next() {
				return (String) propertyNames.nextElement();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}};
    }
    
    public String debugTitle() {
        return "Options";
    }

    public String getString(String name) {
        return properties.getProperty(name);
    }

    public String getString(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public Options getOptions(String name) {
        Options options = (Options) properties.get(name);
        if (options == null) {
            options = new Options();
            addOptions(name, options);
        }
        return options;
    }

    public int getInteger(String name, int defaultValue) {
        String value = getString(name);
        if (value == null) {
            return defaultValue;
        } else {
            return Integer.valueOf(value).intValue();
        }
    }

    public boolean isOptions(String name) {
        return properties.get(name) instanceof Options;
    }

}


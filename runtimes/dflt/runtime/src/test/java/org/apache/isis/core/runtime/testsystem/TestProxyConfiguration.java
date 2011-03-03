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


package org.apache.isis.core.runtime.testsystem;

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.AssertionFailedError;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.commons.resource.ResourceStreamSource;


public class TestProxyConfiguration implements IsisConfiguration {

    private final Hashtable<String,String> valueByKey = new Hashtable<String,String>();

    public void add(final String key, final String value) {
        if (key == null) {
            return;
        }
        if (valueByKey.containsKey(key)) {
            throw new AssertionFailedError("Already have a value for " + " name; cannot set it again: " + value);
        }
        valueByKey.put(key, value);
    }

    @Override
    public boolean getBoolean(final String name) {
        return Boolean.valueOf(getString(name)).booleanValue();
    }

    @Override
    public boolean getBoolean(final String name, final boolean defaultValue) {
        final String str = getString(name);
        return str == null ? defaultValue : Boolean.valueOf(str).booleanValue();
    }

    @Override
    public Color getColor(final String name) {
        throw new NotYetImplementedException();
    }

    @Override
    public Color getColor(final String name, final Color defaultColor) {
        return defaultColor;
    }

    @Override
    public Font getFont(final String name) {
        throw new NotYetImplementedException();
    }

    @Override
    public Font getFont(final String name, final Font defaultValue) {
        return defaultValue;
    }

    @Override
    public int getInteger(final String name) {
        return Integer.valueOf(getString(name)).intValue();
    }

    @Override
    public int getInteger(final String name, final int defaultValue) {
        final String str = getString(name);
        return str == null ? defaultValue : Integer.valueOf(str).intValue();
    }

    @Override
    public String[] getList(final String name) {
        return new String[0];
    }

    @Override
    public IsisConfiguration getProperties(final String withPrefix) {
        final TestProxyConfiguration configuration = new TestProxyConfiguration();
        final Enumeration<String> keys = valueByKey.keys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            if (key.startsWith(withPrefix)) {
                configuration.add(key.substring(withPrefix.length()), valueByKey.get(key));
            }

        }
        return configuration;
    }

    @Override
    public String getString(final String name) {
        return valueByKey.get(name);
    }

    @Override
    public String getString(final String name, final String defaultValue) {
        final String str = getString(name);
        return str == null ? defaultValue : str;
    }

    @Override
    public boolean hasProperty(final String name) {
        throw new NotYetImplementedException();
    }

    public String referedToAs(final String name) {
        throw new NotYetImplementedException();
    }

    @Override
    public IsisConfiguration createSubset(final String prefix) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isEmpty() {
        throw new NotYetImplementedException();
    }

    @Override
    public int size() {
        throw new NotYetImplementedException();
    }

    @Override
    public Iterator<String> iterator() {
        throw new NotYetImplementedException();
    }

	@Override
    public ResourceStreamSource getResourceStreamSource() {
		return null;
	}

    @Override
    public void debugData(final DebugString debug) {}

    @Override
    public String debugTitle() {
        throw new NotYetImplementedException();
    }

    @Override
    public void injectInto(Object candidate) {}


}

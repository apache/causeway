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
package org.apache.isis.unittestsupport.config.internal;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.unittestsupport.config.IsisConfigurationLegacy;

import static org.apache.isis.commons.internal.base._With.computeIfAbsent;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "ofProperties")
final class _Config_Instance implements IsisConfigurationLegacy {

    private final Map<String, String> properties;
    private final Function<String, Boolean> booleanParser;
    private final Function<String, Integer> integerParser;
    private final Function<String, Color> colorParser;
    private final Function<String, Font> fontParser;
    private final Function<String, String[]> listParser;

    @Override
    public boolean getBoolean(String name) {
        return computeIfAbsent(booleanParser.apply(getString(name)), ()->false);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return computeIfAbsent(booleanParser.apply(getString(name)), ()->defaultValue);
    }

    @Override
    public Color getColor(String name) {
        return colorParser.apply(getString(name));
    }

    @Override
    public Color getColor(String name, Color defaultValue) {
        return computeIfAbsent(colorParser.apply(getString(name)), ()->defaultValue);
    }

    @Override
    public Font getFont(String name) {
        return fontParser.apply(getString(name));
    }

    @Override
    public Font getFont(String name, Font defaultValue) {
        return computeIfAbsent(fontParser.apply(getString(name)), ()->defaultValue);
    }

    @Override
    public String[] getList(String name) {
        return computeIfAbsent(listParser.apply(getString(name)), ()->_Constants.emptyStringArray);
    }

    @Override
    public String[] getList(String name, String defaultListAsCommaSeparatedArray) {
        return computeIfAbsent(listParser.apply(getString(name)), ()->listParser.apply(defaultListAsCommaSeparatedArray));
    }

    @Override
    public int getInteger(String name) {
        return computeIfAbsent(integerParser.apply(getString(name)), ()->0);
    }

    @Override
    public int getInteger(String name, int defaultValue) {
        return computeIfAbsent(integerParser.apply(getString(name)), ()->defaultValue);
    }

    @Override
    public String getString(String name) {
        return properties.get(name);
    }

    @Override
    public String getString(String name, String defaultValue) {
        return properties.getOrDefault(name, defaultValue);
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    // -- CONVERSION

    @Override
    public Map<String, String> copyToMap() {
        Map<String, String> copy = _Maps.newHashMap();
        properties.forEach(copy::put);
        return copy;
    }

    @Override
    public IsisConfigurationLegacy copy() {
        val copy = _Maps.<String, String>newHashMap();
        properties.forEach(copy::put);
        return new _Config_Instance(copy, 
                booleanParser, 
                integerParser, 
                colorParser, 
                fontParser, 
                listParser);
    }

    @Override
    public IsisConfigurationLegacy subset(@Nullable String withPrefix) {
        if(_Strings.isNullOrEmpty(withPrefix)) {
            return copy();
        }

        val prefixWithTerminalDot = _Strings.suffix(withPrefix, ".");
        val filteredByPrefix = _Maps.<String, String>newHashMap();

        properties.forEach((k, v)->{
            if(k.startsWith(prefixWithTerminalDot)) {
                filteredByPrefix.put(k, v);
            }
        });

        return new _Config_Instance(filteredByPrefix, 
                booleanParser, 
                integerParser, 
                colorParser, 
                fontParser, 
                listParser);
    }

    @Override
    public IsisConfigurationLegacy subsetWithNamesStripped(String withPrefix) {
        if(_Strings.isNullOrEmpty(withPrefix)) {
            return copy();
        }

        val prefixWithTerminalDot = _Strings.suffix(withPrefix, ".");
        val prefixLen = prefixWithTerminalDot.length();
        val filteredByPrefix = _Maps.<String, String>newHashMap(); 

        properties.forEach((k, v)->{
            if(k.startsWith(prefixWithTerminalDot)) {

                val strippedKey = k.substring(prefixLen);

                if(strippedKey.length()>0) {
                    filteredByPrefix.put(strippedKey, v);
                }
            }
        });

        return new _Config_Instance(filteredByPrefix, 
                booleanParser, 
                integerParser, 
                colorParser, 
                fontParser, 
                listParser);
    }



}

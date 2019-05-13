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
package org.apache.isis.config.internal;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.Properties;

import org.apache.isis.config.IsisConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "ofProperties")
final class _Config_Instance implements IsisConfiguration {
    
    private final Properties properties;

    @Override
    public IsisConfiguration createSubset(String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getBoolean(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Color getColor(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Color getColor(String name, Color defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Font getFont(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Font getFont(String name, Font defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getList(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getList(String name, String defaultListAsCommaSeparatedArray) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getInteger(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getInteger(String name, int defaultValue) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IsisConfiguration getProperties(String withPrefix) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(String name, String defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasProperty(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<String, String> copyToMap() {
        // TODO Auto-generated method stub
        return null;
    }



}

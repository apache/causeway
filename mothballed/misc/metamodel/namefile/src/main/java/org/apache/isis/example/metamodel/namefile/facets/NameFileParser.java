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

package org.apache.isis.example.metamodel.namefile.facets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class NameFileParser {

    private static final String CONFIG_NAMEFILE_PROPERTIES = "config/namefile.properties";
    private Properties properties;

    public void parse() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_NAMEFILE_PROPERTIES);
        if (in == null) {
            in = getClass().getClassLoader().getResourceAsStream(CONFIG_NAMEFILE_PROPERTIES);
        }
        if (in == null) {
            throw new NullPointerException("Cannot locate resource '" + CONFIG_NAMEFILE_PROPERTIES + "'");
        }
        properties = new Properties();
        properties.load(in);
    }

    public String getName(final Class<?> cls) {
        return properties.getProperty(cls.getCanonicalName());
    }

    public String getMemberName(final Class<?> cls, final String memberName) {
        return properties.getProperty(cls.getCanonicalName() + "#" + memberName);
    }

}

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

package org.apache.isis.objectstore.xml.internal.data.xml;

import java.io.IOException;
import java.io.Writer;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;

public class Utils {
    
    private Utils(){}

    public static final String ENCODING_PROPERTY = ConfigurationConstants.ROOT + "xmlos.encoding";
    public static final String DEFAULT_ENCODING = "ISO-8859-1";

    public static String lookupCharset(final IsisConfiguration configuration) {
        return configuration.getString(ENCODING_PROPERTY, DEFAULT_ENCODING);
    }

    public static String attribute(final String name, final String value) {
        return appendAttribute(new StringBuilder(), name, value).toString();
    }

    public static StringBuilder appendAttribute(StringBuilder buf, final String name, final String value) {
        return buf.append(" ").append(name).append("=\"").append(value).append("\"");
    }

    public static Writer appendAttribute(Writer buf, final String name, final String value) throws IOException {
        buf.append(" ");
        buf.append(name);
        buf.append("=\"");
        buf.append(value);
        buf.append("\"");
        return buf;
    }

}

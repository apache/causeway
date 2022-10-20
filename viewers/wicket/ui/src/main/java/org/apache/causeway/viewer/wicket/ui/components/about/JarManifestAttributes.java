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
package org.apache.causeway.viewer.wicket.ui.components.about;

import java.io.Serializable;
import java.net.URL;
import java.util.Map.Entry;

public class JarManifestAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    public static JarManifestAttributes jarName(String jarName) {
        return new JarManifestAttributes(JarManifestAttributes.Type.JAR_NAME, jarName);
    }

    public static JarManifestAttributes jarUrl(URL jarUrl) {
        return new JarManifestAttributes(JarManifestAttributes.Type.JAR_URL, jarUrl != null? jarUrl.toExternalForm(): "");
    }

    public static JarManifestAttributes attribute(Entry<Object,Object> entry) {
        StringBuilder buf = new StringBuilder();
        buf .append("    ")
        .append(entry.getKey())
        .append(": ")
        .append(entry.getValue())
        .append("\n")
        ;
        return new JarManifestAttributes(JarManifestAttributes.Type.MANIFEST_ATTRIBUTE, buf.toString());
    }

    enum Type {
        JAR_NAME,
        JAR_URL,
        MANIFEST_ATTRIBUTE
    }

    private final Type type;
    private final String line;

    public JarManifestAttributes(Type type, String line) {
        this.type = type;
        this.line = line;
    }
    public JarManifestAttributes.Type getType() {
        return type;
    }
    public String getLine() {
        return line;
    }

}
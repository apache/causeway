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
package demoapp.dom._infra.resources;

import java.util.Map;
import java.util.regex.Pattern;

class TagHandler {

    private final Object tagsValue;
    private boolean within = false;

    TagHandler(final Map<String, Object> attributes) {
        tagsValue = attributes.get("tags");
    }

    public String handle(final String line) {
        if(tagsValue == null) {
            return line;
        }

        if (matches(line, "tag")) {
            within = true;
            return null;
        }

        if (matches(line, "end")) {
            within = false;
            return null;
        }

        return within ? line : null;
    }

    private boolean matches(String line, String macro) {
        final Pattern pattern = Pattern.compile("//\\s*" +
                macro +
                "::" +
                tagsValue +
                "\\[]\\s*");
        return pattern.matcher(line).matches();
    }

}

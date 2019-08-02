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

package org.apache.isis.webapp.modules.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.isis.commons.internal.base._Strings.KeyValuePair;

import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.commons.internal.base._With.requires;

/**
 * Package private mixin for ResourceServlet
 * @since 2.0
 */
final class ResourceServlet_HtmlTemplateVariables {

    final Map<String, String> placeholders = new HashMap<>();
    
    public ResourceServlet_HtmlTemplateVariables(KeyValuePair ... kvPairs) {
        requires(kvPairs, "placeholders");
        
        stream(kvPairs)
        .forEach(kvPair->placeholders.put(kvPair.getKey(), kvPair.getValue()));
    }
    
    /**
     * @param template HTML template containing placeholders
     * @return HTML post-processed template with all the placeholders replaced by their values
     */
    public String applyTo(final String template) {
        
        String acc = template;        
        
        for ( Entry<String, String> entry : placeholders.entrySet()) {
            final String placeholderLiteral = "${" + entry.getKey() + "}";  
            final String placeholderValue = entry.getValue();
            
            acc = acc.replace(placeholderLiteral, placeholderValue);
        }

        return acc;
    }

}

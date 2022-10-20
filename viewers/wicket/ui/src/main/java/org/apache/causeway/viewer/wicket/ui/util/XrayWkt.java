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
package org.apache.causeway.viewer.wicket.ui.util;

import java.util.Map;

import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class XrayWkt {

    @Getter @Setter
    private boolean enabled = false;

    public void ifEnabledDo(final @NonNull Runnable runnable) {
        if(isEnabled()) {
            runnable.run();
        }
    }

    // -- FORMATTERS

    public String formatAsListGroup(final @Nullable Map<String, String> keyValuePairs) {
        val sb = new StringBuilder();
        sb.append("<ul class=\"list-group\">");
        if(keyValuePairs!=null) {
            keyValuePairs.forEach((key, value)->{
                sb
                .append("<li class=\"list-group-item\">")
                .append(key)
                .append(": ")
                .append("<b>").append(value).append("</b>")
                .append("</li>");
            });
        }
        sb.append("</ul>");
        return sb.toString();
    }

}

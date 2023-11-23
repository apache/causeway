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
package org.apache.causeway.applib.fa;

import java.util.Map;
import java.util.Optional;

import org.apache.causeway.applib.fa.FontAwesomeLayers.IconType;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.JsonUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
class FontAwesomeJsonParser {

    FontAwesomeLayers parse(final String json) {
        var map = JsonUtils.tryRead(Map.class, json)
                .valueAsNonNullElseFail();
        var iconType = IconType.valueOf((String)map.get("iconType"));
        switch (iconType) {
        case STACKED:{
            final var stackBuilder = FontAwesomeLayers.stackBuilder()
                    .postition(Optional.ofNullable((String)map.get("postition"))
                            .map(CssClassFaPosition::valueOf)
                            .orElse(CssClassFaPosition.LEFT))
                    .containerCssClasses((String)map.get("containerCssClasses"))
                    .containerCssStyle((String)map.get("containerCssStyle"));

            _NullSafe.streamAutodetect(map.get("iconEntries"))
                .map(Map.class::cast)
                .forEach(iconEntryAsMap->{
                    stackBuilder.addIconEntry(
                            (String)iconEntryAsMap.get("cssClasses"),
                            (String)iconEntryAsMap.get("cssStyle"));
                });

            return stackBuilder
                    .build();
        }
        default:
            throw _Exceptions.unmatchedCase(iconType);
        }
    }

}

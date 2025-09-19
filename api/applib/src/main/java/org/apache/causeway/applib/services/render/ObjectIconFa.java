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
package org.apache.causeway.applib.services.render;

import java.nio.charset.StandardCharsets;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.title.TitleService;

/**
 * Icon image based on {@link FontAwesomeLayers}
 *
 * @see ObjectIcon
 * @see TitleService
 * @since 4.0
 */
public record ObjectIconFa(
        String shortName,
        FontAwesomeLayers fontAwesomeLayers
        ) implements ObjectIcon {

    @Override
    public String mediaType() {
        return "text/css";
    }

    @Override
    public byte[] iconData() {
        return fontAwesomeLayers.toQuickNotation().getBytes(StandardCharsets.UTF_8);
    }

}

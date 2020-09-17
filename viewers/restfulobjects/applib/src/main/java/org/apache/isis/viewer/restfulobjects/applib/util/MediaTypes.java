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

package org.apache.isis.viewer.restfulobjects.applib.util;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.isis.commons.internal.base._Strings;

public class MediaTypes {

    /**
     * Same as {@code MediaType.valueOf(type)}, but with fallback in case {@code MediaType.valueOf(type)}
     * throws an IllegalArgumentException.
     * <br/><br/>
     *
     * The fallback is to retry with some special characters replaces in String {@code type}.
     *
     * @param type
     * @return
     */
    public static MediaType parse(String type) {

        if(type==null)
            return MediaType.valueOf(null);

        try {

            return MediaType.valueOf(type);

        } catch (IllegalArgumentException e) {


            List<String> chunks = _Strings.splitThenStream(type, ";")
                    .collect(Collectors.toList());

            final StringBuilder sb = new StringBuilder();
            sb.append(chunks.get(0));

            if(chunks.size()>1) {
                chunks.stream()
                .skip(1)
                .map(chunk->chunk.replace(":", "..").replace("/", "."))
                .forEach(chunk->sb.append(';').append(chunk));
            }

            return MediaType.valueOf(sb.toString());

        }


    }

}

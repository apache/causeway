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

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;

/**
 * <h1>Experimental</h1>
 * Service that renders various aspects of a domain object, enum or value, as presented with the UI.
 * <ul>
 * <li>icon</li>
 * <li>title</li>
 * <li>description</li>
 * </ul>
 *
 * @since 4.0 {@index}
 */
public interface ObjectRenderService {

    String iconToHtml(@Nullable ObjectIcon objectIcon, IconSize iconSize);

    static ObjectRenderService fallback() {
        return (@Nullable ObjectIcon objectIcon, IconSize iconSize) -> {
            //if(true) return "<i class=\"%s\"></i>".formatted("fa-solid fa-thumbs-up");

            //TODO not supported yet as requires resource caching from wicket
//                if(objectIcon instanceof ObjectIconUrlBased urlBased)
//                    return "<img src=\"" + urlBased.url().toExternalForm() + "\"/>";
            if(objectIcon instanceof ObjectIconEmbedded embedded)
                return "<img src=\"" + embedded.dataUri().toExternalForm() + "\"/>";
            if(objectIcon instanceof ObjectIconFa fa)
                return fa.fontAwesomeLayers().toHtml();

            return null;
        };
    }

}

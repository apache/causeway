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
package org.apache.causeway.core.metamodel.facets.object.icon;

import java.io.Serializable;

import org.apache.causeway.applib.services.title.TitleService;

/**
 * Icon image data class-path resource reference.
 *
 * @see TitleService
 * @since 2.0 revised for 4.0 {@index}
 */
public sealed interface ObjectIcon extends Serializable
permits ObjectIconFa, ObjectIconEmbedded, ObjectIconUrlBased {

    /**
     * Name for the image, usually corresponds to the domain object's class simple name.
     */
    String shortName();

    /**
     * The image media type, e.g. {@code image/pdf} or {@code text/css} for font-awesome icons.
     */
    String mediaType();

    /**
     * Image data as bytes (plain, no encoding) or 'quick-notation' in support of font awesome layers.
     */
    byte[] iconData();

}

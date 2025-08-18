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
import java.net.URL;

import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.net.DataUri;

/**
 * Icon image data class-path resource reference.
 *
 * @see ObjectIconService
 * @since 2.0 revised for 4.0
 */
public sealed interface ObjectIcon extends Serializable
permits ObjectIconEmbedded, ObjectIconUrlBased {

    // -- FACTORIES

    /**
     * Create an ObjectIcon and eagerly read in image data from
     * class-path resources.
     */
    public static ObjectIcon eager(
            final String shortName,
            final URL url,
            final CommonMimeType mimeType) {
        var objectIcon = lazy(shortName, url, mimeType);
        ((ObjectIconUrlBased) objectIcon).iconData(); // memoize
        return objectIcon;
    }

    /**
     * Create an ObjectIcon and not yet read in image data from
     * class-path resources.
     */
    public static ObjectIcon lazy(
            final String shortName,
            final URL url,
            final CommonMimeType mimeType) {
        return new ObjectIconUrlBased(shortName, url, mimeType, new _StableValue<>());
    }

    public static ObjectIcon embedded(String shortName, DataUri dataUri) {
        return new ObjectIconEmbedded(shortName, dataUri);
    }

    // --

    String shortName();
    String mediaType();
    byte[] iconData();

}

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

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Objects;

import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.internal.base._Strings;

/**
 * Icon image data class-path resource reference.
 *
 * @see ObjectIconService
 * @since 2.0
 */
public record ObjectIcon(
        String shortName,
        URL url,
        CommonMimeType mimeType,
        String identifier,
        _StableValue<byte[]> iconData
        ) implements Serializable {

    // -- FACTORIES

    /**
     * Create an ObjectIcon and eagerly read in image data from
     * class-path resources.
     */
    public static ObjectIcon eager(
            final String shortName,
            final URL url,
            final CommonMimeType mimeType) {
        var id = _Strings.base64UrlEncode(url.getPath());
        var objectIcon = new ObjectIcon(shortName, url, mimeType, id, new _StableValue<>());
        objectIcon.asBytes(); // memoize
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
        var id = _Strings.base64UrlEncode(url.getPath());
        return new ObjectIcon(shortName, url, mimeType, id, new _StableValue<>());
    }

    // -- EQUALITY

    @Override
    public final boolean equals(Object o) {
        if(this == o) return true;
        return o instanceof ObjectIcon other
            ? Objects.equals(this.shortName, other.shortName)
                    && Objects.equals(this.url, other.url)
                    && Objects.equals(this.mimeType, other.mimeType)
                    && Objects.equals(this.identifier, other.identifier)
            : false;
    }

    public byte[] asBytes() {
        return iconData.orElseSet(()->{
            try(final InputStream is = url.openStream()){
                return _Bytes.of(is);
            } catch (Exception e) {
                return _Constants.emptyBytes;
            }
        });
    }

}

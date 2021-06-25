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
package org.apache.isis.core.metamodel.facets.object.icon;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;
import lombok.val;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class ObjectIcon
implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Create an ObjectIcon and eagerly read in image data from
     * class-path resources.
     */
    public static ObjectIcon eager(
            final String shortName,
            final URL url,
            final CommonMimeType mimeType) {
        val id = _Strings.base64UrlEncode(url.getPath());
        val objectIcon = new ObjectIcon(shortName, url, mimeType, id);
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
        val id = _Strings.base64UrlEncode(url.getPath());
        return new ObjectIcon(shortName, url, mimeType, id);
    }

    @Getter private @NonNull String shortName;
    @Getter private @NonNull URL url;
    @Getter private @NonNull CommonMimeType mimeType;
    @Getter private @NonNull String identifier;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private transient byte[] iconData;

    @Synchronized
    public byte[] asBytes() {

        if(iconData==null) {
            try(final InputStream is = url.openStream()){
                iconData = _Bytes.of(is);
            } catch (Exception e) {
                iconData = _Constants.emptyBytes;
            }
        }

        return iconData;
    }

}

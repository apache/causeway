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
package org.apache.isis.core.metamodel.facets.value.imageawt;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.image._Images;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.image.ImageValueSemanticsProviderAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

public class JavaAwtImageValueSemanticsProvider
extends ImageValueSemanticsProviderAbstract<BufferedImage> {

    public JavaAwtImageValueSemanticsProvider(final FacetHolder holder) {
        super(holder, BufferedImage.class);
    }

    @Override
    public int getWidth(final @Nullable ManagedObject object) {
        return unwrap(object).map(BufferedImage::getWidth).orElse(0);
    }

    @Override
    public int getHeight(final @Nullable ManagedObject object) {
        return unwrap(object).map(BufferedImage::getHeight).orElse(0);
    }

    @Override
    public Optional<BufferedImage> getImage(final @Nullable ManagedObject object) {
        return unwrap(object);
    }

    @Override @Nullable
    public String toEncodedString(final @Nullable BufferedImage image) {
        if(image==null) {
            return null;
        }
        return _Images.toBase64(image);
    }

    @Override @Nullable
    public BufferedImage fromEncodedString(final @Nullable String base64ImageData) {
        if(_Strings.isNullOrEmpty(base64ImageData)) {
            return null;
        }
        /*sonar-ignore-on*/
        return _Images.fromBase64(base64ImageData);
        /*sonar-ignore-off*/
    }

    // -- HELPER

    private Optional<BufferedImage> unwrap(final @Nullable ManagedObject adapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            return Optional.empty();
        }
        /*sonar-ignore-on*/
        return Optional.ofNullable((BufferedImage) adapter.getPojo());
        /*sonar-ignore-off*/
    }


}

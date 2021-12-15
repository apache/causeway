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
package org.apache.isis.core.metamodel.valuesemantics;

import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.inject.Named;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.OrderRelation;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.image._Images;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.BufferedImageValueSemantics")
public class BufferedImageValueSemantics
extends ValueSemanticsAbstract<BufferedImage>
implements
    ImageValueSemantics,
    OrderRelation<BufferedImage, Void>,
    EncoderDecoder<BufferedImage> {

    @Override
    public Class<BufferedImage> getCorrespondingClass() {
        return BufferedImage.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- ORDER RELATION

    @Override
    public Void epsilon() {
        return null; // not used
    }

    @Override
    public int compare(final BufferedImage a, final BufferedImage b, final Void epsilon) {
        return _Images.compare(a, b);
    }

    @Override
    public boolean equals(final BufferedImage a, final BufferedImage b, final Void epsilon) {
        return compare(a, b, epsilon) == 0;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final @Nullable BufferedImage image) {
        if(image==null) {
            return null;
        }
        return _Images.toBase64(image);
    }

    @Override
    public BufferedImage fromEncodedString(final @Nullable String base64ImageData) {
        if(_Strings.isNullOrEmpty(base64ImageData)) {
            return null;
        }
        /*sonar-ignore-on*/
        return _Images.fromBase64(base64ImageData);
        /*sonar-ignore-off*/
    }

    // -- FACET

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

    // -- HELPER

    private Optional<BufferedImage> unwrap(final @Nullable ManagedObject adapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            return Optional.empty();
        }
        /*sonar-ignore-on*/
        return Optional.ofNullable((BufferedImage) adapter.getPojo());
        /*sonar-ignore-off*/
    }

    @Override
    public Can<BufferedImage> getExamples() {
        return Can.of(
                new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB),
                new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB));
    }

}

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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.applib.value.semantics.OrderRelation;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.image._Images;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.schema.common.v2.BlobDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

@Component
@Named("causeway.val.BufferedImageValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class BufferedImageValueSemantics
extends ValueSemanticsAbstract<BufferedImage>
implements
    ImageValueSemantics,
    OrderRelation<BufferedImage, Void> {

    @Override
    public Class<BufferedImage> getCorrespondingClass() {
        return BufferedImage.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BLOB;
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

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final BufferedImage value) {
        return decomposeAsNullable(value, this::toBlob, ()->null);
    }

    @Override
    public BufferedImage compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getBlob, this::fromBlobDto, ()->null);
    }

    private BufferedImage fromBlobDto(final BlobDto blobDto) {
        return blobDto!=null
                    && blobDto.getBytes()!=null
                            ? _Images.fromBytes(blobDto.getBytes())
                            : null;
    }

    private Blob toBlob(final BufferedImage img) {
        return img!=null
                // arbitrary name and mimetype - not used when recovering the image
                ? new Blob("image", CommonMimeType.PNG.getBaseType(), _Images.toBytes(img))
                : null;
    }

    // -- ENCODER DECODER

//    @Override
//    public String toEncodedString(final @Nullable BufferedImage image) {
//        if(image==null) {
//            return null;
//        }
//        return _Images.toBase64(image);
//    }
//
//    @Override
//    public BufferedImage fromEncodedString(final @Nullable String base64ImageData) {
//        if(_Strings.isNullOrEmpty(base64ImageData)) {
//            return null;
//        }
//        /*sonar-ignore-on*/
//        return _Images.fromBase64(base64ImageData);
//        /*sonar-ignore-off*/
//    }

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

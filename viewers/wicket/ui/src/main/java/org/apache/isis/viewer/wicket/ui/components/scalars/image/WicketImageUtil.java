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
package org.apache.isis.viewer.wicket.ui.components.scalars.image;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.apache.wicket.extensions.markup.html.image.resource.ThumbnailImageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.valuesemantics.ImageValueSemantics;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WicketImageUtil {

    public Optional<Image> asWicketImage(
            final @NonNull String id,
            final @Nullable BufferedImage buffImg) {

        if(buffImg == null) {
            return Optional.empty();
        }

        final var imageResource = new BufferedDynamicImageResource();
        imageResource.setImage(buffImg);

        final var thumbnailImageResource = new ThumbnailImageResource(imageResource, 300);

        final var wicketImage = new NonCachingImage(id, thumbnailImageResource);
        wicketImage.setOutputMarkupId(true);

        return Optional.of(wicketImage);
    }

    // -- SHORTCUTS

    public Optional<Image> asWicketImage(
            final @NonNull String id,
            final @Nullable Blob blob) {

        final var buffImg = Optional.ofNullable(blob)
        .flatMap(Blob::asImage)
        .orElse(null);

        return asWicketImage(id, buffImg);
    }

    public Optional<Image> asWicketImage(
            final @NonNull String id,
            final @NonNull ScalarModel model) {

      final ManagedObject adapter = model.getObject();
      if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
          return Optional.empty();
      }

      final var spec = model.getTypeOfSpecification();

      return spec.streamValueSemantics(ImageValueSemantics.class)
      .map(imageValueSemantics->imageValueSemantics.getImage(adapter).orElse(null))
      .filter(_NullSafe::isPresent)
      .map(buffImg->asWicketImage(id, buffImg).orElse(null))
      .filter(_NullSafe::isPresent)
      .findFirst();

    }


}

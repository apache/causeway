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
package demoapp.dom.types.isis.images.samples;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.value.Image;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.resources._Resources;

import lombok.SneakyThrows;
import lombok.val;

import demoapp.dom.types.Samples;

@Service
public class IsisImagesSamples implements Samples<Image> {

    @Override
    public Stream<Image> stream() {
        return Stream.of(
                "apache-wicket.png", "byte-buddy.png", "datanucleus-logo.png",
                "project-lombok.png", "resteasy_logo_600x.gif", "spring-boot-logo.png")
                .map(this::loadImage);
    }


    @SneakyThrows
    private Image loadImage(String name) {
        val bytes = _Bytes.of(_Resources.load(IsisImagesSamples.class, name ));
        return isisImageService.bytesToImage(bytes);
    }

    @Inject
    IsisImageService isisImageService;

}

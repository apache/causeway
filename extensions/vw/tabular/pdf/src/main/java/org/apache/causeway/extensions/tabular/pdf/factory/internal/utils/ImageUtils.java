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
package org.apache.causeway.extensions.tabular.pdf.factory.internal.utils;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.causeway.extensions.tabular.pdf.factory.internal.image.Image;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ImageUtils {

	/**
	 * Simple reading image from file
	 *
	 * @param imageFile
	 *            {@link File} from which image will be loaded
	 * @return {@link Image}
	 * @throws IOException if loading image fails
	 */
	public static Image readImage(final File imageFile) throws IOException {
		final BufferedImage bufferedImage = ImageIO.read(imageFile);
		return new Image(bufferedImage);
	}

	/**
	 * <p>
	 * Provide an ability to scale {@link Image} on desired {@link Dimension}
	 * </p>
	 *
	 * @param imageWidth Original image width
	 * @param imageHeight Original image height
	 * @param boundWidth Desired image width
	 * @param boundHeight Desired image height
	 * @return {@code Array} with image dimension. First value is width and second is height.
	 */
	public static float[] getScaledDimension(final float imageWidth, final float imageHeight, final float boundWidth, final float boundHeight) {
		float newImageWidth = imageWidth;
		float newImageHeight = imageHeight;

		// first check if we need to scale width
		if (imageWidth > boundWidth) {
			newImageWidth = boundWidth;
			// scale height to maintain aspect ratio
			newImageHeight = (newImageWidth * imageHeight) / imageWidth;
		}

		// then check if the new height is also bigger than expected
		if (newImageHeight > boundHeight) {
			newImageHeight = boundHeight;
			// scale width to maintain aspect ratio
			newImageWidth = (newImageHeight * imageWidth) / imageHeight;
		}

		float[] imageDimension = { newImageWidth, newImageHeight };
		return imageDimension;
	}
}

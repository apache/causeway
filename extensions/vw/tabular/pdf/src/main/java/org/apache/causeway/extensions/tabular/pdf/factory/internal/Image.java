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
package org.apache.causeway.extensions.tabular.pdf.factory.internal;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

final class Image {

	private final BufferedImage image;
	private float width;
	private float height;
	private PDImageXObject imageXObject = null;

	// standard DPI
	private float[] dpi = { 72, 72 };
	private float quality = 1f;

	/**
	 * Constructor for default images
	 * @param image
	 *            {@link BufferedImage}
	 */
	public Image(final BufferedImage image) {
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}

	public Image(final BufferedImage image, final float dpi) {
		this(image, dpi, dpi);
	}

	public Image(final BufferedImage image, final float dpiX, final float dpiY) {
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.dpi[0] = dpiX;
		this.dpi[1] = dpiY;
		scaleImageFromPixelToPoints();
	}

	/**
	 * Drawing simple {@link Image} in {@link PDPageContentStream}.
	 * @param doc
	 *            {@link PDDocument} where drawing will be applied
	 * @param stream
	 *            {@link PDPageContentStream} where drawing will be applied
	 * @param x
	 *            X coordinate for image drawing
	 * @param y
	 *            Y coordinate for image drawing
	 * @throws IOException if loading image fails
	 */
	public void draw(final PDDocument doc, final PageContentStreamOptimized stream, final float x, final float y) throws IOException
	{
		if (imageXObject == null) {
			if(quality == 1f) {
				imageXObject = LosslessFactory.createFromImage(doc, image);
			} else {
				imageXObject = JPEGFactory.createFromImage(doc, image, quality);
			}
		}
		stream.drawImage(imageXObject, x, y - height, width, height);
	}

	/**
	 * Method which scale {@link Image} with designated width
	 * @param width
	 *            Maximal width where {@link Image} needs to be scaled
	 * @return Scaled {@link Image}
	 */
	public Image scaleByWidth(final float width) {
		float factorWidth = width / this.width;
		return scale(width, this.height * factorWidth);
	}

	/**
	 * Method which scale {@link Image} with designated height
	 * @param height
	 *            Maximal height where {@link Image} needs to be scaled
	 * @return Scaled {@link Image}
	 */
	public Image scaleByHeight(final float height) {
		float factorHeight = height / this.height;
		return scale(this.width * factorHeight, height);
	}

	public float getImageWidthInPoints(final float dpiX) {
		return this.width * 72f / dpiX;
	}

	public float getImageHeightInPoints(final float dpiY) {
		return this.height * 72f / dpiY;
	}

	/**
	 * Method which scale {@link Image} with designated width und height
	 * @param boundWidth
	 *            Maximal width where {@link Image} needs to be scaled
	 * @param boundHeight
	 *            Maximal height where {@link Image} needs to be scaled
	 * @return scaled {@link Image}
	 */
	public Image scale(final float boundWidth, final float boundHeight) {
		float[] imageDimension = getScaledDimension(this.width, this.height, boundWidth, boundHeight);
		this.width = imageDimension[0];
		this.height = imageDimension[1];
		return this;
	}

	public float getHeight() {
		return height;
	}

	public float getWidth() {
		return width;
	}

	public void setQuality(final float quality) throws IllegalArgumentException {
		if(quality <= 0 || quality > 1) {
			throw new IllegalArgumentException(
					"The quality value must be configured greater than zero and less than or equal to 1");
		}
		this.quality = quality;
	}

	// -- HELPER

	private void scaleImageFromPixelToPoints() {
	    float dpiX = dpi[0];
	    float dpiY = dpi[1];
	    scale(getImageWidthInPoints(dpiX), getImageHeightInPoints(dpiY));
	}

	/**
     * Scales {@link Image} to desired dimensions
     * @param imageWidth Original image width
     * @param imageHeight Original image height
     * @param boundWidth Desired image width
     * @param boundHeight Desired image height
     * @return {@code Array} with image dimension. First value is width and second is height.
     */
	private static float[] getScaledDimension(final float imageWidth, final float imageHeight, final float boundWidth, final float boundHeight) {
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

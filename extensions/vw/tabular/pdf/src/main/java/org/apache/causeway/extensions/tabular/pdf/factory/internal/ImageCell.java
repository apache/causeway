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

import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.causeway.extensions.tabular.pdf.factory.internal.image.Image;

import lombok.Getter;

class ImageCell<T extends PDPage> extends Cell<T> {

    @Getter private Image image;
	private final HorizontalAlignment align;
	private final VerticalAlignment valign;

	ImageCell(final Row<T> row, final float width, final Image image, final boolean isCalculated) {
		super(row, width, null, isCalculated);
		this.image = image;
		if(image.getWidth() > getInnerWidth()){
			scaleToFit();
		}
		this.align = HorizontalAlignment.LEFT;
		this.valign = VerticalAlignment.TOP;
	}

	public void scaleToFit() {
		image = image.scaleByWidth(getInnerWidth());
	}

	ImageCell(final Row<T> row, final float width, final Image image, final boolean isCalculated, final HorizontalAlignment align,
			final VerticalAlignment valign) {
		super(row, width, null, isCalculated, align, valign);
		this.image = image;
		if(image.getWidth() > getInnerWidth()){
			scaleToFit();
		}
		this.align = align;
		this.valign = valign;
	}

	@Override
	public float getTextHeight() {
		return image.getHeight();
	}

	@Override
	public float getHorizontalFreeSpace() {
		return getInnerWidth() - image.getWidth();
	}

	@Override
	public float getVerticalFreeSpace() {
		return getInnerHeight() - image.getHeight();
	}

}

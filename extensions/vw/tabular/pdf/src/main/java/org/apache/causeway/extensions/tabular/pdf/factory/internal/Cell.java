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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.causeway.extensions.tabular.pdf.factory.FontUtils;
import org.apache.causeway.extensions.tabular.pdf.factory.HorizontalAlignment;
import org.apache.causeway.extensions.tabular.pdf.factory.LineStyle;
import org.apache.causeway.extensions.tabular.pdf.factory.VerticalAlignment;

import lombok.Getter;
import lombok.Setter;

public class Cell<T extends PDPage> {

	@Getter private float width;
	/**
     * the height of the single cell.
     */
	@Setter private Float height;
	/** cell's text value */
	@Getter private String text;

	@Getter @Setter private URL url = null;

	private PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

	/**
     * The {@linkplain PDFont font} used for bold text, for example in
     * {@linkplain #isHeaderCell() header cells}.
     */
	@Getter @Setter private PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

	/** {@link PDFont} size for current cell (in points) */
	@Getter private float fontSize = 8;
	@Getter @Setter private Color fillColor;
	/**
	 * {@link Color} of the cell's text
	 */
	@Getter @Setter private Color textColor = Color.BLACK;
	private final Row<T> row;
	private WrappingFunction wrappingFunction;
	@Getter @Setter private boolean headerCell = false;
	@Getter @Setter private boolean colspanCell = false;

	// default padding
	/** cell's left padding (in points) */
	@Getter @Setter private float leftPadding = 5f;
	/** cell's right padding (in points) */
	@Getter @Setter private float rightPadding = 5f;
	/** cell's top padding (in points) */
	@Getter @Setter private float topPadding = 5f;
	/** cell's bottom padding (in points) */
	@Getter @Setter private float bottomPadding = 5f;

	// default border
	@Getter @Setter private LineStyle leftBorderStyle = new LineStyle(Color.BLACK, 1);
	@Getter @Setter private LineStyle rightBorderStyle = new LineStyle(Color.BLACK, 1);
	@Getter @Setter private LineStyle topBorderStyle = new LineStyle(Color.BLACK, 1);
	@Getter @Setter private LineStyle bottomBorderStyle = new LineStyle(Color.BLACK, 1);

	private Paragraph paragraph = null;
	@Getter @Setter private float lineSpacing = 1;
	@Getter @Setter private boolean textRotated = false;

	@Getter @Setter private HorizontalAlignment align;
	@Getter @Setter private VerticalAlignment valign;

	float horizontalFreeSpace = 0;
	float verticalFreeSpace = 0;

	private final List<CellContentDrawnListener<T>> contentDrawnListenerList = new ArrayList<CellContentDrawnListener<T>>();

	/**
	 * Constructs a cell with the default alignment
	 * {@link VerticalAlignment#TOP} {@link HorizontalAlignment#LEFT}.
	 * @see Cell#Cell(Row, float, String, boolean, HorizontalAlignment,
	 *      VerticalAlignment)
	 */
	Cell(final Row<T> row, final float width, final String text, final boolean isCalculated) {
		this(row, width, text, isCalculated, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
	}

	/**
	 * Constructs a cell.
	 * @param row
	 *            The parent row
	 * @param width
	 *            absolute width in points or in % of table width (depending on
	 *            the parameter {@code isCalculated})
	 * @param text
	 *            The text content of the cell
	 * @param isCalculated
	 *            If {@code true}, the width is interpreted in % to the table
	 *            width
	 * @param align
	 *            The {@link HorizontalAlignment} of the cell content
	 * @param valign
	 *            The {@link VerticalAlignment} of the cell content
	 * @see Cell#Cell(Row, float, String, boolean)
	 */
	Cell(final Row<T> row, final float width, final String text, final boolean isCalculated, final HorizontalAlignment align,
			final VerticalAlignment valign) {
		this.row = row;
		if (isCalculated) {
			double calculatedWidth = row.getWidth() * (width / 100);
			this.width = (float) calculatedWidth;
		} else {
			this.width = width;
		}

		if (getWidth() > row.getWidth()) {
			throw new IllegalArgumentException(
					"Cell Width=" + getWidth() + " can't be bigger than row width=" + row.getWidth());
		}
		//check if we have new default font
		if(!FontUtils.getDefaultfonts().isEmpty()){
			font = FontUtils.getDefaultfonts().get("font");
			fontBold = FontUtils.getDefaultfonts().get("fontBold");
		}
		this.text = text == null ? "" : text;
		this.align = align;
		this.valign = valign;
		this.wrappingFunction = null;
	}

	/**
	 * Returns cell's width without (left,right) padding.
	 */
	public float getInnerWidth() {
		return getWidth() - getLeftPadding() - getRightPadding()
				- (leftBorderStyle == null ? 0 : leftBorderStyle.getWidth())
				- (rightBorderStyle == null ? 0 : rightBorderStyle.getWidth());
	}

	/**
	 * Returns cell's height without (top,bottom) padding.
	 */
	public float getInnerHeight() {
		return getHeight() - getBottomPadding() - getTopPadding()
				- (topBorderStyle == null ? 0 : topBorderStyle.getWidth())
				- (bottomBorderStyle == null ? 0 : bottomBorderStyle.getWidth());
	}

	/**
	 * Sets cell's text value
	 * @param text
	 *            Text value of the cell
	 */
	public void setText(final String text) {
		this.text = text;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * Returns appropriate {@link PDFont} for current cell.
	 * @throws IllegalArgumentException
	 *             if <code>font</code> is not set.
	 */
	public PDFont getFont() {
		if (font == null) throw new IllegalArgumentException("Font not set.");
		return isHeaderCell() ? fontBold : font;
	}

	/**
	 * Sets appropriate {@link PDFont} for current cell.
	 * @param font
	 *            {@link PDFont} for current cell
	 */
	public void setFont(final PDFont font) {
		this.font = font;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * Sets {@link PDFont} size for current cell (in points).
	 * @param fontSize
	 *            {@link PDFont} size for current cell (in points).
	 */
	public void setFontSize(final float fontSize) {
		this.fontSize = fontSize;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * Retrieves a valid {@link Paragraph} depending of cell's {@link PDFont}
	 * and value rotation.
	 * <p>
	 * If cell has rotated value then {@link Paragraph} width is depending of
	 * {@link Cell#getInnerHeight()} otherwise {@link Cell#getInnerWidth()}
	 * @return Cell's {@link Paragraph}
	 */
	public Paragraph getParagraph() {
		if (paragraph == null) {
			// if it is header cell then use font bold
			if (isHeaderCell()) {
				if (isTextRotated()) {
					paragraph = new Paragraph(text, fontBold, fontSize, getInnerHeight(), align, textColor, null,
							wrappingFunction, lineSpacing);
				} else {
					paragraph = new Paragraph(text, fontBold, fontSize, getInnerWidth(), align, textColor, null,
							wrappingFunction, lineSpacing);
				}
			} else {
				if (isTextRotated()) {
					paragraph = new Paragraph(text, font, fontSize, getInnerHeight(), align, textColor, null,
							wrappingFunction, lineSpacing);
				} else {
					paragraph = new Paragraph(text, font, fontSize, getInnerWidth(), align, textColor, null,
							wrappingFunction, lineSpacing);
				}
			}
		}
		return paragraph;
	}

	public float getExtraWidth() {
		return this.row.getLastCellExtraWidth() + getWidth();
	}

	/**
	 * Returns the cell's height according to {@link Row}'s height
	 */
	public float getHeight() {
		return row.getHeight();
	}

	/**
	 * Gets the height of the single cell, opposed to {@link #getHeight()},
	 * which returns the row's height.
	 * <p>
	 * Depending of rotated/normal cell's value there is two cases for
	 * calculation:
	 * <ol>
	 * <li>Rotated value - cell's height is equal to overall text length in the
	 * cell with necessery paddings (top,bottom)</li>
	 * <li>Normal value - cell's height is equal to {@link Paragraph}'s height
	 * with necessery paddings (top,bottom)</li>
	 * </ol>
	 * @return Cell's height
	 * @throws IllegalStateException
	 *             if <code>font</code> is not set.
	 */
	public float getCellHeight() {
		if (height != null) {
			return height;
		}

		if (isTextRotated()) {
			try {
				// TODO: maybe find more optimal way then this
				return getFont().getStringWidth(getText()) / 1000 * getFontSize() + getTopPadding()
						+ (getTopBorderStyle() == null ? 0 : getTopBorderStyle().getWidth()) + getBottomPadding()
						+ (getBottomBorderStyle() == null ? 0 : getBottomBorderStyle().getWidth());
			} catch (final IOException e) {
				throw new IllegalStateException("Font not set.", e);
			}
		} else {
			return getTextHeight() + getTopPadding() + getBottomPadding()
					+ (getTopBorderStyle() == null ? 0 : getTopBorderStyle().getWidth())
					+ (getBottomBorderStyle() == null ? 0 : getBottomBorderStyle().getWidth());
		}
	}

	/**
	 * Returns {@link Paragraph}'s height
	 */
	public float getTextHeight() {
		return getParagraph().getHeight();
	}

	/**
	 * Returns {@link Paragraph}'s width
	 */
	public float getTextWidth() {
		return getParagraph().getWidth();
	}

	/**
	 * Returns free vertical space of cell.
	 * <p>
	 * If cell has rotated value then free vertical space is equal inner cell's
	 * height ({@link #getInnerHeight()}) subtracted to the longest line of
	 * rotated {@link Paragraph} otherwise it's just cell's inner height (
	 * {@link #getInnerHeight()}) subtracted with width of the normal
	 * {@link Paragraph}.
	 */
	public float getVerticalFreeSpace() {
		if (isTextRotated()) {
			// need to calculate max line width so we just iterating through
			// lines
			for (String line : getParagraph().getLines()) {
			}
			return getInnerHeight() - getParagraph().getMaxLineWidth();
		} else {
			return getInnerHeight() - getTextHeight();
		}
	}

	/**
	 * Returns free horizontal space of cell.
	 * <p>
	 * If cell has rotated value then free horizontal space is equal cell's
	 * inner width ({@link #getInnerWidth()}) subtracted to the
	 * {@link Paragraph}'s height otherwise it's just cell's
	 * {@link #getInnerWidth()} subtracted with width of longest line in normal
	 * {@link Paragraph}.
	 */
	public float getHorizontalFreeSpace() {
		if (isTextRotated()) {
			return getInnerWidth() - getTextHeight();
		} else {
			return getInnerWidth() - getParagraph().getMaxLineWidth();
		}
	}


	public WrappingFunction getWrappingFunction() {
		return getParagraph().getWrappingFunction();
	}

	public void setWrappingFunction(final WrappingFunction wrappingFunction) {
		this.wrappingFunction = wrappingFunction;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * Easy setting for cell border style.
	 * @param border
	 *            It is {@link LineStyle} for all borders
	 * @see LineStyle Rendering line attributes
	 */
	public void setBorderStyle(final LineStyle border) {
		this.leftBorderStyle = border;
		this.rightBorderStyle = border;
		this.topBorderStyle = border;
		this.bottomBorderStyle = border;
	}

	/**
	 * Copies the style of an existing cell to this cell
	 * @param sourceCell Source {@link Cell} from which cell style will be copied.
	 */
	public void copyCellStyle(final Cell sourceCell) {
		Boolean leftBorder = this.leftBorderStyle == null;
		setBorderStyle(sourceCell.getTopBorderStyle());
		if (leftBorder) {
			this.leftBorderStyle = null;// if left border wasn't set, don't set
										// it now
		}
		this.font = sourceCell.getFont();// otherwise paragraph gets invalidated
		this.fontBold = sourceCell.getFontBold();
		this.fontSize = sourceCell.getFontSize();
		setFillColor(sourceCell.getFillColor());
		setTextColor(sourceCell.getTextColor());
		setAlign(sourceCell.getAlign());
		setValign(sourceCell.getValign());
	}

	/**
	 * Returns whether source cell has the same style
	 * @param sourceCell Source {@link Cell} which will be used for style comparison
	 */
	public Boolean hasSameStyle(final Cell sourceCell) {
		if (!sourceCell.getTopBorderStyle().equals(getTopBorderStyle())) {
			return false;
		}
		if (!sourceCell.getFont().equals(getFont())) {
			return false;
		}
		if (!sourceCell.getFontBold().equals(getFontBold())) {
			return false;
		}
		if (!sourceCell.getFillColor().equals(getFillColor())) {
			return false;
		}
		if (!sourceCell.getTextColor().equals(getTextColor())) {
			return false;
		}
		if (!sourceCell.getAlign().equals(getAlign())) {
			return false;
		}
		if (!sourceCell.getValign().equals(getValign())) {
			return false;
		}
		return true;
	}

	public void addContentDrawnListener(final CellContentDrawnListener<T> listener) {
		contentDrawnListenerList.add(listener);
	}

	public List<CellContentDrawnListener<T>> getCellContentDrawnListeners() {
		return contentDrawnListenerList;
	}

	public void notifyContentDrawnListeners(final PDDocument document, final PDPage page, final PDRectangle rectangle) {
		for(CellContentDrawnListener<T> listener : getCellContentDrawnListeners()) {
			listener.onContentDrawn(this, document, page, rectangle);
		}
	}

}

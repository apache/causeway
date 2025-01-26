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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.causeway.extensions.tabular.pdf.factory.internal.text.Token;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.utils.FontUtils;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.utils.PageContentStreamOptimized;

import lombok.extern.log4j.Log4j2;

@Log4j2
class TableCell<T extends PDPage> extends Cell<T> {

	private final String tableData;
	private final float width;
	private float yStart;
	private float xStart;
	private float height = 0;
	private final PDDocument doc;
	private final PDPage page;
	private float marginBetweenElementsY = FontUtils.getHeight(getFont(), getFontSize());
	private final HorizontalAlignment align;
	private final VerticalAlignment valign;

	// default FreeSans font
//	private PDFont font = FontUtils.getDefaultfonts().get("font");
//	private PDFont fontBold = FontUtils.getDefaultfonts().get("fontBold");
	private PageContentStreamOptimized tableCellContentStream;

	// page margins
	private final float pageTopMargin;
	private final float pageBottomMargin;
	// default title fonts
	private int tableTitleFontSize = 8;

	TableCell(final Row<T> row, final float width, final String tableData, final boolean isCalculated, final PDDocument document, final PDPage page,
			final float yStart, final float pageTopMargin, final float pageBottomMargin) {
		this(row, width, tableData, isCalculated, document, page, yStart, pageTopMargin, pageBottomMargin,
				HorizontalAlignment.LEFT, VerticalAlignment.TOP);
	}

	TableCell(final Row<T> row, final float width, final String tableData, final boolean isCalculated, final PDDocument document, final PDPage page,
			final float yStart, final float pageTopMargin, final float pageBottomMargin, final HorizontalAlignment align,
			final VerticalAlignment valign) {
		super(row, width, tableData, isCalculated);
		this.tableData = tableData;
		this.width = width * row.getWidth() / 100;
		this.doc = document;
		this.page = page;
		this.yStart = yStart;
		this.pageTopMargin = pageTopMargin;
		this.pageBottomMargin = pageBottomMargin;
		this.align = align;
		this.valign = valign;
		fillTable();
	}

	/**
	 * This method just fills up the table's with her content for proper table
	 * cell height calculation. Position of the table (x,y) is not relevant
	 * here.
	 * <p>
	 * NOTE: if entire row is not header row then use bold instead header cell (
	 * {@code
	 *
	<th>})
	 */
	public void fillTable() {
		try {
			// please consider the cell's paddings
			float tableWidth = this.width - getLeftPadding() - getRightPadding();
			tableCellContentStream = new PageContentStreamOptimized(new PDPageContentStream(doc, page,
					PDPageContentStream.AppendMode.APPEND, true));
			// check if there is some additional text outside inner table
			String[] outerTableText = tableData.split("<table");
			// don't forget to attach splited tag
			for (int i = 1; i < outerTableText.length; i++) {
				outerTableText[i] = "<table " + outerTableText[i];
			}
			Paragraph outerTextParagraph = null;
			String caption = "";
			height = 0;
			height = (getTopBorderStyle() == null ? 0 : getTopBorderStyle().getWidth()) + getTopPadding();
			for (String element : outerTableText) {
				if (element.contains("</table")) {
					String[] chunks = element.split("</table>");
					for (String chunkie : chunks) {
						if (chunkie.contains("<table")) {
							// table title
							Document document = Jsoup.parse(chunkie);
							Element captionTag = document.select("caption").first();
							Paragraph tableTitle = null;
							if (captionTag != null) {
								caption = captionTag.text();
								tableTitle = new Paragraph(caption, getFontBold(), tableTitleFontSize, tableWidth,
										HorizontalAlignment.CENTER, null);
								yStart -= tableTitle.getHeight() + marginBetweenElementsY;
							}
							height += (captionTag != null ? tableTitle.getHeight() + marginBetweenElementsY : 0);
							createInnerTable(tableWidth, document, page, false);
						} else {
							// make paragraph and get tokens
							outerTextParagraph = new Paragraph(chunkie, getFont(), 8, (int) tableWidth);
							outerTextParagraph.getLines();
							height += (outerTextParagraph != null
									? outerTextParagraph.getHeight() + marginBetweenElementsY : 0);
							yStart = writeOrCalculateParagraph(outerTextParagraph, true);
						}
					}
				} else {
					// make paragraph and get tokens
					outerTextParagraph = new Paragraph(element, getFont(), 8, (int) tableWidth);
					outerTextParagraph.getLines();
					height += (outerTextParagraph != null ? outerTextParagraph.getHeight() + marginBetweenElementsY
							: 0);
					yStart = writeOrCalculateParagraph(outerTextParagraph, true);
				}
			}
			tableCellContentStream.close();
		} catch (IOException e) {
			log.warn("Cannot create table in TableCell. Table data: '{}' " + tableData + e);
		}
	}

	private void createInnerTable(final float tableWidth, final Document document, final PDPage currentPage, final boolean drawTable) throws IOException {

		BaseTable table = new BaseTable(yStart, PDRectangle.A4.getHeight() - pageTopMargin, pageTopMargin,
				pageBottomMargin, tableWidth, xStart, doc, currentPage, true, true);
		document.outputSettings().prettyPrint(false);
		Element htmlTable = document.select("table").first();

		Elements rows = htmlTable.select("tr");
		for (Element htmlTableRow : rows) {
			Row<PDPage> row = table.createRow(0);
			Elements tableCols = htmlTableRow.select("td");
			Elements tableHeaderCols = htmlTableRow.select("th");
			// do we have header columns?
			boolean tableHasHeaderColumns = tableHeaderCols.isEmpty() ? false : true;
			if (tableHasHeaderColumns) {
				// if entire row is not header row then use bold instead
				// header cell (<th>)
				row.setHeaderRow(true);
			}
			int columnsSize = tableHasHeaderColumns ? tableHeaderCols.size() : tableCols.size();
			// calculate how much really columns do you have (including
			// colspans!)
			for (Element col : tableHasHeaderColumns ? tableHeaderCols : tableCols) {
				if (col.attr("colspan") != null && !col.attr("colspan").isEmpty()) {
					columnsSize += Integer.parseInt(col.attr("colspan")) - 1;
				}
			}
			for (Element col : tableHasHeaderColumns ? tableHeaderCols : tableCols) {
				if (col.attr("colspan") != null && !col.attr("colspan").isEmpty()) {
					Cell<T> cell = (Cell<T>) row.createCell(
							tableWidth / columnsSize * Integer.parseInt(col.attr("colspan")) / row.getWidth() * 100,
							col.html().replace("&amp;", "&"));
				} else {
					Cell<T> cell = (Cell<T>) row.createCell(tableWidth / columnsSize / row.getWidth() * 100,
							col.html().replace("&amp;", "&"));
				}
			}
			yStart -= row.getHeight();
		}
		if (drawTable) {
			table.draw();
		}

		height += table.getHeaderAndDataHeight() + marginBetweenElementsY;
	}

	/**
	 * <p>
	 * Method provides writing or height calculation of possible outer text
	 * </p>
	 *
	 * @param paragraph
	 *            Paragraph that needs to be written or whose height needs to be
	 *            calculated
	 * @param onlyCalculateHeight
	 *            if <code>true</code> the given paragraph will not be drawn
	 *            just his height will be calculated.
	 * @return Y position after calculating/writing given paragraph
	 */
	private float writeOrCalculateParagraph(final Paragraph paragraph, final boolean onlyCalculateHeight) throws IOException {
		int boldCounter = 0;
		int italicCounter = 0;

		if (!onlyCalculateHeight) {
			tableCellContentStream.setRotated(isTextRotated());
		}

		// position at top of current cell descending by font height - font
		// descent, because we are positioning the base line here
		float cursorY = yStart - getTopPadding() - FontUtils.getHeight(getFont(), getFontSize())
				- FontUtils.getDescent(getFont(), getFontSize()) - (getTopBorderStyle() == null ? 0 : getTopBorderStyle().getWidth());
		float cursorX = xStart;

		// loop through tokens
		for (Map.Entry<Integer, List<Token>> entry : paragraph.getMapLineTokens().entrySet()) {

			// calculate the width of this line
			float freeSpaceWithinLine = paragraph.getMaxLineWidth() - paragraph.getLineWidth(entry.getKey());
			if (isTextRotated()) {
				switch (align) {
				case CENTER:
					cursorY += freeSpaceWithinLine / 2;
					break;
				case LEFT:
					break;
				case RIGHT:
					cursorY += freeSpaceWithinLine;
					break;
				}
			} else {
				switch (align) {
				case CENTER:
					cursorX += freeSpaceWithinLine / 2;
					break;
				case LEFT:
					// it doesn't matter because X position is always the same
					// as row above
					break;
				case RIGHT:
					cursorX += freeSpaceWithinLine;
					break;
				}
			}

			// iterate through tokens in current line
			PDFont currentFont = paragraph.getFont(false, false);
			for (Token token : entry.getValue()) {
				switch (token.type()) {
				case OPEN_TAG:
					if ("b".equals(token.text())) {
						boldCounter++;
					} else if ("i".equals(token.text())) {
						italicCounter++;
					}
					break;
				case CLOSE_TAG:
					if ("b".equals(token.text())) {
						boldCounter = Math.max(boldCounter - 1, 0);
					} else if ("i".equals(token.text())) {
						italicCounter = Math.max(italicCounter - 1, 0);
					}
					break;
				case PADDING:
					cursorX += Float.parseFloat(token.text());
					break;
				case ORDERING:
					currentFont = paragraph.getFont(boldCounter > 0, italicCounter > 0);
					tableCellContentStream.setFont(currentFont, getFontSize());
					if (isTextRotated()) {
						// if it is not calculation then draw it
						if (!onlyCalculateHeight) {
							tableCellContentStream.newLineAt(cursorX, cursorY);
							tableCellContentStream.showText(token.text());
						}
						cursorY += token.getWidth(currentFont) / 1000 * getFontSize();
					} else {
						// if it is not calculation then draw it
						if (!onlyCalculateHeight) {
							tableCellContentStream.newLineAt(cursorX, cursorY);
							tableCellContentStream.showText(token.text());
						}
						cursorX += token.getWidth(currentFont) / 1000 * getFontSize();
					}
					break;
				case BULLET:
					float widthOfSpace = currentFont.getSpaceWidth();
					float halfHeight = FontUtils.getHeight(currentFont, getFontSize()) / 2;
					if (isTextRotated()) {
						if (!onlyCalculateHeight) {
							PDStreamUtils.rect(tableCellContentStream, cursorX + halfHeight, cursorY,
									token.getWidth(currentFont) / 1000 * getFontSize(),
									widthOfSpace / 1000 * getFontSize(), getTextColor());
						}
						// move cursorY for two characters (one for bullet, one
						// for space after bullet)
						cursorY += 2 * widthOfSpace / 1000 * getFontSize();
					} else {
						if (!onlyCalculateHeight) {
							PDStreamUtils.rect(tableCellContentStream, cursorX, cursorY + halfHeight,
									token.getWidth(currentFont) / 1000 * getFontSize(),
									widthOfSpace / 1000 * getFontSize(), getTextColor());
						}
						// move cursorX for two characters (one for bullet, one
						// for space after bullet)
						cursorX += 2 * widthOfSpace / 1000 * getFontSize();
					}
					break;
				case TEXT:
					currentFont = paragraph.getFont(boldCounter > 0, italicCounter > 0);
					tableCellContentStream.setFont(currentFont, getFontSize());
					if (isTextRotated()) {
						if (!onlyCalculateHeight) {
							tableCellContentStream.newLineAt(cursorX, cursorY);
							tableCellContentStream.showText(token.text());
						}
						cursorY += token.getWidth(currentFont) / 1000 * getFontSize();
					} else {
						if (!onlyCalculateHeight) {
							tableCellContentStream.newLineAt(cursorX, cursorY);
							tableCellContentStream.showText(token.text());
						}
						cursorX += token.getWidth(currentFont) / 1000 * getFontSize();
					}
					break;
				}
			}
			// reset
			cursorX = xStart;
			cursorY -= FontUtils.getHeight(getFont(), getFontSize());
		}
		return cursorY;
	}

	/**
	 * <p>
	 * This method draw table cell with proper X,Y position which are determined
	 * in {@link Table#draw()} method
	 * </p>
	 * <p>
	 * NOTE: if entire row is not header row then use bold instead header cell (
	 * {@code
	 *
	<th>})
	 * </p>
	 *
	 * @param page
	 *            {@link PDPage} where table cell be written on
	 *
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public void draw(final PDPage page) {
		try {
			// please consider the cell's paddings
			float tableWidth = this.width - getLeftPadding() - getRightPadding();
			tableCellContentStream = new PageContentStreamOptimized(new PDPageContentStream(doc, page,
					PDPageContentStream.AppendMode.APPEND, true));
			// check if there is some additional text outside inner table
			String[] outerTableText = tableData.split("<table");
			// don't forget to attach splited tag
			for (int i = 1; i < outerTableText.length; i++) {
				outerTableText[i] = "<table " + outerTableText[i];
			}
			Paragraph outerTextParagraph = null;
			String caption = "";
			height = 0;
			height = (getTopBorderStyle() == null ? 0 : getTopBorderStyle().getWidth()) + getTopPadding();
			for (String element : outerTableText) {
				if (element.contains("</table")) {
					String[] chunks = element.split("</table>");
					for (String chunkie : chunks) {
						if (chunkie.contains("<table")) {
							// table title
							Document document = Jsoup.parse(chunkie);
							Element captionTag = document.select("caption").first();
							Paragraph tableTitle = null;
							if (captionTag != null) {
								caption = captionTag.text();
								tableTitle = new Paragraph(caption, getFontBold(), tableTitleFontSize, tableWidth,
										HorizontalAlignment.CENTER, null);
								yStart = tableTitle.write(tableCellContentStream, xStart, yStart)
										- marginBetweenElementsY;
							}
							height += (captionTag != null ? tableTitle.getHeight() + marginBetweenElementsY : 0);
							createInnerTable(tableWidth, document, page, true);
						} else {
							// make paragraph and get tokens
							outerTextParagraph = new Paragraph(chunkie, getFont(), 8, (int) tableWidth);
							outerTextParagraph.getLines();
							height += (outerTextParagraph != null
									? outerTextParagraph.getHeight() + marginBetweenElementsY : 0);
							yStart = writeOrCalculateParagraph(outerTextParagraph, false);
						}
					}
				} else {
					// make paragraph and get tokens
					outerTextParagraph = new Paragraph(element, getFont(), 8, (int) tableWidth);
					outerTextParagraph.getLines();
					height += (outerTextParagraph != null ? outerTextParagraph.getHeight() + marginBetweenElementsY
							: 0);
					yStart = writeOrCalculateParagraph(outerTextParagraph, false);
				}
			}
			tableCellContentStream.close();
		} catch (IOException e) {
			log.warn("Cannot draw table for TableCell! Table data: '{}'" + tableData + e);
		}
	}

	public float getXPosition() {
		return xStart;
	}

	public void setXPosition(final float xStart) {
		this.xStart = xStart;
	}

	public float getYPosition() {
		return yStart;
	}

	public void setYPosition(final float yStart) {
		this.yStart = yStart;
	}

	@Override
	public float getTextHeight() {
		return height;
	}

	@Override
	public float getHorizontalFreeSpace() {
		return getInnerWidth() - width;
	}

	@Override
	public float getVerticalFreeSpace() {
		return getInnerHeight() - width;
	}

}
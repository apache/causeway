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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import org.apache.causeway.extensions.tabular.pdf.factory.internal.line.LineStyle;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.page.PageProvider;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.text.Token;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.text.WrappingFunction;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.utils.FontUtils;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.utils.PageContentStreamOptimized;

public abstract class Table<T extends PDPage> {

    public final PDDocument document;
    private float margin;

    private T currentPage;
    private PageContentStreamOptimized tableContentStream;
    private List<PDOutlineItem> bookmarks;
    private List<Row<T>> header = new ArrayList<>();
    private List<Row<T>> rows = new ArrayList<>();

    private final float yStartNewPage;
    private float yStart;
    private final float width;
    private final boolean drawLines;
    private final boolean drawContent;
    private float headerBottomMargin = 4f;
    private float lineSpacing = 1f;

    private boolean tableIsBroken = false;
    private boolean tableStartedAtNewPage = false;
    private boolean removeTopBorders = false;
    private boolean removeAllBorders = false;

    private PageProvider<T> pageProvider;

    // page margins
    private final float pageTopMargin;
    private final float pageBottomMargin;

    private boolean drawDebug;

    public Table(final float yStart, final float yStartNewPage, final float pageTopMargin, final float pageBottomMargin, final float width,
            final float margin, final PDDocument document, final T currentPage, final boolean drawLines, final boolean drawContent,
            final PageProvider<T> pageProvider) throws IOException {
        this.pageTopMargin = pageTopMargin;
        this.document = document;
        this.drawLines = drawLines;
        this.drawContent = drawContent;
        // Initialize table
        this.yStartNewPage = yStartNewPage;
        this.margin = margin;
        this.width = width;
        this.yStart = yStart;
        this.pageBottomMargin = pageBottomMargin;
        this.currentPage = currentPage;
        this.pageProvider = pageProvider;
        loadFonts();
    }

    public Table(final float yStartNewPage, final float pageTopMargin, final float pageBottomMargin, final float width, final float margin,
            final PDDocument document, final boolean drawLines, final boolean drawContent, final PageProvider<T> pageProvider)
            throws IOException {
        this.pageTopMargin = pageTopMargin;
        this.document = document;
        this.drawLines = drawLines;
        this.drawContent = drawContent;
        // Initialize table
        this.yStartNewPage = yStartNewPage;
        this.margin = margin;
        this.width = width;
        this.pageProvider = pageProvider;
        this.pageBottomMargin = pageBottomMargin;

        // Fonts needs to be loaded before page creation
        loadFonts();
        this.currentPage = pageProvider.nextPage();
    }

    protected abstract void loadFonts() throws IOException;

    protected PDType0Font loadFont(final String fontPath) throws IOException {
        return FontUtils.loadFont(getDocument(), fontPath);
    }

    protected PDDocument getDocument() {
        return document;
    }

    public void drawTitle(final String title, final PDFont font, final int fontSize, final float tableWidth, final float height, final String alignment,
            final float freeSpaceForPageBreak, final boolean drawHeaderMargin) throws IOException {
        drawTitle(title, font, fontSize, tableWidth, height, alignment, freeSpaceForPageBreak, null, drawHeaderMargin);
    }

    public void drawTitle(final String title, final PDFont font, final int fontSize, final float tableWidth, final float height, final String alignment,
            final float freeSpaceForPageBreak, final WrappingFunction wrappingFunction, final boolean drawHeaderMargin)
            throws IOException {

        ensureStreamIsOpen();

        if (isEndOfPage(freeSpaceForPageBreak)) {
            this.tableContentStream.close();
            pageBreak();
            tableStartedAtNewPage = true;
        }

        if (title == null) {
            // if you don't have title just use the height of maxTextBox in your
            // "row"
            yStart -= height;
        } else {
            PageContentStreamOptimized articleTitle = createPdPageContentStream();
            Paragraph paragraph = new Paragraph(title, font, fontSize, tableWidth, HorizontalAlignment.get(alignment),
                    wrappingFunction);
            paragraph.setDrawDebug(drawDebug);
            yStart = paragraph.write(articleTitle, margin, yStart);
            if (paragraph.getHeight() < height) {
                yStart -= (height - paragraph.getHeight());
            }

            articleTitle.close();

            if (drawDebug) {
                // margin
                PDStreamUtils.rect(tableContentStream, margin, yStart, width, headerBottomMargin, Color.CYAN);
            }
        }

        if (drawHeaderMargin) {
            yStart -= headerBottomMargin;
        }
    }

    public float getWidth() {
        return width;
    }

    public Row<T> createRow(final float height) {
        Row<T> row = new Row<T>(this, height);
        row.setLineSpacing(lineSpacing);
        this.rows.add(row);
        return row;
    }

    public Row<T> createRow(final List<Cell<T>> cells, final float height) {
        Row<T> row = new Row<T>(this, cells, height);
        row.setLineSpacing(lineSpacing);
        this.rows.add(row);
        return row;
    }

    /**
     * <p>
     * Draws table
     * </p>
     *
     * @return Y position of the table
     * @throws IOException if underlying stream has problem being written to.
     *
     */
    public float draw() throws IOException {
        ensureStreamIsOpen();

        for (Row<T> row : rows) {
            if (header.contains(row)) {
                // check if header row height and first data row height can fit
                // the page
                // if not draw them on another side
                if (isEndOfPage(getMinimumHeight())) {
                    pageBreak();
                    tableStartedAtNewPage = true;
                }
            }
            drawRow(row);
        }

        endTable();
        return yStart;
    }

    private void drawRow(final Row<T> row) throws IOException {
        // row.getHeight is currently an extremely expensive function so get the value
        // once during drawing and reuse it, since it will not change during drawing
        float rowHeight = row.getHeight();

        // if it is not header row or first row in the table then remove row's
        // top border
        if (row != header && row != rows.get(0)) {
            if (!isEndOfPage(rowHeight)) {
                row.removeTopBorders();
            }
        }

        // draw the bookmark
        if (row.getBookmark() != null) {
            PDPageXYZDestination bookmarkDestination = new PDPageXYZDestination();
            bookmarkDestination.setPage(currentPage);
            bookmarkDestination.setTop((int) yStart);
            row.getBookmark().setDestination(bookmarkDestination);
            this.addBookmark(row.getBookmark());
        }

        // we want to remove the borders as often as possible
        removeTopBorders = true;

        // check also if we want all borders removed
        if (allBordersRemoved()) {
            row.removeAllBorders();
        }

        if (isEndOfPage(rowHeight) && !header.contains(row)) {

            // Draw line at bottom of table
            endTable();

            // insert page break
            pageBreak();

            // redraw all headers on each currentPage
            if (!header.isEmpty()) {
                for (Row<T> headerRow : header) {
                    drawRow(headerRow);
                }
                // after you draw all header rows on next page please keep
                // removing top borders to avoid double border drawing
                removeTopBorders = true;
            } else {
                // after a page break, we have to ensure that top borders get
                // drawn
                removeTopBorders = false;
            }
        }
        // if it is first row in the table, we have to draw the top border
        if (row == rows.get(0)) {
            removeTopBorders = false;
        }

        if (removeTopBorders) {
            row.removeTopBorders();
        }

        // if it is header row or first row in the table, we have to draw the
        // top border
        if (row == rows.get(0)) {
            removeTopBorders = false;
        }

        if (removeTopBorders) {
            row.removeTopBorders();
        }

        if (drawLines) {
            drawVerticalLines(row, rowHeight);
        }

        if (drawContent) {
            drawCellContent(row, rowHeight);
        }
    }

    /**
     * <p>
     * Method to switch between the {@link PageProvider} and the abstract method
     * {@link Table#createPage()}, preferring the {@link PageProvider}.
     * </p>
     * <p>
     * Will be removed once {@link #createPage()} is removed.
     * </p>
     *
     * @return
     */
    private T createNewPage() {
        if (pageProvider != null) {
            return pageProvider.nextPage();
        }

        return createPage();
    }

    /**
     * @deprecated Use a {@link PageProvider} instead
     * @return new {@link PDPage}
     */
    @Deprecated
    // remove also createNewPage()
    protected T createPage() {
        throw new IllegalStateException(
                "You either have to provide a " + PageProvider.class.getCanonicalName() + " or override this method");
    }

    private PageContentStreamOptimized createPdPageContentStream() throws IOException {
        return new PageContentStreamOptimized(
                new PDPageContentStream(getDocument(), getCurrentPage(),
                        PDPageContentStream.AppendMode.APPEND, true));
    }

    private void drawCellContent(final Row<T> row, final float rowHeight) throws IOException {

        // position into first cell (horizontal)
        float cursorX = margin;
        float cursorY;

        for (Cell<T> cell : row.getCells()) {
            // remember horizontal cursor position, so we can advance to the
            // next cell easily later
            float cellStartX = cursorX;
            if (cell instanceof ImageCell) {
                final ImageCell<T> imageCell = (ImageCell<T>) cell;

                cursorY = yStart - cell.getTopPadding()
                        - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth());

                // image cell vertical alignment
                switch (cell.getValign()) {
                    case TOP:
                        break;
                    case MIDDLE:
                        cursorY -= cell.getVerticalFreeSpace() / 2;
                        break;
                    case BOTTOM:
                        cursorY -= cell.getVerticalFreeSpace();
                        break;
                }

                cursorX += cell.getLeftPadding() + (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth());

                // image cell horizontal alignment
                switch (cell.getAlign()) {
                    case CENTER:
                        cursorX += cell.getHorizontalFreeSpace() / 2;
                        break;
                    case LEFT:
                        break;
                    case RIGHT:
                        cursorX += cell.getHorizontalFreeSpace();
                        break;
                }
                imageCell.getImage().draw(document, tableContentStream, cursorX, cursorY);

                if (imageCell.getUrl() != null) {
                    List<PDAnnotation> annotations = currentPage.getAnnotations();

                    PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
                    borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
                    borderULine.setWidth(1); // 1 point

                    PDAnnotationLink txtLink = new PDAnnotationLink();
                    txtLink.setBorderStyle(borderULine);

                    // Set the rectangle containing the link
                    // PDRectangle sets a the x,y and the width and height extend upwards from that!
                    PDRectangle position = new PDRectangle(cursorX, cursorY, (imageCell.getImage().getWidth()), -(imageCell.getImage().getHeight()));
                    txtLink.setRectangle(position);

                    // add an action
                    PDActionURI action = new PDActionURI();
                    action.setURI(imageCell.getUrl().toString());
                    txtLink.setAction(action);
                    annotations.add(txtLink);
                }

            } else if (cell instanceof TableCell) {
                final TableCell<T> tableCell = (TableCell<T>) cell;

                cursorY = yStart - cell.getTopPadding()
                        - (cell.getTopBorderStyle() != null ? cell.getTopBorderStyle().getWidth() : 0);

                // table cell vertical alignment
                switch (cell.getValign()) {
                    case TOP:
                        break;
                    case MIDDLE:
                        cursorY -= cell.getVerticalFreeSpace() / 2;
                        break;
                    case BOTTOM:
                        cursorY -= cell.getVerticalFreeSpace();
                        break;
                }

                cursorX += cell.getLeftPadding() + (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth());
                tableCell.setXPosition(cursorX);
                tableCell.setYPosition(cursorY);
                this.tableContentStream.endText();
                tableCell.draw(currentPage);
            } else {
                // no text without font
                if (cell.getFont() == null) {
                    throw new IllegalArgumentException("Font is null on Cell=" + cell.getText());
                }

                if (cell.isTextRotated()) {
                    // debugging mode - drawing (default!) padding of rotated
                    // cells
                    // left
                    // PDStreamUtils.rect(tableContentStream, cursorX, yStart,
                    // 5, cell.getHeight(), Color.GREEN);
                    // top
                    // PDStreamUtils.rect(tableContentStream, cursorX, yStart,
                    // cell.getWidth(), 5 , Color.GREEN);
                    // bottom
                    // PDStreamUtils.rect(tableContentStream, cursorX, yStart -
                    // cell.getHeight(), cell.getWidth(), -5 , Color.GREEN);
                    // right
                    // PDStreamUtils.rect(tableContentStream, cursorX +
                    // cell.getWidth() - 5, yStart, 5, cell.getHeight(),
                    // Color.GREEN);

                    cursorY = yStart - cell.getInnerHeight() - cell.getTopPadding()
                            - (cell.getTopBorderStyle() != null ? cell.getTopBorderStyle().getWidth() : 0);

                    switch (cell.getAlign()) {
                        case CENTER:
                            cursorY += cell.getVerticalFreeSpace() / 2;
                            break;
                        case LEFT:
                            break;
                        case RIGHT:
                            cursorY += cell.getVerticalFreeSpace();
                            break;
                    }
                    // respect left padding and descend by font height to get
                    // position of the base line
                    cursorX += cell.getLeftPadding()
                            + (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth())
                            + FontUtils.getHeight(cell.getFont(), cell.getFontSize())
                            + FontUtils.getDescent(cell.getFont(), cell.getFontSize());

                    switch (cell.getValign()) {
                        case TOP:
                            break;
                        case MIDDLE:
                            cursorX += cell.getHorizontalFreeSpace() / 2;
                            break;
                        case BOTTOM:
                            cursorX += cell.getHorizontalFreeSpace();
                            break;
                    }
                    // make tokenize method just in case
                    cell.getParagraph().getLines();
                } else {
                    // debugging mode - drawing (default!) padding of rotated
                    // cells
                    // left
                    // PDStreamUtils.rect(tableContentStream, cursorX, yStart,
                    // 5, cell.getHeight(), Color.RED);
                    // top
                    // PDStreamUtils.rect(tableContentStream, cursorX, yStart,
                    // cell.getWidth(), 5 , Color.RED);
                    // bottom
                    // PDStreamUtils.rect(tableContentStream, cursorX, yStart -
                    // cell.getHeight(), cell.getWidth(), -5 , Color.RED);
                    // right
                    // PDStreamUtils.rect(tableContentStream, cursorX +
                    // cell.getWidth() - 5, yStart, 5, cell.getHeight(),
                    // Color.RED);

                    // position at top of current cell descending by font height
                    // - font descent, because we are
                    // positioning the base line here
                    cursorY = yStart - cell.getTopPadding() - FontUtils.getHeight(cell.getFont(), cell.getFontSize())
                            - FontUtils.getDescent(cell.getFont(), cell.getFontSize())
                            - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth());

                    if (drawDebug) {
                        // @formatter:off
                        // top padding
                        PDStreamUtils.rect(tableContentStream, cursorX + (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth()), yStart - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth()), cell.getWidth() - (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth()) - (cell.getRightBorderStyle() == null ? 0 : cell.getRightBorderStyle().getWidth()), cell.getTopPadding(), Color.RED);
                        // bottom padding
                        PDStreamUtils.rect(tableContentStream, cursorX + (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth()), yStart - cell.getHeight() + (cell.getBottomBorderStyle() == null ? 0 : cell.getBottomBorderStyle().getWidth()) + cell.getBottomPadding(), cell.getWidth() - (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth()) - (cell.getRightBorderStyle() == null ? 0 : cell.getRightBorderStyle().getWidth()), cell.getBottomPadding(), Color.RED);
                        // left padding
                        PDStreamUtils.rect(tableContentStream, cursorX + (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth()), yStart - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth()), cell.getLeftPadding(), cell.getHeight() - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth()) - (cell.getBottomBorderStyle() == null ? 0 : cell.getBottomBorderStyle().getWidth()), Color.RED);
                        // right padding
                        PDStreamUtils.rect(tableContentStream, cursorX + cell.getWidth() - (cell.getRightBorderStyle() == null ? 0 : cell.getRightBorderStyle().getWidth()), yStart - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth()), -cell.getRightPadding(), cell.getHeight() - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth()) - (cell.getBottomBorderStyle() == null ? 0 : cell.getBottomBorderStyle().getWidth()), Color.RED);
                        // @formatter:on
                    }

                    // respect left padding
                    cursorX += cell.getLeftPadding()
                            + (cell.getLeftBorderStyle() == null ? 0 : cell.getLeftBorderStyle().getWidth());

                    // the widest text does not fill the inner width of the
                    // cell? no
                    // problem, just add it ;)
                    switch (cell.getAlign()) {
                        case CENTER:
                            cursorX += cell.getHorizontalFreeSpace() / 2;
                            break;
                        case LEFT:
                            break;
                        case RIGHT:
                            cursorX += cell.getHorizontalFreeSpace();
                            break;
                    }

                    switch (cell.getValign()) {
                        case TOP:
                            break;
                        case MIDDLE:
                            cursorY -= cell.getVerticalFreeSpace() / 2;
                            break;
                        case BOTTOM:
                            cursorY -= cell.getVerticalFreeSpace();
                            break;
                    }

                    if (cell.getUrl() != null) {
                        List<PDAnnotation> annotations = currentPage.getAnnotations();
                        PDAnnotationLink txtLink = new PDAnnotationLink();

                        // Set the rectangle containing the link
                        // PDRectangle sets a the x,y and the width and height extend upwards from that!
                        PDRectangle position = new PDRectangle(cursorX - 5, cursorY + 10, (cell.getWidth()), -(cell.getHeight()));
                        txtLink.setRectangle(position);

                        // add an action
                        PDActionURI action = new PDActionURI();
                        action.setURI(cell.getUrl().toString());
                        txtLink.setAction(action);
                        annotations.add(txtLink);
                    }

                }

                // remember this horizontal position, as it is the anchor for
                // each
                // new line
                float lineStartX = cursorX;
                float lineStartY = cursorY;

                this.tableContentStream.setNonStrokingColor(cell.getTextColor());

                int italicCounter = 0;
                int boldCounter = 0;

                this.tableContentStream.setRotated(cell.isTextRotated());

                // print all lines of the cell
                for (Map.Entry<Integer, List<Token>> entry : cell.getParagraph().getMapLineTokens().entrySet()) {

                    // calculate the width of this line
                    float freeSpaceWithinLine = cell.getParagraph().getMaxLineWidth()
                            - cell.getParagraph().getLineWidth(entry.getKey());
                    // TODO: need to implemented rotated text yo!
                    if (cell.isTextRotated()) {
                        cursorY = lineStartY;
                        switch (cell.getAlign()) {
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
                        cursorX = lineStartX;
                        switch (cell.getAlign()) {
                            case CENTER:
                                cursorX += freeSpaceWithinLine / 2;
                                break;
                            case LEFT:
                                // it doesn't matter because X position is always
                                // the same
                                // as row above
                                break;
                            case RIGHT:
                                cursorX += freeSpaceWithinLine;
                                break;
                        }
                    }

                    // iterate through tokens in current line
                    PDFont currentFont = cell.getParagraph().getFont(false, false);
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
                                currentFont = cell.getParagraph().getFont(boldCounter > 0, italicCounter > 0);
                                this.tableContentStream.setFont(currentFont, cell.getFontSize());
                                if (cell.isTextRotated()) {
                                    tableContentStream.newLineAt(cursorX, cursorY);
                                    this.tableContentStream.showText(token.text());
                                    cursorY += token.getWidth(currentFont) / 1000 * cell.getFontSize();
                                } else {
                                    this.tableContentStream.newLineAt(cursorX, cursorY);
                                    this.tableContentStream.showText(token.text());
                                    cursorX += token.getWidth(currentFont) / 1000 * cell.getFontSize();
                                }
                                break;
                            case BULLET:
                                float widthOfSpace = currentFont.getSpaceWidth();
                                float halfHeight = FontUtils.getHeight(currentFont, cell.getFontSize()) / 2;
                                if (cell.isTextRotated()) {
                                    PDStreamUtils.rect(tableContentStream, cursorX + halfHeight, cursorY,
                                            token.getWidth(currentFont) / 1000 * cell.getFontSize(),
                                            widthOfSpace / 1000 * cell.getFontSize(),
                                            cell.getTextColor());
                                    // move cursorY for two characters (one for
                                    // bullet, one for space after bullet)
                                    cursorY += 2 * widthOfSpace / 1000 * cell.getFontSize();
                                } else {
                                    PDStreamUtils.rect(tableContentStream, cursorX, cursorY + halfHeight,
                                            token.getWidth(currentFont) / 1000 * cell.getFontSize(),
                                            widthOfSpace / 1000 * cell.getFontSize(),
                                            cell.getTextColor());
                                    // move cursorX for two characters (one for
                                    // bullet, one for space after bullet)
                                    cursorX += 2 * widthOfSpace / 1000 * cell.getFontSize();
                                }
                                break;
                            case TEXT:
                                currentFont = cell.getParagraph().getFont(boldCounter > 0, italicCounter > 0);
                                this.tableContentStream.setFont(currentFont, cell.getFontSize());
                                if (cell.isTextRotated()) {
                                    tableContentStream.newLineAt(cursorX, cursorY);
                                    this.tableContentStream.showText(token.text());
                                    cursorY += token.getWidth(currentFont) / 1000 * cell.getFontSize();
                                } else {
                                    try {
                                        this.tableContentStream.newLineAt(cursorX, cursorY);
                                        this.tableContentStream.showText(token.text());
                                        cursorX += token.getWidth(currentFont) / 1000 * cell.getFontSize();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                        }
                    }
                    if (cell.isTextRotated()) {
                        cursorX = cursorX + cell.getParagraph().getFontHeight() * cell.getLineSpacing();
                    } else {
                        cursorY = cursorY - cell.getParagraph().getFontHeight() * cell.getLineSpacing();
                    }
                }
            }

            PDRectangle rectangle = new PDRectangle(cellStartX, yStart - cell.getHeight(), cell.getWidth(), cell.getHeight());
            cell.notifyContentDrawnListeners(getDocument(), getCurrentPage(), rectangle);

            // set cursor to the start of this cell plus its width to advance to
            // the next cell
            cursorX = cellStartX + cell.getWidth();
        }
        // Set Y position for next row
        yStart = yStart - rowHeight;

    }

    private void drawVerticalLines(final Row<T> row, final float rowHeight) throws IOException {
        float xStart = margin;

        Iterator<Cell<T>> cellIterator = row.getCells().iterator();
        while (cellIterator.hasNext()) {
            Cell<T> cell = cellIterator.next();

            float cellWidth = cellIterator.hasNext()
                    ? cell.getWidth()
                    : this.width - (xStart - margin);
            fillCellColor(cell, yStart, xStart, rowHeight, cellWidth);

            drawCellBorders(rowHeight, cell, xStart);

            xStart += cellWidth;
        }

    }

    private void drawCellBorders(final float rowHeight, final Cell<T> cell, final float xStart) throws IOException {

        float yEnd = yStart - rowHeight;

        // top
        LineStyle topBorder = cell.getTopBorderStyle();
        if (topBorder != null) {
            float y = yStart - topBorder.getWidth() / 2;
            drawLine(xStart, y, xStart + cell.getWidth(), y, topBorder);
        }

        // right
        LineStyle rightBorder = cell.getRightBorderStyle();
        if (rightBorder != null) {
            float x = xStart + cell.getWidth() - rightBorder.getWidth() / 2;
            drawLine(x, yStart - (topBorder == null ? 0 : topBorder.getWidth()), x, yEnd, rightBorder);
        }

        // bottom
        LineStyle bottomBorder = cell.getBottomBorderStyle();
        if (bottomBorder != null) {
            float y = yEnd + bottomBorder.getWidth() / 2;
            drawLine(xStart, y, xStart + cell.getWidth() - (rightBorder == null ? 0 : rightBorder.getWidth()), y,
                    bottomBorder);
        }

        // left
        LineStyle leftBorder = cell.getLeftBorderStyle();
        if (leftBorder != null) {
            float x = xStart + leftBorder.getWidth() / 2;
            drawLine(x, yStart, x, yEnd + (bottomBorder == null ? 0 : bottomBorder.getWidth()), leftBorder);
        }

    }

    private void drawLine(final float xStart, final float yStart, final float xEnd, final float yEnd, final LineStyle border) throws IOException {
        PDStreamUtils.setLineStyles(tableContentStream, border);
        tableContentStream.moveTo(xStart, yStart);
        tableContentStream.lineTo(xEnd, yEnd);
        tableContentStream.stroke();
    }

    private void fillCellColor(final Cell<T> cell, float yStart, final float xStart, final float rowHeight, final float cellWidth)
            throws IOException {

        if (cell.getFillColor() != null) {
            this.tableContentStream.setNonStrokingColor(cell.getFillColor());

            // y start is bottom pos
            yStart = yStart - rowHeight;
            float height = rowHeight - (cell.getTopBorderStyle() == null ? 0 : cell.getTopBorderStyle().getWidth());

            this.tableContentStream.addRect(xStart, yStart, cellWidth, height);
            this.tableContentStream.fill();
        }
    }

    private void ensureStreamIsOpen() throws IOException {
        if (tableContentStream == null) {
            tableContentStream = createPdPageContentStream();
        }
    }

    private void endTable() throws IOException {
        this.tableContentStream.close();
    }

    public T getCurrentPage() {
        if (this.currentPage == null) {
            throw new NullPointerException("No current page defined.");
        }
        return this.currentPage;
    }

    private boolean isEndOfPage(final float freeSpaceForPageBreak) {
        float currentY = yStart - freeSpaceForPageBreak;
        boolean isEndOfPage = currentY <= pageBottomMargin;
        if (isEndOfPage) {
            setTableIsBroken(true);
        }
        return isEndOfPage;
    }

    private void pageBreak() throws IOException {
        tableContentStream.close();
        this.yStart = yStartNewPage - pageTopMargin;
        this.currentPage = createNewPage();
        this.tableContentStream = createPdPageContentStream();
    }

    private void addBookmark(final PDOutlineItem bookmark) {
        if (bookmarks == null) {
            bookmarks = new ArrayList<>();
        }
        bookmarks.add(bookmark);
    }

    public List<PDOutlineItem> getBookmarks() {
        return bookmarks;
    }

    /**
     * /**
     *
     * @deprecated Use {@link #addHeaderRow(Row)} instead, as it supports
     * multiple header rows
     * @param header row that will be set as table's header row
     */
    @Deprecated
    public void setHeader(final Row<T> header) {
        this.header.clear();
        addHeaderRow(header);
    }

    /**
     * <p>
     * Calculate height of all table cells (essentially, table height).
     * </p>
     * <p>
     * IMPORTANT: Doesn't acknowledge possible page break. Use with caution.
     * </p>
     *
     * @return {@link Table}'s height
     */
    public float getHeaderAndDataHeight() {
        float height = 0;
        for (Row<T> row : rows) {
            height += row.getHeight();
        }
        return height;
    }

    /**
     * <p>
     * Calculates minimum table height that needs to be drawn (all header rows +
     * first data row heights).
     * </p>
     *
     * @return height
     */
    public float getMinimumHeight() {
        float height = 0.0f;
        int firstDataRowIndex = 0;
        if (!header.isEmpty()) {
            for (Row<T> headerRow : header) {
                // count all header rows height
                height += headerRow.getHeight();
                firstDataRowIndex++;
            }
        }

        if (rows.size() > firstDataRowIndex) {
            height += rows.get(firstDataRowIndex).getHeight();
        }

        return height;
    }

    /**
     * <p>
     * Setting current row as table header row
     * </p>
     *
     * @param row The row that would be added as table's header row
     */
    public void addHeaderRow(final Row<T> row) {
        this.header.add(row);
        row.setHeaderRow(true);
    }

    /**
     * <p>
     * Retrieves last table's header row
     * </p>
     *
     * @return header row
     */
    public Row<T> getHeader() {
        if (header == null) {
            throw new IllegalArgumentException("Header Row not set on table");
        }

        return header.get(header.size() - 1);
    }

    public float getMargin() {
        return margin;
    }

    protected void setYStart(final float yStart) {
        this.yStart = yStart;
    }

    public boolean isDrawDebug() {
        return drawDebug;
    }

    public void setDrawDebug(final boolean drawDebug) {
        this.drawDebug = drawDebug;
    }

    public boolean tableIsBroken() {
        return tableIsBroken;
    }

    public void setTableIsBroken(final boolean tableIsBroken) {
        this.tableIsBroken = tableIsBroken;
    }

    public List<Row<T>> getRows() {
        return rows;
    }

    public boolean tableStartedAtNewPage() {
        return tableStartedAtNewPage;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(final float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public boolean allBordersRemoved() {
        return removeAllBorders;
    }

    public void removeAllBorders(final boolean removeAllBorders) {
        this.removeAllBorders = removeAllBorders;
    }

}

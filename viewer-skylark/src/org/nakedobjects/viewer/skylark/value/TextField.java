package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.value.MultilineTextString;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.SimpleIdentifier;
import org.nakedobjects.viewer.skylark.core.AbstractFieldSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.apache.log4j.Category;


public class TextField extends AbstractField {

    public class CursorPosition {
        private int character;
        private int line;

        CursorPosition(CursorPosition pos) {
            this(pos.line, pos.character);
        }

        CursorPosition(int line, int afterCharacter) {
            this.line = line;
            this.character = afterCharacter;
        }

        public void asFor(CursorPosition pos) {
            line = pos.line;
            character = pos.character;
        }

        /**
         * Move the cursor to the bottom-right of the field
         */
        void bottom() {
            line = textContent.noLines() - 1;
            alignDisplay(line);
            end();
        }

        public void cursorAt(Location atLocation) {
            LOG.debug("At " + atLocation);

            int x = atLocation.getX() - 3; /*
                                                           * slight offsetting mouse helps the user
                                                           * position the cursor between characters
                                                           * near the pointer rather than always
                                                           * after the pointer
                                                           */

            int y = atLocation.getY() - (getBaseline() - style.getAscent());

            // work through the text lines and check if mouse is on that
            int lineIndex = displayFromLine + y / lineHeight();
            lineIndex = Math.max(lineIndex, 0);

            int at = 0;

            if ((displayFromLine + lineIndex) <= displayToLine) {
                String text = textContent.getText(lineIndex);
                int endAt = text.length();
                int width = HPADDING;

                while ((at < endAt) && (x > width)) {
                    width += style.charWidth(text.charAt(at));
                    at++;
                }
            } else {
                lineIndex = displayToLine;
                at = textContent.getText(lineIndex).length();
            }

            LOG.debug("Character at " + at + " line " + lineIndex);

            line = lineIndex;
            character = at;
        }

        /**
         * Move the cursor to the end of the line
         */
        void end() {
            character = textContent.getText(line).length();
        }

        /**
         * @return the character within this line.
         */
        public int getCharacter() {
            return character;
        }

        /**
         * @return the line within the field
         */
        public int getLine() {
            return line;
        }

        /**
         * Move the cursor to the top-left hand of the field
         */
        public void home() {
            character = 0;
            line = 0;
        }

        /**
         * Movet the cursor left by one character.
         */
        public void left() {
            if (!((line == 0) && (character == 0))) {
                character--;

                if (character < 0) {
                    line--;
                    alignDisplay(line);
                    end();
                }
            }
        }

        /**
         * Move down one line.
         */
        public void lineDown() {
            moveDown(1);
        }

        /**
         * Move up one line.
         */
        public void lineUp() {
            moveUp(1);
        }

        private void moveDown(int byLines) {
            int size = textContent.noLines();

            if (line < (size - 1)) {
                line += byLines;
                line = Math.min(size, line);

                character = Math.min(character, textContent.getText(line).length());

                alignDisplay(line);
            }
        }

        /**
         * Move the cursor right by one character.
         */
        public void moveForward(int characters) {
            int length = textContent.getText(line).length();

            if ((character + characters) > length) {
                if ((line + 1) < textContent.noLines()) {
                    line++;
                    alignDisplay(line);

                    int remainder = (character + characters) - length;
                    character = 0;
                    moveForward(remainder);
                }
            } else {
                character += characters;
            }
        }

        private void moveUp(int byLines) {
            if (line > 0) {
                line -= byLines;
                line = Math.max(0, line);
                alignDisplay(line);
            }
        }

        /**
         * Move down one line.
         */
        public void pageDown() {
            moveDown(noDisplayLines - 1);
            markDamaged();
        }

        /**
         * Move cursor up by a page
         */
        public void pageUp() {
            moveUp(noDisplayLines - 1);
        }

        /**
         * Move the cursor right by one character.
         */
        public void right() {
            moveForward(1);
        }

        /**
         * Move the cursor to the top-left of the field
         */
        public void top() {
            line = 0;
            character = 0;
            alignDisplay(line);
        }

        public String toString() {
            return "CursorPosition [line=" + line + ",character=" + character + "]";
        }

        /**
         * Move the cursor left to the beginning of the previous word.
         */
        public void wordLeft() {
            if (!((line == 0) && (character == 0))) {
                if (character == 0) {
                    line--;
                    end();
                }

                String text = textContent.getText(line);

                do
                    character--;
                while ((character >= 0) && (text.charAt(character) == ' '));

                while ((character >= 0) && (text.charAt(character) != ' ')) {
                    character--;
                }

                character++;
            }
        }

        /**
         * Move the cursor right to the end of the current word.
         */
        public void wordRight() {
            if (!((line == (textContent.noLines() - 1)) && (character == textContent.getText(line).length()))) {
                if (character == textContent.getText(line).length()) {
                    line++;
                    character = 0;
                }

                int size = textContent.getText(line).length();

                do {
                    character++;
                } while ((character < size) && (textContent.getText(line).charAt(character) == ' '));

                while ((character < size) && (textContent.getText(line).charAt(character) != ' '))
                    character++;
            }
        }
    }

    public class Selection {
        private CursorPosition start = new CursorPosition(0, 0);

        /**
         * Determin if the selection is back to front. Returns true if the cursor postion is before
         * the start postion.
         */
        private boolean backwardSelection() {
            return cursor.line < start.line || (cursor.line == start.line && cursor.character < start.character);
        }

        public void extendTo(CursorPosition pos) {
            cursor.asFor(pos);
        }

        /**
         * extends the selection so the end point is the same as the cursor.
         * 
         * @param at
         */
        public void extendTo(Location at) {
            cursor.cursorAt(at);
        }

        public CursorPosition from() {
            return backwardSelection() ? cursor : start;
        }

        //		private CursorPosition end = new CursorPosition(0,0);

        /**
         * returns true is a selection exists - if the start and end locations are not the same
         * 
         * @return
         */
        boolean hasSelection() {
            return !(cursor.line == start.line && cursor.character == start.character);
        }

        /**
         * clears the selection so nothing is selected. The start and end points are set to the same
         * values as the cursor.
         */
        public void resetTo(CursorPosition pos) {
            start.asFor(pos);
            cursor.asFor(pos);
        }

        public void selectSentence() {
            resetSelection();
            start.character = 0;
            cursor.end();
        }

        /**
         * set the selection to be for the word marked by the current cursor
         *  
         */
        public void selectWord() {
            resetSelection();
            start.wordLeft();
            cursor.wordRight();
        }

        public CursorPosition to() {
            return backwardSelection() ? start : cursor;
        }

        public String toString() {
            return "Selection [from=" + start.line + "/" + start + ",to=" + cursor.line + "/" + cursor.character + "]";
        }
    }

    public static class Specification extends AbstractFieldSpecification {

        public View createView(Content content, ViewAxis axis) {
            return new SimpleIdentifier(new TextField(content, this, axis, true));
        }

        public String getName() {
            return "Text";
        }
    }

    private static final int LINE_SPACING = 0;
    private static final Category LOG = Category.getInstance(TextField.class);
    private static final int MULTILINE_FIELD_SIZE = 8;
    private static final Text style = Style.NORMAL;
    private CursorPosition cursor;
    private int displayFromLine;
    private int displayToLine;

    private boolean identified;
    private boolean inError; // true if the data is currently not valid
    private String invalidReason = null;
    private boolean isResizing;
    private boolean isSaved = true;
    private int maxTextWidth;
    private boolean multiline = false;
    private int noDisplayLines = 1; // number of lines to display
    private int resizeMarkerSize;
    private Selection selection;
    private boolean showLines = true;
    private TextFieldContent textContent;

    public TextField(Content content, ViewSpecification specification, ViewAxis axis, boolean showLines) {
        super(content, specification, axis);
        this.showLines = showLines;
        setMaxTextWidth(25);

        NakedValue value = getValue();
        cursor = new CursorPosition(0, 0);
        selection = new Selection();
        textContent = new TextFieldContent(this);
        multiline = value instanceof MultilineTextString;
        noDisplayLines = multiline ? MULTILINE_FIELD_SIZE : 1;
        textContent.setText(value.title().toString());
        cursor.home();
        displayFromLine = 0;
        displayToLine = noDisplayLines - 1;
        alignDisplay(0);
        inError = false;
        isSaved = true;

        resizeMarkerSize = lineHeight() * 3 / 5;

    }

    private void alignDisplay(int line) {
        int noContentLines = textContent.noLines();
        int lastLine = noContentLines - 1;

        if (noContentLines < noDisplayLines) {
            displayFromLine = 0;
            displayToLine = lastLine;
        } else {
            if (line > displayToLine) {
                displayToLine = line + 3;
                displayToLine = Math.min(displayToLine, lastLine);

                displayFromLine = displayToLine - noDisplayLines;
                displayFromLine = Math.max(displayFromLine, 0);
            }

            if (line < displayFromLine) {
                displayFromLine = line;
                displayToLine = (displayFromLine + noDisplayLines) - 1;

                if (displayToLine >= noContentLines) {
                    displayToLine = lastLine;
                    displayFromLine = Math.max(0, displayToLine - noDisplayLines);
                }
            }
        }

        LOG.debug("display line " + line + " " + displayFromLine + "~" + displayToLine);
    }

    public boolean canFocus() {
        return canChangeValue();
    }

    /**
     * returns the width (in pixels) of the specified character if it were used in this field.
     */
    public int charWidth(char ch) {
        return style.charWidth(ch);
    }

    private void copy() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        String text = textContent.getText(selection);
        StringSelection ss = new StringSelection(text);
        cb.setContents(ss, ss);

        LOG.debug("copied " + text);
    }

    /**
     * Delete the character to the left of the cursor.
     */
    public void delete() {
        if (selection.hasSelection()) {
            textContent.delete(selection);
            selection.resetTo(selection.from());
            markDamaged();
        } else {
            textContent.deleteLeft(cursor);
            cursor.left();
            selection.resetTo(cursor);
        }
    }

    /**
     * Delete the character to the right of the cursor.
     */
    public void deleteForward() {
        if (selection.hasSelection()) {
            textContent.delete(selection);
            selection.resetTo(selection.from());
            markDamaged();
        } else {
            textContent.deleteRight(cursor);
            markDamaged();
        }
    }

    public void drag(InternalDrag drag) {
        if (isResizing) {
            ;
        } else {
            if (canChangeValue()) {
                selection.extendTo(drag.getTargetLocation());
                markDamaged();
            }
        }
    }

    public View dragFrom(InternalDrag drag) {
        Location at = drag.getSourceLocation();

        if (isOnResize(at)) {
            isResizing = true;
        } else {
            if (canChangeValue()) {
                isResizing = false;
                cursor.cursorAt(at);
                resetSelection();
            }
        }

        markDamaged();

        return null;
    }

    public void dragTo(InternalDrag drag) {
        Location at = drag.getTargetLocation();

        if (isResizing) {
            isResizing = false;
            getViewManager().showTextCursor();

            int lines = Math.max(1, at.getY() / lineHeight());
            setNoLines(lines);

            int width = Math.max(80, at.getX() - HPADDING - lineHeight() * 3 / 5);
            setMaxWidth(width);
            LOG.debug(lines + " x " + width);

            invalidateLayout();
            //  validateLayout();
        } else {
            if (canChangeValue()) {
                selection.extendTo(at);
                markDamaged();
            }
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (DEBUG) {
            Bounds r = getBounds();
            canvas.drawRectangle(r.getSize(), Color.DEBUG2);
            canvas.drawRectangle(HPADDING, VPADDING, r.getWidth() - HPADDING * 2 - 1, r.getHeight() - VPADDING * 2 - 1,
                    Color.DEBUG2);
            canvas.drawLine(0, r.getHeight() / 2, r.getWidth() - 1, r.getHeight() / 2, Color.DEBUG2);
        }

        int width = getMaxWidth();

        drawHighlight(canvas, width);
        drawLines(canvas, width);
        drawText(canvas, width);

        if (isResizing) {
            Color color = Style.SECONDARY1;
            canvas.drawLine(0, 0, 10, 0, color);
            canvas.drawLine(0, 1, 10, 1, color);
            canvas.drawLine(0, 0, 0, 10, color);
            canvas.drawLine(1, 0, 1, 10, color);
        }
    }

    private void drawHighlight(Canvas canvas, int maxWidth) {
        if (hasFocus() && selection.hasSelection()) {
            int baseline = getBaseline();
            int top = baseline - style.getAscent();

            CursorPosition from = selection.from();
            CursorPosition to = selection.to();

            for (int i = displayFromLine; i <= displayToLine; i++) {
                if ((i >= from.line) && (i <= to.line)) {
                    String line = textContent.getText(i);
                    int start = 0;
                    int end = style.stringWidth(line);

                    if (from.line == i) {
                        int at = Math.min(from.character, line.length());
                        start = style.stringWidth(line.substring(0, at));
                    }

                    if (to.line == i) {
                        int at = Math.min(to.character, line.length());
                        end = style.stringWidth(line.substring(0, at));
                    }

                    canvas.drawSolidRectangle(start + (HPADDING), top, end - start, lineHeight(), Style.PRIMARY3);
                }

                top += lineHeight();
            }
        }
    }

    private void drawLines(Canvas canvas, int width) {
        if (showLines = true && canChangeValue()) {
            int lineHeight = lineHeight();
            int baseline = 0;

            Color color = identified ? Style.IDENTIFIED : Style.SECONDARY2;
            color = hasFocus() ? Style.PRIMARY1 : color;

            for (int line = 0; line < noDisplayLines; line++) {
                baseline += lineHeight();
                canvas.drawLine(HPADDING, baseline, HPADDING + width, baseline, color);
            }

            if (identified) {
                Shape shape = new Shape(0, 0);
                shape.addLine(resizeMarkerSize, 0);
                shape.addLine(0, resizeMarkerSize);
                shape.addLine(-resizeMarkerSize, -resizeMarkerSize);
                canvas.drawSolidShape(shape, HPADDING + width + 1, baseline, color);
            }
        }

    }

    private void drawText(Canvas canvas, int width) {
        Color textColor;
        int baseline = getBaseline();

        if (inError) {
            textColor = new Color(0x999900);
        } else if (isInvalid()) {
            textColor = Style.INVALID;
        } else if (hasFocus()) {
            if (isSaved) {
                textColor = Style.PRIMARY1;
            } else {
                textColor = Style.PRIMARY2;
            }
        } else if (getParent().getState().isRootViewIdentified()) {
            textColor = Style.PRIMARY3;
        } else {
            textColor = Style.BLACK;
        }

        //LOG.debug(displayFromLine + " -> " + displayToLine);

        for (int i = displayFromLine; i <= displayToLine; i++) {
            String chars = textContent.getText(i);

            if (chars.endsWith("\n")) { throw new RuntimeException(); }

            // paint cursor
            if (hasFocus() && (cursor.line == i) && canChangeValue()) {
                //                g.setColor(Style.ACTIVE);
                int at = Math.min(cursor.character, chars.length());
                int pos = style.stringWidth(chars.substring(0, at));
                canvas.drawLine(pos + (HPADDING), (baseline + style.getDescent()), pos + (HPADDING),
                        baseline - style.getAscent(), Style.PRIMARY1);

                // highlight cursor location for moving mouse
                /*
                 * g.setColor(Color.pink); at = Math.min(mouseOver.character, chars.length()); pos =
                 * style.stringWidth(chars.substring(0, at)); g.drawLine(pos + (PADDING), (baseline +
                 * style.getDescent()), pos + (PADDING), baseline - style.getAscent());
                 */
            }

            // paint text
            //LOG.debug(i + " painting: " + chars);
            canvas.drawText(chars, HPADDING, baseline, textColor, style);
            baseline += lineHeight();
        }

        /*
         * TODO re-instate if (end < entryLength) { int x = style.stringWidth(new String(buffer,
         * start, end)); g.setColor(Color.red); g.drawString("\u00bb", x, baseline - lineHeight()); }
         */
    }

    public void editComplete() {
        if (canChangeValue()) {
            String entry = textContent.getText();

            // do nothing if entry is same as the value object
            if (!entry.equals(getValue().title().toString())) {
                LOG.debug("Field edited: \'" + entry + "\' to replace \'" + getValue().title() + "\'");

                try {
                    set(entry.toString());
                    invalidReason = null;
                    isSaved = true;
                    markDamaged();
                    getViewManager().setStatus("VALID ENTRY: " + entry);
                    //getParent().invalidateLayout();
                } catch (InvalidEntryException e) {
                    invalidReason = "INVALID ENTRY: " + e.getMessage();
                    getViewManager().setStatus(invalidReason);
                    markDamaged();
                }
            }
        }
    }

    public void entered() {
        getViewManager().showTextCursor();
        identified = true;
        markDamaged();
    }

    public void exited() {
        getViewManager().showArrowCursor();
        identified = false;
        markDamaged();
    }

    /**
     * Responds to first click by placing the cursor between the two characters nearest the point of
     * the mouse.
     * 
     * @see View#firstClick(Click)
     */
    public void firstClick(Click click) {
        if (canChangeValue()) {
            Location at = click.getLocation();
            cursor.cursorAt(at);
            resetSelection();
            markDamaged();
        }
    }

    public void focusLost() {
        LOG.debug("Focus lost " + this);
        editComplete();
    }

    public void focusRecieved() {
        getViewManager().setStatus(invalidReason == null ? "" : invalidReason);
        resetSelection();
    }

    public int getBaseline() {
        return style.getAscent() + VPADDING;
    }

    public int getMaxWidth() {
        return maxTextWidth;
    }

    public int getNoDisplayLines() {
        return noDisplayLines;
    }

    public Size getRequiredSize() {
        int width = HPADDING + maxTextWidth + 1 + resizeMarkerSize + HPADDING;
        int height = noDisplayLines * (style.getHeight() + LINE_SPACING) + VPADDING * 2;
        height = Math.max(height, Style.defaultFieldHeight());

        return new Size(width, height);
    }

    /**
     * modifies the selection object so that text is selected if the flag is true, or text is
     * unselected if false.
     * 
     * @param select
     */
    private void highlight(boolean select) {
        if (canChangeValue()) {
            if (!select) {
                selection.resetTo(cursor);
            } else {
                selection.extendTo(cursor);
            }
        }
    }

    private void insert(char character) {
        insert("" + character);
        selection.resetTo(cursor);
    }

    private void insert(String characters) {
        textContent.insert(cursor, characters);
        cursor.moveForward(characters.length());
        isSaved = false;
    }

    public boolean isIdentified() {
        return identified;
    }

    private boolean isInvalid() {
        return invalidReason != null;
    }

    private boolean isOnResize(Location at) {
        int size = lineHeight() * 3 / 5;
        int x = AbstractView.HPADDING + getMaxWidth() + 1;
        int y = getBaseline() - size - lineHeight();

        return (at.getX() >= x) && (at.getY() >= y);
    }

    /**
     * Called when the user presses any key on the keyboard while this view has the focus.
     * 
     * @param keyCode
     * @param modifiers
     */
    public void keyPressed(final int keyCode, final int modifiers) {
        if (!canChangeValue()) { return; }

        if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_ALT) { return; }

        // modifiers
        final boolean alt = (modifiers & InputEvent.ALT_MASK) > 0;
        final boolean shift = (modifiers & InputEvent.SHIFT_MASK) > 0;
        final boolean ctrl = (modifiers & InputEvent.CTRL_MASK) > 0;

        switch (keyCode) {
        // ctrl+pgup
        case KeyEvent.VK_PAGE_UP:

            if (ctrl) {
                noDisplayLines++;
                alignDisplay(cursor.line);
                invalidateLayout();
            } else {
                cursor.pageUp();
            }

            break;

        // ctrl+pgdn
        case KeyEvent.VK_PAGE_DOWN:

            if (ctrl) {
                if ((noDisplayLines > 1)) {
                    noDisplayLines--;
                    alignDisplay(cursor.line);
                    invalidateLayout();
                }
            } else {
                cursor.pageDown();
            }

            break;

        case KeyEvent.VK_V:

            if (ctrl) {
                paste();
                highlight(false);
            }

            break;

        case KeyEvent.VK_C:

            if (ctrl) {
                copy();
            }

            break;

        case KeyEvent.VK_DOWN:
            cursor.lineDown();
            highlight(shift);

            break;

        case KeyEvent.VK_UP:
            cursor.lineUp();
            highlight(shift);

            break;

        case KeyEvent.VK_HOME:

            if (alt) {
                cursor.top();
            } else {
                cursor.home();
            }

            highlight(shift);

            break;

        case KeyEvent.VK_END:

            if (alt) {
                cursor.bottom();
            } else {
                cursor.end();
            }

            highlight(shift);

            break;

        case KeyEvent.VK_LEFT:

            if (alt) {
                cursor.wordLeft();
            } else {
                cursor.left();
            }

            highlight(shift);

            break;

        case KeyEvent.VK_RIGHT:

            if (alt) {
                cursor.wordRight();
            } else {
                cursor.right();
            }

            highlight(shift);

            break;

        case KeyEvent.VK_DELETE:
            deleteForward();

            break;

        case KeyEvent.VK_BACK_SPACE:
            delete();

            break;

        case KeyEvent.VK_TAB:

            if (!isSaved) {
                editComplete();
            }

            // TODO movement between fields to happen in view manager
            if ((modifiers & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) {
                focusPrevious();
            } else {
                focusNext();
            }

            break;

        case KeyEvent.VK_ENTER:

            if (multiline) {
                newline();
            } else {
                if (!isSaved) {
                    editComplete();
                }
            }

            break;

        case KeyEvent.VK_ESCAPE:
            invalidReason = null;
            refresh();

            break;

        default:
            break;
        }

        markDamaged();

        LOG.debug("Character at " + cursor.character + " line " + cursor.line + " (" + displayFromLine + "~" + displayToLine
                + ")");
        LOG.debug(selection);
    }

    /**
     * Called when the user releases any key on the keyboard while this view has the focus.
     * 
     * @param keyCode
     * @param modifiers
     */
    public void keyReleased(int keyCode, int modifiers) {}

    /**
     * Called when the user presses a non-control key (i.e. data entry keys and not shift, up-arrow
     * etc). Such a key press will result in a prior call to <code>keyPressed</code> and a
     * subsequent call to <code>keyReleased</code>.
     * 
     * @param keyCode
     */
    public void keyTyped(char keyCode) {
        if (canChangeValue()) {
            insert(keyCode);
        }
    }

    public int lineHeight() {
        return style.getHeight() + LINE_SPACING;
    }

    public void menuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.OBJECT, new MenuOption("Refresh") {
            public void execute(Workspace workspace, View view, Location at) {
                invalidReason = null;
                refresh();
            }
            
            public Permission disabled(View component) {
                return Permission.allow(invalidReason != null);
            }
        });
        super.menuOptions(options);
    }

    /**
     * Detects wheter the point is in the lower right corner, and if so changes the cursor to show
     * it can be resized.
     * 
     * @see View#mouseMoved(Location)
     */
    public void mouseMoved(Location at) {
        if (isOnResize(at)) {
            getViewManager().showResizeDownRightCursor();
        } else {
            getViewManager().showTextCursor();
        }
    }

    /**
     * Inserts a newline at the cursor
     */
    private void newline() {
        if (!multiline) { throw new IllegalStateException(
                "Newline can not be inserted into a one-line field (only in multiline fields)"); }

        textContent.breakBlock(cursor);
        cursor.lineDown();
        cursor.home();
        markDamaged();
    }

    public void paste() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable content = cb.getContents(this);

        try {
            String text = (String) content.getTransferData(DataFlavor.stringFlavor);
            insert(text);
            LOG.debug("pasted " + text);
        } catch (Throwable e) {
            LOG.error("Invalid paste operation " + e);
        }
    }

    public void refresh() {
        Value objectField = getObjectField();
        if (invalidReason == null) {
            // TODO decide how to deal with updates that affect a field that is being edited
            String text = getValue().title().toString();

            if (!text.equals(textContent.getText())) {
                LOG.debug("Refreshing '" + textContent.getText() + "' with '" + text + "'");
                textContent.setText(text);
                cursor.home();
                displayFromLine = 0;
                displayToLine = noDisplayLines - 1;
                alignDisplay(0);
                markDamaged();
            }

            inError = false;
            isSaved = true;
        }
    }

    private void resetSelection() {
        selection.resetTo(cursor);
    }

    public void secondClick(Click click) {
        if (canChangeValue()) {
            selection.selectWord();
        }
    }

    /**
     * Set the maximum width of the field, as a number of characters
     */
    public void setMaxTextWidth(int noCharacters) {
        maxTextWidth = charWidth('o') * noCharacters;
    }

    /**
     * Set the maximum width of the field, as a number of pixels
     */
    public void setMaxWidth(int width) {
        maxTextWidth = width;
    }

    /**
     * Sets the number of lines to display
     */
    public void setNoLines(int noLines) {
        noDisplayLines = noLines;
    }

    public int stringWidth(String string) {
        return style.stringWidth(string);
    }

    public void thirdClick(Click click) {
        if (canChangeValue()) {
            selection.selectSentence();
            markDamaged();
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
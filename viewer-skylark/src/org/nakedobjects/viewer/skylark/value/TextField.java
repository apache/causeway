
package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.SimpleInternalDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.LabelAxis;
import org.nakedobjects.viewer.skylark.basic.SimpleIdentifier;
import org.nakedobjects.viewer.skylark.core.AbstractFieldSpecification;
import org.nakedobjects.viewer.skylark.core.BackgroundTask;
import org.nakedobjects.viewer.skylark.core.BackgroundThread;
import org.nakedobjects.viewer.skylark.text.CursorPosition;
import org.nakedobjects.viewer.skylark.text.TextBlockTarget;
import org.nakedobjects.viewer.skylark.text.TextContent;
import org.nakedobjects.viewer.skylark.text.TextSelection;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;


public class TextField extends AbstractField implements TextBlockTarget {
    private static final int ORIGINAL_WIDTH = 20;

    public static class Specification extends AbstractFieldSpecification {

        public View createView(Content content, ViewAxis axis) {
            return new SimpleIdentifier(new TextField(content, this, axis, true));
        }

        public String getName() {
            return "Text";
        }
    }

    private static final Logger LOG = Logger.getLogger(TextField.class);
    private static final Text style = Style.NORMAL;        
    private CursorPosition cursor;
    private boolean identified;
    private String invalidReason = null;
    private boolean isSaved = true;
    private int maxTextWidth;
    private boolean multiline = false;
    private boolean wrapping = false;
    private TextSelection selection;
    private TextContent textContent;
    private boolean showLines;
    private int maximumLength = 0;
    private int minumumLength = 0;

    public TextField(Content content, ViewSpecification specification, ViewAxis axis, boolean showLines) {
        super(content, specification, axis);
        this.showLines = showLines;
        setMaxTextWidth(ORIGINAL_WIDTH);

        NakedValue value = getValue();
        if(value != null) {
            maximumLength = value.getMaximumLength();
            minumumLength = value.getMinumumLength();
        }
        textContent = new TextContent(this, 1);
        cursor = new CursorPosition(textContent, 0, 0);
        selection = new TextSelection(cursor, cursor);
        textContent.setText(value == null ? "" : value.titleString());
        cursor.home();
        isSaved = true;
    }

    private NakedValue getValue() {
        return (NakedValue) getContent().getNaked();
    }

    public void setSize(Size size) {
        super.setSize(size);
        
        setMaxWidth(size.getWidth() - 2 * HPADDING);
        textContent.setNoDisplayLines(size.getHeight() / style.getTextHeight());
    }
    
    public boolean canFocus() {
        return canChangeValue();
    }

    private void copy() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        String text = textContent.getText(selection);
        StringSelection ss = new StringSelection(text);
        cb.setContents(ss, ss);

        LOG.debug("copied " + text);
    }

    public String debugDetails() {
        return super.debugDetails() + "\n"+ textContent;
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
        isSaved = false;
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
        isSaved = false;
    }

    public void drag(InternalDrag drag) {
        if (canChangeValue()) {
            selection.extendTo(drag.getLocation());
            markDamaged();
        }
    }

    public Drag dragStart(DragStart drag) {
        Location at = drag.getLocation();
        
        Size size = getView().getSize();
        Location anchor = getView().getAbsoluteLocation();
        ViewAxis axis = getViewAxis();
        if(axis instanceof LabelAxis) {
            int width = ((LabelAxis) axis).getWidth();
            size.contractWidth(width);
            anchor.add(width, 0);
        }

        if (canChangeValue()) {
            cursor.cursorAt(at);
            resetSelection();
            return new SimpleInternalDrag(this, anchor);
        }
        
        markDamaged();
        
        return null;
    }
    
    public void dragTo(InternalDrag drag) {
        Location at = drag.getLocation();
        if (canChangeValue()) {
            selection.extendTo(at);
            markDamaged();
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getMaxWidth();
        drawHighlight(canvas, width);
        drawLines(canvas, width);
        drawText(canvas, width);
    }

    private void drawHighlight(Canvas canvas, int maxWidth) {
        if (hasFocus() && selection.hasSelection()) {
            int baseline = getBaseline();
            int top = baseline - style.getAscent();

            CursorPosition from = selection.from();
            CursorPosition to = selection.to();
/**
            for (int i = displayFromLine; i <= displayToLine; i++) {
                if ((i >= from.getLine()) && (i <= to.getLine())) {
                    String line = textContent.getText(i);
                    int start = 0;
                    int end = style.stringWidth(line);

                    if (from.getLine() == i) {
                        int at = Math.min(from.getCharacter(), line.length());
                        start = style.stringWidth(line.substring(0, at));
                    }

                    if (to.getLine() == i) {
                        int at = Math.min(to.getCharacter(), line.length());
                        end = style.stringWidth(line.substring(0, at));
                    }

                    canvas.drawSolidRectangle(start + (HPADDING), top, end - start, lineHeight(), Style.PRIMARY3);
                }

                top += lineHeight();
            }
     */
            	}
    }

    private void drawLines(Canvas canvas, int width) {
        if (showLines == true && canChangeValue()) {
            int baseline = getBaseline() + 1;

            Color color = identified ? Style.IDENTIFIED : Style.SECONDARY2;
            color = hasFocus() ? Style.PRIMARY1 : color;

            int noDisplayLines = textContent.getNoDisplayLines();
            for (int line = 0; line < noDisplayLines; line++) {
                canvas.drawLine(HPADDING, baseline, HPADDING + width, baseline, color);
                baseline += getText().getLineHeight();
            }
        }
    }

    private void drawText(Canvas canvas, int width) {
        Color textColor;
        int baseline = getBaseline();

        if (getState().isInvalid()) {
            textColor = Style.BLACK;
        } else if (hasFocus()) {
            if (isSaved) {
                textColor = Style.PRIMARY1;
            } else {
                textColor = Style.TEXT_EDIT;
            }
        } else {
            textColor = Style.BLACK;
        }

        //LOG.debug(displayFromLine + " -> " + displayToLine);
        
        String[] lines = textContent.getDisplayLines();
        for (int i = 0; i < lines.length; i++) {
            String chars = lines[i];
            if(chars == null) {
                throw new NakedObjectRuntimeException();
            }
            if (chars.endsWith("\n")) { throw new RuntimeException(); }

            // draw cursor
            if (hasFocus() && (cursor.getLine() == i) && canChangeValue()) {
                int at = Math.min(cursor.getCharacter(), chars.length());
                int pos = style.stringWidth(chars.substring(0, at));
                canvas.drawLine(pos + (HPADDING), (baseline + style.getDescent()), pos + (HPADDING),
                        baseline - style.getAscent(), Style.PRIMARY1);
            }

            // draw text
            canvas.drawText(chars, HPADDING, baseline, textColor, style);
            baseline += getText().getLineHeight();
        }
/*
        if (end < entryLength) {
            int x = style.stringWidth(new String(buffer, start, end));
            g.setColor(Color.red);
            g.drawString("\u00bb", x, baseline - lineHeight());
        }
        */
    }

    public void editComplete() {
        if (canChangeValue() && !isSaved) {
            isSaved = true;
            BackgroundThread.run(this, new BackgroundTask() {
                protected void execute() {
                    save();
                    getParent().updateView();
                    invalidateLayout();
                }
            });
        }      
    }
    
    protected void save() {
        String entry = textContent.getText();
        
        // do nothing if entry is same as the value object
        NakedValue value = getValue();
        if (!entry.equals(value == null ? "" : value.titleString())) {
            LOG.debug("Field edited: \'" + entry + "\' to replace \'" + (value == null ? "" : value.titleString()) + "\'");
            
            if(entry.length() < minumumLength) {
                invalidReason = "Entry not long enough, must be at least " + minumumLength + " characters";
                LOG.error(invalidReason);
                getViewManager().setStatus(invalidReason);
                getState().setInvalid();
                markDamaged();
            } else {
	            try {
	                parseEntry(entry.toString());
	                invalidReason = null;
	                getViewManager().getSpy().addAction("VALID ENTRY: " + entry);
	                getState().setValid();
	                markDamaged();
	                getParent().invalidateContent();
	            } catch (NakedObjectRuntimeException e) {
	                invalidReason = "UPDATE FAILURE: " + e.getMessage();
	                LOG.error(invalidReason, e);
	                getViewManager().setStatus(invalidReason);
	                getState().setOutOfSynch();
	                markDamaged();
	          } catch (InvalidEntryException e) {
	                invalidReason = "INVALID ENTRY: " + e.getMessage();
	                getViewManager().setStatus(invalidReason);
	                getState().setInvalid();
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
     */
    public void firstClick(Click click) {
        if (canChangeValue()) {
            Location at = click.getLocation();
            at.subtract(HPADDING, VPADDING);
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

    public Size getRequiredSize() {
        int width = HPADDING + maxTextWidth + HPADDING;
        int height = textContent.getNoDisplayLines() * getText().getLineHeight() + VPADDING * 2;
        height = Math.max(height, Style.defaultFieldHeight());

        return new Size(width, height);
    }

    public Text getText() {
        return style;
    }
    
    /**
     * modifies the selection object so that text is selected if the flag is true, or text is
     * unselected if false.
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
        if(withinMaximum(1)) {
	        insert("" + character);
	        selection.resetTo(cursor);
        } else {
            getViewManager().setStatus("Entry can be no longer than " + maximumLength + " characters");
        }
    }

    private boolean withinMaximum(int characters) {
        return maximumLength == 0 || textContent.getText().length() + characters <= maximumLength;
    }

    private void insert(String characters) {
        if(withinMaximum(characters.length())) {
            int noLines = textContent.getNoDisplayLines();
	        textContent.insert(cursor, characters);
	        cursor.right(characters.length());
	        if(textContent.getNoDisplayLines() != noLines) {
	            invalidateLayout();
	        }
	        isSaved = false;
        } else {
            getViewManager().setStatus("Entry can be no longer than " + maximumLength + " characters");
        }
    }

    public boolean isIdentified() {
        return identified;
    }

    /**
     * Called when the user presses any key on the keyboard while this view has the focus.
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
                textContent.increaseDepth();        
                textContent.alignDisplay(cursor.getLine());    
                invalidateLayout();
            } else {
                cursor.pageUp();
            }

            break;

        // ctrl+pgdn
        case KeyEvent.VK_PAGE_DOWN:

            if (ctrl) {
                if( textContent.decreaseDepth()) {
                    textContent.alignDisplay(cursor.getLine());
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

        LOG.debug("Character at " + cursor.getCharacter() + " line " + cursor.getLine());
        LOG.debug(selection);
    }

    /**
     * Called when the user releases any key on the keyboard while this view has the focus.
     */
    public void keyReleased(int keyCode, int modifiers) {}

    /**
     * Called when the user presses a non-control key (i.e. data entry keys and not shift, up-arrow
     * etc). Such a key press will result in a prior call to <code>keyPressed</code> and a
     * subsequent call to <code>keyReleased</code>.
     */
    public void keyTyped(char keyCode) {
        if (canChangeValue()) {
            insert(keyCode);
        }
    }

    public void contentMenuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.OBJECT, new MenuOption("Refresh") {
            public void execute(Workspace workspace, View view, Location at) {
                invalidReason = null;
                refresh();
            }
            
            public Consent disabled(View component) {
                return AbstractConsent.allow(invalidReason != null);
            }
        });
        super.contentMenuOptions(options);
    }

    /**
     * Inserts a newline at the cursor
     */
    private void newline() {
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
        super.refresh();
        NakedValue object = getValue();
        if(object == null) {
            textContent.setText("");
        } else {
	        String value = object.titleString();
	        textContent.setText(value);
	        maximumLength = object.getMaximumLength();
	        minumumLength = object.getMinumumLength();
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
        maxTextWidth = getText().charWidth('o') * noCharacters;
    }
    
    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }
    
    public void setWrapping(boolean wrapping) {
        this.wrapping = wrapping;
    }

    public void setRequiredSize(Size size) {
        int lines = Math.max(1, size.getHeight() / getText().getLineHeight());
        setNoLines(lines);
	    int width = Math.max(180, size.getWidth() - HPADDING);
        setMaxWidth(width);
        LOG.debug(lines + " x " + width);
        invalidateLayout();
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
        textContent.setNoDisplayLines(noLines);
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
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
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
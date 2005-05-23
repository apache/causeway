
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

import org.apache.log4j.Category;


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

    private static final int LINE_SPACING = 0;
    private static final Category LOG = Category.getInstance(TextField.class);
    private static final Text style = Style.NORMAL;
    private CursorPosition cursor;

    private boolean identified;
    private String invalidReason = null;
 //   private boolean isResizing;
    private boolean isSaved = true;
    private int maxTextWidth;
    private boolean multiline = false;
  //  private int resizeMarkerSize;
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

        //resizeMarkerSize = lineHeight() * 3 / 5;

    }

    private NakedValue getValue() {
        return (NakedValue) getContent().getNaked();
    }

    public void setSize(Size size) {
        super.setSize(size);
        
        setMaxWidth(size.getWidth() - 2 * HPADDING);
        textContent.setNoDisplayLines(size.getHeight() / style.getHeight());
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
    //    if (!isResizing) {
            if (canChangeValue()) {
                selection.extendTo(drag.getLocation());
                markDamaged();
            }
      //  }
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
        
/*        if (isOnResize(at)) {
            isResizing = true;
            
            return new ResizeDrag(this, new Bounds(anchor, size), ResizeDrag.BOTTOM_RIGHT, new Size(80, lineHeight()), null);
        } else {
    */        if (canChangeValue()) {
//                isResizing = false;
                cursor.cursorAt(at);
                resetSelection();
                return new SimpleInternalDrag(this, anchor);
        //    }
        }
        
        markDamaged();
        
        return null;
    }
    
    public void dragTo(InternalDrag drag) {
/*
        if (isResizing) {
            isResizing = false;
            getViewManager().showTextCursor();
            
	        Location at = drag.getLocation();
   
	        // TODO this adjustment shoud be done in drag - so can be seen whilst dragging
            int lines = Math.max(1, at.getY() / lineHeight());
            setNoLines(lines);

            int width = Math.max(80, at.getX() - HPADDING - lineHeight() * 3 / 5);
            setMaxWidth(width);
            LOG.debug(lines + " x " + width);

            invalidateLayout();
        } else {
	*/        Location at = drag.getLocation();
            if (canChangeValue()) {
                selection.extendTo(at);
                markDamaged();
            }
       // }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getMaxWidth();
        drawHighlight(canvas, width);
        drawLines(canvas, width);
        drawText(canvas, width);

     /*   if (isResizing) {
            Color color = Style.SECONDARY1;
            canvas.drawLine(0, 0, 10, 0, color);
            canvas.drawLine(0, 1, 10, 1, color);
            canvas.drawLine(0, 0, 0, 10, color);
            canvas.drawLine(1, 0, 1, 10, color);
        } */
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
            int baseline = getBaseline();

            Color color = identified ? Style.IDENTIFIED : Style.SECONDARY2;
            color = hasFocus() ? Style.PRIMARY1 : color;

            int noDisplayLines = textContent.getNoDisplayLines();
            for (int line = 0; line < noDisplayLines; line++) {
                canvas.drawLine(HPADDING, baseline, HPADDING + width, baseline, color);
                baseline += lineHeight();
            }
/*
            if (false && identified) {
                Shape shape = new Shape(0, 0);
                shape.addLine(resizeMarkerSize, 0);
                shape.addLine(0, resizeMarkerSize);
                shape.addLine(-resizeMarkerSize, -resizeMarkerSize);
        //        canvas.drawSolidShape(shape, HPADDING + width + 1, getBaseline() + (noDisplayLines -1) * lineHeight(), color);
                Size size = getSize();
                canvas.drawSolidShape(shape, size.getWidth() - resizeMarkerSize, size.getHeight(), Style.SECONDARY2);
                canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Style.SECONDARY2);
            }
    */    }

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
//        for (int i = displayFromLine; i <= displayToLine; i++) {
//            String chars = textContent.getText(i);
            if (chars.endsWith("\n")) { throw new RuntimeException(); }

            // paint cursor
            if (hasFocus() && (cursor.getLine() == i) && canChangeValue()) {
                int at = Math.min(cursor.getCharacter(), chars.length());
                int pos = style.stringWidth(chars.substring(0, at));
                canvas.drawLine(pos + (HPADDING), (baseline + style.getDescent()), pos + (HPADDING),
                        baseline - style.getAscent(), Style.PRIMARY1);
            }

            // paint text
            canvas.drawText(chars, HPADDING, baseline, textColor, style);
            baseline += lineHeight();
        }

        //TODO re-instate 
  //      if (end < entryLength) { 
   //         int x = style.stringWidth(new String(buffer, start, end)); g.setColor(Color.red);
  //          g.drawString("\u00bb", x, baseline - lineHeight());
    //        }
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
	  //              isSaved = true;
	                getViewManager().getSpy().addAction("VALID ENTRY: " + entry);
	                getState().setValid();
	                markDamaged();
	                getParent().invalidateContent();
	                //getParent().invalidateLayout();
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

    public int getAscent() {
        return style.getAscent();
    }
    
    public int getMaxWidth() {
        return maxTextWidth;
    }

    public Size getRequiredSize() {
        int width = HPADDING + maxTextWidth + HPADDING;
        int height = textContent.getNoDisplayLines() * (style.getHeight() + LINE_SPACING) + VPADDING * 2;
        height = Math.max(height, Style.defaultFieldHeight());

        return new Size(width, height);
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
/*
    private boolean isOnResize(Location at) {
        int size = lineHeight() * 3 / 5;
        int x = View.HPADDING + getMaxWidth() + 1;
        int y = getBaseline() - size - lineHeight();

        return (at.getX() >= x) && (at.getY() >= y);
    }
*/
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

            // TODO movement between fields to happen in view manager
/*            if ((modifiers & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) {
                focusPrevious();
            } else {
                focusNext();
            }
*/
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

    public int lineHeight() {
        return style.getHeight() + LINE_SPACING;
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
     * Detects wheter the point is in the lower right corner, and if so changes the cursor to show
     * it can be resized.
     * 
     * @see View#mouseMoved(Location)
     */
    public void mouseMoved(Location at) {
 /*       if (isOnResize(at)) {
            getViewManager().showResizeDownRightCursor();
        } else {
     */       getViewManager().showTextCursor();
        //}
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
        maxTextWidth = charWidth('o') * noCharacters;
    }

    public void setRequiredSize(Size size) {
        int lines = Math.max(1, size.getHeight() / lineHeight());
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
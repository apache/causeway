package org.nakedobjects.viewer.skylark.text;

import org.nakedobjects.viewer.skylark.Location;

public class TextSelection {
    private final CursorPosition cursor;
    private final CursorPosition start; // = new CursorPosition(TextField.this, 0, 0);
    
    public TextSelection(TextContent content, CursorPosition cursor) {
        this.cursor = cursor;
        this.start = new CursorPosition(content, 0, 0);
    }

    /**
     * Determine if the selection is back to front. Returns true if the cursor postion is before
     * the start postion.
     */
    private boolean backwardSelection() {
        return cursor.isBefore(start);
    }

    public void extendTo(CursorPosition pos) {
        cursor.asFor(pos);
    }

    /**
     * extends the selection so the end point is the same as the cursor.
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
     */
    public boolean hasSelection() {
        return ! cursor.samePosition(start);
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
        resetTo(cursor);
        start.home();
        cursor.end();
    }

    /**
     * set the selection to be for the word marked by the current cursor
     *  
     */
    public void selectWord() {
       resetTo(cursor);
        start.wordLeft();
        cursor.wordRight();
    }

    public CursorPosition to() {
        return backwardSelection() ? start : cursor;
    }

    public String toString() {
        return "Selection [from=" + start.getLine() + ":" + start.getCharacter() + ",to=" + cursor.getLine() + ":" + cursor.getCharacter() + "]";
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
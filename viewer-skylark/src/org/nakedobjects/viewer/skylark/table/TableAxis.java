package org.nakedobjects.viewer.skylark.table;

import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;


public class TableAxis implements ViewAxis {
    private final String[] names;
    private int rowHeaderOffet;
    private View table;
    private final int[] widths;
    private final NakedObjectField[] fields;

    public TableAxis(NakedObjectField[] fields) {
        this.fields = fields;
        widths = new int[fields.length];
        names = new String[fields.length];
        for (int i = 0; i < widths.length; i++) {
            names[i] = fields[i].getName();
        }
    }

    public void ensureOffset(int offset) {
        rowHeaderOffet = Math.max(rowHeaderOffet, offset + 5);
    }

    /**
     * Returns 0 for left side of first column, 1 for right side of first
     * column, 2 for right side of second column, etc.
     * 
     * If no column border is identified then returns -1.
     */
    public int getColumnBorderAt(int xPosition) {
        int width = getHeaderOffset();
        for (int i = 0, cols = getColumnCount(); i < cols; i++) {
            if (xPosition >= width - 1 && xPosition <= width + 1) {
                return i;
            }
            width += getColumnWidth(i);
        }
        if (xPosition >= width - 1 && xPosition <= width + 1) {
            return getColumnCount();
        }

        return -1;
    }

    public int getColumnCount() {
        return names.length;
    }

    public String getColumnName(int column) {
        return names[column];
    }

    public int getColumnWidth(int column) {
        return widths[column];
    }

    public int getHeaderOffset() {
        return rowHeaderOffet;
    }

    public int getLeftEdge(int resizeColumn) {
        int width = getHeaderOffset();
        for (int i = 0, cols = getColumnCount(); i < resizeColumn && i < cols; i++) {
            width += getColumnWidth(i);
        }
        return width;
    }

    public void invalidateLayout() {
        View[] rows = table.getSubviews();
        for (int i = 0; i < rows.length; i++) {
            rows[i].invalidateLayout();
        }
        table.invalidateLayout();
    }

    public void setOffset(int offset) {
        rowHeaderOffet = offset;
    }

    public void setRoot(View view) {
        table = view;
    }

    public void setWidth(int index, int width) {
        widths[index] = width;
    }

    public void setupColumnWidths(ColumnWidthStrategy strategy) {
        for (int i = 0; i < widths.length; i++) {
            widths[i] = strategy.getPreferredWidth(i, fields[i]);
        }

        
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;


class TableRowLayout extends AbstractBuilderDecorator {
    public TableRowLayout(CompositeViewBuilder design) {
        super(design);
    }

    public Size getRequiredSize(View row) {
        int maxHeight = 0;
        int totalWidth = 0;
        TableAxis axis = ((TableAxis) row.getViewAxis());
        View[] cells = row.getSubviews();
        int maxBaseline = maxBaseline(cells);

        for (int i = 0; i < cells.length; i++) {
            totalWidth += axis.getColumnWidth(i);

            Size s = cells[i].getRequiredSize();
            int b = cells[i].getBaseline();
            int baselineOffset = Math.max(0, maxBaseline - b);
            maxHeight = Math.max(maxHeight, s.getHeight() + baselineOffset);
        }

        return new Size(totalWidth, maxHeight);
    }

    public void layout(View row) {
        int x = 0;

        int rowHeight = row.getSize().getHeight();
        TableAxis axis = ((TableAxis) row.getViewAxis());
        View[] cells = row.getSubviews();
        int maxBaseline = maxBaseline(cells);

        for (int i = 0; i < cells.length; i++) {
            View cell = cells[i];
            Size s = cell.getRequiredSize();
            s.setWidth(axis.getColumnWidth(i));
            s.setHeight(rowHeight);
            cell.setSize(s);

            int b = cell.getBaseline();
            int baselineOffset = Math.max(0, maxBaseline - b);
            cell.setLocation(new Location(x, baselineOffset));

            x += s.getWidth();
        }

        Padding padding = row.getPadding();
        Size size = new Size(padding.getLeftRight(), padding.getTopBottom());
        row.setSize(size);
    }

    private int maxBaseline(View[] cells) {
        int maxBaseline = 0;
        for (int i = 0; i < cells.length; i++) {
            View cell = cells[i];
            maxBaseline = Math.max(maxBaseline, cell.getBaseline());
        }
        return maxBaseline;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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

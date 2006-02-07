package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.FocusManager;
import org.nakedobjects.viewer.skylark.View;


public class TableFocusManager implements FocusManager {
    private int row;
    private int cell;
    private WindowBorder table;

    public TableFocusManager(WindowBorder table) {
        this.table = table;
        
        focusInitialChildView();
    }

    public void focusNextView() {
        View r = table.getSubviews()[row];
        View[] cells = r.getSubviews();
        for (int j = cell + 1; j < cells.length; j++) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell =j;
//                setFocus(cells[cell]);
                cells[j].markDamaged();
                return; 
            }
        }
        
        row++;
        if(row == table.getSubviews().length) {
            row = 0;
        }
        
        r = table.getSubviews()[row];
        cells = r.getSubviews();
        for (int j = 0; j < cells.length; j++) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell =j;
                cells[j].markDamaged();
//                setFocus(cells[cell]);
                return;
            }
        }
    }
    


    public void focusPreviousView() {
        View r = table.getSubviews()[row];
        View[] cells = r.getSubviews();
        for (int j = cell - 1; j >= 0; j--) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell =j;
                cells[j].markDamaged();
                return; 
            }
        }
        
        row--;
        if(row == -1) {
            row = table.getSubviews().length - 1;
        }
        
        r = table.getSubviews()[row];
        cells = r.getSubviews();
        for (int j = cells.length - 1; j >= 0; j--) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell =j;
                cells[j].markDamaged();
                return;
            }
        }
    }

    public void focusParentView() {}

    public void focusFirstChildView() {}

    public void focusLastChildView() {}

    public void focusInitialChildView() {
        row = cell = - 1;
        
        View[] rows = table.getSubviews();
        if (rows.length > 0) {
            row = 0;
            View[] cells = rows[0].getSubviews();
            for (int j = 0; j < cells.length; j++) {
                if (cells[j].canFocus()) {
                    cells[cell].markDamaged();
                    cell =j;
                    cells[j].markDamaged();
                  //  setFocus(cells[cell]);
                    return;
                }
            }
        }
    }

    public View getFocus() {
        View[] rows = table.getSubviews();
        if(row < 0 || row >= rows.length) {
            return table;
        }
        View view = rows[row];
        return view.getSubviews()[cell];
    }

    public void setFocus(View view) {
        if(view == table) {
            return;
        }
    
        View[] rows = table.getSubviews();
        for(row = 0; row < rows.length; row++) {
            View[] cells = rows[row].getSubviews();
            for (int j = 0; j < cells.length; j++) {
                if (view == cells[j] && cells[j].canFocus()) {
                    cells[cell].markDamaged();
                    cell =j;
                    cells[j].markDamaged();
                    return;
                }
            }
        }
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("row", row);
        str.append("cell", cell);
        return str.toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
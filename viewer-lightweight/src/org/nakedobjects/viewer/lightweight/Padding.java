/*
        Naked Objects - a framework that exposes behaviourally complete
        business objects directly to the user.
        Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
package org.nakedobjects.viewer.lightweight;

import java.awt.Insets;


public class Padding {
    int bottom;
    int left;
    int right;
    int top;

    public Padding(int top, int left, int bottom, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public Padding() {
        top = 0;
        bottom = 0;
        left = 0;
        right = 0;
    }

    public Padding(Padding padding) {
        this.top = padding.top;
        this.bottom = padding.bottom;
        this.left = padding.left;
        this.right = padding.right;
    }

    Padding(Insets insets) {
        this(insets.top, insets.left, insets.bottom, insets.right);
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getLeft() {
        return left;
    }

    public int getLeftRight() {
        return left + right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getRight() {
        return right;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getTop() {
        return top;
    }

    public int getTopBottom() {
        return top + bottom;
    }

    /**
     * Extend the padding on the bottom by the specified amount.
     */
    public void extendBottom(int pad) {
        bottom += pad;
    }

    /**
     * Extend the padding on the left by the specified amount.
     */
    public void extendLeft(int pad) {
        left += pad;
    }

    /**
     * Extend the padding on the right by the specified amount.
     */
    public void extendRight(int pad) {
        right += pad;
    }

    /**
     * Extend the padding on the top by the specified amount.
     */
    public void extendTop(int pad) {
        top += pad;
    }
    
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof Padding) {
			Padding object = (Padding) obj;

			return object.top == this.top && object.bottom == this.bottom && object.left == this.left && object.right == this.right;
		}

		return false;
	}


    
    public String toString() {
		return "Padding [top=" + top + ",bottom=" + bottom + ",left=" + left + ",right=" + right + "]";
	}
}

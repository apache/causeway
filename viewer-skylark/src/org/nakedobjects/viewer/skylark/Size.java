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
package org.nakedobjects.viewer.skylark;

import java.awt.Dimension;


public class Size {
    int height;
    int width;

    public Size() {
        width = 0;
        height = 0;
    }

    Size(Dimension dimension) {
        this(dimension.width, dimension.height);
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size(Size size) {
        width = size.width;
        height = size.height;
    }
    
    public void contract(int width, int height) {
        this.width -= width;
        this.height -= height;
    }

    public void contract(Size size) {
        this.width -= size.width;
        this.height -= size.height;
    }

    public void contractHeight(int height) {
        this.height -= height;
    }

	public void contract(Padding padding) {
		height -= padding.top + padding.bottom;
		width -= padding.left + padding.right;
	}

    public void contractWidth(int width) {
        this.width -= width;
    }

    public void ensureHeight(int height) {
        this.height = Math.max(this.height, height);
    }

    public void ensureWidth(int width) {
        this.width = Math.max(this.width, width);
    }
    
	public boolean equals(Object obj) {
		 if (obj == this) {
			 return true;
		 }

		 if (obj instanceof Size) {
			Size object = (Size) obj;

			 return object.width == this.width && object.height == this.height;
		 }

		 return false;
	 }

    public void extend(int width, int height) {
        this.width += width;
        this.height += height;
    }
    
	public void extend(Padding padding) {
        this.width += padding.getLeftRight();
        this.height += padding.getTopBottom();
	}

    public void extend(Size size) {
        this.width += size.width;
        this.height += size.height;
    }

    public void extendHeight(int height) {
        this.height += height;
    }

    public void extendWidth(int width) {
        this.width += width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String toString() {
        return width + "x" + height;
    }

}

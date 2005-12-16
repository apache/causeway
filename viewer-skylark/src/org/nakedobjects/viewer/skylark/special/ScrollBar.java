package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Location;


public class ScrollBar {
    private int maximum;
    private int minimum;
    private int scrollPosition = 0;
    private int visibleAmount;

    public ScrollBar() {
        super();
    }

    public void setHorizontalPostion(final int position) {
        scrollPosition = Math.min(position, maximum);
        scrollPosition = Math.max(scrollPosition, minimum);
    }

    public void firstClick(Click click) {
        Location location1 = click.getLocation();
        int x = location1.getX();
        if (click.button3()) {
            setHorizontalPostion(x - visibleAmount / 2);
        } else if (click.button1()) {
            if (x < scrollPosition) {
                setHorizontalPostion(scrollPosition - visibleAmount);
            } else if (x > scrollPosition + visibleAmount) {
                setHorizontalPostion(scrollPosition + visibleAmount);
            }
        }
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getPosition() {
        return scrollPosition;
    }

    public int getVisibleAmount() {
        return visibleAmount;
    }

    public void limit() {
        if (scrollPosition > maximum) {
            scrollPosition = maximum;
        }
    }

    public void reset() {
        scrollPosition = 0;
    }

    public boolean isOnThumb(int pos) {
        return pos > scrollPosition && pos < scrollPosition + visibleAmount;
    }

    public void setSize(int viewportSize, int contentSize) {
        visibleAmount = viewportSize * viewportSize / contentSize;
        maximum = viewportSize - visibleAmount;
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
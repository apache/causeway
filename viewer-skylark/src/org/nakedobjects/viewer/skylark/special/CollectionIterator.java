package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public class CollectionIterator implements CollectionDisplayIterator {
    final static Logger LOG = Logger.getLogger(CollectionIterator.class);
    final static private int DEFAULT_PAGE_SIZE = 12;
    private NakedCollection collection;
    private int displaySize = DEFAULT_PAGE_SIZE;
    private int displayFrom = 0;


    public CollectionIterator(NakedCollection collection) {
        this.collection = collection;
    }

    /**
     * Set the size of the display or viewing window. If the cache size is less
     * than the display size the max cache size will also be set to specified
     * size.
     */
    public void setDisplaySize(int displaySize) {
        if (displaySize <= 1) {
            throw new IllegalArgumentException("Display size must be 1 or greater");
        }

        this.displaySize = displaySize;
    }

    /**
     * 
     * @return int
     */
    public int getDisplaySize() {
        return displaySize;
    }

    /**
     * Return cache to be viewed on current page
     */
    public Enumeration displayElements() {
//        collection.resolve();
        Vector display = new Vector(displaySize);

        int from = displayFrom;
        int to = Math.min(from + displaySize, collection.size());

        for (int i = from; i < to; i++) {
            NakedObject element = (NakedObject) collection.elementAt(i);
  //          getObjectManager().resolve(element);
            display.addElement(element);
        }
        return display.elements();
    }

    /**
     * Position cursor at first element
     */
    public void first() {
 //       resolve();
        displayFrom = 0;
    }

    /**
     * If true there is a next page to display, and 'next' and 'last' options
     * are valid
     */
    public boolean hasNext() {
//        resolve();
        return displayFrom + displaySize < collection.size();
    }

    public boolean hasPrevious() {
        return displayFrom > 0;
    }

    /**
     * Position cursor at last
     */
    public void last() {
   //     resolve();
        displayFrom = collection.size() - displaySize;
        displayFrom = Math.max(0, displayFrom);
    }

    /**
     * Position cursor at beginning of next page
     */
    public void next() {
       // resolve();
        if ((displayFrom + displaySize + displaySize) > collection.size()) {
            displayFrom = collection.size() - displaySize;
            displayFrom = Math.max(0, displayFrom);
        } else {
            displayFrom += displaySize;
        }
    }

    public int position() {
        return displayFrom;
    }

    /**
     * Position cursor at beginning of previous page
     */
    public void previous() {
        if (displayFrom - displaySize < 0) {
            displayFrom = 0;
        } else {
            displayFrom -= displaySize;
        }
    }

    int getStartWindowAt() {
        return displayFrom;
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
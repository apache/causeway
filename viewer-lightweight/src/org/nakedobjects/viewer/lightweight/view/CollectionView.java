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

package org.nakedobjects.viewer.lightweight.view;

import java.util.Enumeration;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.lightweight.AbstractCompositeView;
import org.nakedobjects.viewer.lightweight.AbstractView;
import org.nakedobjects.viewer.lightweight.Bounds;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Click;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.util.StackLayout;


public abstract class CollectionView extends AbstractCompositeView {

    private final int SLIDER_WIDTH = 14;

	public CollectionView() {
        setLayout(new StackLayout());
    }

    public Padding getPadding() {
        Padding padding = super.getPadding();

        if (isSliderNeeded()) {
            padding.extendLeft(SLIDER_WIDTH);
        }

        return padding;
    }

     public void firstClick(Click click) {
        if (isSliderNeeded()) {
			int y = click.getLocation().getY();
			int x = click.getLocation().getX();
            int left = 0;
			int top = getPadding().getTop();
			int width = getPadding().getLeft();

            if ((x >= left) && (x <= (left + width)) && (y >= top)) {
                NakedCollection collection = (NakedCollection) getObject();
				Bounds slider = sliderAt();

                if (y < (top + 10)) {
                    collection.first();
                } else if ((y < slider.getY())) {
                    collection.previous();
                } else if (y > (getSize().getHeight() - getPadding().getTop() -
                        getPadding().getBottom())) {
                    collection.last();
                } else if (y > (slider.getY() + slider.getHeight())) {
                    collection.next();
                }

                refresh();

                return;
            }
        }

        super.firstClick(click);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (isSliderNeeded()) {
            // draw scroll bar
        	Color color = getState().isViewIdentified() ? Style.IN_FOREGROUND : Style.FEINT;
            canvas.drawFullRectangle(sliderAt(), color);

            int x = super.getPadding().getLeft();
            int top = getPadding().getTop();
            int width = SLIDER_WIDTH;
            int height = getSize().getHeight() - getPadding().getTop() -
                getPadding().getBottom();

            canvas.drawLine(x + (width / 2) + 0, top, x + (width / 2) + 0,
                top + height, color);
            canvas.drawLine((x + (width / 2)) - 1, top, (x + (width / 2)) - 1,
                top + height, color);

            if (AbstractView.DEBUG) {
                canvas.drawRectangle(x, top, width, top + height, Color.DEBUG2);
            }
        }
    }

    private boolean isSliderNeeded() {
        NakedCollection collection = (NakedCollection) getObject();

        return (collection.getDisplaySize() > 0) &&
        (collection.size() > collection.getDisplaySize());
    }

    private Bounds sliderAt() {
        NakedCollection collection = (NakedCollection) getObject();

        int x = super.getPadding().getLeft();
        int top = getPadding().getTop();
        int width = SLIDER_WIDTH;
        int height = getSize().getHeight() - getPadding().getTop() - getPadding().getBottom();

        int through = (height * collection.position()) / collection.size();
        int proportion = (height * collection.getDisplaySize()) / collection.size();

        return new Bounds(x + 4, top + through, width - 8, proportion);
    }

    protected void refresh() {
        InternalView[] views = getComponents();
        int i = 0;

        NakedCollection c = (NakedCollection) getObject();
        Enumeration e = c.displayElements();

        while (e.hasMoreElements()) {
            NakedObject obj = (NakedObject) e.nextElement();

            if (i < views.length) {
                if (((ObjectView) views[i]).getObject() != obj) {
                    InternalView view = createListElement(obj);
                    replaceView(views[i], view);
                }
            } else {
                InternalView view = createListElement(obj);
                addView(view);
            }

            i++;
        }
    }
    
    protected abstract InternalView createListElement(NakedObject obj);
    
    public void collectionRemoveUpdate(Object collection, NakedObject element) {
		InternalView[] views = getComponents();

		for (int i = 0; i < views.length; i++) {
			if(((ObjectView)views[i]).getObject().equals(element))
			{
				removeView(views[i]);
			}
		}
		validateLayout();
	}

    public void collectionAddUpdate(Object collection, NakedObject element) {
		InternalView view = createListElement(element);
		addView(view);
		validateLayout();
	}
    
    public void objectUpdate(NakedObject object) {
    	LOG.debug("Object changed, refreshing entries");
    	refresh();
    	validateLayout();
    }
}

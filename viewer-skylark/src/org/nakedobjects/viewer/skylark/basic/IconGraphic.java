package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Icon;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class IconGraphic {
    private ObjectContent content;
    private int iconHeight;

    public IconGraphic(View view, int height) {
        content = (ObjectContent) view.getContent();
        iconHeight = height;
    }

    public boolean isImageAvailable() {
        return ImageFactory.getImageFactory().imageAvailable(iconName(content.getObject()), iconHeight, null);  
    }
    
    public IconGraphic(View view, Text style) {
        content = (ObjectContent) view.getContent();
        iconHeight = (style.getHeight() * 120) / 100;
    }

    public void draw(Canvas canvas, int x, int baseline) {
        Icon icon = icon();

        int iconWidth;
        iconWidth = icon.getWidth();

        int xi = x + View.HPADDING;
        int yi = baseline - getBaseline();  /// TODO move down toward baseline
        canvas.drawIcon(icon, xi, yi);

        if (AbstractView.DEBUG) {
            Size size = getSize();
            canvas.drawRectangle(x, baseline - getBaseline(), size.getWidth() - 1, size.getHeight() - 1, Color.DEBUG3);
            canvas.drawRectangle(xi, yi, iconWidth - 1, iconHeight - 1, Color.DEBUG3);
        }
    }

    public int getBaseline() {
    	return iconHeight - 4;
    }
    
    protected ObjectContent getContent() {
        return content;
    }

    public Size getSize() {
        int height = View.VPADDING + iconHeight + View.VPADDING;

        Icon icon = icon();

        int iconWidth;
        iconWidth = icon.getWidth();

        int width = View.HPADDING + iconWidth + View.HPADDING;

        return new Size(width, height);
    }

    protected Icon icon() {
        return ImageFactory.getImageFactory().createIcon(iconName(content.getObject()), iconHeight, null);
    }

    protected String iconName(NakedObject object) {
        return object.getIconName();
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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

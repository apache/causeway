package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


/*
 *  TODO why does this pass out the baseline, and then expect it back when doing the drawing?
 */
public class IconGraphic {
    private Content content;
    private Image icon;
    private int iconHeight;
    private String lastIconName;
    private int baseline;

    public IconGraphic(View view, int height, int baseline) {
        content = view.getContent();
        iconHeight = height - View.VPADDING * 2;
        this.baseline = baseline;
    }

    public IconGraphic(View view, Text style) {
        content = view.getContent();
        iconHeight = (style.getTextHeight() * 120) / 100 - View.VPADDING * 2;
        //this.baseline = - (style.getAscent() - style.getDescent() - iconHeight) / 2;
        this.baseline = style.getAscent() + View.VPADDING;
    }

    public void draw(Canvas canvas, int x, int baseline) {
        Image icon = icon();

        int xi = x + View.HPADDING;
        // TODO move down toward baseline
        int yi = baseline - getBaseline() + View.VPADDING;
        canvas.drawIcon(icon, xi, yi);

        if (AbstractView.debug) {
            Size size = getSize();
            canvas.drawRectangle(x, baseline - getBaseline(), size.getWidth() - 1, size.getHeight() - 1, Color.DEBUG_DRAW_BOUNDS);
            canvas.drawLine(x, baseline - getBaseline() - size.getHeight() / 2, x + size.getWidth(), baseline - getBaseline()
                    - size.getHeight() / 2, Color.DEBUG_DRAW_BOUNDS);
            canvas.drawLine(x, baseline, x + size.getWidth(), baseline, Color.DEBUG_BASELINE);
        }
    }

    public int getBaseline() {
        return baseline;
    }

    public Size getSize() {
        int height = View.VPADDING + iconHeight + View.VPADDING;

        Image icon = icon();

        int iconWidth;
        iconWidth = icon.getWidth();

        int width = View.HPADDING + iconWidth + View.HPADDING;

        return new Size(width, height);
    }

    protected Image icon() {
        final String iconName = content.getIconName();

        /*
         * If the graphic is based on a name provided by the object then the
         * icon could be changed at any time, so we won't lazily load it.
         */
        if (icon != null && (iconName == null || iconName.equals(lastIconName))) {
            return icon;
        }
        lastIconName = iconName;

        if (iconName != null) {
            icon = ImageFactory.getInstance().createIcon(iconName, iconHeight, null);
        }

        if (icon == null) {
            icon = content.getIconPicture(iconHeight);
        }
        
        return icon;
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

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
package org.nakedobjects.viewer.skylark.util;

import org.nakedobjects.configuration.Configuration;
import org.nakedobjects.viewer.skylark.Background;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Icon;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Viewer;

import org.apache.log4j.Logger;


public class LogoBackground implements Background {
    private static final Logger LOG = Logger.getLogger(LogoBackground.class);
    private static final String PARAMETER_BASE = Viewer.PROPERTY_BASE + "logo-background.";
    private Size logoSize;
    private Icon logo;
    private Location location;

    public LogoBackground() {
        Configuration  cp = Configuration.getInstance();
        String fileName = cp.getString(PARAMETER_BASE + "image", "logo.gif");
        logo = ImageFactory.getImageFactory().loadImage(fileName);

        if (logo == null) {
            LOG.warn("Logo image not found");
        } else {
            location = new Location();
			location.setX(cp.getInteger(PARAMETER_BASE + "position.x", 0));
			location.setY(cp.getInteger(PARAMETER_BASE + "position.y", 0));

			logoSize = new Size();
			logoSize.setWidth(cp.getInteger(PARAMETER_BASE + "size.width", logo.getWidth()));
			logoSize.setHeight(cp.getInteger(PARAMETER_BASE + "size.height", logo.getHeight()));
        }
    }

    public void draw(Canvas canvas, Size viewSize) {
        if (logo != null) {
            int x;
            int y;

            if (location.getX() == 0 && location.getY() == 0) {
                x = viewSize.getWidth() / 2 - logoSize.getWidth() / 2;
                y = viewSize.getHeight() / 2 - logoSize.getHeight() / 2;
            } else {
                x = (location.getX() >= 0) ? location.getX() : viewSize.getWidth() + location.getX() - logoSize.getWidth();
                y = (location.getY() >= 0) ? location.getY() : viewSize.getHeight() + location.getY() - logoSize.getHeight();
            }
            canvas.drawIcon(logo, x, y, logoSize.getWidth(), logoSize.getHeight());
        }
    }
}

package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Picture;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.util.PictureFactory;

public class AboutView extends AbstractView {
    private final int padding = 6;
    private final Picture image;
    private final int left;
    
    public AboutView() {
        super(null, null, null);

        image = PictureFactory.getInstance().loadPicture(AboutNakedObjects.getImageName());
        if(showingImage()) {
            left = padding + image.getWidth() + padding;
        } else {
            left = padding;
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        Size size = getSize();
        canvas.drawSolidRectangle(size, Style.WHITE);
        canvas.drawRectangle(size, Style.SECONDARY1);
        
        if(showingImage()) {
            canvas.drawIcon(image, padding, padding);
        }
        int line = padding + Style.LABEL.getAscent();
        canvas.drawText(AboutNakedObjects.getName(), left, line, Style.BLACK, Style.TITLE);
        line += Style.TITLE.getHeight();
        canvas.drawText(AboutNakedObjects.getVersion(), left, line, Style.BLACK, Style.LABEL);
        line += Style.LABEL.getHeight();
        canvas.drawText(AboutNakedObjects.getBuildId(), left, line, Style.BLACK, Style.LABEL);
        line += Style.LABEL.getHeight();
        canvas.drawText(AboutNakedObjects.getCopyrightNotice(), left, line, Style.BLACK, Style.LABEL);
    }
    
    private boolean showingImage() {
        return image != null;
    }

    public Size getRequiredSize() {
        int height = Style.TITLE.getAscent();
        height += Style.LABEL.getHeight();
        height += Style.LABEL.getHeight();
        
        int width = Style.TITLE.stringWidth(AboutNakedObjects.getName());
        width = Math.max(width, Style.LABEL.stringWidth(AboutNakedObjects.getName()));
        width = Math.max(width, Style.LABEL.stringWidth(AboutNakedObjects.getCopyrightNotice()));
        
        if(showingImage()) {
            height = Math.max(height, image.getHeight());
	        width = image.getWidth() + padding + width;
        }
        
        return new Size(padding + width + padding, padding + height + padding);
    }
    
    public void firstClick(Click click) {
        getWorkspace().removeView(this);
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
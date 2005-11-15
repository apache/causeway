package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.utility.AboutNakedObjects;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.util.ImageFactory;

public class AboutView extends AbstractView {
    private final int padding = 6;
    private final Image image;
    private final int left;
    
    public AboutView() {
        super(null, null, null);

        image = ImageFactory.getInstance().createImage(AboutNakedObjects.getImageName());
        if(showingImage()) {
            left = padding + image.getWidth() + padding;
        } else {
            left = padding;
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        canvas.clearBackground(this, Style.WHITE);
        canvas.drawRectangleAround(this, Style.SECONDARY1);
        
        if(showingImage()) {
            canvas.drawIcon(image, padding, padding);
        }
        int line = padding + Style.LABEL.getAscent();
        
        // application details
        String text = AboutNakedObjects.getApplicationName();
        if(text != null) {
            canvas.drawText(text, left, line, Style.BLACK, Style.TITLE);
            line += Style.TITLE.getLineHeight();
        }
        text = AboutNakedObjects.getApplicationCopyrightNotice();
        if(text != null) {
            canvas.drawText(text, left, line, Style.BLACK, Style.LABEL);
            line += Style.LABEL.getLineHeight();
        }
        text = AboutNakedObjects.getApplicationVersion();
        if(text != null) {
            canvas.drawText(text, left, line, Style.BLACK, Style.LABEL);
            line += 2 * Style.LABEL.getLineHeight();
        }
        
        // framework details
        canvas.drawText(AboutNakedObjects.getFrameworkName(), left, line, Style.BLACK, Style.TITLE);
        line += Style.TITLE.getLineHeight();
        canvas.drawText(AboutNakedObjects.getFrameworkCopyrightNotice(), left, line, Style.BLACK, Style.LABEL);
        line += Style.LABEL.getLineHeight();
        canvas.drawText(frameworkVersion(), left, line, Style.BLACK, Style.LABEL);

    }

    private String frameworkVersion() {
        return AboutNakedObjects.getFrameworkVersion() + " ("+ AboutNakedObjects.getFrameworkBuild() + ")";
    }
    
    private boolean showingImage() {
        return image != null;
    }

    public Size getRequiredSize() {
        int height = Style.TITLE.getAscent();
        height += Style.LABEL.getLineHeight();
        height += 2 * Style.LABEL.getLineHeight();
        
        int width = Style.TITLE.stringWidth(AboutNakedObjects.getFrameworkName());
        width = Math.max(width, Style.LABEL.stringWidth(AboutNakedObjects.getFrameworkCopyrightNotice()));
        width = Math.max(width, Style.LABEL.stringWidth(frameworkVersion()));
        
        
        String text = AboutNakedObjects.getApplicationName();
        if(text != null) {
            height += Style.TITLE.getAscent();
            width = Math.max(width, Style.TITLE.stringWidth(text));
        }
        text = AboutNakedObjects.getApplicationCopyrightNotice();
        if(text != null) {
            height += Style.LABEL.getLineHeight();
            width = Math.max(width, Style.LABEL.stringWidth(text));
        }
        text = AboutNakedObjects.getApplicationVersion();
        if(text != null) {
            height += Style.LABEL.getLineHeight();
            width = Math.max(width, Style.LABEL.stringWidth(text));
        }
        
        if(showingImage()) {
            height = Math.max(height, image.getHeight());
	        width = image.getWidth() + padding + width;
        }
        
        return new Size(padding + width + padding, padding + height + padding);
    }
    
    public void firstClick(Click click) {
        dispose();
    }
}


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
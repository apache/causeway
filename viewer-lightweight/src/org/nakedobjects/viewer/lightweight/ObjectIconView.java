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

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Title;
import org.nakedobjects.viewer.lightweight.Style.Text;
import org.nakedobjects.viewer.lightweight.util.ImageFactory;


public abstract class ObjectIconView extends AbstractObjectView {
    private static int iconSize(Style.Text style) {
        return (style.getHeight() * 120) / 100;
    }

    private Icon createIconImage(int iconHeight, Color tint) {
        return ImageFactory.getImageFactory().createIcon(iconName(), iconHeight, tint);
	}

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color;
        if (includeIcon()) {
            if (getState().canDrop()) {
                color = Style.VALID;
                
            } else if (getState().cantDrop()) {
                color = Style.INVALID;
                
            } else if (getState().isObjectIdentified()) {
                color = shaded() ? Style.IN_BACKGROUND : Style.ACTIVE;
                
            } else if (getState().isRootViewIdentified()) {
                color = shaded() ? Style.IN_BACKGROUND : Style.IN_FOREGROUND;
                
            } else {
                color = Style.IN_BACKGROUND;
            }

            Border border = getBorder();
            int x = (border == null) ? 0 : border.getPadding(this).left;
            int y = (border == null) ? 0 : border.getPadding(this).top;

            Text style = getTitleTextStyle();
            int iconHeight = iconSize(style);
            int centre = (iconHeight + (AbstractView.VPADDING * 2)) / 2;
            int iconWidth = 0;

            if (includeIcon()) {
	            String name = iconName();
	            Icon icon = ImageFactory.getImageFactory().createIcon(name, iconHeight, shaded() ? color : null);
                iconWidth = drawIcon(canvas, x, y, centre, iconHeight, icon);
            }

            if (includeTitle()) {
                int maxWidth = getSize().width - getPadding().left - getPadding().right;
                drawText(canvas, x, y, centre, maxWidth, iconWidth, color, style);
            }
        }
    }
 
    private int drawIcon(Canvas canvas, int x, int y, int centre,
            int iconHeight, Icon icon) {
        int iconWidth;
        iconWidth = icon.getWidth();

        int xi = x + AbstractView.HPADDING;
        int yi = (y + centre) - (iconHeight / 2);
        canvas.drawIcon(icon, xi, yi);

        if (AbstractView.DEBUG) {
            Size size = size();
            canvas.drawRectangle(x, y, size.width - 1, size.height - 1,
                Color.DEBUG3);
            canvas.drawRectangle(xi, yi, iconWidth - 1, iconHeight - 1,
                Color.DEBUG3);
        }

        return iconWidth;
    }

    private void drawText(Canvas canvas, int x,
            int y, int centre, int maxWidth, int iconWidth, Color textColor, Text style) {
        String text = title();
        int xt = x + iconWidth + (AbstractView.HPADDING * 2);
        int yt = y + centre + (style.getAscent() / 2);

        if (style.stringWidth(text) > maxWidth) {
            int elip = style.stringWidth("...");

            do {
                int last = text.lastIndexOf(' ');

                if (last == -1) {
                    for (int i = text.length() - 1; i > 5; i--) {
                        if ((style.stringWidth(text.substring(0, i)) + elip) < maxWidth) {
                            text = text.substring(0, i);

                            break;
                        }
                    }

                    break;
                }

                text = text.substring(0, last);
            } while ((style.stringWidth(text) + elip) > maxWidth);

            text += "...";
        }

        canvas.drawText(text, xt, yt, textColor, style);

        if (AbstractView.DEBUG) {
            canvas.drawRectangle(xt, yt - style.getAscent(),
                style.stringWidth(text) - 1, style.getAscent() - 1, Color.DEBUG3);
        }
    }

    public int getBaseline() {
        int iconSize = iconSize(getTitleTextStyle());
        int iconHeight = iconSize;
        int centre = (iconHeight + (AbstractView.VPADDING * 2)) / 2;

        return centre + (getTitleTextStyle().getAscent() / 2);
    }

    /** @deprecated  replaced by getTitleTextStyle() */
    protected Style.Text getStyle() {
        return getTitleTextStyle();
    }

    /**
     *  Defines the text style that should be used when rendering the title.
     * By default returns the 'normal' text style.
     */
    protected Style.Text getTitleTextStyle() {
        return Style.NORMAL;
    }

	protected String iconName() {
		return getObject().getIconName();
	}

    /**
     * Flags that an icon should be included in this view.  By default returns true.
     */
    protected boolean includeIcon() {
        return true;
    }

    /**
     * Flags that an icon title should be included in this view.  By default returns true.
     */
    protected boolean includeTitle() {
        return true;
    }

    public boolean indicatesForView(Location mouseLocation) {
        if (includeIcon()) {
            return !titleIconBounds().contains(mouseLocation);
        } else {
            return true;
        }
    }

    public boolean objectLocatedAt(Location mouseLocation) {
        if (includeIcon()) {
            return titleIconBounds().contains(mouseLocation);
        } else {
            return false;
        }
    }

    /**
     * Flags that an icon shown as part of background; shaded out.  By default returns false.
     */
    protected boolean shaded() {
        return false;
    }


    /** @deprecated */
    public Size size() {
        return titleSize();
    }

    protected String title() {
        NakedObject object = getObject();
        Title title = object.title();

        if ((title == null) || title.toString().equals("")) {
            return "A " +
            object.getNakedClass().getSingularName().toLowerCase();
        } else {
            return title.toString();
        }
    }

    protected final Bounds titleIconBounds() {
        Text style = getTitleTextStyle();

        int iconHeight = iconSize(style);
        Icon icon = createIconImage(iconHeight, null);

        if (icon == null) {
            return new Bounds();
        }

        Border border = getBorder();
        int x = (border == null) ? 0 : border.getPadding(this).left;
        int y = (border == null) ? 0 : border.getPadding(this).top;

        Size size = titleSize();

        int width = getSize().width - getPadding().left - getPadding().right;

        return new Bounds(x, y, Math.min(width, size.width - 1), size.height -
            1);
    }

	protected final Size titleSize() {
        Text style = getTitleTextStyle();
        int width = 0;

        if (includeTitle()) {
            int textWidth = style.stringWidth(title());
            width += (AbstractView.HPADDING + textWidth +
            AbstractView.HPADDING);
        }

        int iconHeight = iconSize(style);
        Icon icon = createIconImage(iconHeight, null);

        int iconWidth = (icon == null) ? iconHeight : icon.getWidth();
  //      int iconHeight = (icon == null) ? iconHeight : icon.getHeight();

        if (includeIcon()) {
            width += (iconWidth + AbstractView.HPADDING);
        }

        int height = (AbstractView.VPADDING * 2) + iconHeight;

        return new Size(width, height);
    }

    /**
     * Distance to the start of the title text
     */
    public final Bounds titleTextBounds() {
        Text style = getTitleTextStyle();
        int width = 0;
        int height = 0;

        if (includeTitle()) {
            int textWidth = 0;
            textWidth = style.stringWidth(title());
            width += (AbstractView.HPADDING + textWidth +
            AbstractView.HPADDING);
            height += (VPADDING + style.getHeight() + VPADDING);
        }

        int iconSize = iconSize(style);
        Icon icon = createIconImage(iconSize, null);

        int iconWidth = (icon == null) ? iconSize : icon.getWidth();
        int iconHeight = (icon == null) ? iconSize : icon.getHeight();

        if (includeIcon()) {
            height = Math.max(height, VPADDING + iconHeight + VPADDING);
        }

        return new Bounds(HPADDING + iconWidth + HPADDING, 0, width, height);
    }
}

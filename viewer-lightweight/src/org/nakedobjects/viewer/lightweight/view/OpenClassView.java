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

import java.util.Vector;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.AbstractObjectView;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.Icon;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.util.ImageFactory;


public class OpenClassView extends AbstractObjectView implements RootView, DragSource {
    private static Style.Text labelStyle = Style.LABEL;
	private static Style.Text textStyle = Style.NORMAL;
	private static Style.Text titleStyle = Style.TITLE;
	protected static final int ICON_SIZE = textStyle.getAscent() * 3;

	private Vector labels;
	private Vector values;
	private String title;
	private Icon icon;

    public Size getRequiredSize() {
 		int iconWidth = icon.getWidth() + HPADDING * 2 + titleStyle.stringWidth(title);
		int iconHeight = icon.getHeight() + VPADDING * 2;

		
		
        int maxWidth1 = 0;
        int height1 = 0;

        for (int i = 0; i < labels.size(); i++) {
            maxWidth1 = Math.max(maxWidth1, labelStyle.stringWidth(labels.elementAt(i) + ":"));
            height1 += labelStyle.getHeight();
        }

        int maxWidth2 = 0;
        int height2 = 0;

        for (int i = 0; i < values.size(); i++) {
            maxWidth2 = Math.max(maxWidth2, textStyle.stringWidth(values.elementAt(i).toString()));
            height2 += textStyle.getHeight();
        }

        Padding padding = getPadding();

        return new Size(Math.max(iconWidth, maxWidth1 + maxWidth2) + padding.getLeftRight() + 5,
            iconHeight + Math.max(height1, height2) + padding.getTopBottom());
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

		Padding padding = getPadding();

        // icon
//		FontMetrics fm = titleStyle.getMetrics();
		int iconWidth = icon.getWidth();
		int iconHeight = icon.getHeight();
		int xi = padding.getLeft() + HPADDING;
		int yi = padding.getTop() + VPADDING;
		int xt = xi + iconWidth + HPADDING;
		int yt = padding.getTop() + VPADDING + iconHeight / 2 + titleStyle.getAscent() /2;
		canvas.drawIcon(icon, xi, yi);
		canvas.drawText(title, xt, yt, Style.IN_FOREGROUND, titleStyle);


        // labels
//        canvas.setColor(Style.IN_BACKGROUND.getColor());
//        canvas.setFont(labelStyle.getFont());
//
        int lineHeight = labelStyle.getHeight();
        int baseline = padding.getTop() + VPADDING * 2 + iconHeight + labelStyle.getAscent();
        int left = super.getPadding().getLeft();
        int center = 0;
        int x = left;
        int y = baseline;
        
        for (int i = 0; i < labels.size(); i++) {
        	String label = (String) labels.elementAt(i) + ": ";
        	if(label.length() > 2) {
	            canvas.drawText(label, x, y, Style.IN_BACKGROUND, labelStyle);
	            center = Math.max(center, labelStyle.stringWidth(label));
        	}
			y += lineHeight;
        }

        // values
//        canvas.setColor(Style.IN_FOREGROUND.getColor());
//        canvas.setFont(textStyle.getFont());

        x += center + 5;
        y = baseline;
        
        for (int i = 0; i < values.size(); i++) {
            canvas.drawText(values.elementAt(i).toString(), x, y, Style.IN_FOREGROUND, textStyle);
            y += lineHeight;
        }
    }

    protected void init(NakedObject object) {
		NakedClass cls = (NakedClass) object;
		
		title = cls.fullName();
		title = title.substring(title.lastIndexOf('.') + 1);
		icon = ImageFactory.getImageFactory().createIcon(title, ICON_SIZE, null);
 
        labels = new Vector();
		values = new Vector();

        Field[] fields = cls.getVisibleFields(object);
		for (int i = 0; i < fields.length; i++) {
            labels.addElement(i == 0 ? "Fields" : "");
            values.addElement(fields[i].getName());
        }

        Action[] objectActions = cls.getObjectActions(Action.USER);
        for (int i = 0; i < objectActions.length; i++) {
            labels.addElement(i == 0 ? "Actions" : "");
            Action method = objectActions[i];
            String param = method.parameters()[0].fullName();
            values.addElement(method.getName() + "(" + param + ")");
        }
    }
}

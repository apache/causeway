package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;

import java.sql.Time;

class ScheduleLayout extends AbstractBuilderDecorator {
    private final int from = 7 * Time.HOUR;
    private final int to = Time.HOUR * 10;

    public ScheduleLayout(CollectionElementBuilder viewer) {
        super(viewer);
    }

    public Size getRequiredSize(View view) {
		return new Size(155, 400);
	}
    
    public void layout(View view) {
        NakedObjectField field = null;
        int x = 0;
        Size size = view.getSize();
        size.contract(view.getPadding());

        int width = size.getWidth();
        int maxHeight = size.getHeight();

        View[] views = view.getSubviews();

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            NakedObject object = ((ObjectContent) v.getContent()).getObject();

            if (field == null) {
                NakedObjectSpecification nc = object.getSpecification();
                NakedObjectField[] fields = nc.getFields();

                for (int j = 0; j < fields.length; j++) {
                    field = fields[j];

                    if (field.getType().isOfType(NakedObjectSpecificationLoader.getInstance().loadSpecification(TimePeriod.class))) {
                        break;
                    }
                }
            }

            TimePeriod tp = (TimePeriod) object.getField(field);
			int y = (int) (((tp.getStart().longValue() - from) * maxHeight) / to);
            int height = (int) (((tp.getEnd().longValue() - tp.getStart().longValue()) * maxHeight) / to);

            v.setLocation(new Location(x, y));
            v.setSize(new Size(width, height));
        }
    }

	public Time getTime(View view, int y) {
        Size size = view.getSize();
        int maxHeight = size.getHeight();

        int longtime = (y * to) / maxHeight + from;
        Time t = new Time();
        t.setValue(longtime);
        return  t;
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
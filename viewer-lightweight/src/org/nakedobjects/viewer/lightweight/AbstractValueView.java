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

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.lightweight.options.ClearValueOption;
import org.nakedobjects.viewer.lightweight.options.CopyValueOption;
import org.nakedobjects.viewer.lightweight.options.DefaultValueOption;
import org.nakedobjects.viewer.lightweight.options.PasteValueOption;


public abstract class AbstractValueView extends AbstractView implements InternalView,
    KeyboardAccessible, DragInternal {
    protected Value objectField;
    private boolean identified;

    public NakedObject getContainedBy() {
        if (getParent() == null) {
            throw new IllegalStateException("A value field must be contained by an object view");
        }

        return parentObjectView().getObject();
    }

    public abstract NakedValue getValue();

    public Field getFieldOf() {
        return null;
    }

    public boolean getIdentified() {
        return identified;
    }

    public Padding getPadding() {
        return new Padding(0, 0, 0, 0);
    }

    public View getRoot() {
        throw new NotImplementedException();
    }

    /**
     * Determines if the user is able to change the held value.
     */
    public boolean canChangeValue() {
        boolean persistent = !objectField.isDerived();
        boolean fieldReadable = objectField.getAbout(Session.getSession().getSecurityContext(), getContainedBy()).canUse().isAllowed();
        boolean parentReadable = getContainedBy().about().canUse().isAllowed();
        boolean objectEditable = getValue().about().canUse().isAllowed();

        return persistent && fieldReadable && parentReadable && objectEditable;
    }

    public boolean canFocus() {
        return false;
    }

	protected Object clone() throws CloneNotSupportedException {
		AbstractValueView clone = (AbstractValueView) super.clone();
		clone.objectField = objectField;

		return clone;
	}


    /**
     * Indicates the drag started within this view's bounds is continuing.  By default does nothing.
     */
    public void drag(InternalDrag drag) {
    }

    /**
     * Default implementation - does nothing
     */
    public void dragCancel(InternalDrag drag) {
    }

    /**
     * Indicates the start of a drag within this view's bounds.  By default does nothing.
     */
    public void dragFrom(InternalDrag drag) {
    }

    /**
     * Indicates the drag started within this view's bounds has been finished (although the location may now be
     * outside of its bounds).  By default does nothing.
     */
    public void dragTo(InternalDrag drag) {
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        // outline bounds
        if (DEBUG) {
            canvas.drawRectangle(getBounds().getSize(), identified ? Color.DEBUG1 : Color.DEBUG2);
        }
    }

    /**
     * Indicates that editing has been completed and the entry should be saved.  Will be called by the
     * view manager when other action place within the parent.
     *
     */
    public void editComplete() {
    }

    public void entered() {
        identified = true;
    }

    public void exited() {
        identified = false;
    }

    public void focusLost() {
    }

    public void focusRecieved() {
    }

    public boolean hasFocus() {
        return getWorkspace().hasFocus(this);
    }

    public boolean indicatesForView(Location mouseLocation) {
        return false;
    }

    /**
     * Called when the user presses any key on the keyboard while this view has
     * the focus.
     * @param keyCode
     * @param modifiers
     */
    public void keyPressed(final int keyCode, final int modifiers) {
    }

    /**
     * Called when the user releases any key on the keyboard while this view has
     * the focus.
     * @param keyCode
     * @param modifiers
     */
    public void keyReleased(int keyCode, int modifiers) {
    }

    /**
     * Called when the user presses a non-control key (i.e. data entry keys and
     * not shift, up-arrow etc).  Such a key press will result in a prior call
     * to <code>keyPressed</code> and a subsequent call to <code>keyReleased</code>.
     *
     * @param keyCode
     */
    public void keyTyped(char keyCode) {
    }

	public View makeView(Naked object, Field field) throws CloneNotSupportedException {
		if (object instanceof NakedValue) {
			AbstractValueView clone = (AbstractValueView) clone();
			clone.init((NakedValue) object);
			clone.objectField = (Value) field;
			clone.assignId();

			return clone;
		} else {
			throw new IllegalArgumentException("A value field view must be created with a NakedValue");
		}
	}


    protected void init(NakedValue value) {
	}

	public void menuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.OBJECT, new ClearValueOption());
        options.add(MenuOptionSet.OBJECT, new DefaultValueOption());
        options.add(MenuOptionSet.OBJECT, new CopyValueOption());
        options.add(MenuOptionSet.OBJECT, new PasteValueOption());
        options.setColor(Style.VALUE_MENU);
    }

    /**
     * returns the parent view
     */
    public ObjectView parentObjectView() {
        return (ObjectView) getParent();
    }

    /**
     * refresh this views state from the value objects state so that it accurately reflects the
     * value objects value.
     *
     */
    public abstract void refresh();

    public String toString() {
        String cls = getClass().getName();
        NakedValue value = null;

        try {
            value = getValue();
        } catch (NullPointerException ignore) {
            ;
        }

        return cls.substring(cls.lastIndexOf('.') + 1) + getId() + " [x=" + getLocation().x + ",y=" + getLocation().y +
        ",object=" + value + "]";
    }

    protected void refreshDerivedValue() {
    }

    protected void refreshValue() {
    }

    protected void set(String value) {
        try {
            objectField.set(((ObjectView) getParent()).getObject(), value);
        } catch (InvalidEntryException e) {
            throw new NotImplementedException();
        }
    }
}

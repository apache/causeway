package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.BackgroundTask;
import org.nakedobjects.viewer.skylark.util.ViewFactory;


public abstract class AbstractField extends AbstractView {
    private boolean identified;
//    private boolean isSaving;

	protected AbstractField(Content content, ViewSpecification design, ViewAxis axis) {
		super(content, design, axis);
	}

    /**
     * Determines if the user is able to change the held value.
     */
    public boolean canChangeValue() {
    	Value objectField = getObjectField();
        boolean persistent = !objectField.isDerived();
        boolean fieldReadable = objectField.getAbout(Session.getSession().getSecurityContext(), getContainedBy()).canUse().isAllowed();
        boolean parentReadable = getContainedBy().about().canUse().isAllowed();
        boolean objectEditable = getValue().about().canUse().isAllowed();

        return persistent && fieldReadable && parentReadable && objectEditable;
    }

	public boolean canFocus() {
        return false;
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
    public View dragFrom(InternalDrag drag) {
    	return null;
    }

    /**
     * Indicates the drag started within this view's bounds has been finished (although the location may now be
     * outside of its bounds).  By default does nothing.
     */
    public void dragTo(InternalDrag drag) {
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
 
        if(getState().isActive()) {
            canvas.drawSolidRectangle(new Location(), getBounds().getSize(), Color.DEBUG2);
        }

        if(getState().isOutOfSynch()) {
            canvas.drawSolidRectangle(new Location(), getBounds().getSize(), Color.DEBUG1);
        }
 
        if(getState().isInvalid()) {
            canvas.drawSolidRectangle(new Location(), getBounds().getSize(), Color.DEBUG3);
        }
        
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


    public NakedObject getContainedBy() {
    	Content content = getParent().getContent();
    	if(content instanceof ObjectContent) {
    		return ((ObjectContent)content).getObject();
    	} else {
    		throw new NakedObjectRuntimeException();
    	}
    }

    
    public Field getFieldOf() {
        return null;
    }

    public boolean getIdentified() {
        return identified;
    }

    public Value getObjectField() {
		return ((ValueContent) getContent()).getValueField();
	}

    public Padding getPadding() {
        return new Padding(0, 0, 0, 0);
    }

    public View getRoot() {
        throw new NotImplementedException();
    }

    public final NakedValue getValue() {
    	return ((ValueContent) getContent()).getValue(); 
    }

    public boolean hasFocus() {
        return getViewManager().hasFocus(getView());
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

    public void menuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.OBJECT, new ClearValueOption());
        options.add(MenuOptionSet.OBJECT, new DefaultValueOption());
        options.add(MenuOptionSet.OBJECT, new CopyValueOption());
        options.add(MenuOptionSet.OBJECT, new PasteValueOption());
        options.setColor(Style.VALUE_MENU);
        if (getView().getSpecification().isReplaceable()) {
            replaceOptions(ViewFactory.getViewFactory().valueViews(getContent(), this), options);
     }

    }

    protected void refreshDerivedValue() {
    }

    protected void refreshValue() {
    }

    protected final void initiateSave() {
         run(new BackgroundTask() {
            protected void execute() {
                save();	
                invalidateLayout();
            }
        });
     }

    protected abstract void save();

    protected void saveValue(NakedValue value) throws InvalidEntryException {
        parseEntry(value.title().toString());
    }
    
    protected void parseEntry(final String entryText) throws InvalidEntryException {
         try {
	        NakedObject parent = ((ObjectContent) getParent().getContent()).getObject();
	        getObjectField().parseAndSave(parent, entryText);
	        getState().setValid();
        } catch(InvalidEntryException e) {
            getState().setInvalid();
            throw e;
        }
    }

    public String toString() {
        String cls = getClass().getName();
        NakedValue value = null;

        try {
            value = getValue();
        } catch (NullPointerException ignore) {
            ;
        }

        return cls.substring(cls.lastIndexOf('.') + 1) + getId() + " [location=" + getLocation() +
        ",object=" + value + "]";
    }
    
    public ViewAreaType viewAreaType(Location mouseLocation) {
        return ViewAreaType.INTERNAL;
    }
	
    public int getBaseline() {
        return Style.defaultBaseline();
    }

    public Size getRequiredSize() {
        return new Size(0, Style.defaultFieldHeight());
    }
}

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
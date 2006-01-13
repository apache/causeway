package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Skylark;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.BackgroundTask;
import org.nakedobjects.viewer.skylark.core.BackgroundThread;


public abstract class AbstractField extends AbstractView {
    private boolean identified;

    protected AbstractField(Content content, ViewSpecification design, ViewAxis axis) {
        super(content, design, axis);
    }

    public boolean canFocus() {
        return false;
    }

    public boolean canChangeValue() {
        ValueContent cont = (ValueContent) getContent();
        return ! cont.isDerived() && cont.isEditable().isAllowed();
    }
    
    /**
     * Indicates the drag started within this view's bounds is continuing. By
     * default does nothing.
     */
    public void drag(InternalDrag drag) {}

    /**
     * Default implementation - does nothing
     */
    public void dragCancel(InternalDrag drag) {}

    /**
     * Indicates the start of a drag within this view's bounds. By default does
     * nothing.
     */
    public View dragFrom(Location location) {
        return null;
    }

    /**
     * Indicates the drag started within this view's bounds has been finished
     * (although the location may now be outside of its bounds). By default does
     * nothing.
     */
    public void dragTo(InternalDrag drag) {}

    public void draw(Canvas canvas) {
        if (getState().isActive()) {
            canvas.clearBackground(this, Style.IDENTIFIED);
        }

        if (getState().isOutOfSynch()) {
            canvas.clearBackground(this, Style.OUT_OF_SYNCH);
        }

        if (getState().isInvalid()) {
            canvas.clearBackground(this, Style.INVALID);
        }

        super.draw(canvas);

        // outline bounds
        if (debug) {
            canvas.drawRectangleAround(this, Color.DEBUG_DRAW_BOUNDS);
            canvas.drawLine(0, getSize().getHeight() / 2, getSize().getWidth(), getSize().getHeight() / 2, Color.DEBUG_DRAW_BOUNDS);
            canvas.drawLine(0, getBaseline(), getSize().getWidth(), getBaseline(), Color.DEBUG_BASELINE);
            
        }
    }

    /**
     * Indicates that editing has been completed and the entry should be saved.
     * Will be called by the view manager when other action place within the
     * parent.
     *  
     */
    public void editComplete() {}

    public void entered() {
        identified = true;
        markDamaged();
    }

    public void exited() {
        identified = false;
        markDamaged();
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

    public boolean hasFocus() {
        return getViewManager().hasFocus(getView());
    }

    public boolean indicatesForView(Location mouseLocation) {
        return false;
    }

    /**
     * Called when the user presses any key on the keyboard while this view has
     * the focus.
     * 
     * @param keyCode
     * @param modifiers
     */
    public void keyPressed(final int keyCode, final int modifiers) {}

    /**
     * Called when the user releases any key on the keyboard while this view has
     * the focus.
     * 
     * @param keyCode
     * @param modifiers
     */
    public void keyReleased(int keyCode, int modifiers) {}

    /**
     * Called when the user presses a non-control key (i.e. data entry keys and
     * not shift, up-arrow etc). Such a key press will result in a prior call to
     * <code>keyPressed</code> and a subsequent call to
     * <code>keyReleased</code>.
     */
    public void keyTyped(char keyCode) {}

    public void contentMenuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.USER, new ClearValueOption());
        options.add(MenuOptionSet.USER, new CopyValueOption());
        options.add(MenuOptionSet.USER, new PasteValueOption());
        if (getView().getSpecification().isReplaceable()) {
            replaceOptions(Skylark.getViewFactory().valueViews(getContent(), this), options);
        }

        super.contentMenuOptions((options));
        options.setColor(Style.VALUE_MENU);
    }

    protected final void initiateSave() {
       BackgroundThread.run(this, new BackgroundTask() {
            public void execute() {
                save();
                getParent().updateView();
                invalidateLayout();
            }

            public String getName() {
                return "Save field";
            }

            public String getDescription() {
                return "Saving " + getContent().windowTitle();
            }
            
            
        });
    }

    protected abstract void save();

    protected void saveValue(NakedObject value) throws InvalidEntryException {
        parseEntry(value.titleString());
    }

    protected void parseEntry(String entryText) throws InvalidEntryException {
        ValueContent content = (ValueContent) getContent();
        content.parseTextEntry(entryText);
        content.entryComplete();
    }

    public String toString() {
        String cls = getClass().getName();
        Naked naked = getContent().getNaked();
        return cls.substring(cls.lastIndexOf('.') + 1) + getId() + " [location=" + getLocation() + ",object=" + (naked == null ? "" : naked.getObject()) + "]";
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

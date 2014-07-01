/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.dnd.field;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.view.BackgroundTask;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.InternalDrag;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.action.BackgroundWork;
import org.apache.isis.viewer.dnd.view.base.AbstractView;

public abstract class AbstractField extends AbstractView {
    protected static final int TEXT_WIDTH = 20;
    private boolean identified;

    protected AbstractField(final Content content, final ViewSpecification design) {
        super(content, design);
    }

    @Override
    public boolean canFocus() {
        return canChangeValue().isAllowed();
    }

    protected boolean provideClearCopyPaste() {
        return false;
    }

    protected Consent canClear() {
        return Allow.DEFAULT;
    }

    protected void clear() {
    }

    protected void copyToClipboard() {
    }

    protected void pasteFromClipboard() {
    }

    /**
     * Indicates the drag started within this view's bounds is continuing. By
     * default does nothing.
     */
    @Override
    public void drag(final InternalDrag drag) {
    }

    /**
     * Default implementation - does nothing
     */
    @Override
    public void dragCancel(final InternalDrag drag) {
    }

    /**
     * Indicates the start of a drag within this view's bounds. By default does
     * nothing.
     */
    @Override
    public View dragFrom(final Location location) {
        return null;
    }

    /**
     * Indicates the drag started within this view's bounds has been finished
     * (although the location may now be outside of its bounds). By default does
     * nothing.
     */
    @Override
    public void dragTo(final InternalDrag drag) {
    }

    @Override
    public void draw(final Canvas canvas) {
        if (getState().isActive()) {
            clearBackground(canvas, Toolkit.getColor(ColorsAndFonts.COLOR_IDENTIFIED));
        }

        if (getState().isOutOfSynch()) {
            clearBackground(canvas, Toolkit.getColor(ColorsAndFonts.COLOR_OUT_OF_SYNC));
        }

        if (getState().isInvalid()) {
            final Image image = ImageFactory.getInstance().loadIcon("invalid-entry", 12, null);
            if (image != null) {
                canvas.drawImage(image, getSize().getWidth() - 16, 2);
            }

            // canvas.clearBackground(this,
            // Toolkit.getColor(ColorsAndFonts.COLOR_INVALID));
        }

        super.draw(canvas);
    }

    @Override
    public void entered() {
        super.entered();
        identified = true;
        final Consent changable = canChangeValue();
        if (changable.isVetoed()) {
            getFeedbackManager().setViewDetail(changable.getReason());
        }
        markDamaged();
    }

    @Override
    public void exited() {
        super.exited();
        identified = false;
        markDamaged();
    }

    public boolean getIdentified() {
        return identified;
    }

    @Override
    public Padding getPadding() {
        return new Padding(0, 0, 0, 0);
    }

    public View getRoot() {
        throw new NotYetImplementedException();
    }

    String getSelectedText() {
        return "";
    }

    @Override
    public boolean hasFocus() {
        return getViewManager().hasFocus(getView());
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean indicatesForView(final Location mouseLocation) {
        return false;
    }

    /**
     * Called when the user presses any key on the keyboard while this view has
     * the focus.
     */
    @Override
    public void keyPressed(final KeyboardAction key) {
    }

    /**
     * Called when the user releases any key on the keyboard while this view has
     * the focus.
     */
    @Override
    public void keyReleased(final KeyboardAction action) {
    }

    /**
     * Called when the user presses a non-control key (i.e. data entry keys and
     * not shift, up-arrow etc). Such a key press will result in a prior call to
     * <code>keyPressed</code> and a subsequent call to <code>keyReleased</code>
     * .
     */
    @Override
    public void keyTyped(final KeyboardAction action) {
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        if (provideClearCopyPaste()) {
            options.add(new CopyValueOption(this));
            options.add(new PasteValueOption(this));
            options.add(new ClearValueOption(this));
        }

        super.contentMenuOptions((options));
        options.setColor(Toolkit.getColor(ColorsAndFonts.COLOR_MENU_VALUE));
    }

    protected final void initiateSave(final boolean moveToNextField) {
        BackgroundWork.runTaskInBackground(this, new BackgroundTask() {
            @Override
            public void execute() {
                save();
                getParent().updateView();
                invalidateLayout();
                if (moveToNextField) {
                    getFocusManager().focusNextView();
                }
            }

            @Override
            public String getName() {
                return "Save field";
            }

            @Override
            public String getDescription() {
                return "Saving " + getContent().windowTitle();
            }

        });
    }

    protected abstract void save();

    @Override
    public String toString() {
        final ToString str = new ToString(this, getId());
        str.append("location", getLocation());
        final ObjectAdapter adapter = getContent().getAdapter();
        str.append("object", adapter == null ? "" : adapter.getObject());
        return str.toString();
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return ViewAreaType.INTERNAL;
    }

    @Override
    public int getBaseline() {
        return Toolkit.defaultBaseline();
    }
}

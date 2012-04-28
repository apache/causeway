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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.InternalDrag;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.action.ParameterContent;
import org.apache.isis.viewer.dnd.view.axis.LabelAxis;
import org.apache.isis.viewer.dnd.view.content.FieldContent;

public class DroppableLabelBorder extends LabelBorder {

    public static View createObjectFieldLabelBorder(final LabelAxis axis, final View wrappedView) {
        final FieldContent fieldContent = (FieldContent) wrappedView.getContent();
        return new DroppableLabelBorder(fieldContent, axis, wrappedView);
    }

    public static View createObjectParameterLabelBorder(final LabelAxis axis, final View wrappedView) {
        final ParameterContent parameterContent = (ParameterContent) wrappedView.getContent();
        return new DroppableLabelBorder(parameterContent, axis, wrappedView);
    }

    private final ViewState labelState = new ViewState();
    private boolean overContent;

    public DroppableLabelBorder(final FieldContent fieldContent, final LabelAxis axis, final View wrappedView) {
        super(fieldContent, axis, wrappedView);
    }

    public DroppableLabelBorder(final ParameterContent fieldContent, final LabelAxis axis, final View wrappedView) {
        super(fieldContent, axis, wrappedView);
    }

    @Override
    public ViewAreaType viewAreaType(final Location location) {
        if (overBorder(location)) {
            return ViewAreaType.CONTENT; // used to ensure menu options for
                                         // contained object are shown
        } else {
            return super.viewAreaType(location);
        }
    }

    @Override
    public void dragCancel(final InternalDrag drag) {
        super.dragCancel(drag);
        labelState.clearViewIdentified();
    }

    @Override
    public void drag(final ContentDrag drag) {
        final Location targetLocation = drag.getTargetLocation();
        if (overContent(targetLocation) && overContent == false) {
            overContent = true;
            super.dragIn(drag);
            dragOutOfLabel();
        } else if (overBorder(targetLocation) && overContent == true) {
            overContent = false;
            super.dragOut(drag);
            dragInToLabel(drag.getSourceContent());
        }

        super.drag(drag);
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        if (overContent(drag.getTargetLocation())) {
            super.dragIn(drag);
        } else {
            final Content sourceContent = drag.getSourceContent();
            dragInToLabel(sourceContent);
            markDamaged();
        }
    }

    private void dragInToLabel(final Content sourceContent) {
        overContent = false;
        final Consent canDrop = canDrop(sourceContent);
        if (canDrop.isAllowed()) {
            labelState.setCanDrop();
        } else {
            labelState.setCantDrop();
        }
        final String actionText = canDrop.isVetoed() ? canDrop.getReason() : "Set to " + sourceContent.title();
        getFeedbackManager().setAction(actionText);
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        super.dragOut(drag);
        dragOutOfLabel();
    }

    private void dragOutOfLabel() {
        labelState.clearObjectIdentified();
        markDamaged();
    }

    @Override
    public void drop(final ContentDrag drag) {
        if (overContent(drag.getTargetLocation())) {
            super.drop(drag);
        } else {
            dragOutOfLabel();
            final Content sourceContent = drag.getSourceContent();
            if (canDrop(sourceContent).isAllowed()) {
                drop(sourceContent);
            }
        }
    }

    protected Consent canDrop(final Content dropContent) {
        if (dropContent instanceof ObjectContent) {
            final ObjectAdapter source = ((ObjectContent) dropContent).getObject();
            final ObjectContent content = (ObjectContent) getContent();
            return content.canSet(source);
        } else {
            return Veto.DEFAULT;
        }
    }

    protected void drop(final Content dropContent) {
        if (dropContent instanceof ObjectContent) {
            final ObjectAdapter object = ((ObjectContent) dropContent).getObject();
            ((ObjectContent) getContent()).setObject(object);
            getParent().invalidateContent();
        }
    }

    @Override
    protected Color textColor() {
        Color color;
        if (labelState.canDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_VALID);
        } else if (labelState.cantDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_INVALID);
        } else {
            color = super.textColor();
        }
        return color;
    }
}

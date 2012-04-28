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

package org.apache.isis.viewer.dnd.icon;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.base.ObjectView;
import org.apache.isis.viewer.dnd.view.text.ObjectTitleText;
import org.apache.isis.viewer.dnd.view.text.TitleText;

public class Icon extends ObjectView {
    private IconGraphic icon;
    private boolean isVertical;
    private IconGraphic selectedGraphic;
    private TitleText title;
    private IconGraphic unselectedGraphic;

    public Icon(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        ensureHasIcon();

        int x = 0;
        int y = 0;
        icon.draw(canvas, x, getBaseline());
        if (isVertical) {
            final int w = title.getSize().getWidth();
            x = (w > icon.getSize().getWidth()) ? x : getSize().getWidth() / 2 - w / 2;
            y = icon.getSize().getHeight() + Toolkit.getText(ColorsAndFonts.TEXT_ICON).getAscent() + ViewConstants.VPADDING;
        } else {
            x += icon.getSize().getWidth();
            x += ViewConstants.HPADDING;
            y = icon.getBaseline();
        }
        final int maxWidth = getSize().getWidth() - x;
        title.draw(canvas, x, y, maxWidth);

        if (getState().isActive()) {
            final Image busyImage = ImageFactory.getInstance().loadIcon("busy", 16, null);
            canvas.drawImage(busyImage, icon.getSize().getWidth() - 16 - 4, 4);
        }

    }

    private void ensureHasIcon() {
        if (icon == null) {
            // icon = selectedGraphic;
        }
    }

    @Override
    public void entered() {
        icon = selectedGraphic;
        markDamaged();
        super.entered();
    }

    @Override
    public void exited() {
        icon = unselectedGraphic;
        markDamaged();
        super.exited();
    }

    @Override
    public int getBaseline() {
        ensureHasIcon();
        return icon.getBaseline();
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        if (icon == null) {
            // icon = unselectedGraphic;
        }

        final Size size = icon.getSize();
        final Size textSize = title.getSize();
        if (isVertical) {
            size.extendHeight(ViewConstants.VPADDING + textSize.getHeight() + ViewConstants.VPADDING);
            size.ensureWidth(textSize.getWidth());
        } else {
            size.extendWidth(ViewConstants.HPADDING);
            size.extendWidth(textSize.getWidth());
        }
        return size;
    }

    /**
     * Set up the graphic to be used when displaying the icon and the icon is
     * selected.
     */
    public void setSelectedGraphic(final IconGraphic selectedGraphic) {
        this.selectedGraphic = selectedGraphic;
        if (unselectedGraphic == null) {
            unselectedGraphic = selectedGraphic;
            icon = selectedGraphic;
        }
    }

    /**
     * Set up the title to be used when displaying the icon.
     */
    public void setTitle(final ObjectTitleText text) {
        title = text;
    }

    /**
     * Set up the graphic to be used when displaying the icon and the icon is
     * unselected. If this returns null the graphic will not be changed when the
     * icon becomes unselected.
     */
    public void setUnselectedGraphic(final IconGraphic unselectedGraphic) {
        this.unselectedGraphic = unselectedGraphic;
        icon = unselectedGraphic;
    }

    /**
     * Specifies if the graphic should be aligned vertical above the label;
     * otherwise aligned horizontally.
     */
    public void setVertical(final boolean isVertical) {
        this.isVertical = isVertical;
    }

    @Override
    public void update(final ObjectAdapter object) {
        final View p = getParent();
        if (p != null) {
            p.invalidateLayout();
        }
    }
}

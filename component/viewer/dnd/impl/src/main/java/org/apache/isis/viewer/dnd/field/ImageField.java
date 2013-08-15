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

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.progmodel.facets.value.image.ImageValueFacet;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractFieldSpecification;
import org.apache.isis.viewer.dnd.view.base.AwtImage;
import org.apache.isis.viewer.dnd.view.content.FieldContent;
import org.apache.isis.viewer.dnd.view.field.OneToOneField;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class ImageField extends AbstractField {
    public static class Specification extends AbstractFieldSpecification {
        @Override
        public boolean canDisplay(final ViewRequirement requirement) {
            return requirement.isForValueType(ImageValueFacet.class);
        }

        @Override
        public View createView(final Content content, final Axes axes, final int sequence) {
            return new ImageField(content, this);
        }

        @Override
        public String getName() {
            return "Image";
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ImageField.class);
    private static final MediaTracker mt = new MediaTracker(new java.awt.Canvas());

    public ImageField(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public boolean canFocus() {
        return true;
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        super.contentMenuOptions(options);

        options.add(new UserActionAbstract("Load image from file...") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final String file = getViewManager().selectFilePath("Load image", ".");
                if (new File(file).exists()) {
                    loadImageFromFile(file);
                }
            }
        });
    }

    private void copy() {
    }

    @Override
    public void draw(final Canvas canvas) {
        Color color;

        if (hasFocus()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1);
        } else if (getParent().getState().isObjectIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_IDENTIFIED);
        } else if (getParent().getState().isRootViewIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2);
        } else {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        }

        int top = 0;
        int left = 0;

        final Size size = getSize();
        int w = size.getWidth() - 1;
        int h = size.getHeight() - 1;
        canvas.drawRectangle(left, top, w, h, color);
        left++;
        top++;
        w -= 1;
        h -= 1;

        final ObjectAdapter value = getContent().getAdapter();
        if (value != null) {
            final ImageValueFacet facet = value.getSpecification().getFacet(ImageValueFacet.class);
            final java.awt.Image image = facet.getImage(value);
            if (image != null) {
                final Size imageSize = new Size(facet.getWidth(value), facet.getHeight(value));
                if (imageSize.getWidth() <= w && imageSize.getHeight() <= h) {
                    canvas.drawImage(new AwtImage(image), left, top);
                } else {
                    canvas.drawImage(new AwtImage(image), left, top, w, h);
                }
            }
        }
    }

    @Override
    public int getBaseline() {
        return ViewConstants.VPADDING + Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getAscent();
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final ObjectAdapter value = getContent().getAdapter();
        if (value == null) {
            return super.getRequiredSize(availableSpace);
        } else {
            final ImageValueFacet facet = value.getSpecification().getFacet(ImageValueFacet.class);
            final int width = Math.min(120, Math.max(32, facet.getWidth(value)));
            final int height = Math.min(120, Math.max(32, facet.getHeight(value)));
            return new Size(width, height);
        }
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (canChangeValue().isVetoed()) {
            return;
        }

        final int keyCode = key.getKeyCode();
        if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_ALT) {
            return;
        }

        final int modifiers = key.getModifiers();
        final boolean ctrl = (modifiers & InputEvent.CTRL_MASK) > 0;

        switch (keyCode) {
        case KeyEvent.VK_V:
            if (ctrl) {
                key.consume();
                pasteFromClipboard();
            }
            break;
        case KeyEvent.VK_C:
            if (ctrl) {
                key.consume();
                copy();
            }
            break;
        }
    }

    private void loadImage(final Image image) {
        mt.addImage(image, 1);
        try {
            mt.waitForAll();
        } catch (final InterruptedException e) {
            throw new IsisException(e);
        }

        // final ObjectAdapter value = getContent().getAdapter();
        final ImageValueFacet facet = ((FieldContent) getContent()).getSpecification().getFacet(ImageValueFacet.class);
        final ObjectAdapter object = facet.createValue(image);
        ((OneToOneField) getContent()).setObject(object);
        // ((TextParseableField) getContent()).entryComplete();
        invalidateLayout();
    }

    /*
     * private void loadImageFromURL(final String filename) { try { final URL
     * url = new URL("file://" + filename); final Image image =
     * java.awt.Toolkit.getDefaultToolkit().getImage(url); loadImage(image); }
     * catch (final MalformedURLException e) { throw new
     * IsisException("Failed to load image from " + filename); } }
     */
    private void loadImageFromFile(final String filename) {
        final Image image = java.awt.Toolkit.getDefaultToolkit().getImage(filename);
        loadImage(image);
    }

    @Override
    protected void pasteFromClipboard() {
        final Clipboard cb = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable content = cb.getContents(this);

        try {
            if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                // treat a string as a file
                final String filename = (String) content.getTransferData(DataFlavor.stringFlavor);
                LOG.debug("pasted image from " + filename);
                loadImageFromFile("file://" + filename);

            } else {
                LOG.info("unsupported paste operation " + content);

                // note java does not support transferring images from the
                // clipboard
                // although it has an image flavor for it !!?
                /*
                 * DataFlavor[] transferDataFlavors =
                 * content.getTransferDataFlavors(); for (int i = 0; i <
                 * transferDataFlavors.length; i++) {
                 * LOG.debug("data transfer as " +
                 * transferDataFlavors[i].getMimeType()); }
                 * 
                 * Image image = (Image)
                 * content.getTransferData(DataFlavor.imageFlavor);
                 * LOG.debug("pasted " + image);
                 */

            }

        } catch (final Throwable e) {
            LOG.error("invalid paste operation " + e);
        }

    }

    @Override
    protected void save() {
    }
}

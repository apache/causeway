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


package org.apache.isis.viewer.dnd.configurable;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;


public class Panel {
    
    private static enum Orientation {Undefined, Horizontal, Vertical}; 

    private static interface Content {
        public Size getRequiredSize(Size availableSpace);

        public void setLocation(Location location);

        public void setSize(Size size);

        public void layout(Size maximumSize);

        public void debug(DebugBuilder debug);
    }

    private static class ViewContent implements Content {
        private final View view;

        public ViewContent(View view) {
            this.view = view;
        }

        public Size getRequiredSize(Size availableSpace) {
            return view.getRequiredSize(availableSpace);
        }

        public void layout(Size maximumSize) {
            view.layout();
        }

        public void setLocation(Location location) {
            view.setLocation(location);
        }

        public void setSize(Size size) {
            view.setSize(size);
        }

        public void debug(DebugBuilder debug) {
            debug.appendln(view.toString());
        }
    }

    private static class PanelContent implements Content {
        private final Panel panel;
        private Location location;

        public PanelContent(Panel panel) {
            this.panel = panel;
        }

        public Size getRequiredSize(Size availableSpace) {
            return panel.getRequiredSize(availableSpace);
        }

        public void layout(Size maximumSize) {
            panel.layout(location, maximumSize);
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public void setSize(Size size) {
        }
        
        public void debug(DebugBuilder debug) {
            panel.debug(debug);
        }
    }

    private List<Content> contents = new ArrayList<Content>();
    private Orientation orientation;
    
    public void debug(DebugBuilder debug) {
        debug.appendln("orientation", orientation);
        debug.appendln("size", getRequiredSize(Size.createMax()));
        debug.indent();
        for (Content content : contents) {
            content.debug(debug);
        }
        debug.unindent();
    }

    public void addView(View view, PanelView.Position position) {
        if (contents.isEmpty() || position == null) {
            addToContents(view, false);
        } else if (position == PanelView.Position.East || position == PanelView.Position.West) {
            if (orientation == Orientation.Undefined) {
                orientation = Orientation.Horizontal;
            }
            if (orientation == Orientation.Horizontal) {
                addToContents(view, position == PanelView.Position.West);
            } else {
                replaceViewsWithPanel(view, position == PanelView.Position.West);
            }
         } else if (position == PanelView.Position.South || position == PanelView.Position.North) {
             if (orientation == Orientation.Undefined) {
                 orientation = Orientation.Vertical;
             }
            if (orientation == Orientation.Horizontal) {
                replaceViewsWithPanel(view, position == PanelView.Position.North);
            } else {
                addToContents(view, position == PanelView.Position.North);
            }
        }
    }

    private void addToContents(View view, boolean atBeginning) {
        if (atBeginning) {
            contents.add(0, new ViewContent(view));
        } else {
            contents.add(new ViewContent(view));
        }
    }

    private void replaceViewsWithPanel(View view, boolean atBeginning) {
        Panel panel = new Panel();
        panel.contents = contents;
        contents = new ArrayList<Content>();
        contents.add(new PanelContent(panel));
        addToContents(view, atBeginning);
        panel.orientation = orientation;
        orientation = orientation == Orientation.Horizontal ? Orientation.Vertical : Orientation.Horizontal;
    }

    public void layout(Size maximumSize) {
        Location location = new Location();
        layout(location, maximumSize);
    }

    private void layout(Location location, Size maximumSize) {
        for (Content content : contents) {
            content.setLocation(new Location(location));
            Size requiredSize = content.getRequiredSize(maximumSize);
            content.setSize(requiredSize);
            content.layout(maximumSize);
            if (orientation == Orientation.Horizontal) {
                location.add(requiredSize.getWidth(), 0);
            } else {
                location.add(0, requiredSize.getHeight());                
            }
        }
    }

    public Size getRequiredSize(Size availableSpace) {
        Size size = new Size();
        for (Content content : contents) {
            Size requiredSize = content.getRequiredSize(availableSpace);
            if (orientation == Orientation.Horizontal) {
                size.extendWidth(requiredSize.getWidth());
                size.ensureHeight(requiredSize.getHeight());
            } else {
                size.extendHeight(requiredSize.getHeight());
                size.ensureWidth(requiredSize.getWidth());
            }
        }
        return size;
    }

}


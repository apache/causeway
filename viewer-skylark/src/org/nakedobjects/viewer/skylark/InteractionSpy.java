package org.nakedobjects.viewer.skylark;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class InteractionSpy {
    private class SpyFrame extends Frame {
        public SpyFrame() {
            super("View/Interaction Spy");
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    close();
                }
            });
        }

        public void paint(Graphics g) {
            int baseline = getInsets().top + 15;

            g.drawString("Event " + event, 10, baseline);
            baseline += 18;

            for (int i = 0; i < label[0].length; i++) {
                if (label[0][i] != null) {
                    g.drawString(label[0][i], 10, baseline);
                    g.drawString(label[1][i], 150, baseline);
                }
                baseline += 12;
            }

            baseline += 6;
            for (int i = 0; i < traceIndex; i++) {
                if (trace[i] != null) {
                    g.drawString(trace[i], 10, baseline);
                }
                baseline += 12;
            }
        }
    }

    private int actionCount;
    private String damagedArea;
    private int event;
    private String label[][] = new String[2][20];
    private SpyFrame spy;
    private String[] trace = new String[60];
    private int traceIndex;
    private boolean isVisible;

    public void addAction(String action) {
        if (isVisible) {
            set(actionCount++, "Action", action);
        }
    }

    public void addDamagedArea(Bounds bounds) {
        if (isVisible) {
            damagedArea += bounds + "; ";
            set(7, "Damaged areas", damagedArea);
        }
    }

    public void addTrace(String message) {
        if (isVisible && traceIndex < trace.length) {
            trace[traceIndex] = message;
            traceIndex++;
        }
    }

    public void addTrace(View view, String message, Object object) {
        if (isVisible && traceIndex < trace.length) {
            trace[traceIndex] = view.getClass().getName() + " " + message + ": " + object;
            traceIndex++;
        }
    }

    public void close() {
        if (isVisible) {
            spy.hide();
            spy.dispose();
            isVisible = false;
        }
    }

    public void reset() {
        if (isVisible) {
            event++;
            traceIndex = 0;
            actionCount = 8;
            damagedArea = "";
            setDownAt(null);
            for (int i = actionCount; i < label[0].length; i++) {
                label[0][i] = null;
                label[1][i] = null;
            }
        }
    }

    private void set(int index, String label, Object debug) {
        this.label[0][index] = debug == null ? null : label + ":";
        this.label[1][index] = debug == null ? null : debug.toString();

        spy.repaint();
    }

    public void setAbsoluteLocation(Location absoluteLocation) {
        if (isVisible) {
            set(6, "Absolute view location", absoluteLocation);
        }
    }

    public void setDownAt(Location downAt) {
        if (isVisible) {
            set(0, "Down at", downAt);
        }
    }

    public void setLocationInView(Location internalLocation) {
        if (isVisible) {
            set(3, "Relative mouse location", internalLocation);
        }
    }

    public void setLocationInViewer(Location mouseLocation) {
        if (isVisible) {
            set(1, "Mouse location", mouseLocation);
        }
    }

    public void setOver(Object data) {
        if (isVisible) {
            set(2, "Mouse over", data);
        }
    }

    public void setType(ViewAreaType type) {
        if (isVisible) {
            set(4, "Area type", type);
        }
    }

    public void setViewLocation(Location locationWithinViewer) {
        if (isVisible) {
            set(5, "View location", locationWithinViewer);
        }
    }

    public void open() {
        if (!isVisible) {
            spy = new SpyFrame();
            spy.setBounds(10, 10, 800, 500);
            spy.show();
            isVisible = true;
        }
    }

    public boolean isVisible() {
        return isVisible;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
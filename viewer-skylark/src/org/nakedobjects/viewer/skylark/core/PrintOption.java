package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;


public class PrintOption extends MenuOption {
    private final int HEIGHT = 60;
    private final int LEFT = 60;

    public PrintOption() {
        super("Print...");
    }

    public Consent disabled(View component) {
        return Allow.DEFAULT;
    }

    public void execute(Workspace workspace, View view, Location at) {
        Frame frame = new Frame();
        PrintJob job = Toolkit.getDefaultToolkit().getPrintJob(frame, "Print object", null);

        if (job != null) {
            Graphics pg = job.getGraphics();
            Dimension pageSize = job.getPageDimension();

            if (pg != null) {
                pg.translate(LEFT, HEIGHT);
                pg.drawRect(0, 0, pageSize.width - LEFT - 1, pageSize.height - HEIGHT - 1);
                view.print(new PrintCanvas(pg, view));
                pg.dispose();
            }

            job.end();
        }
        frame.dispose();
    }

    private class PrintCanvas extends DrawingCanvas {
        PrintCanvas(Graphics g, View view) {
            super(g, 0, 0, view.getSize().getWidth(), view.getSize().getHeight());
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
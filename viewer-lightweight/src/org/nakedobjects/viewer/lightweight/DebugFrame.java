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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import org.nakedobjects.utility.DebugInfo;


/**
 * A specialised frame for displaying the details of an object and its display mechanisms.
 */
public class DebugFrame extends Frame {
    private static Vector frames = new Vector();
    private DebugInfo strategy;
    private TextArea field;

    public DebugFrame() {
        frames.addElement(this);
        setBackground(Style.APPLICATION_BACKGROUND.getAwtColor());
        setLayout(new BorderLayout(7, 7));


        addWindowListener(new WindowAdapter() {
            	DebugFrame frame = DebugFrame.this;
                public void windowClosing(WindowEvent e) {
                    frame.dispose();
                }
            });

        TextArea area = new TextArea("", 40, 80, TextArea.SCROLLBARS_BOTH);

        area.setForeground(Style.ACTIVE.getAwtColor());

        Font font = Style.DEBUG.getAwtFont();
        area.setFont(font);
        add("Center", area);
        field = area;

        // add buttons
        // ...
        Button b = new java.awt.Button("Refresh");
        b.setFont(font);

        add(b, BorderLayout.SOUTH);
        b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    refresh();
                }
            });
    }

    /**
     * Calls dispose on all the open debug frames
     *
     */
    public static void disposeAll() {
        Frame[] f = new Frame[frames.size()];

        for (int i = 0; i < f.length; i++) {
            f[i] = (Frame) frames.elementAt(i);
        }

        for (int i = 0; i < f.length; i++) {
            f[i].dispose();
        }
    }

    public TextArea getField() {
        return field;
    }

    /**
     * set the display strategy to use to display the information.
     * @param view
     */
    public void setInfo(DebugInfo view) {
        strategy = view;
    }

    public void dispose() {
        frames.removeElement(this);
        super.dispose();
    }

    public void refresh() {
        setTitle(strategy.getDebugTitle());
        field.setText(strategy.getDebugData());

        //        info.append("\n");
    }

    /**
     * show the frame at the specified coordinates
     */
    public void show(int x, int y) {
        refresh();
        pack();
        setLocation(x, y);
        show();
    }

    void setField(java.awt.TextArea newField) {
        field = newField;
    }
}

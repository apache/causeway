package org.nakedobjects.utility;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;


public abstract class ErrorDialog extends Frame {

    public ErrorDialog(final String title) {
        super(title);
        setLayout(new BorderLayout());
        setBounds(100, 200, 600, 400);
    }
    
    public void show(final String message, final String text) {
        add(new MessagePanel(message, text), BorderLayout.CENTER);
        Button button = new Button("OK");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(button, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        show();
    }

}

class MessagePanel extends Panel {
    private final String message;
    private final String stackTrace;

    public MessagePanel(final String message, final String stackTtrace) {
        this.message = message;
        this.stackTrace = stackTtrace;
    }

    public void paint(Graphics g) {
        int lineHeight = g.getFontMetrics().getHeight();

        g.setColor(Color.red);

        int x = 10;
        int y = lineHeight;
        g.drawString(message, x, y);

        y += lineHeight + 14;

        StringTokenizer st = new StringTokenizer(stackTrace, "\n\r");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            g.drawString(line, x, y);
            y += lineHeight;
        }
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
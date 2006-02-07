package org.nakedobjects.utility;

import org.nakedobjects.object.NakedObjects;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Vector;


/**
 * A specialised frame for displaying the details of an object and its display mechanisms.
 */
public abstract class DebugFrame extends Frame {
    private static Vector frames = new Vector();
    private int panel = 0;

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

    private TextArea field;
    private TabPane tabPane;

    public DebugFrame() {
        frames.addElement(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });

        URL url = DebugFrame.class.getResource("/" + "images/debug.png");
        if (url != null) {
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            if (image != null) {
                setIconImage(image);
            }
        }

        setLayout(new BorderLayout(7, 7));
        Panel tabPane = createTabPane();
        add(tabPane);
    }

    private Panel createTabPane() {
        tabPane = new TabPane();
        tabPane.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                panel = tabPane.select(point);

                DebugInfo info = tabPane.getInfo();
                setTitle(info.getDebugTitle());
                field.setText(info.getDebugData());
            }
        });

        tabPane.setLayout(new BorderLayout(7, 7));

        TextArea textArea = new TextArea("", 60, 110, TextArea.SCROLLBARS_BOTH);
        textArea.setForeground(Color.black);
        textArea.setEditable(false);
        Font font = NakedObjects.getConfiguration().getFont("nakedobjects.debug.font", new Font("Courier", Font.PLAIN, 12));
        textArea.setFont(font);
        tabPane.add("Center", textArea);
        field = textArea;

        Panel buttons = new Panel();
        buttons.setLayout(new FlowLayout());
        tabPane.add(buttons, BorderLayout.SOUTH);

        // add buttons
        Button b = new java.awt.Button("Refresh");
        b.setFont(font);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        b = new java.awt.Button("Print...");
        b.setFont(font);
        b.setEnabled(false);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            // TODO add print option
            }
        });

        b = new java.awt.Button("Save...");
        b.setFont(font);
        b.setEnabled(false);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            // TODO add save option
            }
        });

        b = new java.awt.Button("Close");
        b.setFont(font);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });

        return tabPane;
    }

    public Insets getInsets() {
        Insets insets = super.getInsets();
        insets.left += 10;
        insets.right += 10;
        insets.top += 10;
        insets.bottom += 10;
        return insets;
    }

    private void closeDialog() {
        dialogClosing();
        hide();
        dispose();
    }

    public void dialogClosing() {}

    public void dispose() {
        frames.removeElement(this);
        super.dispose();
    }

    public void refresh() {
        /*
         * WARNING - When refresh button is pressed it is in the AWT thread; if the naked objects repository
         * is thread based then the wrong set of components will be used giving strange results, particularly
         * in the object persistor.
         */
        // TODO run in correct thread
        DebugInfo[] infos = getInfo();
        tabPane.setInfo(infos);
        DebugInfo info = infos[panel];
        if (info != null) {
            setTitle(info.getDebugTitle());
            field.setText(info.getDebugData());
        }
    }

    protected abstract DebugInfo[] getInfo();

    /**
     * show the frame at the specified coordinates
     */
    public void show(int x, int y) {
        refresh();
        pack();
        limitBounds(x, y);
        show();
    }

    private void limitBounds(int x, int y) {
        Dimension screenSize = getToolkit().getScreenSize();
        int maxWidth = screenSize.width - 50;
        int maxHeight = screenSize.height - 50;

        int width = getSize().width;
        int height = getSize().height;

        if (x + width > maxWidth) {
            x = 0;
            if (x + width > maxWidth) {
                width = maxWidth;
            }
        }

        if (y + height > maxHeight) {
            y = 0;
            if (y + height > maxHeight) {
                height = maxHeight;
            }
        }

        setSize(width, height);
        setLocation(x, y);
    }
}

class TabPane extends Panel {
    private DebugInfo[] info;
    private Rectangle[] tabs;
    private int panel = 0;

    public int select(Point point) {
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] != null && tabs[i].contains(point)) {
                panel = i;
                repaint();
                break;
            }
        }
        return panel;
    }

    public DebugInfo getInfo() {
        return info[panel];
    }

    public void setInfo(DebugInfo[] info) {
        this.info = info;
        tabs = new Rectangle[info.length];
    }

    public Insets getInsets() {
        Insets insets = super.getInsets();
        insets.left += 10;
        insets.right += 10;
        insets.top += 30;
        insets.bottom += 10;
        return insets;
    }

    public void paint(Graphics g) {
        if (info != null) {
            Dimension size = getSize();
            g.setColor(Color.gray);
            g.drawRect(0, 20, size.width - 1, size.height - 21);

            FontMetrics fm;
            fm = g.getFontMetrics();
            int offset = 0;
            int maxWidth = size.width / info.length - 1;
            for (int i = 0; i < info.length; i++) {
                String title = info[i].getDebugTitle();
                title = title == null ? info[i].getClass().getName() : title;
                int width = Math.min(maxWidth, fm.stringWidth(title) + 20);

                tabs[i] = new Rectangle(offset, 0, width, 20);
                g.setColor(Color.gray);
                g.drawRect(offset + 0, 0, width, 20);
                if (i == panel) {
                    g.setColor(Color.white);
                    g.fillRect(offset + 1, 1, width - 1, 20);
                    // g.drawLine(offset + 1, 20, offset + width, 20);
                    g.setColor(Color.black);
                } else {
                    g.setColor(Color.lightGray);
                    g.fillRect(offset + 1, 1, width - 1, 20 - 1);
                    g.setColor(Color.gray);
                }

                g.drawString(title, offset + 9, 20 - 5);

                offset += width;
            }
            g.setColor(Color.white);
            g.fillRect(offset + 1, 1, size.width - offset, 20 - 1);
        }
    }

}
/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */

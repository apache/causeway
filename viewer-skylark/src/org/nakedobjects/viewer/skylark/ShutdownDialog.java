package org.nakedobjects.viewer.skylark;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.apache.log4j.Logger;


class ShutdownDialog extends Dialog implements ActionListener, KeyListener {
    private static final Logger LOG = Logger.getLogger(ShutdownDialog.class);
    private final static int BORDER = 10;
    private Button cancel;
    private Button quit;
    private static String CANCEL_LABEL = "Cancel";
    private static String QUIT_LABEL = "Ok";

    public ShutdownDialog(ViewerFrame owner) {
        super(owner, "Naked Objects", true);

        setLayout(new GridLayout(2, 3, 10, 10));

        add(new Label("Exit Naked Objects?", Label.LEFT));

        add(new Panel());
        add(new Panel());
        add(new Panel());
        
        
        add(quit = new Button(QUIT_LABEL));
        quit.addActionListener(this);
        quit.addKeyListener(this);
        
        add(cancel = new Button(CANCEL_LABEL));
        cancel.addActionListener(this);
        cancel.addKeyListener(this);

        pack();
        int width = getSize().width; // getWidth();
        int height = getSize().height; // getHeight();
        Dimension screen = owner.getSize();
        Point point = owner.getLocation();

        int x = point.x + (screen.width / 2) - (width / 2);
        int y = point.y + (screen.height / 2) - (height / 2);

        setLocation(x, y);
        show();
        quit.requestFocus();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public Insets getInsets() {
        Insets in = super.getInsets();
        in.top += BORDER;
        in.bottom += BORDER;
        in.left += BORDER;
        in.right += BORDER;
        return in;
    }

    public void actionPerformed(ActionEvent evt) {
        action(evt.getSource());
    }

    public void keyPressed(KeyEvent e) {
    // ignore
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            action(e.getComponent());
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancel(e.getComponent());
        }
    }

    public void keyTyped(KeyEvent e) {
    // ignore
    }

    private synchronized void cancel(Object widget) {
        dispose();
    }

    private synchronized void action(Object widget) {
        if (widget == cancel) {
            cancel(widget);
        } else if (widget == quit) {
            quit();
        }
    }

    private void quit() {
        dispose();
        ((ViewerFrame) getParent()).quit();
    }
    
    public void dispose() {
        LOG.debug("dispose...");
        super.dispose();
        LOG.debug("...disposed");

    }
}

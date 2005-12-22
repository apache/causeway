package org.nakedobjects.system.security;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.apache.log4j.Logger;


public class LoginDialog extends Frame implements ActionListener, KeyListener {
    private static final Logger LOG = Logger.getLogger(LoginDialog.class);
    private final static int BORDER = 10;
    private TextField user;
    private TextField password;
    private Button cancel;
    private Button login;

    private static String CANCEL_LABEL = "Cancel";
    private static String LOGIN_LABEL = "Login";
    private boolean logIn = true;

    public LoginDialog() {
        super("Naked Objects Login");

        setLayout(new GridLayout(3, 2, 10, 10));

        add(new Label("User name:", Label.LEFT));
        add(user = new TextField());
        user.addKeyListener(this);

        add(new Label("Password:", Label.LEFT));
        add(password = new TextField());
        password.addKeyListener(this);
        password.setEchoChar('*');

        add(cancel = new Button(CANCEL_LABEL));
        cancel.addActionListener(this);
        cancel.addKeyListener(this);

        add(login = new Button(LOGIN_LABEL));
        login.addActionListener(this);
        login.addKeyListener(this);

        pack();
        int width = getSize().width; // getWidth();
        int height = getSize().height; // getHeight();
        Dimension screen = getToolkit().getScreenSize();

        int x = (screen.width / 2) - (width / 2);

        if ((screen.width / screen.height) >= 2) {
            x = (screen.width / 4) - (width / 2);
        }

        int y = (screen.height / 2) - (height / 2);
        setLocation(x, y);
        show();
        user.requestFocus();
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
        logIn = false;
        notify();
    }

    private synchronized void action(Object widget) {
        if (widget == cancel) {
            cancel(widget);
        } else if (widget == login || widget == password) {
            logIn = true;
            notify();
        } else if (widget == user) {
            password.requestFocus();
        }
    }

    public void dispose() {
        LOG.debug("dispose...");
        super.dispose();
        LOG.debug("...disposed");

    }

    public String getUser() {
        return user.getText();
    }

    public String getPassword() {
        return password.getText();
    }

    public synchronized boolean login() {
        try {
            wait();
        } catch (InterruptedException e) {}
        return logIn;
    }

}

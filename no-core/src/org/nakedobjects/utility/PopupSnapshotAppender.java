package org.nakedobjects.utility;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PopupSnapshotAppender extends SnapshotAppender {

    protected void writeSnapshot(String message, String details) {
        SubmitDialog dialog = new SubmitDialog("Error logged");
        dialog.show(message, details);
    }
}

class SubmitDialog extends Frame {
    
    public SubmitDialog(final String title) {
        super(title);
        setLayout(new BorderLayout());
        setBounds(0, 200, 800, 400);
    }
    
    public void show(final String message, final String text) {
        TextArea messagePanel = new TextArea();
        messagePanel.setText(message + "\n\n" + text);
        messagePanel.setForeground(Color.black);
        messagePanel.setEditable(false);
        messagePanel.setFont(new Font("Dialog", Font.PLAIN, 9));
        
        add(messagePanel, BorderLayout.CENTER);
        
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        add(buttonPanel, BorderLayout.SOUTH);
        
        Button ok = new Button("Close");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok(true);
            }
        });
        buttonPanel.add(ok);
   
        /*
        Button cancel = new Button("Save and Close");
        cancel .addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok(false);
            }
        });
        buttonPanel.add(cancel);
*/
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ok(false);
            }
        });
        
        show();
    }
    
    protected synchronized void ok(boolean b) {
        dispose();
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
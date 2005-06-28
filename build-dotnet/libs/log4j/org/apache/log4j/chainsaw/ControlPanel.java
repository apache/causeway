/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */
package org.apache.log4j.chainsaw;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * Represents the controls for filtering, pausing, exiting, etc.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
class ControlPanel extends JPanel {
    /** use the log messages **/
    private static final Category LOG = 
                                  Category.getInstance(ControlPanel.class);

    /**
     * Creates a new <code>ControlPanel</code> instance.
     *
     * @param aModel the model to control
     */
    ControlPanel(final MyTableModel aModel) {
        setBorder(BorderFactory.createTitledBorder("Controls: "));
        final GridBagLayout gridbag = new GridBagLayout();
        final GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        // Pad everything
        c.ipadx = 5;
        c.ipady = 5;

        // Add the 1st column of labels
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;

        c.gridy = 0;
        JLabel label = new JLabel("Filter Level:");
        gridbag.setConstraints(label, c);
        add(label);

        c.gridy++;
        label = new JLabel("Filter Thread:");
        gridbag.setConstraints(label, c);
        add(label);

        c.gridy++;
        label = new JLabel("Filter Category:");
        gridbag.setConstraints(label, c);
        add(label);

        c.gridy++;
        label = new JLabel("Filter NDC:");
        gridbag.setConstraints(label, c);
        add(label);

        c.gridy++;
        label = new JLabel("Filter Message:");
        gridbag.setConstraints(label, c);
        add(label);

        // Add the 2nd column of filters
        c.weightx = 1;
        //c.weighty = 1;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        final Priority[] allPriorities = Priority.getAllPossiblePriorities();
        final JComboBox priorities = new JComboBox(allPriorities);
        final Priority lowest = allPriorities[allPriorities.length - 1];
        priorities.setSelectedItem(lowest);
        aModel.setPriorityFilter(lowest);
        gridbag.setConstraints(priorities, c);
        add(priorities);
        priorities.setEditable(false);
        priorities.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aEvent) {
                    aModel.setPriorityFilter(
                        (Priority) priorities.getSelectedItem());
                }
            });


        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy++;
        final JTextField threadField = new JTextField("");
        threadField.getDocument().addDocumentListener(new DocumentListener () {
                public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setThreadFilter(threadField.getText());
                }
                public void removeUpdate(DocumentEvent aEvente) {
                    aModel.setThreadFilter(threadField.getText());
                }
                public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setThreadFilter(threadField.getText());
                }
            });
        gridbag.setConstraints(threadField, c);
        add(threadField);

        c.gridy++;
        final JTextField catField = new JTextField("");
        catField.getDocument().addDocumentListener(new DocumentListener () {
                public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setCategoryFilter(catField.getText());
                }
                public void removeUpdate(DocumentEvent aEvent) {
                    aModel.setCategoryFilter(catField.getText());
                }
                public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setCategoryFilter(catField.getText());
                }
            });
        gridbag.setConstraints(catField, c);
        add(catField);

        c.gridy++;
        final JTextField ndcField = new JTextField("");
        ndcField.getDocument().addDocumentListener(new DocumentListener () {
                public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setNDCFilter(ndcField.getText());
                }
                public void removeUpdate(DocumentEvent aEvent) {
                    aModel.setNDCFilter(ndcField.getText());
                }
                public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setNDCFilter(ndcField.getText());
                }
            });
        gridbag.setConstraints(ndcField, c);
        add(ndcField);

        c.gridy++;
        final JTextField msgField = new JTextField("");
        msgField.getDocument().addDocumentListener(new DocumentListener () {
                public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setMessageFilter(msgField.getText());
                }
                public void removeUpdate(DocumentEvent aEvent) {
                    aModel.setMessageFilter(msgField.getText());
                }
                public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setMessageFilter(msgField.getText());
                }
            });


        gridbag.setConstraints(msgField, c);
        add(msgField);

        // Add the 3rd column of buttons
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 2;

        c.gridy = 0;
        final JButton exitButton = new JButton("Exit");
        exitButton.setMnemonic('x');
        exitButton.addActionListener(ExitAction.INSTANCE);
        gridbag.setConstraints(exitButton, c);
        add(exitButton);

        c.gridy++;
        final JButton clearButton = new JButton("Clear");
        clearButton.setMnemonic('c');
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aEvent) {
                    aModel.clear();
                }
            });
        gridbag.setConstraints(clearButton, c);
        add(clearButton);

        c.gridy++;
        final JButton toggleButton = new JButton("Pause");
        toggleButton.setMnemonic('p');
        toggleButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aEvent) {
                    aModel.toggle();
                    toggleButton.setText(
                        aModel.isPaused() ? "Resume" : "Pause");
                }
            });
        gridbag.setConstraints(toggleButton, c);
        add(toggleButton);
    }
}

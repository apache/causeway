/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */
package org.apache.log4j.chainsaw;

import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Category;

/**
 * A panel for showing a stack trace.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
class DetailPanel
    extends JPanel
    implements ListSelectionListener
{
    /** used to log events **/
    private static final Category LOG =
        Category.getInstance(DetailPanel.class);

    /** used to format the logging event **/
    private static final MessageFormat FORMATTER = new MessageFormat(
        "<b>Time:</b> <code>{0,time,medium}</code>" +
        "&nbsp;&nbsp;<b>Priority:</b> <code>{1}</code>" +
        "&nbsp;&nbsp;<b>Thread:</b> <code>{2}</code>" +
        "&nbsp;&nbsp;<b>NDC:</b> <code>{3}</code>" +
        "<br><b>Category:</b> <code>{4}</code>" +
        "<br><b>Location:</b> <code>{5}</code>" +
        "<br><b>Message:</b>" +
        "<pre>{6}</pre>" +
        "<b>Throwable:</b>" +
        "<pre>{7}</pre>");

    /** the model for the data to render **/
    private final MyTableModel mModel;
    /** pane for rendering detail **/
    private final JEditorPane mDetails;

    /**
     * Creates a new <code>DetailPanel</code> instance.
     *
     * @param aTable the table to listen for selections on
     * @param aModel the model backing the table
     */
    DetailPanel(JTable aTable, final MyTableModel aModel) {
        mModel = aModel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Details: "));

        mDetails = new JEditorPane();
        mDetails.setEditable(false);
        mDetails.setContentType("text/html");
        add(new JScrollPane(mDetails), BorderLayout.CENTER);

        final ListSelectionModel rowSM = aTable.getSelectionModel();
        rowSM.addListSelectionListener(this);
    }

    /** @see ListSelectionListener **/
    public void valueChanged(ListSelectionEvent aEvent) {
        //Ignore extra messages.
        if (aEvent.getValueIsAdjusting()) {
            return;
        }

        final ListSelectionModel lsm = (ListSelectionModel) aEvent.getSource();
        if (lsm.isSelectionEmpty()) {
            mDetails.setText("Nothing selected");
        } else {
            final int selectedRow = lsm.getMinSelectionIndex();
            final EventDetails e = mModel.getEventDetails(selectedRow);
            final Object[] args =
            {
                new Date(e.getTimeStamp()),
                e.getPriority(),
                escape(e.getThreadName()),
                escape(e.getNDC()),
                escape(e.getCategoryName()),
                escape(e.getLocationDetails()),
                escape(e.getMessage()),
                escape(getThrowableStrRep(e))
            };
            mDetails.setText(FORMATTER.format(args));
            mDetails.setCaretPosition(0);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a string representation of a throwable.
     *
     * @param aEvent contains the throwable information
     * @return a <code>String</code> value
     */
    private static String getThrowableStrRep(EventDetails aEvent) {
        final String[] strs = aEvent.getThrowableStrRep();
        if (strs == null) {
            return null;
        }

        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strs.length; i++) {
            sb.append(strs[i]).append("\n");
        }

        return sb.toString();
    }

    /**
     * Escape &lt;, &gt; &amp; and &quot; as their entities. It is very
     * dumb about &amp; handling.
     * @param aStr the String to escape.
     * @return the escaped String
     */
    private String escape(String aStr) {
        if (aStr == null) {
            return null;
        }

        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < aStr.length(); i++) {
            char c = aStr.charAt(i);
            switch (c) {
            case '<':
                buf.append("&lt;");
                break;
            case '>':
                buf.append("&gt;");
                break;
            case '\"':
                buf.append("&quot;");
                break;
            case '&':
                buf.append("&amp;");
                break;
            default:
                buf.append(c);
                break;
            }
        }
        return buf.toString();
    }
}

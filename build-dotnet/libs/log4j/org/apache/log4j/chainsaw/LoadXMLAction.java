/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */
package org.apache.log4j.chainsaw;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Category;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Encapsulates the action to load an XML file.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 * @version 1.0
 */
class LoadXMLAction
    extends AbstractAction
{
    /** use to log messages **/
    private static final Category LOG =
        Category.getInstance(LoadXMLAction.class);

    /** the parent frame **/
    private final JFrame mParent;

    /**
     * the file chooser - configured to allow only the selection of a
     * single file.
     */
    private final JFileChooser mChooser = new JFileChooser();
    {
        mChooser.setMultiSelectionEnabled(false);
        mChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    /** parser to read XML files **/
    private final XMLReader mParser;
    /** the content handler **/
    private final XMLFileHandler mHandler;


    /**
     * Creates a new <code>LoadXMLAction</code> instance.
     *
     * @param aParent the parent frame
     * @param aModel the model to add events to
     * @exception SAXException if an error occurs
     * @throws ParserConfigurationException if an error occurs
     */
    LoadXMLAction(JFrame aParent, MyTableModel aModel)
        throws SAXException, ParserConfigurationException
    {
        mParent = aParent;
        mHandler = new XMLFileHandler(aModel);
        mParser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        mParser.setContentHandler(mHandler);
    }

    /**
     * Prompts the user for a file to load events from.
     * @param aIgnore an <code>ActionEvent</code> value
     */
    public void actionPerformed(ActionEvent aIgnore) {
        LOG.info("load file called");
        if (mChooser.showOpenDialog(mParent) == JFileChooser.APPROVE_OPTION) {
            LOG.info("Need to load a file");
            final File chosen = mChooser.getSelectedFile();
            LOG.info("loading the contents of " + chosen.getAbsolutePath());
            try {
                final int num = loadFile(chosen.getAbsolutePath());
                JOptionPane.showMessageDialog(
                    mParent,
                    "Loaded " + num + " events.",
                    "CHAINSAW",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                LOG.warn("caught an exception loading the file", e);
                JOptionPane.showMessageDialog(
                    mParent,
                    "Error parsing file - " + e.getMessage(),
                    "CHAINSAW",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Loads the contents of file into the model
     *
     * @param aFile the file to extract events from
     * @return the number of events loaded
     * @throws SAXException if an error occurs
     * @throws IOException if an error occurs
     */
    private int loadFile(String aFile)
        throws SAXException, IOException
    {
        synchronized (mParser) {
            // Create a dummy document to parse the file
            final StringBuffer buf = new StringBuffer();
            buf.append("<?xml version=\"1.0\" standalone=\"yes\"?>\n");
            buf.append("<!DOCTYPE log4j:eventSet ");
            buf.append("[<!ENTITY data SYSTEM \"file:///");
            buf.append(aFile);
            buf.append("\">]>\n");
            buf.append("<log4j:eventSet xmlns:log4j=\"Claira\">\n");
            buf.append("&data;\n");
            buf.append("</log4j:eventSet>\n");

            final InputSource is =
                new InputSource(new StringReader(buf.toString()));
            mParser.parse(is);
            return mHandler.getNumEvents();
        }
    }
}

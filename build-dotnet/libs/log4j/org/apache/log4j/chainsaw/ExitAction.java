/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */
package org.apache.log4j.chainsaw;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.apache.log4j.Category;

/**
 * Encapsulates the action to exit.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 * @version 1.0
 */
class ExitAction
    extends AbstractAction
{
    /** use to log messages **/
    private static final Category LOG = Category.getInstance(ExitAction.class);
    /** The instance to share **/
    public static final ExitAction INSTANCE = new ExitAction();

    /** Stop people creating instances **/
    private ExitAction() {}

    /**
     * Will shutdown the application.
     * @param aIgnore ignored
     */
    public void actionPerformed(ActionEvent aIgnore) {
        LOG.info("shutting down");
        System.exit(0);
    }
}

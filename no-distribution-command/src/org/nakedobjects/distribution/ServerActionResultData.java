package org.nakedobjects.distribution;

public interface ServerActionResultData {

    /**
     * Return the Data for the result from executing the action.
     */
    Data getReturn();

    /**
     * Return the ObjectData for the target if it was persisited by the server.
     */
    ObjectData getPersistedTarget();

    /**
     * Return the ObjectDatas for any of the parameters (in the same seqence as passed to the server) if they
     * were was persisited by the server.
     */
    ObjectData[] getPersistedParameters();

    /**
     * Return the ObjectDatas for any objects that where changed by the server while executing the action.
     */
    ObjectData[] getUpdates();

    /**
     * Return all messages created by the action.
     */
    String[] getMessages();

    /**
     * Return all warnings created by the action.
     */
    String[] getWarnings();
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
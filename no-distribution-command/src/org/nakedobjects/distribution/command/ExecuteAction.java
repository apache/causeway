package org.nakedobjects.distribution.command;

import org.nakedobjects.distribution.Data;
import org.nakedobjects.distribution.Distribution;
import org.nakedobjects.distribution.ReferenceData;
import org.nakedobjects.distribution.ServerActionResultData;
import org.nakedobjects.object.Session;
import org.nakedobjects.utility.ToString;


public class ExecuteAction extends AbstractRequest {
    private final String actionIdentifier;
    private final String actionType;
    private final Data[] parameters;
    private final ReferenceData target;

    public ExecuteAction(Session session, String actionType, String actionIdentifier, ReferenceData target, Data[] parameters) {
        super(session);
        this.actionType = actionType;
        this.actionIdentifier = actionIdentifier;
        this.target = target;
        this.parameters = parameters;
    }

    public void execute(Distribution distribution) {
        setResponse(distribution.executeServerAction(session, actionType, actionIdentifier, target, parameters));
    }

    public ServerActionResultData getActionResult() {
        return (ServerActionResultData) getResponse();
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("method", actionIdentifier);
        str.append("target", target);
        return str.toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
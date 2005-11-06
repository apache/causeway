package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.utility.UnexpectedCallException;
import org.nakedobjects.viewer.skylark.basic.AbstractContent;

import java.util.Enumeration;


public abstract class CollectionContent extends AbstractContent implements Content {

    public abstract Enumeration allElements();

    public abstract NakedCollection getCollection();

    public void menuOptions(MenuOptionSet options) {
        Naked object = getNaked();
        
  		// TODO find all collection actions, and make them available
  		// not valid       ObjectOption.menuOptions((NakedObject) object, options);
            
        Action[] actions = object.getSpecification().getObjectActions(Action.USER);

        for (int i = 0; i < actions.length; i++) {
            
            MenuOption option;
            option = new MenuOption(actions[i].getName()) {
                public void execute(Workspace workspace, View view, Location at) {}
            };
            
            if (option != null) {
                options.add(MenuOptionSet.OBJECT, option);
            }
        }
        
    }
    
    public void parseTextEntry(String entryText) throws InvalidEntryException {
        throw new UnexpectedCallException();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
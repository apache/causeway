package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class SaveTransientObjectBorder extends ButtonBorder {

    private static class CloseAction extends ButtonAction {
        public CloseAction() {
            super("Discard");
        }

        public void execute(Workspace workspace, View view, Location at) {
            close(workspace, view);
        }
    }

    private static class SaveAction extends ButtonAction {
        public SaveAction() {
            super("Save");
        }

        public Consent disabled(View view) {
            return canSave(view);
        }
        

        public void execute(Workspace workspace, View view, Location at) {
            NakedObject transientObject = save(view);
            workspace.addOpenViewFor(transientObject, view.getLocation());
            workspace.removeView(view);
        }
    }

        private static Consent canSave(View view) {
           Action action = view.getContent().getSpecification().getObjectAction(Action.USER, "save");
            if(action == null) {
                action = view.getContent().getSpecification().getObjectAction(Action.USER, "persist");
            }
            if(action == null) {
                return Allow.DEFAULT;
            } else {
		        Naked transientObject = (NakedObject) view.getContent().getNaked();
                return transientObject.getHint(ClientSession.getSession(), action,new Naked[0]).canUse();
            }
        }
        
    private static class SaveAndCloseAction extends ButtonAction {
        public SaveAndCloseAction() {
            super("Save & Close");
        }

        public Consent disabled(View view) {
            return canSave(view);
        }
        
        public void execute(Workspace workspace, View view, Location at) {
            save(view);
            close(workspace, view);
        }
    }

    private static void close(Workspace workspace, View view) {
        workspace.removeView(view);
    }

    private static NakedObject save(View view) {
        NakedObject transientObject = (NakedObject) view.getContent().getNaked();
        Action action = view.getContent().getSpecification().getObjectAction(Action.USER, "save");
        if(action == null) {
            action = view.getContent().getSpecification().getObjectAction(Action.USER, "persist");
        }
        if(action == null) {
	        NakedObjectManager objectManager = NakedObjects.getObjectManager();
	        try {
		        objectManager.startTransaction();
	            objectManager.makePersistent(transientObject);
	            objectManager.endTransaction();
	        } catch (RuntimeException e) {
	            objectManager.abortTransaction();
	            throw e;
	        }
        } else {
            transientObject.execute(action, new Naked[0]);
        }
        return transientObject;
    }

    public SaveTransientObjectBorder(View view) {
        super(new UserAction[] { new SaveAction(), new SaveAndCloseAction(), new CloseAction(), }, view);
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
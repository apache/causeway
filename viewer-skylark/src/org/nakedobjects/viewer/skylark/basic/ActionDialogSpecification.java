package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;
import org.nakedobjects.viewer.skylark.metal.ButtonAction;
import org.nakedobjects.viewer.skylark.metal.ButtonBorder;
import org.nakedobjects.viewer.skylark.metal.WindowBorder;
import org.nakedobjects.viewer.skylark.special.DialogFieldBuilder;
import org.nakedobjects.viewer.skylark.special.ParameterContent;
import org.nakedobjects.viewer.skylark.special.StackLayout;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;

public class ActionDialogSpecification extends AbstractCompositeViewSpecification {

    public ActionDialogSpecification() {
        builder = new StackLayout(new DialogFieldBuilder(new DialogFormSubviews()));
    }

    public View createView(Content content, ViewAxis axis) {
        UserAction[] actions = new UserAction[2];
        actions[0] = new OKAction();
        actions[1] = new CancelAction();
        return new WindowBorder(new ButtonBorder(actions, super.createView(content, new LabelAxis())));
    }
    
    private static class  CancelAction extends ButtonAction {
        public CancelAction() {
            super("Cancel");
        }
        
        public void execute(Workspace workspace, View view, Location at) {
            workspace.removeView(view);
        }
    }
    
    
    private static class  OKAction extends ButtonAction {
        public OKAction() {
            super("OK");
        }
        
        public Permission disabled(View view) {
            ActionContent actionContent = ((ActionContent) view.getContent());
            NakedObject target = actionContent.getObject();
            Action action = actionContent.getAction();
            About about = action.getAbout(Session.getSession().getSecurityContext(), target);
            return about.canUse();
        }
        
        public void execute(Workspace workspace, View view, Location at) {
            ActionContent actionContent = ((ActionContent) view.getContent());
            Action action = actionContent.getAction();
            NakedObject target = actionContent.getObject();
            
            ParameterContent[] parameters = actionContent.getParameterContents();
            NakedObject params[] = new NakedObject[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                params[i] = parameters[i].getObject();
            }
            
            NakedObject result = action.execute(target, params);
            at.move(30, 60);
            workspace.addOpenViewFor(result, at);
        }
    }
    
    private static class DialogFormSubviews implements SubviewSpec {

        public View createSubview(Content content, ViewAxis axis) {
            ParameterContent pc = (ParameterContent)content;
            NakedClass nc = pc.getNakedClass();

            View view = (new ActionParameterField.Specification()).createView(pc, axis);
            return view;
        }

        public View decorateSubview(View view) {
            return view;
        }
    }

    public String getName() {
        return "Action Dialog";
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.FastFinder;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.FieldLabel;
import org.nakedobjects.viewer.skylark.basic.LabelAxis;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;
import org.nakedobjects.viewer.skylark.special.ObjectFieldBuilder;
import org.nakedobjects.viewer.skylark.special.StackLayout;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;
import org.nakedobjects.viewer.skylark.util.ViewFactory;

public class FindFormSpecification  extends AbstractCompositeViewSpecification {
	protected Hint about;

    private static class DataFormSubviews implements SubviewSpec {
		public View createSubview(Content content, ViewAxis axis) {
			ViewFactory factory = ViewFactory.getViewFactory();
			
			if(content instanceof OneToManyField) {
				return null;
	        } else if (content instanceof ValueContent) {
	            ViewSpecification specification = factory.getValueFieldSpecification((ValueContent) content);
				return specification.createView(content, axis);
			}  else if(content instanceof ObjectContent) {
			    ViewSpecification spec = factory.getIconizedSubViewSpecification((ObjectContent) content);
			    return spec.createView(content, axis);
			}
			
			return null;
		}
		
		public View decorateSubview(View view) {
			return new FieldLabel(view);
		}
	}

	public FindFormSpecification() {
		builder = new StackLayout(new ObjectFieldBuilder(new DataFormSubviews()));
	}

    public boolean canDisplay(Naked object) {
        // use this view for a finder
        return super.canDisplay(object) && (object.getObject() instanceof FastFinder);
    }
	
    public View createView(Content content, ViewAxis axis) {
        UserAction[] actions = new UserAction[2];
        actions[0] = new ButtonAction("Find") {
            public Consent disabled(View view) {
                NakedObject target = ((ObjectContent) view.getContent()).getObject();
                Action action = target.getSpecification().getObjectAction(Action.USER, "Find");
                Hint about = target.getHint(ClientSession.getSession(), action, null);
                return about.canUse();
            }
            
            public void execute(Workspace workspace, View view, Location at) {
                NakedObject target = ((ObjectContent) view.getContent()).getObject();
                Action action = target.getSpecification().getObjectAction(Action.USER, "Find");
                Naked result = target.execute(action, null);
                at.move(30, 60);
                workspace.addOpenViewFor(result, at);
            }
        };
        actions[1] = new ButtonAction("Close") {
            public void execute(Workspace workspace, View view, Location at) {
                workspace.removeView(view);
            }
        };
        return new WindowBorder(new ButtonBorder(actions, super.createView(content, new LabelAxis())), false);
    }
    
	public String getName() {
		return "Find Form";
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
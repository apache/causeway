package org.nakedobjects.viewer.skylark.basic;

import java.util.Enumeration;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.InternalCollectionContent;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToOneContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.special.InternalListSpecification;
import org.nakedobjects.viewer.skylark.util.ViewFactory;

public class WorkspaceBuilder extends AbstractViewBuilder {
	private static final Location ZERO = new Location(0, 0);
	private static final ViewSpecification internalList = new InternalListSpecification();
	private boolean layoutInvalid = true;

	public void build(View view) {
	       NakedObject object = ((ObjectContent) view.getContent()).getObject();

	       if(object != null && view.getSubviews().length == 0) {
		        Field[] flds = object.getNakedClass().getVisibleFields(object);
		        ViewFactory factory = ViewFactory.getViewFactory();
		        
		        for (int f = 0; f < flds.length; f++) {
		            Field field = flds[f];
					Naked attribute = field.get(object);
					
					
					if(field.getName().equals("classes") && attribute instanceof InternalCollection) {
						Enumeration elements = ((InternalCollection) attribute).elements();
						while (elements.hasMoreElements()) {
							NakedObject cls = (NakedObject) elements.nextElement();
							View classIcon = factory.createIconizedRootView(cls);
							view.addView(classIcon);
						}
						
					} else if(field.getName().equals("objects") && attribute instanceof InternalCollection) {
							Enumeration elements = ((InternalCollection) attribute).elements();
							while (elements.hasMoreElements()) {
								NakedObject obj = (NakedObject) elements.nextElement();
								View objectIcon = factory.createIconizedRootView(obj);
								view.addView(objectIcon);
							}
					
						
					} else {
						ViewSpecification specification;
						View fieldView;
						
						ViewAxis axis = view.getViewAxis();
						
						if(attribute instanceof NakedCollection) {
							InternalCollectionContent content;
							content = new InternalCollectionContent(object, (InternalCollection) attribute, (OneToManyAssociation) field);
							specification = internalList;
							fieldView =specification.createView(content, axis);
						
						} else if(attribute instanceof NakedObject) {
							OneToOneContent content;
							content = new OneToOneContent(object, (NakedObject) attribute, (OneToOneAssociation) field);
							specification = factory.getIconizedSubViewSpecification(content);
							fieldView =specification.createView(content, axis);
						
						} else if(attribute instanceof NakedValue) { 
							ValueContent content;
							content = new ValueContent(object, (NakedValue) attribute, (Value) field);  
							specification = factory.getValueFieldSpecification(content);
							fieldView =specification.createView(content, axis);
							
						} else {
							OneToOneContent content = new OneToOneContent(object, null, (OneToOneAssociation) field);
							specification = factory.getEmptyFieldSpecification();
							fieldView =specification.createView(content, axis);
						}
						
						String label = field.getLabel(Session.getSession().getSecurityContext(), object);
						fieldView = new LabelBorder(label, fieldView);
						fieldView =  new SimpleBorder(1, fieldView);
//						specification.build(fieldView);
						view.addView(fieldView);
					}
		        }
	       }
	}
	
	public boolean canDisplay(Naked object) {
		return object instanceof NakedObject && object != null;
	}
	
	public Size getRequiredSize(View view) {
		return new Size(500, 500);
	}

	public String getName() {
		return "Simple Workspace";
	}
	
	public void layout(View view) {
        View views[] = view.getSubviews();

        for (int i = 0; i < views.length; i++) {
            View subview = views[i];
			subview.layout();
		}
		
		if(layoutInvalid) {
			Size size = view.getSize();
			size.contract(view.getPadding());
			
			int xClass = 10;
			int yClass = 10;
			
			int xObject = size.getWidth() - 10; 
			int yObject = 10;

			int xField= 10; 
			int yField = size.getHeight() - 10;
			
			int xWindow= 150; 
			int yWindow = 10;

	        for (int i = 0; i < views.length; i++) {
	            View v = views[i];
				Size componentSize = v.getRequiredSize();
				v.setSize(componentSize);
				if(v.getLocation().equals(ZERO)) {
					if(v.getSpecification().isOpen()) {
						v.setLocation(new Location(xWindow, yWindow));
						yWindow += componentSize.getHeight() + 6;
						
					} else if(v.getContent() instanceof FieldContent) {
						v.setLocation(new Location(xField, yField - componentSize.getHeight()));
						xField += componentSize.getWidth() + 6;
					
						
					} else {
						NakedObject object = ((ObjectContent) v.getContent()).getObject();
						if(object instanceof NakedClass) {
							v.setLocation(new Location(xClass, yClass));
							yClass += componentSize.getHeight() + 6;
							
						} else {
							v.setLocation(new Location(xObject - componentSize.getWidth(), yObject));
							yObject += componentSize.getHeight() + 6;
							
						}
					}
					
				}
//				((Workspace) this).limitBounds(view);
			}
		}
	}

	public View createCompositeView(Content content, CompositeViewSpecification specification , ViewAxis axis) {
		throw new NotImplementedException();
	}
	
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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
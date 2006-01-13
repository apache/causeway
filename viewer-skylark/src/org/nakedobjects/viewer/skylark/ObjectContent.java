package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.UserContext;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.UnexpectedCallException;
import org.nakedobjects.viewer.skylark.basic.AbstractContent;
import org.nakedobjects.viewer.skylark.basic.ClassOption;
import org.nakedobjects.viewer.skylark.basic.ObjectOption;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public abstract class ObjectContent extends AbstractContent {

    public abstract Consent canClear();

    public Consent canDrop(Content sourceContent) {
        if (!(sourceContent instanceof ObjectContent)) {
            return new Veto("Can't drop " + sourceContent.getNaked());
        } else {
            NakedObject source = ((ObjectContent) sourceContent).getObject();
            NakedObject target = (NakedObject) getObject();

            Action action = dropAction(source, target);
            if (action != null) {
                return target.isValid(action, new NakedObject[] { source });
            } else {
                if (target.getOid() != null && source.getOid() == null) {
                    return new Veto("Can't set field in persistent object with reference to non-persistent object");

                } else {
                    NakedObjectField[] fields = target.getVisibleFields();
                    for (int i = 0; i < fields.length; i++) {
                        if (source.getSpecification().isOfType(fields[i].getSpecification())) {
                            if (target.getField(fields[i]) == null) {
                                return new Allow("Set field " + fields[i].getName());
                            }
                        }
                    }
                    return new Veto("No empty field accepting object of type " + source.getSpecification().getSingularName());
                }
            }
        }
    }

    public abstract Consent canSet(NakedObject dragSource);

    public abstract void clear();

    public Naked drop(Content sourceContent) {
        NakedObject source = (NakedObject) sourceContent.getNaked();
        Assert.assertNotNull(source);

        NakedObject target = (NakedObject) getObject();
        Assert.assertNotNull(target);

        if (canDrop(sourceContent).isAllowed()) {
            Action action = dropAction(source, target);

            if ((action != null) && target.isValid(action, new NakedObject[] { source }).isAllowed()) {
                return target.execute(action, new NakedObject[] { source });

            } else {
                NakedObjectField[] fields = target.getVisibleFields();
                for (int i = 0; i < fields.length; i++) {
                    if (source.getSpecification().isOfType(fields[i].getSpecification()) && target.getField(fields[i]) == null) {
                        target.setAssociation(((OneToOneAssociation) fields[i]), source);
                        break;
                    }
                }
            }
        }
        return null;
    }

    private Action dropAction(NakedObject source, NakedObject target) {
        Action action;
        if (target.getObject() instanceof NakedClass) {
            NakedObjectSpecification forNakedClass = ((NakedClass) target.getObject()).forObjectType();
            action = forNakedClass
                    .getClassAction(Action.USER, null, new NakedObjectSpecification[] { source.getSpecification() });
        } else {
            action = target.getSpecification().getObjectAction(Action.USER, null,
                    new NakedObjectSpecification[] { source.getSpecification() });
        }
        return action;
    }

    public Hint getHint() {
        return null;
    }

    public abstract NakedObject getObject();
    
    public boolean isPersistable() {
        return getObject().persistable() == Persistable.USER_PERSISTABLE;
    }

    public void contentMenuOptions(MenuOptionSet options) {
        final NakedObject object = getObject();
        ObjectOption.menuOptions(object, options);

        if (getObject() == null) {
            ClassOption.menuOptions(getSpecification(), options);
        }

        options.add(MenuOptionSet.EXPLORATION, new MenuOption("Instances") {
            public Consent disabled(View component) {
                return AbstractConsent.allow(object != null);
            }
            
            public void execute(Workspace workspace, View view, Location at) {
                NakedObjectSpecification spec = getObject().getSpecification();
                TypedNakedCollection instances = NakedObjects.getObjectPersistor().allInstances(spec, false);
                
                Content content = Skylark.getContentFactory().createRootContent(instances);
                View cloneView = Skylark.getViewFactory().createWindow(content);
                cloneView.setLocation(at);
                workspace.addView(cloneView);
            }
        });

        options.add(MenuOptionSet.EXPLORATION, new MenuOption("Class") {
            public Consent disabled(View component) {
                return AbstractConsent.allow(object != null);
            }
            
            public void execute(Workspace workspace, View view, Location at) {
                /*
                 * TODO reimplement return
                 * getObjectManager().getNakedClass(getObject().getSpecification());
                 */
            }
        });

        options.add(MenuOptionSet.EXPLORATION, new MenuOption("Clone") {
            public Consent disabled(View component) {
                return AbstractConsent.allow(object != null);
            }

            public void execute(Workspace workspace, View view, Location at) {
                NakedObject original = getObject();
                NakedObjectSpecification spec = original.getSpecification();
                
                NakedObject clone = NakedObjects.getObjectPersistor().createTransientInstance(spec);
                NakedObjectField[] fields = spec.getFields();
                for (int i = 0; i < fields.length; i++) {
                    Naked fld = original.getField(fields[i]);
                    
                    if(fields[i].isObject()) {
                        clone.setAssociation(fields[i], (NakedObject) fld);
                    } else if(fields[i].isValue()) {
                        clone.setValue((OneToOneAssociation) fields[i], fld.getObject());
                    } else if(fields[i].isCollection()) {
                        
//                        clone.setValue((OneToOneAssociation) fields[i], fld.getObject());
                    }
                }
                
                //AbstractNakedObject clone = (AbstractNakedObject) createInstance(getClass());
                //clone.copyObject(this);
                //clone.objectChanged();
                
                Content content = Skylark.getContentFactory().createRootContent(clone);
                View cloneView = Skylark.getViewFactory().createWindow(content);
                cloneView.setLocation(at);
                workspace.addView(cloneView);
                //newWorkspace.markDamaged();
             
            }
        });

        options.add(MenuOptionSet.DEBUG, new MenuOption("Clear resolved") {
            public Consent disabled(View component) {
                return AbstractConsent.allow(object == null ||  object.getResolveState() != ResolveState.TRANSIENT || 
                        object.getResolveState() == ResolveState.GHOST);
            }

            public void execute(Workspace workspace, View view, Location at) {
                object.debugClearResolved();
            }
        });

    }

    public void parseTextEntry(String entryText) throws InvalidEntryException {
        throw new UnexpectedCallException();
    }

    public abstract void setObject(NakedObject object);
    
    public String getIconName() {
        NakedObject object = getObject();
        return object == null ? null : object.getIconName();
    }
    
    public Image getIconPicture(int iconHeight) {
        NakedObject nakedObject = getObject();
        if (nakedObject == null) {
            return  ImageFactory.getInstance().createIcon("emptyField", iconHeight, null);
        }
        Object object = nakedObject.getObject();
        if( object instanceof NakedClass) {
            NakedObjectSpecification specification = ((NakedClass) object).forObjectType();
            return ImageFactory.getInstance().loadClassIcon(specification, "", iconHeight);
        } else {
	        NakedObjectSpecification specification = nakedObject.getSpecification();
	        return ImageFactory.getInstance().loadObjectIcon(specification, "", iconHeight);
        }
    }
    
    public void viewMenuOptions(MenuOptionSet options) {
        final NakedObject object = getObject();
        
        if (object instanceof UserContext) {
            options.add(MenuOptionSet.USER, new MenuOption("New Workspace") {
                public Consent disabled(View component) {
                    return AbstractConsent.allow(object instanceof UserContext);
                }

                public void execute(Workspace workspace, View view, Location at) {
                    View newWorkspace;
                    Content content = Skylark.getContentFactory().createRootContent(object);
                    newWorkspace = Skylark.getViewFactory().createInnerWorkspace(content);
                    newWorkspace.setLocation(at);
                    workspace.addView(newWorkspace);
                    newWorkspace.markDamaged();
                }
            });
        }
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
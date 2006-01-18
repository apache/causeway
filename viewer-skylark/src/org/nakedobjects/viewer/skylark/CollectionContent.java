package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.UnexpectedCallException;
import org.nakedobjects.viewer.skylark.basic.AbstractContent;
import org.nakedobjects.viewer.skylark.basic.FieldComparator;
import org.nakedobjects.viewer.skylark.basic.TitleComparator;
import org.nakedobjects.viewer.skylark.basic.TypeComparator;

import java.util.Enumeration;


public abstract class CollectionContent extends AbstractContent implements Content {
    private static final TypeComparator TYPE_COMPARATOR = new TypeComparator();
    private static final TitleComparator TITLE_COMPARATOR = new TitleComparator();
    private final static CollectionSorter sorter = new SimpleCollectionSorter();
    private Comparator order;
    private boolean reverse;


    public final Enumeration allElements() {
        final NakedObject[] elements = elements();
        
        sorter.sort(elements, order, reverse);
        
        return new Enumeration() {
            int i = 0;
            int size = elements.length;
            
            public boolean hasMoreElements() {
                return i < size;
            }

            public Object nextElement() {
                return elements[i++];
            }
        };
    }
    
    public void debugDetails(DebugString debug) {
        debug.appendln(4, "order", order);
        debug.appendln(4, "reverse order", reverse);
    }

    public abstract NakedObject[] elements();
  
    public abstract NakedCollection getCollection();

    public void contentMenuOptions(UserActionSet options) {
        final NakedCollection collection = getCollection();
        
  		// TODO find all collection actions, and make them available
  		// not valid       ObjectOption.menuOptions((NakedObject) object, options);
            
        Action[] actions = collection.getSpecification().getObjectActions(Action.USER);

        for (int i = 0; i < actions.length; i++) {
            
            AbstractUserAction option;
            option = new AbstractUserAction(actions[i].getId()) {
                public void execute(Workspace workspace, View view, Location at) {}
            };
            
            if (option != null) {
                options.add(option);
            }
        }
        
        options.add(new AbstractUserAction("Clear resolved", UserAction.DEBUG) {
            public Consent disabled(View component) {
                return AbstractConsent.allow(collection == null ||  collection.getResolveState() != ResolveState.TRANSIENT || 
                        collection.getResolveState() == ResolveState.GHOST);
            }

            public void execute(Workspace workspace, View view, Location at) {
                collection.debugClearResolved();
            }
        });

    }
    
    public void viewMenuOptions(UserActionSet options) {
        UserActionSet sortOptions = new UserActionSet("Sort", options);
        options.add(sortOptions);
        
        sortOptions.add(new AbstractUserAction("Clear") {
            public Consent disabled(View component) {
                return AbstractConsent.allow(order != null);
            }
            
            public void execute(Workspace workspace, View view, Location at) {
                order = null;
                view.invalidateContent();
            }
        });
        
        if(reverse) {
            sortOptions.add(new AbstractUserAction("Normal sort order") {
                public Consent disabled(View component) {
                    return AbstractConsent.allow(order != null);
                }
                
                public void execute(Workspace workspace, View view, Location at) {
                    reverse = false;
                    view.invalidateContent();
                }
            });
        } else {
            sortOptions.add(new AbstractUserAction("Reverse sort order") {
                public Consent disabled(View component) {
                    return AbstractConsent.allow(order != null);
                }
                
                public void execute(Workspace workspace, View view, Location at) {
                    reverse = true;
                    view.invalidateContent();
                }
            });
        }
        
        sortOptions.add(new AbstractUserAction("Sort by title") {
            public Consent disabled(View component) {
                return AbstractConsent.allow(order != TITLE_COMPARATOR);
            }
            
            public void execute(Workspace workspace, View view, Location at) {
                order = TITLE_COMPARATOR;
                view.invalidateContent();
            }
        });
        
        
        sortOptions.add(new AbstractUserAction("Sort by type") {
            public Consent disabled(View component) {
                return AbstractConsent.allow(order != TYPE_COMPARATOR);
            }
            
            public void execute(Workspace workspace, View view, Location at) {
                order = TYPE_COMPARATOR;
                view.invalidateContent();
            }
        });
        
        NakedCollection c = getCollection();
        if(c instanceof TypedNakedCollection) {
            NakedObjectSpecification spec = ((TypedNakedCollection) c).getElementSpecification();
            NakedObjectField[] fields = spec.getFields();
            for (int i = 0; i < fields.length; i++) {
                final NakedObjectField field = fields[i];
                
                sortOptions.add(new AbstractUserAction("Sort by " + field.getName()) {
                    public void execute(Workspace workspace, View view, Location at) {
                        order = new FieldComparator(field);
                        view.invalidateContent();
                    }
                });
            }
        }
    }
    
    public void parseTextEntry(String entryText) throws InvalidEntryException {
        throw new UnexpectedCallException();
    }

    public void setOrder(Comparator order) {
        this.order = order;
    }
    
    public void setOrderByField(NakedObjectField field) {
        if(order  instanceof FieldComparator && ((FieldComparator) order).getField() == field) {
            reverse = !reverse;
        } else {
            order = new FieldComparator(field);
            reverse = false;
        }
    }

    public void setOrderByElement() {
        if(order == TITLE_COMPARATOR) {
            reverse = !reverse;
        } else {
            order = TITLE_COMPARATOR;
            reverse = false;
        }
    }
    
    public NakedObjectField getFieldSortOrder() {
        if(order  instanceof FieldComparator) {
            return ((FieldComparator) order).getField();
        } else {
            return null;
        }
    }
    
    public boolean getOrderByElement() {
        return order == TITLE_COMPARATOR;
    }
    
    public boolean getReverseSortOrder() {
        return reverse;
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
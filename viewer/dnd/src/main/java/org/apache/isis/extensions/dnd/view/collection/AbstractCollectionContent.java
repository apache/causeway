/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.extensions.dnd.view.collection;

import java.util.Enumeration;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.commons.exceptions.UnexpectedCallException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.ConsentAbstract;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.util.CollectionFacetUtils;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.drawing.ImageFactory;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.content.AbstractContent;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;


public abstract class AbstractCollectionContent extends AbstractContent implements CollectionContent {
    private static final TypeComparator TYPE_COMPARATOR = new TypeComparator();
    private static final TitleComparator TITLE_COMPARATOR = new TitleComparator();
    private final static CollectionSorter sorter = new SimpleCollectionSorter();
    private Comparator order;
    private boolean reverse;

    public final Enumeration allElements() {
        final ObjectAdapter[] elements = elements();

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

    public void debugDetails(final DebugString debug) {
        debug.appendln("order", order);
        debug.appendln("reverse order", reverse);
    }

    @Override
    public final boolean isCollection() {
        return true;
    }

    public ObjectAdapter[] elements() {
        final ObjectAdapter collection = getCollection();
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final ObjectAdapter[] elementsArray = new ObjectAdapter[facet.size(collection)];
        int i = 0;
        for (ObjectAdapter element : facet.iterable(collection)) {
            elementsArray[i++] = element;
        }
        return elementsArray;
    }

    public abstract ObjectAdapter getCollection();

    public ObjectSpecification getElementSpecification() {
        final ObjectAdapter collection = getCollection();
        final TypeOfFacet facet = collection.getTypeOfFacet();
        return facet.valueSpec();
    }

    public String getDescription() {
        return "Collection";
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        final ObjectAdapter collection = getCollection();
        options.addObjectMenuOptions(collection);

        // TODO find all collection actions, and make them available
        // not valid ObjectOption.menuOptions((ObjectAdapter) object, options);
        /*
         * Action[] actions = collection.getSpecification().getObjectActions(Action.USER);
         * 
         * for (int i = 0; i < actions.length; i++) { final Action action = actions[i]; AbstractUserAction
         * option; option = new AbstractUserAction(actions[i].getId()) { public void execute(final Workspace
         * workspace, final View view, final Location at) { [[NAME]] result = collection.execute(action, new
         * [[NAME]][0]); at.add(20, 20); workspace.addOpenViewFor(result, at); } };
         * 
         * if (option != null) { options.add(option); } }
         */
        options.add(new UserActionAbstract("Clear resolved", ObjectActionType.DEBUG) {
            @Override
            public Consent disabled(final View component) {
                return ConsentAbstract.allowIf(collection == null || collection.getResolveState() != ResolveState.TRANSIENT
                        || collection.getResolveState() == ResolveState.GHOST);
            }

            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                collection.changeState(ResolveState.GHOST);
            }
        });

    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        final UserActionSet sortOptions = options.addNewActionSet("Sort");

        sortOptions.add(new UserActionAbstract("Clear") {
            @Override
            public Consent disabled(final View component) {
                return ConsentAbstract.allowIf(order != null);
            }

            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                order = null;
                view.invalidateContent();
            }
        });

        if (reverse) {
            sortOptions.add(new UserActionAbstract("Normal sort order") {
                @Override
                public Consent disabled(final View component) {
                    return ConsentAbstract.allowIf(order != null);
                }

                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    reverse = false;
                    view.invalidateContent();
                }
            });
        } else {
            sortOptions.add(new UserActionAbstract("Reverse sort order") {
                @Override
                public Consent disabled(final View component) {
                    return ConsentAbstract.allowIf(order != null);
                }

                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    reverse = true;
                    view.invalidateContent();
                }
            });
        }

        sortOptions.add(new UserActionAbstract("Sort by title") {
            @Override
            public Consent disabled(final View component) {
                return ConsentAbstract.allowIf(order != TITLE_COMPARATOR);
            }

            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                order = TITLE_COMPARATOR;
                view.invalidateContent();
            }
        });

        sortOptions.add(new UserActionAbstract("Sort by type") {
            @Override
            public Consent disabled(final View component) {
                return ConsentAbstract.allowIf(order != TYPE_COMPARATOR);
            }

            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                order = TYPE_COMPARATOR;
                view.invalidateContent();
            }
        });

        final ObjectAssociation[] fields = getElementSpecification().getAssociations();
        for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation field = fields[i];

            sortOptions.add(new UserActionAbstract("Sort by " + field.getName()) {
                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    order = new FieldComparator(field);
                    view.invalidateContent();
                }
            });
        }
    }

    public void parseTextEntry(final String entryText) {
        throw new UnexpectedCallException();
    }

    public void setOrder(final Comparator order) {
        this.order = order;
    }

    public void setOrderByField(final ObjectAssociation field) {
        if (order instanceof FieldComparator && ((FieldComparator) order).getField() == field) {
            reverse = !reverse;
        } else {
            order = new FieldComparator(field);
            reverse = false;
        }
    }

    public void setOrderByElement() {
        if (order == TITLE_COMPARATOR) {
            reverse = !reverse;
        } else {
            order = TITLE_COMPARATOR;
            reverse = false;
        }
    }

    public ObjectAssociation getFieldSortOrder() {
        if (order instanceof FieldComparator) {
            return ((FieldComparator) order).getField();
        } else {
            return null;
        }
    }

    public Image getIconPicture(final int iconHeight) {
        final ObjectAdapter adapter = getCollection();
        if (adapter == null) {
            return ImageFactory.getInstance().loadIcon("emptyField", iconHeight, null);
        }
        final ObjectSpecification specification = adapter.getSpecification();
        Image icon = ImageFactory.getInstance().loadIcon(specification, iconHeight, null);
        if (icon == null) {
            icon = ImageFactory.getInstance().loadDefaultIcon(iconHeight, null);
        }
        return icon;
    }

    public boolean getOrderByElement() {
        return order == TITLE_COMPARATOR;
    }

    public boolean getReverseSortOrder() {
        return reverse;
    }

    public boolean isOptionEnabled() {
        return false;
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }
}

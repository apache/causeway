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

package org.apache.isis.viewer.dnd.view.content;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.ConsentAbstract;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public abstract class AbstractObjectContent extends AbstractContent implements ObjectContent {

    public static final class ExplorationInstances extends UserActionAbstract {

        public ExplorationInstances() {
            super("Instances", ActionType.EXPLORATION);
        }

        @Override
        public Consent disabled(final View view) {
            final ObjectAdapter object = view.getContent().getAdapter();
            return ConsentAbstract.allowIf(object != null);
        }

        @Override
        public void execute(final Workspace workspace, final View view, final Location at) {
            final ObjectAdapter object = view.getContent().getAdapter();
            final ObjectSpecification spec = object.getSpecification();
            final ObjectAdapter instances = IsisContext.getPersistenceSession().findInstances(new QueryFindAllInstances(spec.getFullIdentifier()), QueryCardinality.MULTIPLE);
            workspace.objectActionResult(instances, new Placement(view));
        }
    }

    public static final class ExplorationClone extends UserActionAbstract {

        public ExplorationClone() {
            super("Clone", ActionType.EXPLORATION);
        }

        @Override
        public Consent disabled(final View view) {
            final ObjectAdapter object = view.getContent().getAdapter();
            return ConsentAbstract.allowIf(object != null);
        }

        @Override
        public void execute(final Workspace workspace, final View view, final Location at) {
            final ObjectAdapter original = view.getContent().getAdapter();
            // ObjectAdapter original = getObject();
            final ObjectSpecification spec = original.getSpecification();

            final ObjectAdapter clone = getPersistenceSession().createTransientInstance(spec);
            final List<ObjectAssociation> fields = spec.getAssociations(Contributed.EXCLUDED);
            for (int i = 0; i < fields.size(); i++) {
                final ObjectAdapter fld = fields.get(i).get(original);

                if (fields.get(i).isOneToOneAssociation()) {
                    ((OneToOneAssociation) fields.get(i)).setAssociation(clone, fld);
                } else if (fields.get(i).isOneToManyAssociation()) {
                    // clone.setValue((OneToOneAssociation) fields[i],
                    // fld.getObject());
                }
            }

            workspace.objectActionResult(clone, new Placement(view));
        }
    }

    public static final class DebugClearResolvedOption extends UserActionAbstract {

        private DebugClearResolvedOption() {
            super("Clear resolved", ActionType.DEBUG);
        }

        @Override
        public Consent disabled(final View view) {
            final ObjectAdapter object = view.getContent().getAdapter();
            return ConsentAbstract.allowIf(object == null || !object.isTransient() || object.isGhost());
        }

        @Override
        public void execute(final Workspace workspace, final View view, final Location at) {
            final ObjectAdapter object = view.getContent().getAdapter();
            object.changeState(ResolveState.GHOST);
        }
    }

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with 
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly) 
    // for any other value for Where
    protected final Where where = Where.ANYWHERE;

    @Override
    public abstract Consent canClear();

    @Override
    public Consent canDrop(final Content sourceContent) {
        final ObjectAdapter target = getObject();
        if (!(sourceContent instanceof ObjectContent) || target == null) {
            // TODO: move logic into Facet
            return new Veto(String.format("Can't drop %s onto empty target", sourceContent.getAdapter().titleString()));
        } else {
            final ObjectAdapter source = ((ObjectContent) sourceContent).getObject();
            return canDropOntoObject(target, source);
        }
    }

    private Consent canDropOntoObject(final ObjectAdapter target, final ObjectAdapter source) {
        final ObjectAction action = dropAction(source, target);
        if (action != null) {
            final Consent parameterSetValid = action.isProposedArgumentSetValid(target, new ObjectAdapter[] { source });
            parameterSetValid.setDescription("Execute '" + action.getName() + "' with " + source.titleString());
            return parameterSetValid;
        } else {
            return setFieldOfMatchingType(target, source);
        }
    }

    private Consent setFieldOfMatchingType(final ObjectAdapter targetAdapter, final ObjectAdapter sourceAdapter) {
        if (targetAdapter.isTransient() && sourceAdapter.representsPersistent()) {
            // TODO: use Facet for this test instead.
            return new Veto("Can't set field in persistent object with reference to non-persistent object");
        }
        final List<ObjectAssociation> fields = targetAdapter.getSpecification().getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.dynamicallyVisible(IsisContext.getAuthenticationSession(), targetAdapter, where));
        for (final ObjectAssociation fld : fields) {
            if (!fld.isOneToOneAssociation()) {
                continue;
            }
            if (!sourceAdapter.getSpecification().isOfType(fld.getSpecification())) {
                continue;
            }
            if (fld.get(targetAdapter) != null) {
                continue;
            }
            final Consent associationValid = ((OneToOneAssociation) fld).isAssociationValid(targetAdapter, sourceAdapter);
            if (associationValid.isAllowed()) {
                return associationValid.setDescription("Set field " + fld.getName());
            }

        }
        // TODO: use Facet for this test instead
        return new Veto(String.format("No empty field accepting object of type %s in %s", sourceAdapter.getSpecification().getSingularName(), title()));
    }

    @Override
    public abstract Consent canSet(final ObjectAdapter dragSource);

    @Override
    public abstract void clear();

    @Override
    public ObjectAdapter drop(final Content sourceContent) {
        if (!(sourceContent instanceof ObjectContent)) {
            return null;
        }

        final ObjectAdapter source = sourceContent.getAdapter();
        Assert.assertNotNull(source);

        final ObjectAdapter target = getObject();
        Assert.assertNotNull(target);

        if (!canDrop(sourceContent).isAllowed()) {
            return null;
        }

        final ObjectAction action = dropAction(source, target);
        if ((action != null) && action.isProposedArgumentSetValid(target, new ObjectAdapter[] { source }).isAllowed()) {
            return action.execute(target, new ObjectAdapter[] { source });
        }

        final List<ObjectAssociation> associations = target.getSpecification().getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.dynamicallyVisible(IsisContext.getAuthenticationSession(), target, where));

        for (int i = 0; i < associations.size(); i++) {
            final ObjectAssociation association = associations.get(i);
            if (association.isOneToOneAssociation() && source.getSpecification().isOfType(association.getSpecification())) {
                final OneToOneAssociation otoa = (OneToOneAssociation) association;
                if (association.get(target) == null && otoa.isAssociationValid(target, source).isAllowed()) {
                    otoa.setAssociation(target, source);
                    break;
                }
            }
        }

        return null;
    }

    private ObjectAction dropAction(final ObjectAdapter source, final ObjectAdapter target) {
        final ObjectAction action = target.getSpecification().getObjectAction(ActionType.USER, null, Arrays.asList(source.getSpecification()));
        return action;
    }

    @Override
    public abstract ObjectAdapter getObject();

    @Override
    public boolean isPersistable() {
        return getObject().getSpecification().persistability() == Persistability.USER_PERSISTABLE;
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        final ObjectAdapter object = getObject();
        options.addObjectMenuOptions(object);

        if (getObject() == null) {
            options.addCreateOptions(getSpecification());
        } else {
            options.add(new ExplorationInstances());
        }

        options.add(new ExplorationClone());
        options.add(new DebugClearResolvedOption());
    }

    public void parseTextEntry(final String entryText) {
        throw new UnexpectedCallException();
    }

    @Override
    public abstract void setObject(final ObjectAdapter object);

    @Override
    public String getIconName() {
        final ObjectAdapter object = getObject();
        return object == null ? null : object.getIconName();
    }

    @Override
    public Image getIconPicture(final int iconHeight) {
        final ObjectAdapter adapter = getObject();
        if (adapter == null) {
            return ImageFactory.getInstance().loadIcon("empty-field", iconHeight, null);
        }
        final ObjectSpecification specification = adapter.getSpecification();
        final Image icon = ImageFactory.getInstance().loadIcon(specification, iconHeight, null);
        return icon;
    }

    // ////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////////////

    private static Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}

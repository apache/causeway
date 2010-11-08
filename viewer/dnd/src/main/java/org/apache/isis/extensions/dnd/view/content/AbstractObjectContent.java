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


package org.apache.isis.extensions.dnd.view.content;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.ConsentAbstract;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.metamodel.services.container.query.QueryFindAllInstances;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Persistability;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.drawing.ImageFactory;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ObjectContent;
import org.apache.isis.extensions.dnd.view.Placement;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;


public abstract class AbstractObjectContent extends AbstractContent implements ObjectContent {

    public static final class ExplorationInstances extends UserActionAbstract {

        public ExplorationInstances() {
            super("Instances", ObjectActionType.EXPLORATION);
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
            final ObjectAdapter instances = IsisContext.getPersistenceSession().findInstances(
                    new QueryFindAllInstances(spec), QueryCardinality.MULTIPLE);
            workspace.objectActionResult(instances, new Placement(view));
        }
    }

    public static final class ExplorationClone extends UserActionAbstract {

        public ExplorationClone() {
            super("Clone", ObjectActionType.EXPLORATION);
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

            final ObjectAdapter clone = getPersistenceSession().createInstance(spec);
            final ObjectAssociation[] fields = spec.getAssociations();
            for (int i = 0; i < fields.length; i++) {
                final ObjectAdapter fld = fields[i].get(original);

                if (fields[i].isOneToOneAssociation()) {
                    ((OneToOneAssociation) fields[i]).setAssociation(clone, fld);
                } else if (fields[i].isOneToManyAssociation()) {
                    // clone.setValue((OneToOneAssociation) fields[i], fld.getObject());
                }
            }

            workspace.objectActionResult(clone, new Placement(view));
        }
    }

    public static final class DebugClearResolvedOption extends UserActionAbstract {

        private DebugClearResolvedOption() {
            super("Clear resolved", ObjectActionType.DEBUG);
        }

        @Override
        public Consent disabled(final View view) {
            final ObjectAdapter object = view.getContent().getAdapter();
            return ConsentAbstract.allowIf(object == null || object.getResolveState() != ResolveState.TRANSIENT
                    || object.getResolveState() == ResolveState.GHOST);
        }

        @Override
        public void execute(final Workspace workspace, final View view, final Location at) {
            final ObjectAdapter object = view.getContent().getAdapter();
            object.changeState(ResolveState.GHOST);
        }
    }

    public abstract Consent canClear();

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
        if (targetAdapter.isTransient() && sourceAdapter.isPersistent()) {
            // TODO: use Facet for this test instead.
            return new Veto("Can't set field in persistent object with reference to non-persistent object");
        }
        final ObjectAssociation[] fields = targetAdapter.getSpecification().getAssociations(
                ObjectAssociationFilters.dynamicallyVisible(IsisContext.getAuthenticationSession(), targetAdapter));
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
        return new Veto(String.format("No empty field accepting object of type %s in %s", sourceAdapter.getSpecification()
                .getSingularName(), title()));
    }

    public abstract Consent canSet(final ObjectAdapter dragSource);

    public abstract void clear();

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

        final ObjectAssociation[] associations = target.getSpecification().getAssociations(
                ObjectAssociationFilters.dynamicallyVisible(IsisContext.getAuthenticationSession(), target));

        for (int i = 0; i < associations.length; i++) {
            final ObjectAssociation association = associations[i];
            if (association.isOneToOneAssociation() && source.getSpecification().isOfType(association.getSpecification())) {
                OneToOneAssociation otoa = (OneToOneAssociation) association;
                if (association.get(target) == null && otoa.isAssociationValid(target, source).isAllowed()) {
                    otoa.setAssociation(target, source);
                    break;
                }
            }
        }

        return null;
    }

    private ObjectAction dropAction(final ObjectAdapter source, final ObjectAdapter target) {
        ObjectAction action = target.getSpecification().getObjectAction(ObjectActionType.USER, null,
                new ObjectSpecification[] { source.getSpecification() });
        return action;
    }

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

    public abstract void setObject(final ObjectAdapter object);

    public String getIconName() {
        final ObjectAdapter object = getObject();
        return object == null ? null : object.getIconName();
    }

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

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}

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

package org.apache.isis.viewer.wicket.ui.components.scalars.primitive;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxX;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxXConfig;

import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Panel for rendering scalars of type {@link Boolean} or <tt>boolean</tt>.
 */
public class BooleanPanel extends ScalarPanelAbstract {

    private static final CheckBoxXConfig THREE_STATE_CONFIG = new CheckBoxXConfig()
        .withSize(CheckBoxXConfig.Sizes.xs)
        .withEnclosedLabel(false)
        .withIconChecked("<i class='fa fa-fw fa-check'></i>")
        .withIconNull("<i class='fa fa-fw fa-square'></i>");

    private static final CheckBoxXConfig TWO_STATE_CONFIG = new CheckBoxXConfig(THREE_STATE_CONFIG).withThreeState(false);

    private static final long serialVersionUID = 1L;

    private CheckBoxX checkBox;

    public BooleanPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected MarkupContainer addComponentForRegular() {
        final String name = getModel().getName();

        checkBox = createCheckBox(ID_SCALAR_VALUE);

        checkBox.setLabel(Model.of(name));

        final FormGroup labelIfRegular = new FormGroup(ID_SCALAR_IF_REGULAR, checkBox);
        labelIfRegular.add(checkBox);
        if(getModel().isRequired()) {
            labelIfRegular.add(new CssClassAppender("mandatory"));
        }

        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", Model.of(describedAs)));
        }
        
        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(checkBox));
        labelIfRegular.add(scalarName);
        NamedFacet namedFacet = getModel().getFacet(NamedFacet.class);
        if (namedFacet != null) {
            scalarName.setEscapeModelStrings(namedFacet.escaped());
        }

        final List<LinkAndLabel> entityActions = EntityActionUtil.getEntityActionLinksForAssociation(this.scalarModel, getDeploymentType());

        addPositioningCssTo(labelIfRegular, entityActions);

        addOrReplace(labelIfRegular);
        addFeedbackTo(labelIfRegular, checkBox);

        // ... add entity links to panel (below and to right)
        addEntityActionLinksBelowAndRight(labelIfRegular, entityActions);

        return labelIfRegular;
    }



    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#COMPACT compact} format.
     */
    @Override
    protected Component addComponentForCompact() {
        final CheckBoxX component = createCheckBox(ID_SCALAR_IF_COMPACT);
        addOrReplace(component);
        return component;
    }

    private CheckBoxX createCheckBox(final String id) {
        final CheckBoxX checkBox = new CheckBoxX(id, new Model<Boolean>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                final ScalarModel model = getModel();
                final ObjectAdapter adapter = model.getObject();
                return adapter != null? (Boolean) adapter.getObject(): null;
            }

            @Override
            public void setObject(final Boolean object) {
                final ObjectAdapter adapter = getAdapterManager().adapterFor(object);
                getModel().setObject(adapter);
            }
        }) {
            @Override
            public CheckBoxXConfig getConfig() {
                return BooleanPanel.this.getModel().isRequired()
                    ? TWO_STATE_CONFIG
                    : THREE_STATE_CONFIG;
            }
        };
        checkBox.setOutputMarkupId(true);
        checkBox.setEnabled(false); // will be enabled before rendering if
                                    // required

        // must prime the underlying model if this is a primitive boolean
        final ObjectSpecification objectSpecification = getModel().getTypeOfSpecification();
        if(objectSpecification.getFullIdentifier().equals("boolean")) {
            if(getModel().getObject() == null) {
                getModel().setObject(getAdapterManager().adapterFor(false));
            }
        }

        return checkBox;
    }

    @Override
    protected void onBeforeRenderWhenEnabled() {
        super.onBeforeRenderWhenEnabled();
        checkBox.setEnabled(true);
    }

    @Override
    protected void onBeforeRenderWhenViewMode() {
        super.onBeforeRenderWhenViewMode();
        checkBox.setEnabled(false);
    }

    @Override
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        super.onBeforeRenderWhenDisabled(disableReason);
        checkBox.setEnabled(false);
    }

    @Override
    protected void addFormComponentBehavior(Behavior behavior) {
        checkBox.add(behavior);
    }

    @Override
    public String getVariation() {
        String variation;
        final LabelAtFacet facet = getModel().getFacet(LabelAtFacet.class);
        if (facet != null && LabelPosition.RIGHT == facet.label()) {
            variation = "labelRightPosition";
        } else {
            variation = super.getVariation();
        }
        return variation;
    }
}

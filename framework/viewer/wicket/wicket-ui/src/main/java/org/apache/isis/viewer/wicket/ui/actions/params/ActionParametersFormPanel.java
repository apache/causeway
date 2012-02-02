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

package org.apache.isis.viewer.wicket.ui.actions.params;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionExecutor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormFeedbackPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 */
public class ActionParametersFormPanel extends PanelAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_OK_BUTTON = "okButton";
    private static final String ID_ACTION_PARAMETERS = "parameters";

    private final ActionExecutor actionExecutor;

    public ActionParametersFormPanel(final String id, final ActionModel model) {
        super(id, model);

        Ensure.ensureThatArg(model.getExecutor(), is(not(nullValue())));

        this.actionExecutor = model.getExecutor();
        buildGui();
    }

    private void buildGui() {
        add(new ActionParameterForm("inputForm", getModel()));
    }

    class ActionParameterForm extends Form<ObjectAdapter> {

        private static final long serialVersionUID = 1L;

        private static final String ID_FEEDBACK = "feedback";

        public ActionParameterForm(final String id, final ActionModel actionModel) {
            super(id, actionModel);

            addParameters();

            addOrReplace(new FormFeedbackPanel(ID_FEEDBACK));
            addOkButton();
        }

        private ActionModel getActionModel() {
            return (ActionModel) super.getModel();
        }

        private void addParameters() {
            final ActionModel actionModel = getActionModel();
            final ObjectAction ObjectAction = actionModel.getActionMemento().getAction();

            final List<ObjectActionParameter> parameters = ObjectAction.getParameters();

            final RepeatingView rv = new RepeatingView(ID_ACTION_PARAMETERS);
            add(rv);
            final List<ActionParameterMemento> mementos = buildParameterMementos(parameters);
            for (final ActionParameterMemento apm : mementos) {
                final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
                rv.add(container);

                final ScalarModel argumentModel = actionModel.getArgumentModel(apm);
                getComponentFactoryRegistry().addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE, argumentModel);
            }
        }

        private void addOkButton() {
            add(new Button(ID_OK_BUTTON) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    actionExecutor.executeActionAndProcessResults();
                };
            });
        }

        private List<ActionParameterMemento> buildParameterMementos(final List<ObjectActionParameter> parameters) {
            final List<ActionParameterMemento> parameterMementoList = Lists.transform(parameters, Mementos.fromActionParameter());
            // we copy into a new array list otherwise we get lazy evaluation =
            // reference to a non-serializable object
            return Lists.newArrayList(parameterMementoList);
        }
    }

}

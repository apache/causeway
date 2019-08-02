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
package org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Response;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Settings;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.security.authentication.MessageBroker;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class BreadcrumbPanel extends PanelAbstract<IModel<Void>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_BREADCRUMBS = "breadcrumbs";
    /**
     * A configuration setting which value determines whether the breadcrumbs should be available in the footer
     */
    public static final String SHOW_BREADCRUMBS_KEY = "isis.viewer.wicket.breadcrumbs.showChooser";
    public static final boolean SHOW_BREADCRUMBS_DEFAULT = true;

    public BreadcrumbPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();

        final IModel<EntityModel> entityModel = new Model<>();
        ChoiceProvider<EntityModel> choiceProvider = new ChoiceProvider<EntityModel>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getDisplayValue(EntityModel choice) {
                return titleFor(choice);
            }

            private String titleFor(final EntityModel model) {
                return model.getObjectAdapterMemento().getObjectAdapter().titleString(null);
            }


            @Override
            public String getIdValue(EntityModel choice) {
                try {
                    final PageParameters pageParameters = choice.getPageParametersWithoutUiHints();
                    final String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
                    final RootOid result = RootOid.deString(oidStr);
                    return Oid.marshaller().marshal(result);
                } catch (Exception ex) {
                    breadcrumbModel.remove(choice);
                    return null;
                }
            }


            @Override
            public void query(String term, int page, Response<EntityModel> response) {
                final List<EntityModel> breadCrumbList = _Lists.newArrayList(breadcrumbModel.getList());
                final List<EntityModel> checkedList = _Lists.newArrayList(
                        Iterables.filter(breadCrumbList, new Predicate<EntityModel>() {
                            @Override
                            public boolean apply(final EntityModel input) {
                                final Object id = getIdValue(input);
                                return id != null;
                            }
                        })
                        );
                response.addAll(checkedList);
            }

            @Override
            public Collection<EntityModel> toChoices(Collection<String> ids) {
                return breadcrumbModel.getList();
            }

        };
        final Select2Choice<EntityModel> breadcrumbChoice = new Select2Choice<>(ID_BREADCRUMBS, entityModel, choiceProvider);

        breadcrumbChoice.add(
                new AjaxFormComponentUpdatingBehavior("change"){

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        final String oidStr = breadcrumbChoice.getInput();
                        final EntityModel selectedModel = breadcrumbModel.lookup(oidStr);
                        if(selectedModel == null) {
                            final MessageBroker messageBroker = getIsisSessionFactory().getCurrentSession()
                                    .getAuthenticationSession().getMessageBroker();
                            messageBroker.addWarning("Cannot find object");
                            String feedbackMsg = JGrowlUtil.asJGrowlCalls(messageBroker);
                            target.appendJavaScript(feedbackMsg);
                            breadcrumbModel.remove(oidStr);
                            return;
                        }
                        setResponsePage(EntityPage.class, selectedModel.getPageParametersWithoutUiHints());
                    }
                });

        final Settings settings = breadcrumbChoice.getSettings();
        settings.setMinimumInputLength(0);
        settings.setWidth("100%");

        addOrReplace(breadcrumbChoice);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        boolean shouldShow = getConfiguration().getBoolean(SHOW_BREADCRUMBS_KEY, SHOW_BREADCRUMBS_DEFAULT);
        setVisible(shouldShow);
    }

}

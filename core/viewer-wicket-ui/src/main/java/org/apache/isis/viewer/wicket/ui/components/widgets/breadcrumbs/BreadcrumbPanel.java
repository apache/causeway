/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.select2.Response;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Settings;
import org.wicketstuff.select2.TextChoiceProvider;

import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class BreadcrumbPanel extends PanelAbstract<IModel<Void>> {

    private static final long serialVersionUID = 1L;
    
    private static final String ID_BREADCRUMBS = "breadcrumbs";

    public BreadcrumbPanel(String id) {
        super(id);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
        
        final IModel<EntityModel> entityModel = new Model<EntityModel>();
        TextChoiceProvider<EntityModel> choiceProvider = new TextChoiceProvider<EntityModel>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected String getDisplayText(EntityModel choice) {
                return breadcrumbModel.titleFor(choice);
            }

            @Override
            protected Object getId(EntityModel choice) {
                return breadcrumbModel.getId(choice);
            }

            @Override
            public void query(String term, int page, Response<EntityModel> response) {
                final List<EntityModel> breadCrumbList = Lists.newArrayList(breadcrumbModel.getList());
                final List<EntityModel> checkedList = Lists.newArrayList(
                        Iterables.filter(breadCrumbList, new Predicate<EntityModel>() {
                            @Override
                            public boolean apply(final EntityModel input) {
                                final Object id = getId(input);
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
                    setResponsePage(EntityPage.class, selectedModel.getPageParameters());
                }
            });
        
        final Settings settings = breadcrumbChoice.getSettings();
        settings.setMinimumInputLength(0);
        settings.setWidth("100%");
        
        addOrReplace(breadcrumbChoice);
    }

    
}

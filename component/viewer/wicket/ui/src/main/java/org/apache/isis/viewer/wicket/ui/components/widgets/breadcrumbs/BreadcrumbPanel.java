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
import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.Settings;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
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
        final Select2Choice<EntityModel> breadcrumbChoice = new Select2Choice<EntityModel>(ID_BREADCRUMBS, entityModel);

        breadcrumbChoice.add(
            new AjaxFormComponentUpdatingBehavior("change"){
    
                private static final long serialVersionUID = 1L;
    
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    final String oidStr = breadcrumbChoice.getInput();
                    final EntityModel selectedModel = breadcrumbModel.lookup(oidStr);
                    if(selectedModel == null) {
                        final MessageBroker messageBroker = IsisContext.getAuthenticationSession().getMessageBroker();
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
        
        breadcrumbChoice.setProvider(new TextChoiceProvider<EntityModel>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected String getDisplayText(EntityModel choice) {
                return breadcrumbModel.titleFor(choice);
            }

            @Override
            protected Object getId(EntityModel choice) {
                try {
                    return PageParameterNames.OBJECT_OID.getStringFrom(choice.getPageParameters());
                } catch(Exception ex) {
                    breadcrumbModel.remove(choice);
                    return null;
                }
            }

            @Override
            public void query(String term, int page, Response<EntityModel> response) {
                response.addAll(breadcrumbModel.getList());
            }

            @Override
            public Collection<EntityModel> toChoices(Collection<String> ids) {
                return breadcrumbModel.getList();
            }
            
        });
        addOrReplace(breadcrumbChoice);
    }

    
}

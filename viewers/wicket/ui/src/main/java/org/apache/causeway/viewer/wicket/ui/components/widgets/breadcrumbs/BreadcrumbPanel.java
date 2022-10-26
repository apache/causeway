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
package org.apache.causeway.viewer.wicket.ui.components.widgets.breadcrumbs;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Response;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Settings;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.causeway.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

public class BreadcrumbPanel
extends PanelAbstract<Void, IModel<Void>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_BREADCRUMBS = "breadcrumbs";

    public BreadcrumbPanel(final String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final BreadcrumbModel breadcrumbModel = _Casts.castTo(BreadcrumbModelProvider.class, getSession())
                .map(BreadcrumbModelProvider::getBreadcrumbModel)
                .orElseGet(()->new BreadcrumbModel(getMetaModelContext())); // for testing

        final IModel<UiObjectWkt> entityModel = new Model<>();
        ChoiceProvider<UiObjectWkt> choiceProvider = new ChoiceProvider<UiObjectWkt>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getDisplayValue(final UiObjectWkt choice) {
                return titleFor(choice);
            }

            private String titleFor(final UiObjectWkt model) {
                return model.getManagedObject().getTitle();
            }

            @Override
            public String getIdValue(final UiObjectWkt choice) {
                try {
                    final PageParameters pageParameters = choice.getPageParametersWithoutUiHints();
                    final String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);

                    return Bookmark.parse(oidStr)
                    .map(Bookmark::stringify)
                    .orElseGet(()->{
                        breadcrumbModel.remove(choice);
                        return null;
                    });

                } catch (Exception ex) {
                    breadcrumbModel.remove(choice);
                    return null;
                }
            }

            @Override
            public void query(final String term, final int page, final Response<UiObjectWkt> response) {
                final List<UiObjectWkt> breadCrumbList = _Lists.newArrayList(breadcrumbModel.getList());
                final List<UiObjectWkt> checkedList = _Lists.filter(breadCrumbList,
                        new Predicate<UiObjectWkt>() {
                            @Override
                            public boolean test(final UiObjectWkt input) {
                                final Object id = getIdValue(input);
                                return id != null;
                            }
                        });
                response.addAll(checkedList);
            }

            @Override
            public Collection<UiObjectWkt> toChoices(final Collection<String> ids) {
                return breadcrumbModel.getList();
            }

        };
        final Select2Choice<UiObjectWkt> breadcrumbChoice =
                new Select2Choice<>(ID_BREADCRUMBS, entityModel, choiceProvider);

        breadcrumbChoice.add(
                new AjaxFormComponentUpdatingBehavior("change"){

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(final AjaxRequestTarget target) {
                        final String oidStr = breadcrumbChoice.getInput();
                        final UiObjectWkt selectedModel = breadcrumbModel.lookup(oidStr);
                        if(selectedModel == null) {
                            val configuration = getMetaModelContext().getConfiguration();

                            getMetaModelContext().getMessageBroker()
                            .ifPresent(messageBroker->{
                                messageBroker.addWarning("Cannot find object");
                                String feedbackMsg = JGrowlUtil.asJGrowlCalls(messageBroker, configuration);
                                target.appendJavaScript(feedbackMsg);
                            });
                            breadcrumbModel.remove(oidStr);
                            return;
                        }
                        setResponsePage(EntityPage.class, selectedModel.getPageParametersWithoutUiHints());
                    }

                    private MetaModelContext getMetaModelContext() {
                        return BreadcrumbPanel.this.getMetaModelContext();
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

        boolean shouldShow = getWicketViewerSettings().getBookmarkedPages().isShowDropDownOnFooter();
        setVisible(shouldShow);
    }

}

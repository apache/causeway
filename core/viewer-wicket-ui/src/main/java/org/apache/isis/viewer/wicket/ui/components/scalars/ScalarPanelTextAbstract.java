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

package org.apache.isis.viewer.wicket.ui.components.scalars;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Fragment;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormExecutor;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Adapter for {@link PanelAbstract panel}s that use a {@link ScalarModel} as
 * their backing model.
 * 
 * <p>
 * Supports the concept of being {@link Rendering#COMPACT} (eg within a table) or
 * {@link Rendering#REGULAR regular} (eg within a form).
 */
public abstract class ScalarPanelTextAbstract extends ScalarPanelAbstract  {

    private static final long serialVersionUID = 1L;

    public enum CompactType {
        INPUT_CHECKBOX,
        SPAN
    }



    public ScalarPanelTextAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }


    protected Fragment getCompactFragment(CompactType type) {
        Fragment compactFragment;
        switch (type) {
            case INPUT_CHECKBOX:
                compactFragment = new Fragment("scalarIfCompact", "compactAsInputCheckbox", ScalarPanelTextAbstract.this);
                break;
            case SPAN:
            default:
                compactFragment = new Fragment("scalarIfCompact", "compactAsSpan", ScalarPanelTextAbstract.this);
                break;
        }
        return compactFragment;
    }




    protected void configureInlineEditCallback() {

        final PromptStyle editStyle = this.scalarModel.getPromptStyle();
        if(editStyle == PromptStyle.INLINE) {

            if(editInlineLink != null) {
                editInlineLink.add(new AjaxEventBehavior("click") {
                    @Override
                    protected void onEvent(final AjaxRequestTarget target) {

                        scalarModel.toEditMode();

                        // dynamically update the edit form.
                        final PropertyEditFormExecutor formExecutor =
                                new PropertyEditFormExecutor(ScalarPanelTextAbstract.this, scalarModel);
                        scalarModel.setFormExecutor(formExecutor);
                        scalarModel.setInlinePromptContext(
                                new ScalarModel.InlinePromptContext(
                                        getComponentForRegular(),
                                        scalarIfRegularInlinePromptForm));

                        switchFormForInlinePrompt();

                        getComponentForRegular().setVisible(false);
                        scalarIfRegularInlinePromptForm.setVisible(true);

                        target.add(ScalarPanelTextAbstract.this);
                    }

                    @Override
                    public boolean isEnabled(final Component component) {
                        return true;
                    }
                });
            }
        }
    }


}

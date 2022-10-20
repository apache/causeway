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
package org.apache.causeway.viewer.wicket.ui.panels;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.ui.components.widgets.formcomponent.FormFeedbackPanel;
import org.apache.causeway.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Form with <i>Feedback</i> panel and <i>Ok/Cancel</i> buttons.
 */
public abstract class OkCancelForm<T extends IModel<ManagedObject>>
extends FormAbstract<ManagedObject>{

    private static final long serialVersionUID = 1L;
    private static final String ID_OK_BUTTON = "okButton";
    public  static final String ID_CANCEL_BUTTON = "cancelButton";
    private static final String ID_FEEDBACK = "feedback";

    protected final AjaxButton okButton;
    protected final AjaxButton cancelButton;

    protected OkCancelForm(final String id, final IModel<ManagedObject> model) {
        super(id, model);
        okButton = Wkt.buttonAddOk(this, ID_OK_BUTTON, ()->translate("OK"), getWicketViewerSettings(), this::onOkSubmitted);
        cancelButton = Wkt.buttonAdd(this, ID_CANCEL_BUTTON, ()->translate("Cancel"), (button, target)->{
            onCancelSubmitted(target);
        });
        configureOkButton(okButton);
        configureCancelButton(cancelButton);
        setDefaultButton(okButton);
        Wkt.add(this, new FormFeedbackPanel(ID_FEEDBACK));
        setOutputMarkupId(true);
    }

    protected abstract void onOkSubmitted(AjaxButton okButton, AjaxRequestTarget target);
    protected abstract void onCancelSubmitted(AjaxRequestTarget target);

    protected void configureOkButton(final AjaxButton okButton) {
        okButton.add(new JGrowlBehaviour(getMetaModelContext()));
    }

    protected void configureCancelButton(final AjaxButton cancelButton) {
        // so can submit with invalid content (eg mandatory params missing)
        cancelButton.setDefaultFormProcessing(false);
    }

    // workaround for https://issues.apache.org/jira/browse/WICKET-6364
    @Override
    protected final void appendDefaultButtonField() {
        AppendingStringBuffer buffer = new AppendingStringBuffer();
        buffer.append(
                "<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">");
        buffer.append("<input type=\"text\" tabindex=\"-1\" autocomplete=\"off\"/>");
        Component submittingComponent = this.defaultSubmittingComponent();
        buffer.append("<input type=\"submit\" tabindex=\"-1\" name=\"");
        buffer.append(this.defaultSubmittingComponent().getInputName());
        buffer.append("\" onclick=\" var b=document.getElementById(\'");
        buffer.append(submittingComponent.getMarkupId());
        buffer.append("\'); if (b!=null&amp;&amp;b.onclick!=null&amp;&amp;typeof(b.onclick) != \'undefined\') {  "
                + "var r = Wicket.bind(b.onclick, b)(); if (r != false) b.click(); } else { b.click(); };  return false;\" ");
        buffer.append(" />");
        buffer.append("</div>");
        this.getResponse().write(buffer);
    }

    // -- HELPER

    private AjaxButton defaultSubmittingComponent() {
        return okButton;
    }

}

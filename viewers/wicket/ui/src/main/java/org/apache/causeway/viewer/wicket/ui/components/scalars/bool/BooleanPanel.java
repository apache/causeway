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
package org.apache.causeway.viewer.wicket.ui.components.scalars.bool;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.models.BooleanModel;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelFormFieldAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.PromptFragment;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxX;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxXConfig;

/**
 * Panel for rendering scalars of type {@link Boolean} or <tt>boolean</tt>.
 */
public class BooleanPanel
extends ScalarPanelFormFieldAbstract<Boolean> {

    private static final long serialVersionUID = 1L;

    private CheckBoxX checkBox;

    public BooleanPanel(final String id, final PopModel popModel) {
        super(id, popModel, Boolean.class);
    }

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.of(InputFragment.CHECKBOX);
    }

    @Override
    protected FormComponent<Boolean> createFormComponent(final String id, final PopModel popModel) {
        checkBox = Wkt.checkboxX(
                id,
                BooleanModel.forPopModel(popModel),
                popModel.isRequired(),
                CheckBoxXConfig.Sizes.xl);
        return checkBox;
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        Boolean b = (Boolean) popModel().getObject().getPojo();
        if(b==null
                && popModel().isRequired()) {
            b = false;
        }
        return getRenderScenario().isCompact()
                ? CompactFragment.createCheckboxFragment(id, this, b)
                : PromptFragment.createCheckboxFragment(id, this, b);
    }

    @Override
    protected UiString obtainOutputFormat() {
        throw _Exceptions.unexpectedCodeReach(); // not used in createComponentForOutput(...)
    }

    @Override
    protected void onInitializeEditable() {
        super.onInitializeEditable();
        if(checkBox==null) return;
        checkBox.setEnabled(true);
    }

    @Override
    protected void onInitializeNotEditable() {
        super.onInitializeNotEditable();
        if(checkBox==null) return;
        checkBox.setEnabled(false);
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        super.onInitializeReadonly(disableReason);
        if(checkBox==null) return;
        checkBox.setEnabled(false);
        Wkt.attributeReplace(checkBox, "title", disableReason);
    }

    @Override
    protected void onMakeNotEditable(final String disableReason) {
        super.onMakeNotEditable(disableReason);
        if(checkBox==null) return;
        checkBox.setEnabled(false);
        Wkt.attributeReplace(checkBox, "title", disableReason);
    }

    @Override
    protected void onMakeEditable() {
        super.onMakeEditable();
        if(checkBox==null) return;
        checkBox.setEnabled(true);
    }

    @Override
    public String getVariation() {
//        val labelAtFacet = getModel().getFacet(LabelAtFace~t.class);
//        return labelAtFacet != null
//                && labelAtFacet.label() == LabelPosition.RIGHT
//            ? "labelRightPosition"
//            : super.getVariation();
        return super.getVariation();
    }

}

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
package org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract.PlaceholderLiteral;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.commons.model.components.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormComponentPanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * {@link FormComponentPanel} representing a reference to an entity: a link and
 * (optionally) an auto-complete field.
 */
public class EntityLinkSimplePanel
extends FormComponentPanelAbstract<ManagedObject>
implements CancelHintRequired  {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_ICON_AND_TITLE = "entityIconAndTitle";
    private static final String ID_ENTITY_TITLE_NULL = "entityTitleNull";

    public EntityLinkSimplePanel(final String id, final IModel<ManagedObject> model) {
        super(id, model);
        _Assert.assertTrue(model instanceof HasCommonContext);
        setType(ManagedObject.class);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {

        val objectModelForLink = getModel();
        val isEmpty = ManagedObjects.isNullOrUnspecifiedOrEmpty(objectModelForLink.getObject());

        if(isEmpty) {
            // represent null reference by a simple markup displaying '(none)'
            Wkt.markupAdd(this, ID_ENTITY_TITLE_NULL,
                    PlaceholderLiteral.NULL_REPRESENTATION.asHtml(this::translate));
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);

        } else {

            val componentFactory = getComponentFactoryRegistry()
                    .findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, objectModelForLink);

            final Component component = componentFactory
                    .createComponent(ID_ENTITY_ICON_AND_TITLE, objectModelForLink);
            addOrReplace(component);
            permanentlyHide(ID_ENTITY_TITLE_NULL);
        }
    }

    @Override
    public FormComponent<ManagedObject> setModelObject(final ManagedObject object) {
        // no-op since immutable
        return this;
    }

    @Override
    public void updateModel() {
        // no-op since immutable
    }

    @Override
    public void onCancel() {
     // no-op since immutable
    }

    @Override
    public void validate() {
        // no-op since immutable
    }

    // -- TRANSLATION

    /**
     * Translate without context: Tooltips, Button-Labels, etc.
     */
    public final String translate(final String input) {
        return ((HasCommonContext)getModel()).getCommonContext().getTranslationService()
                .translate(TranslationContext.empty(), input);
    }


}

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
package org.apache.causeway.viewer.wicket.ui.components.widgets.entitysimplelink;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbbreviationOptions;
import org.apache.causeway.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.causeway.viewer.wicket.ui.components.widgets.formcomponent.FormComponentPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * {@link FormComponentPanel} representing a reference to an entity: a link and
 * (optionally) an auto-complete field.
 */
public class EntityLinkSimplePanel
extends FormComponentPanelAbstract<ManagedObject>
implements CancelHintRequired, HasMetaModelContext  {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_ICON_AND_TITLE = "entityIconAndTitle";
    private static final String ID_ENTITY_TITLE_NULL = "entityTitleNull";

    public EntityLinkSimplePanel(final String id, final IModel<ManagedObject> model) {
        super(id, model);
        _Assert.assertTrue(model instanceof HasMetaModelContext);
        setType(ManagedObject.class);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {

        var objectModelForLink = getModel();
        var isEmpty = ManagedObjects.isNullOrUnspecifiedOrEmpty(objectModelForLink.getObject());

        if(isEmpty) {
            // represent null reference by a simple markup displaying '(none)'
            Wkt.markupAdd(this, ID_ENTITY_TITLE_NULL,
                    getPlaceholderRenderService().asHtml(PlaceholderLiteral.NULL_REPRESENTATION));
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);

        } else {

            var componentFactory = getComponentFactoryRegistry()
                    .findComponentFactory(UiComponentType.ENTITY_ICON_AND_TITLE, objectModelForLink);

            final Component component = componentFactory
                    .createComponent(ID_ENTITY_ICON_AND_TITLE, objectModelForLink);

            // propagate options (if any) from this component to the child component, that's using them
            ColumnAbbreviationOptions.lookupIn(this)
                .ifPresent(opts->opts.applyTo(component));

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
        return getTranslationService()
                .translate(TranslationContext.empty(), input);
    }

}

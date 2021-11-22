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
package demoapp.webapp.wicket.common.ui.custom;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.ByteArrayResource;

import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.SneakyThrows;
import lombok.val;

import demoapp.dom.featured.customui.geocoding.GeoapifyClient;
import demoapp.dom.featured.customui.vm.WhereInTheWorldVm;

//tag::class[]
public class WhereInTheWorldPanel
extends PanelAbstract<ManagedObject, EntityModel>  {

    private static final long serialVersionUID = 1L;    // <.>

    private final GeoapifyClient geoapifyClient;        // <.>

    public WhereInTheWorldPanel(
            final String id,
            final EntityModel model,
            final GeoapifyClient geoapifyClient) {
        super(id, model);
        this.geoapifyClient = geoapifyClient;
    }
    // ...
//end::class[]


    @Override
    public UiHintContainer getUiHintContainer() {
        // disables hinting by this component
        return null;
    }

//tag::onInitialize[]
    @Override
    public void onInitialize() {
        super.onInitialize();

        val managedObject = getModel().getObject();;                       // <.>
        val customUiVm = (WhereInTheWorldVm) managedObject.getPojo();      // <.>

        val latitude = new Label("latitude", customUiVm.getLatitude());    // <.>
        val longitude = new Label("longitude", customUiVm.getLongitude()); // <.>
        val address = new Label("address", customUiVm.getAddress());       // <.>

        val map = createMapComponent("map", customUiVm);                   // <.>

        val sourcesComponent = createPropertyComponent("sources");         // <.>
        val descriptionComponent = createPropertyComponent("description"); // <.>

        addOrReplace(
                latitude, longitude, address, map,
                sourcesComponent, descriptionComponent);                   // <.>
    }
//end::onInitialize[]

//tag::createMapComponent[]
    @SneakyThrows
    private Image createMapComponent(final String id, final WhereInTheWorldVm vm)  {
        val bytes = geoapifyClient.toJpeg(
                        vm.getLatitude(), vm.getLongitude(), vm.getZoom());  // <.>
        return new Image(id, new ByteArrayResource("image/jpeg", bytes));    // <.>
    }
//end::createMapComponent[]

//tag::createPropertyComponent[]
    private Component createPropertyComponent(final String propertyId) {
        val managedObject = getModel().getManagedObject();
        val spec = managedObject.getSpecification();                               // <.>
        val property = spec.getPropertyElseFail(propertyId);                       // <.>
        //val pm = otoa.getMemento();                                              // <.>

        val scalarModel =
                getModel().getPropertyModel(                                       // <.>
                    property, ScalarRepresentation.VIEWING,
                    ObjectUiModel.RenderingHint.REGULAR);
        return getComponentFactoryRegistry().createComponent(                      // <.>
                ComponentType.SCALAR_NAME_AND_VALUE, propertyId, scalarModel);
    }
//end::createPropertyComponent[]

//tag::class[]
}
//end::class[]

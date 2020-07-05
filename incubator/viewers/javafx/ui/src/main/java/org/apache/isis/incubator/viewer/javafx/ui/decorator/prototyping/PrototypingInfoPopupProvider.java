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
package org.apache.isis.incubator.viewer.javafx.ui.decorator.prototyping;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Component;

import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.dialog.Dialogs;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingUiModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PrototypingInfoPopupProvider {

    private final Provider<UiComponentFactoryFx> uiComponentFactory;

    public void showPrototypingPopup(final PrototypingUiModel prototypingUiModel) {
        val infoNode = getPrototypingInfoUiComponent(prototypingUiModel);
        val headerText = prototypingUiModel.getFeatureFullLabel();
        val contentText = prototypingUiModel.getFeatureType().toString();
        Dialogs.message("Inspect Metamodel", headerText, contentText, infoNode);
    }

    // -- HELPER
    
    @Value(staticConstructor = "of")
    private static class Info implements Comparable<Info> {
        private final String key;
        private final String value;
        @Override
        public int compareTo(Info other) {
            return this.getKey().compareTo(other.getKey());
        }
    }
    
    private Node getPrototypingInfoUiComponent(final PrototypingUiModel prototypingUiModel) {

        val infos = _Sets.<Info>newTreeSet();
        
        val handlerInfo = (String)uiComponentFactory.get().getRegisteredHandlers()
                .stream()
                .map(Class::getSimpleName)
                .map(handlerName->" • " + handlerName)
                .collect(Collectors.joining("\n"));

        infos.add(Info.of("Handlers", handlerInfo));

        prototypingUiModel.streamFeatureFacets()
        .forEach(facet -> 
            infos.add(Info.of(
                    facet.facetType().getSimpleName(), 
                    summarize(facet))));

        val detailPane = new VBox();
        TableView<Info> tableView = _fx.add(detailPane, new TableView<Info>());
        
        TableColumn<Info, String> column1 = new TableColumn<>("Key");
        column1.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getKey()));

        TableColumn<Info, String> column2 = new TableColumn<>("Value");
        column2.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getValue()));

        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        
        infos.forEach(tableView.getItems()::add);
        
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        //TODO this is a candidate to be moved to _fx, to also account for max screen sizes
        val scrollPane = new ScrollPane(detailPane);
        scrollPane.fitToWidthProperty().set(true);
        scrollPane.fitToHeightProperty().set(true);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(800);
        scrollPane.setMaxHeight(800);
        scrollPane.setPrefWidth(1200);
        
        tableView.prefHeightProperty().bind(scrollPane.heightProperty());
        
        return scrollPane;
    }

    private String summarize(Facet facet) {
        val sb = new StringBuilder();
        sb.append(facet.getClass().getSimpleName());
        if(facet instanceof FacetAbstract) {
            val attributeMap = _Maps.<String, Object>newTreeMap();
            ((FacetAbstract)facet).appendAttributesTo(attributeMap);
            attributeMap.forEach((k, v)->{
                sb.append("\n • ").append(k).append(": ").append(v);    
            });
        }
        return sb.toString();
    }


}

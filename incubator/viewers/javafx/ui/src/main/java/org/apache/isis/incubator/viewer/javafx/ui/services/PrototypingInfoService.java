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
package org.apache.isis.incubator.viewer.javafx.ui.services;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.viewer.common.model.debug.DebugUiModel;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.val;

@Service
public class PrototypingInfoService {

    @Inject private Provider<UiComponentFactoryFx> uiComponentFactory;

    public Node getPrototypingInfoUiComponent(ManagedMember managedMember) {

        final FacetHolder facetHolder = (managedMember instanceof ManagedAction)
                ? ((ManagedAction) managedMember).getAction()
                : managedMember.getSpecification();
    
        val debugUiModel = DebugUiModel.of(managedMember.getId())
//                        .withProperty("ObjectFeature.specification.fullIdentifier",  spec.getFullIdentifier())
                        .withProperty("ObjectFeature.identifier",  managedMember.getIdentifier().toString());

        val handlerInfo = uiComponentFactory.get().getRegisteredHandlers()
                .stream()
                .map(Class::getSimpleName)
                .map(handlerName->" • " + handlerName)
                .collect(Collectors.joining("\n"));

        debugUiModel.withProperty("Handlers", handlerInfo);

        facetHolder.streamFacets()
        .forEach(facet -> {
            debugUiModel.withProperty(
                    facet.facetType().getSimpleName(), 
                    summarize(facet));
        });

        val detailPane = new VBox();
        _fx.add(detailPane, _fx.h2(new Label(debugUiModel.getSummaryText())));
        
        debugUiModel.getKeyValuePairs().forEach((k, v)->{
            _fx.add(detailPane, new Label(k));
            val text = _fx.add(detailPane, new TextArea(v));
            val prefHeight = 16*(1+(int)_Strings.splitThenStream(v, "\n").count());
            text.setPrefHeight(prefHeight);
            text.setWrapText(true);
            text.setEditable(false);
            text.autosize();
        });
        
        val scrollPane = new ScrollPane(detailPane);
        scrollPane.fitToWidthProperty().set(true);
        scrollPane.fitToHeightProperty().set(true);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPrefHeight(800);
        scrollPane.setMaxHeight(800);
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

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
package org.apache.isis.incubator.viewer.vaadin.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.facets.value.blobs.BlobValueFacet;
import org.apache.isis.core.metamodel.facets.value.clobs.ClobValueFacet;
import org.apache.isis.core.metamodel.facets.value.string.StringValueFacet;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentMapperVaa.Request;
import org.apache.isis.incubator.viewer.vaadin.ui.components.blob.BlobField;
import org.apache.isis.incubator.viewer.vaadin.ui.components.debug.DebugField;
import org.apache.isis.incubator.viewer.vaadin.ui.components.debug.DebugUiModel;

import lombok.Getter;
import lombok.val;

final class UiComponentMapperVaa_builtinHandlers {
    
    @Getter(lazy = true) private final static BlobHandler blob = new BlobHandler();
    @Getter(lazy = true) private final static ClobHandler clob = new ClobHandler();
    @Getter(lazy = true) private final static TextHandler text = new TextHandler();
    @Getter(lazy = true) private final static Other other = new Other();

    // -- HANDLER CLASSES
    
    final static class BlobHandler implements UiComponentMapperVaa.Handler {

        @Override
        public boolean isHandling(Request request) {
            val spec = request.getObjectFeature().getSpecification();
            return spec.getFacet(BlobValueFacet.class)!=null;
        }

        @Override
        public Component handle(Request request) {
            val uiField = new BlobField(request.getObjectFeature().getName());
            uiField.setValue((Blob)request.getManagedObject().getPojo()); // pojo is nullable!
            return uiField;
        }
        
    }
    
    final static class ClobHandler implements UiComponentMapperVaa.Handler {

        @Override
        public boolean isHandling(Request request) {
            val spec = request.getObjectFeature().getSpecification();
            return spec.getFacet(ClobValueFacet.class)!=null;
        }

        @Override
        public Component handle(Request request) {
            val clob = (Clob)request.getManagedObject().getPojo();
            
            val uiField = new TextField(request.getObjectFeature().getName());
            uiField.setValue("Clob type not handled yet: " + clob);
            uiField.setEnabled(false);
            return uiField;
        }

    }

    final static class TextHandler implements UiComponentMapperVaa.Handler {

        @Override
        public boolean isHandling(Request request) {
            val spec = request.getObjectFeature().getSpecification();
            return spec.getFacet(StringValueFacet.class)!=null;
        }

        @Override
        public Component handle(Request request) {
            val uiField = new TextField(request.getObjectFeature().getName());
            uiField.setValue(request.getManagedObject().titleString(null));
            return uiField;
        }
        
    }
    
    final static class Other implements UiComponentMapperVaa.Handler {

        @Override
        public boolean isHandling(Request request) {
            return true; // the last handler in the chain
        }

        @Override
        public Component handle(Request request) {
            
            val spec = request.getObjectFeature().getSpecification();
            
            val debugUiModel = DebugUiModel.of("type not handled")
            .withProperty("ObjectFeature.specification.fullIdentifier",  spec.getFullIdentifier())
            .withProperty("ObjectFeature.identifier",  request.getObjectFeature().getIdentifier().toString());
            
            spec.streamFacets()
            .forEach(facet -> {
                debugUiModel.withProperty(facet.facetType().getSimpleName(), facet.getClass().getSimpleName());
            });
            
            
            val uiField = new DebugField(request.getObjectFeature().getName());
            uiField.setValue(debugUiModel);
            return uiField;
        }


    }
    
    
}

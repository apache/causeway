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
package org.apache.causeway.valuetypes.vega.ui.wkt.components;

import java.util.Optional;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.valuetypes.vega.applib.value.Vega;
import org.apache.causeway.valuetypes.vega.ui.wkt.components.js.VegaEmbedJsReference;
import org.apache.causeway.valuetypes.vega.ui.wkt.components.js.VegaJsReference;
import org.apache.causeway.valuetypes.vega.ui.wkt.components.js.VegaLiteJsReference;
import org.apache.causeway.viewer.wicket.ui.components.scalars.markup.MarkupComponent;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VegaComponentWkt extends MarkupComponent {

    private static final long serialVersionUID = 1L;

    public VegaComponentWkt(final String id, final IModel<?> model){
        super(id, model,
                org.apache.causeway.viewer.wicket.ui.components.scalars.markup.MarkupComponent.Options.builder()
                .syntaxHighlighter(SyntaxHighlighter.NONE)
                .build());
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        vegaSchema()
        .ifPresent(vegaSchema->{
            if(vegaSchema.isVega()) {
                response.render(VegaJsReference.asHeaderItem());
            }
            else
            if(vegaSchema.isVegaLite()) {
                response.render(VegaJsReference.asHeaderItem());
                response.render(VegaLiteJsReference.asHeaderItem());
                response.render(VegaEmbedJsReference.asHeaderItem());
            }
        });
    }

    // -- HELPER

    private Optional<Vega.Schema> vegaSchema() {
        val modelObject = getDefaultModelObject();
        if(modelObject==null) {
            return Optional.empty();
        }
        if(!(modelObject instanceof ManagedObject)) {
            log.error("framework bug: unexpected type {}", modelObject.getClass().getName());
            return Optional.empty();
        }
        return _Casts.castTo(Vega.class, MmUnwrapUtil.single((ManagedObject)modelObject))
                .map(Vega::getSchema);
    }

}

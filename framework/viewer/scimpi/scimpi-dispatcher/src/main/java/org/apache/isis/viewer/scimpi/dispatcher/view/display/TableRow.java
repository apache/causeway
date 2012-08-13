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

package org.apache.isis.viewer.scimpi.dispatcher.view.display;

import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request.RepeatMarker;

public class TableRow extends AbstractElementProcessor {

    @Override
    public String getName() {
        return "table-row";
    }

    @Override
    public void process(final Request request) {
        final TableBlock block = (TableBlock) request.getBlockContent();
        
        final RepeatMarker start = request.createMarker();
        block.setMarker(start);
        
        final String linkView = request.getOptionalProperty(LINK_VIEW);
        if (linkView != null) {
            block.setlinkView(linkView);
            block.setlinkName(request.getOptionalProperty(LINK_NAME, RequestContext.RESULT));
        }
        
        request.skipUntilClose();
    }
}

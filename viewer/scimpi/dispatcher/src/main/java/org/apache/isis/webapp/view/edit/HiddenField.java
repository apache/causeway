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


package org.apache.isis.webapp.view.edit;

import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.BlockContent;
import org.apache.isis.webapp.TagOrderException;
import org.apache.isis.webapp.processor.Request;


public class HiddenField extends AbstractElementProcessor {

    public void process(Request request) {
        BlockContent blockContent = request.getBlockContent();
        if (!(blockContent instanceof EditFieldBlock)) {
            throw new TagOrderException(request);
        }

        String field = request.getOptionalProperty("name");
        String value = request.getRequiredProperty("value");
        EditFieldBlock block = (EditFieldBlock) blockContent;
        block.value(field, value);
        block.exclude(field);
    }
    
    public String getName() {
        return "hidden-field";
    }

}



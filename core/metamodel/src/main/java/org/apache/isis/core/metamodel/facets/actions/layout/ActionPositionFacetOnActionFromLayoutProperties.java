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

package org.apache.isis.core.metamodel.facets.actions.layout;

import java.util.Properties;
import com.google.common.base.Strings;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacetAbstract;

public class ActionPositionFacetOnActionFromLayoutProperties extends ActionPositionFacetAbstract {

    public static ActionPositionFacet create(Properties properties, FacetHolder holder) {
        final ActionLayout.Position position = position(properties);
        return position != null? new ActionPositionFacetOnActionFromLayoutProperties(position, holder): null;
    }

    private ActionPositionFacetOnActionFromLayoutProperties(ActionLayout.Position position, FacetHolder holder) {
        super(position, holder);
    }

    private static ActionLayout.Position position(Properties properties) {
        if(properties == null) {
            return null;
        }
        String position = Strings.emptyToNull(properties.getProperty("position"));
        if(position == null) {
            position = Strings.emptyToNull(properties.getProperty("actionPosition"));
        }
        if(position == null) {
            return null;
        }
        return ActionLayout.Position.valueOf(position);
    }

}

/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.wicketstuff.select2.Select2Choice;

public final class Select2ChoiceUtil  {

    private Select2ChoiceUtil(){}

    // a guesstimate to convert a single character into 'em' units
    private static final double CHAR_TO_EM_MULTIPLIER = 0.8;
    
    // a further fudge, add some additional characters prior to multiplication.
    private static final int ADDITIONAL_CHARS = 3;
    
    public static Select2Choice<ObjectAdapterMemento> newSelect2Choice(String id, final IModel<ObjectAdapterMemento> modelObject, ScalarModel scalarModel) {
        Select2Choice<ObjectAdapterMemento> select2Choice = new Select2Choice<ObjectAdapterMemento>(id, modelObject);
        int typicalLength = scalarModel.getTypicalLength();
        select2Choice.add(new AttributeAppender("style", asCssStyleWidth(typicalLength)));
        select2Choice.setRequired(scalarModel.isRequired());
        return select2Choice;
    }

    private static String asCssStyleWidth(int numChars) {
        return "width: " + ((numChars+ADDITIONAL_CHARS) * CHAR_TO_EM_MULTIPLIER) + "em;";
    }

}

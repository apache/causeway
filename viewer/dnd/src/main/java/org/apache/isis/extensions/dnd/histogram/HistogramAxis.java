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


package org.apache.isis.extensions.dnd.histogram;

import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ViewAxis;
import org.apache.isis.extensions.dnd.view.collection.CollectionContent;


class HistogramAxis implements ViewAxis {
    private ObjectAssociation fields[];
    private double maxValues[];
    private int noBars;

    public HistogramAxis(Content content) {
        List<? extends ObjectAssociation> associationList = HistogramSpecification
                .availableFields((CollectionContent) content);
        noBars = associationList.size();
        fields = new ObjectAssociation[noBars];
        maxValues = new double[noBars];
        int i = 0;
        for (ObjectAssociation association : associationList) {
            fields[i++] = association;
        }
    }

    public double getLengthFor(Content content, int fieldNo) {
        return NumberAdapters.doubleValue(fields[fieldNo],  fields[fieldNo].get(content.getAdapter())) / maxValues[fieldNo];
    }

    public void determineMaximum(Content content) {
        int i = 0;
        for (ObjectAssociation field : fields) {
            maxValues[i] = 0;
            CollectionFacet collectionFacet = content.getAdapter().getSpecification().getFacet(CollectionFacet.class);
            for (ObjectAdapter element : collectionFacet.iterable(content.getAdapter())) {
                ObjectAdapter value = field.get(element);
                double doubleValue = NumberAdapters.doubleValue(field, value);
                maxValues[i] = Math.max(maxValues[i], doubleValue);
            }
            i++;
        }
    }

    public int getNoBars() {
        return noBars;
    }
}


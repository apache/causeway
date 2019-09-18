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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.summary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using as a table of summary values with a
 * chart alongside.
 */
public class CollectionContentsAsSummary extends PanelAbstract<EntityCollectionModel> implements CollectionCountProvider {

    private static final String ID_MAX = "max";

    private static final String ID_MIN = "min";

    private static final String ID_AVG = "avg";

    private static final String ID_SUM = "sum";

    private static final String ID_PROPERTY_NAME = "propertyName";

    private static final String ID_REPEATING_SUMMARY = "repeatingSummary";

    private static final long serialVersionUID = 1L;

    private static final String ID_FEEDBACK = "feedback";

    public CollectionContentsAsSummary(final String id, final EntityCollectionModel model) {
        super(id, model);

        buildGui();
    }

    private void buildGui() {

        final EntityCollectionModel model = getModel();

        final ObjectSpecification elementSpec = model.getTypeOfSpecification();

        final NotificationPanel feedback = new NotificationPanel(ID_FEEDBACK);
        feedback.setOutputMarkupId(true);
        addOrReplace(feedback);

        final Stream<ObjectAssociation> numberAssociations = elementSpec
                .streamAssociations(Contributed.EXCLUDED)
                .filter(CollectionContentsAsSummaryFactory.OF_TYPE_BIGDECIMAL);

        final RepeatingView repeating = new RepeatingView(ID_REPEATING_SUMMARY);
        addOrReplace(repeating);

        numberAssociations.forEach(numberAssociation->{
            AbstractItem item = new AbstractItem(repeating.newChildId());

            repeating.add(item);

            String propertyName = numberAssociation.getName();
            item.add(new Label(ID_PROPERTY_NAME, new Model<String>(propertyName)));


            List<ObjectAdapter> adapters = model.getObject();
            Summary summary = new Summary(propertyName, adapters, numberAssociation);
            addItem(item, ID_SUM, summary.getTotal());
            addItem(item, ID_AVG, summary.getAverage());
            addItem(item, ID_MIN, summary.getMin());
            addItem(item, ID_MAX, summary.getMax());
        });

    }

    public static class Summary {

        private BigDecimal sum = BigDecimal.ZERO;
        private BigDecimal min = null;
        private BigDecimal max = null;
        private final List<String> titles = _Lists.newArrayList();
        private final List<BigDecimal> values = _Lists.newArrayList();
        private BigDecimal average;
        private String propertyName;

        public Summary(List<ObjectAdapter> adapters, ObjectAssociation numberAssociation) {
            this(null, adapters, numberAssociation);
        }

        public Summary(String propertyName, List<ObjectAdapter> adapters, ObjectAssociation numberAssociation) {
            this.propertyName = propertyName;
            int nonNullCount = 0;
            for (ObjectAdapter objectAdapter : adapters) {
                titles.add(objectAdapter.titleString(null));
                final ObjectAdapter valueAdapter = numberAssociation.get(objectAdapter, InteractionInitiatedBy.USER);
                if (valueAdapter == null) {
                    values.add(null);
                    continue;
                }
                final Object valueObj = ObjectAdapter.Util.unwrapPojo(valueAdapter);
                if (valueObj == null) {
                    values.add(null);
                    continue;
                }

                nonNullCount++;
                BigDecimal value = (BigDecimal) valueObj;
                sum = sum.add(value);
                min = min != null && min.compareTo(value) < 0 ? min : value;
                max = max != null && max.compareTo(value) > 0 ? max : value;
                values.add(value);
            }
            average = nonNullCount != 0 ? sum.divide(BigDecimal.valueOf(nonNullCount), 2, RoundingMode.HALF_UP) : null;
        }

        public String getPropertyName() {
            return propertyName;
        }
        public BigDecimal getTotal() {
            return sum;
        }
        public BigDecimal getAverage() {
            return average;
        }
        public BigDecimal getMax() {
            return max;
        }
        public BigDecimal getMin() {
            return min;
        }
        public List<String> getTitles() {
            return Collections.unmodifiableList(titles);
        }
        public List<BigDecimal> getValues() {
            return Collections.unmodifiableList(values);
        }
        public List<Number> getValuesAsNumbers() {
            return asNumbers(getValues());
        }

        private static List<Number> asNumbers(List<BigDecimal> values) {
            return _Lists.newArrayList(Iterables.transform(values, BIGDECIMAL_TO_NUMBER));
        }

        private static final com.google.common.base.Function<BigDecimal, Number> BIGDECIMAL_TO_NUMBER = new com.google.common.base.Function<BigDecimal, Number>(){
            @Override
            public Number apply(BigDecimal value) {
                return value;
            }
        };


    }

    private void addItem(AbstractItem item, String id, BigDecimal amt) {
        TextField<String> textField = new TextField<String>(id, new Model<String>(format(amt)));
        item.add(textField);
    }

    private String format(BigDecimal amt) {
        return amt != null ? amt.setScale(2, RoundingMode.HALF_UP).toPlainString() : "";
    }

    @Override
    protected void onModelChanged() {
        buildGui();
    }

    @Override
    public Integer getCount() {
        final EntityCollectionModel model = getModel();
        return model.getCount();
    }

}

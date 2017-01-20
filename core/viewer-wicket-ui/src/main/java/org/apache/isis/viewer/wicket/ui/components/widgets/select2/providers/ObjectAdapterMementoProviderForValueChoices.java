package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

public class ObjectAdapterMementoProviderForValueChoices
        extends ObjectAdapterMementoProviderAbstract {

    private static final long serialVersionUID = 1L;
    private final List<ObjectAdapterMemento> choicesMementos;

    public ObjectAdapterMementoProviderForValueChoices(
            final ScalarModel scalarModel,
            final List<ObjectAdapterMemento> choicesMementos,
            final WicketViewerSettings wicketViewerSettings) {
        super(scalarModel, wicketViewerSettings);
        this.choicesMementos = choicesMementos;
    }

    @Override
    protected List<ObjectAdapterMemento> obtainMementos(String term) {
        return obtainMementos(term, choicesMementos);
    }

    public List<ObjectAdapterMemento> getChoicesMementos() {
        return choicesMementos;
    }

    @Override
    public Collection<ObjectAdapterMemento> toChoices(final Collection<String> ids) {
        final List<ObjectAdapterMemento> mementos = obtainMementos(null);

        final Predicate<ObjectAdapterMemento> lookupOam = new Predicate<ObjectAdapterMemento>() {
            @Override
            public boolean apply(ObjectAdapterMemento input) {
                final String id = (String) getId(input);
                return ids.contains(id);
            }
        };
        return Lists.newArrayList(FluentIterable.from(mementos).filter(lookupOam).toList());
    }


}

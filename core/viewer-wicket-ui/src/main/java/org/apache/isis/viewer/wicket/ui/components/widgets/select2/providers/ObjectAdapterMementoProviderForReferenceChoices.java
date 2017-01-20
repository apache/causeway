package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

public class ObjectAdapterMementoProviderForReferenceChoices
        extends ObjectAdapterMementoProviderAbstract {

    private static final long serialVersionUID = 1L;
    private final List<ObjectAdapterMemento> choiceMementos;

    public ObjectAdapterMementoProviderForReferenceChoices(
            final ScalarModel model,
            final WicketViewerSettings wicketViewerSettings,
            final List<ObjectAdapterMemento> choiceMementos) {
        super(model, wicketViewerSettings);
        this.choiceMementos = choiceMementos;
    }

    @Override
    protected List<ObjectAdapterMemento> obtainMementos(String term) {
        return obtainMementos(term, choiceMementos);
    }

    public List<ObjectAdapterMemento> getChoiceMementos() {
        return choiceMementos;
    }

    @Override
    public Collection<ObjectAdapterMemento> toChoices(final Collection<String> ids) {
        final Function<String, ObjectAdapterMemento> function = new Function<String, ObjectAdapterMemento>() {

            @Override
            public ObjectAdapterMemento apply(final String input) {
                if(NULL_PLACEHOLDER.equals(input)) {
                    return null;
                }
                final RootOid oid = RootOid.deString(input);
                return ObjectAdapterMemento.createPersistent(oid);
            }
        };
        return Collections2.transform(ids, function);
    }


}

package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * For custom {@link ScalarPanelTextFieldAbstract}s to use as the {@link Model}
 * of their {@link TextField} (as constructed in {@link ScalarPanelTextFieldAbstract#createTextField()}).
 */
public class TextFieldValueModel<T extends Serializable> extends Model<T> {
    
    private static final long serialVersionUID = 1L;
    
    public interface ScalarModelProvider {
        ScalarModel getModel();
        AdapterManager getAdapterManager();
    }
    
    private final ScalarModelProvider scalarModelProvider;
    
    public TextFieldValueModel(ScalarModelProvider scalarModelProvider) {
        this.scalarModelProvider = scalarModelProvider;
    }

    @Override
    public T getObject() {
        final ScalarModel model = scalarModelProvider.getModel();
        final ObjectAdapter objectAdapter = model.getObject();
        return asT(objectAdapter);
    }

    @SuppressWarnings("unchecked")
    private T asT(final ObjectAdapter objectAdapter) {
        return (T) (objectAdapter != null? objectAdapter.getObject(): null);
    }

    @Override
    public void setObject(final T object) {
        if (object == null) {
            scalarModelProvider.getModel().setObject(null);
        } else {
            final ObjectAdapter objectAdapter = scalarModelProvider.getAdapterManager().adapterFor(object);
            scalarModelProvider.getModel().setObject(objectAdapter);
        }
    }
}
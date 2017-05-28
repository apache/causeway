package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

public interface ObjectAdapterModel extends IModel<ObjectAdapter> {

    ObjectAdapterMemento getContextAdapterIfAny();
    void setContextAdapterIfAny(ObjectAdapterMemento contextAdapterIfAny);

    EntityModel.RenderingHint getRenderingHint();
    void setRenderingHint(final EntityModel.RenderingHint renderingHint);

    PageParameters getPageParametersWithoutUiHints();

    ObjectSpecification getTypeOfSpecification();

    EntityModel.Mode getMode();

    PageParameters getPageParameters();

    boolean isInlinePrompt();
}

package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

import static org.apache.isis.viewer.wicket.model.models.EntityModel.createPageParameters;

public class EntityModelForReference implements ObjectAdapterModel {

    private static final long serialVersionUID = 1L;

    private final ScalarModel scalarModel;

    private ObjectAdapterMemento contextAdapterIfAny;
    private EntityModel.RenderingHint renderingHint;


    public EntityModelForReference(final ScalarModel scalarModel) {
        this.scalarModel = scalarModel;
    }

    @Override
    public ObjectAdapter getObject() {
        return scalarModel.getPendingElseCurrentAdapter();
    }

    @Override
    public void setObject(final ObjectAdapter adapter) {
        // no-op
    }

    @Override
    public void detach() {
        // no-op
    }

    @Override
    public ObjectAdapterMemento getContextAdapterIfAny() {
        return contextAdapterIfAny;
    }

    public void setContextAdapterIfAny(ObjectAdapterMemento contextAdapterIfAny) {
        this.contextAdapterIfAny = contextAdapterIfAny;
    }

    @Override
    public EntityModel.RenderingHint getRenderingHint() {
        return renderingHint;
    }

    public void setRenderingHint(final EntityModel.RenderingHint renderingHint) {
        this.renderingHint = renderingHint;
    }

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        PageParameters pageParameters = createPageParameters(getObject());
        ObjectAdapterMemento oam = ObjectAdapterMemento.createOrNull(getObject());
        HintPageParameterSerializer.hintStoreToPageParameters(pageParameters, oam);
        return pageParameters;
    }

    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return scalarModel.getTypeOfSpecification();
    }

    @Override
    public EntityModel.Mode getMode() {
        return EntityModel.Mode.VIEW;
    }

    @Override
    public PageParameters getPageParameters() {
        PageParameters pageParameters = createPageParameters(getObject());
        ObjectAdapterMemento oam = ObjectAdapterMemento.createOrNull(getObject());
        HintPageParameterSerializer.hintStoreToPageParameters(pageParameters, oam);
        return pageParameters;
    }

    @Override
    public boolean isInlinePrompt() {
        return scalarModel.getPromptStyle().isInlineOrInlineAsIfEdit()
                && scalarModel.isEnabled();
    }

}

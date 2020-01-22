package org.apache.isis.extensions.fullcalendar.ui.component;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

public abstract class CalendaredCollectionFactoryAbstract extends ComponentFactoryAbstract implements CollectionContentsAsFactory {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "calendar";

    private Class<?> cls;

    public CalendaredCollectionFactoryAbstract(final Class<?> cls) {
        super(ComponentType.COLLECTION_CONTENTS, NAME);
        this.cls = cls;
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if(!(model instanceof EntityCollectionModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final EntityCollectionModel entityCollectionModel = (EntityCollectionModel) model;
        
        final ObjectSpecification elementSpec = entityCollectionModel.getTypeOfSpecification();
        final Class<?> correspondingClass = elementSpec.getCorrespondingClass();
        
        return appliesIf(cls.isAssignableFrom(correspondingClass));
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityCollectionModel collectionModel = (EntityCollectionModel) model;
        return newComponent(id, collectionModel);
    }

    protected abstract Component newComponent(final String id, final EntityCollectionModel collectionModel);


    @Override
    public IModel<String> getTitleLabel() {
        return Model.of("Calendar");
    }

    @Override
    public IModel<String> getCssClass() {
        return Model.of("fa fa-calendar");
    }
}

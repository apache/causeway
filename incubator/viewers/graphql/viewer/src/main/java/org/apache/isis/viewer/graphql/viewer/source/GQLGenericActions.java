package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

@Data
public class GQLGenericActions {

    private final ObjectTypeConstructionHelper constructionHelper;
    private final Bookmark bookmark;

    private ManagedObject getManagedObject() {
        return constructionHelper.getManagedObject(bookmark);
    }

    public boolean hideAction(final ObjectAction objectAction){
        ManagedObject managedObject = getManagedObject();
        if (managedObject == null) return true;
        return !objectAction.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed();
    }

    public String disableAction(final ObjectAction objectAction){
        ManagedObject managedObject = getManagedObject();
        if (managedObject == null) return "No managed object found";
        Consent usable = objectAction.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usable.isAllowed()) return usable.getReason();
        return usable.getReason();
    }

    public String validateAction(final ObjectAction objectAction){
        ManagedObject managedObject = getManagedObject();
        if (managedObject == null) return "No managed object found";
        // TODO: implement correctly
        return objectAction.isArgumentSetValidForAction(InteractionHead.regular(managedObject), Can.empty(), InteractionInitiatedBy.USER).getReason();
    }

    public String semanticsOf(final ObjectAction objectAction){
        return objectAction.getSemantics().name();
    }

    public Object defaultValueFor(ObjectAction objectAction, ObjectActionParameter parameter) {
        ManagedObject paramValue = getParamValue(objectAction, parameter);
        BeanSort beanSort = parameter.getElementType().getBeanSort();
        switch (beanSort){
            case ABSTRACT:
            case ENTITY:
                return paramValue.getPojo();
            case VALUE:
                return parameter.getElementType().getCorrespondingClass().cast(paramValue.getPojo());
        }
        return null;
    }

    private ManagedObject getParamValue(ObjectAction objectAction, ObjectActionParameter parameter) {
        return ManagedAction.of(getManagedObject(), objectAction, Where.ANYWHERE).startParameterNegotiation().getParamValue(parameter.getParameterIndex());
    }

}

package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@Data
public class GQLGenericActions {

    private final ObjectTypeConstructionHelper constructionHelper;
    private final Bookmark bookmark;

    public boolean hideAction(final ObjectAction objectAction){
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return true;
        return !objectAction.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE).isAllowed();
    }

    public String disableAction(final ObjectAction objectAction){
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return "No managed object found";
        Consent usable = objectAction.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usable.isAllowed()) return usable.getReason();
        return usable.getReason();
    }

    public String validateAction(final ObjectAction objectAction){
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return "No managed object found";
        // TODO: implement
        return "not yet implemented";
    }

}

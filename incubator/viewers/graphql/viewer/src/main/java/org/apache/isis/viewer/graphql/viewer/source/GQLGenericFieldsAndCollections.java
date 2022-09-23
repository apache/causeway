package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@Data
public class GQLGenericFieldsAndCollections {

    private final ObjectTypeConstructionHelper constructionHelper;
    private final Bookmark bookmark;

    public boolean hideOTMA(OneToManyAssociation oneToManyAssociation) {
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return true;
        Consent visible = oneToManyAssociation.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visible.isAllowed()) return false;
        return true;
    }

    public String disableOTMA(OneToManyAssociation oneToManyAssociation) {
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return "No managed object found";
        Consent usable = oneToManyAssociation.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usable.isAllowed()) return usable.getReason();
        return usable.getReason();
    }

    public boolean hideOTOA(OneToOneAssociation oneToOneAssociation) {
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return true;
        Consent visible = oneToOneAssociation.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visible.isAllowed()) return false;
        return true;
    }

    public String disableOTOA(OneToOneAssociation oneToOneAssociation) {
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return "No managed object found";
        Consent usable = oneToOneAssociation.isUsable(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (usable.isAllowed()) return usable.getReason();
        return usable.getReason();
    }
}

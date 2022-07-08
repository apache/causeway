package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@Data
public class GQLGenericFieldsAndCollections {

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

    public boolean hide(OneToManyAssociation oneToManyAssociation) {
        ManagedObject managedObject = constructionHelper.getManagedObject(bookmark);
        if (managedObject == null) return true;
        Consent visible = oneToManyAssociation.isVisible(managedObject, InteractionInitiatedBy.USER, Where.ANYWHERE);
        if (visible.isAllowed()) return false;
        return true;
    }

    public String disable(OneToManyAssociation oneToManyAssociation) {
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

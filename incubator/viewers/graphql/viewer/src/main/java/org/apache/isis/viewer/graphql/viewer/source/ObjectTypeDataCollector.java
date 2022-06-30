package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Setter;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import java.util.Arrays;
import java.util.Optional;

import static org.apache.isis.applib.services.metamodel.BeanSort.*;

@Setter
public class ObjectTypeDataCollector {

    private ObjectSpecification objectSpecification;

    private String gqlObjectTypeName;

    private String inputTypeName;

    private String metaTypeName;

    private String metaMutationsTypeName;

    private String metaFieldsTypeName;

    public boolean objectHasMutations() {
        Optional<ObjectAction> firstNonSafeAction = objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> !objectAction.getSemantics().isSafeInNature())
                .findFirst();
        if (firstNonSafeAction.isPresent()) return true;
        return false;
    }

    public boolean objectHasFields() {

        Optional<OneToOneAssociation> firstField = objectSpecification.streamProperties(MixedIn.INCLUDED)
                .filter(otoa -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otoa.getElementType().getBeanSort()))
                .findFirst();
        if (firstField.isPresent()) return true;
        Optional<OneToManyAssociation> firstCollection = objectSpecification.streamCollections(MixedIn.INCLUDED)
                .filter(otom -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otom.getElementType().getBeanSort()))
                .findFirst();
        if (firstCollection.isPresent()) return true;
        // safe actions we treat as parameterized fields
        Optional<ObjectAction> firstSafeAction = objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isSafeInNature())
                .findFirst();
        if (firstSafeAction.isPresent()) return true;

        return false;

    }
}

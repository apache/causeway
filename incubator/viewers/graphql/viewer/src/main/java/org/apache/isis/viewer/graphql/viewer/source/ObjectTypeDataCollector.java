package org.apache.isis.viewer.graphql.viewer.source;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import lombok.Getter;
import lombok.Setter;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.isis.applib.services.metamodel.BeanSort.*;

@Setter @Getter
public class ObjectTypeDataCollector {

    private ObjectSpecification objectSpecification;

    private String gqlObjectTypeName;

    private String inputTypeName;

    private String metaTypeName;

    private String metaMutationsTypeName;

    private String metaFieldsTypeName;

    private String mutatorsTypeName;

    public static GraphQLObjectType getObjectTypeFor(String mutatorsTypeName, Set<GraphQLType> gqlTypes) {
        return gqlTypes.stream()
                .filter(t->t.getClass().isAssignableFrom(GraphQLObjectType.class))
                .map(GraphQLObjectType.class::cast)
                .filter(ot->ot.getName().equals(mutatorsTypeName))
                .findFirst().orElse(null);
    }

    public boolean objectHasMutations() {
        return !nonIdempotentActionNames().isEmpty() || !idempotentActions().isEmpty();
    }

    public boolean objectHasFields() {
        return !properties().isEmpty() || !collections().isEmpty() || !idempotentActions().isEmpty();
    }

    public List<String> properties(){
        return objectSpecification.streamProperties(MixedIn.INCLUDED)
                .filter(otoa -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otoa.getElementType().getBeanSort()))
                .map(OneToOneAssociation::getId)
                .collect(Collectors.toList());
    }

    public List<String> collections(){
        return objectSpecification.streamCollections(MixedIn.INCLUDED)
                .filter(otom -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otom.getElementType().getBeanSort()))
                .map(OneToManyAssociation::getId)
                .collect(Collectors.toList());
    }

    public List<String> safeActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isSafeInNature())
                .map(ObjectAction::getId)
                .collect(Collectors.toList());
    }

    public List<String> idempotentActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isIdempotentInNature())
                .map(ObjectAction::getId)
                .collect(Collectors.toList());
    }

    public List<String> nonIdempotentActionNames(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> !objectAction.getSemantics().isSafeInNature())
                .filter(objectAction -> !objectAction.getSemantics().isIdempotentInNature())
                .map(ObjectAction::getId)
                .collect(Collectors.toList());
    }

    public List<String> mutatorActionNames(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> !objectAction.getSemantics().isSafeInNature())
                .map(ObjectAction::getId)
                .collect(Collectors.toList());
    }

    public List<ObjectAction> mutatorActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> !objectAction.getSemantics().isSafeInNature())
                .collect(Collectors.toList());
    }

}

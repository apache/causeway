package org.apache.isis.viewer.graphql.viewer.source;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import lombok.Getter;
import lombok.Setter;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.ProtoObject;
import org.apache.isis.core.metamodel.objectmanager.ObjectLoader;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;

import org.apache.isis.core.metamodel.spec.ActionScope;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.isis.applib.services.metamodel.BeanSort.*;

@Setter @Getter
public class ObjectTypeConstructionHelper {

    public ObjectTypeConstructionHelper(BookmarkService bookmarkService, ObjectManager objectManager) {
        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;
    }

    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;

    public ManagedObject getManagedObject(final Bookmark bookmark) {
        try {
            ProtoObject protoObject = ProtoObject.of(getObjectSpecification(), bookmark);
            return objectManager.loadObject(protoObject);
        } catch (Exception e){

        }
        return null;
    }

    private ObjectSpecification objectSpecification;

    public String logicalTypeNameSanitized(){
        return _Utils.logicalTypeNameSanitized(objectSpecification.getLogicalTypeName());
    }

    public String gqlObjectTypeName(){
        return _Utils.logicalTypeNameSanitized(objectSpecification.getLogicalTypeName());
    }

    public String inputTypeName(){
        return _Utils.GQL_INPUTTYPE_PREFIX + logicalTypeNameSanitized();
    }

    public String mutationsTypeName(){
        return _Utils.mutationsTypeName(logicalTypeNameSanitized());
    }

    public String genericTypeName(){
        return _Utils.genericTypeName(logicalTypeNameSanitized());
    }

    public String genericActionsTypename(){
        return _Utils.genericActionsTypeName(logicalTypeNameSanitized());
    }

    public String genericFieldsTypeName(){
        return _Utils.genericPropertiesTypeName(logicalTypeNameSanitized());
    }

    public String genericCollectionsTypeName(){
        return _Utils.genericCollectionsTypeName(logicalTypeNameSanitized());
    }

    public String actionGenericTypeName(final String actionName){
        return _Utils.objectActionGenericTypeName(logicalTypeNameSanitized(), actionName);
    }

    public String objectActionGenericParamsTypeName(final String objectActionname){
        return _Utils.objectActionGenericParamsTypeName(logicalTypeNameSanitized(), objectActionname);
    }

    public String objectActionParameterGenericTypeName(final String objectActionname, final String parameterName){
        return _Utils.objectActionParameterGenericTypeName(logicalTypeNameSanitized(), objectActionname, parameterName);
    }

    public static GraphQLObjectType getObjectTypeFor(String typeName, Set<GraphQLType> gqlTypes) {
        return gqlTypes.stream()
                .filter(t->t.getClass().isAssignableFrom(GraphQLObjectType.class))
                .map(GraphQLObjectType.class::cast)
                .filter(ot->ot.getName().equals(typeName))
                .findFirst().orElse(null);
    }

    public static GraphQLEnumType getEnumTypeFor(String typeName, Set<GraphQLType> gqlTypes) {
        return gqlTypes.stream()
                .filter(t->t.getClass().isAssignableFrom(GraphQLEnumType.class))
                .map(GraphQLEnumType.class::cast)
                .filter(ot->ot.getName().equals(typeName))
                .findFirst().orElse(null);
    }

    public boolean objectHasMutations() {
        return !nonIdempotentActionNames().isEmpty() || !idempotentActionNames().isEmpty();
    }

    public boolean objectHasProperties() {
        return !oneToOneAssociations().isEmpty();
    }

    public boolean objectHasCollections() {
        return !oneToManyAssociations().isEmpty();
    }

    public boolean objectHasActions() {
        return !allActionNames().isEmpty();
    }

    public List<OneToOneAssociation> oneToOneAssociations() {
        return objectSpecification.streamProperties(MixedIn.INCLUDED)
                .filter(otoa -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otoa.getElementType().getBeanSort()))
                .collect(Collectors.toList());
    }

    public List<String> oneToOneAssociationNames(){
        return oneToOneAssociations().stream()
                .map(OneToOneAssociation::getId)
                .collect(Collectors.toList());
    }

    public List<OneToManyAssociation> oneToManyAssociations(){
        return objectSpecification.streamCollections(MixedIn.INCLUDED)
                .filter(otom -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otom.getElementType().getBeanSort()))
                .collect(Collectors.toList());
    }

    public List<String> oneToManyAssociationNames(){
        return oneToManyAssociations().stream()
                .map(OneToManyAssociation::getId)
                .collect(Collectors.toList());
    }

    public List<ObjectAction> safeActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isSafeInNature())
                .collect(Collectors.toList());
    }

    public List<String> idempotentActionNames(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isIdempotentInNature())
                .filter(objectAction -> !objectAction.getSemantics().isSafeInNature())
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

    public List<ObjectAction> mutatorActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> !objectAction.getSemantics().isSafeInNature())
                .collect(Collectors.toList());
    }

    public List<String> safeActionNames() {
        return safeActions().stream().map(a->a.getId()).collect(Collectors.toList());
    }

    public List<ObjectAction> allActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED).collect(Collectors.toList());
    }

    public List<String> allActionNames() {
        return Stream.of(safeActionNames(), idempotentActionNames(), nonIdempotentActionNames()).flatMap(Collection::stream).collect(Collectors.toList());
    }

}

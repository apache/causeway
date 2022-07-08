package org.apache.isis.viewer.graphql.viewer.source;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import lombok.Getter;
import lombok.Setter;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
            ObjectLoader.Request request = ObjectLoader.Request.of(getObjectSpecification(), bookmark);
            return objectManager.loadObject(request);
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

    public String metaMutationsTypeName(){
        return _Utils.metaMutationsTypeName(logicalTypeNameSanitized());
    }

    public String genericFieldsTypeName(){
        return _Utils.metaFieldsTypeName(logicalTypeNameSanitized());
    }

    public String parameterizedFieldMetaDataTypeName(final String parameterizedFieldName){
        return _Utils.parameterizedFieldMetaDataTypeName(logicalTypeNameSanitized(), parameterizedFieldName);
    }

    public String parametersMetaDataTypeName(final String parameterizedFieldName){
        return _Utils.parametersMetaDataTypeName(logicalTypeNameSanitized(), parameterizedFieldName);
    }

    public String parameterMetaDataTypeName(final String parameterizedFieldName, final String parameterName){
        return _Utils.parameterMetaDataTypeName(logicalTypeNameSanitized(), parameterizedFieldName, parameterName);
    }

    public static GraphQLObjectType getObjectTypeFor(String typeName, Set<GraphQLType> gqlTypes) {
        return gqlTypes.stream()
                .filter(t->t.getClass().isAssignableFrom(GraphQLObjectType.class))
                .map(GraphQLObjectType.class::cast)
                .filter(ot->ot.getName().equals(typeName))
                .findFirst().orElse(null);
    }

    public boolean objectHasMutations() {
        return !nonIdempotentActionNames().isEmpty() || !idempotentActionNames().isEmpty();
    }

    public boolean objectHasFields() {
        return !oneToOneAssociationNames().isEmpty() || !oneToManyAssociationNames().isEmpty() || !idempotentActionNames().isEmpty();
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

    public List<String> allActionNames() {
        return Stream.of(safeActionNames(), idempotentActionNames(), nonIdempotentActionNames()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<String> fieldNames(){
        return Stream.of(oneToOneAssociationNames(), oneToManyAssociationNames(), safeActionNames())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}

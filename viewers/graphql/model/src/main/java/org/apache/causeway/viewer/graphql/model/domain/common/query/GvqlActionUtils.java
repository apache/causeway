package org.apache.causeway.viewer.graphql.model.domain.common.query;

import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.Environment;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvMetaSaveAs;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;

public class GvqlActionUtils {

    public static Optional<Object> asPojo(
            final SchemaStrategy schemaStrategy,
            final ObjectSpecification elementType,
            final Object argumentValueObj,
            final Environment environment,
            final Context context
    ) {
        val argumentValue = (Map<String, String>) argumentValueObj;

        val refValue = argumentValue.get("ref");
        if (refValue != null) {
            String key = keyFor(refValue);
            BookmarkedPojo bookmarkedPojo = environment.getGraphQlContext().get(key);
            if (bookmarkedPojo == null) {
                throw new IllegalArgumentException(String.format(
                    "Could not find object referenced '%s' in the execution context; was it saved previously using \"saveAs\" ?", refValue));
            }
            val targetPojoClass = bookmarkedPojo.getTargetPojo().getClass();
            val targetPojoSpec = context.specificationLoader.loadSpecification(targetPojoClass);
            if (targetPojoSpec == null) {
                throw new IllegalArgumentException(String.format(
                    "The object referenced '%s' is not part of the metamodel (has class '%s')",
                    refValue, targetPojoClass.getCanonicalName()));
            }
            if (!elementType.isPojoCompatible(bookmarkedPojo.getTargetPojo())) {
                throw new IllegalArgumentException(String.format(
                    "The object referenced '%s' has a type '%s' that is not assignable to the required type '%s'",
                    refValue, targetPojoSpec.getLogicalTypeName(), elementType.getLogicalTypeName()));
            }
            return Optional.of(bookmarkedPojo).map(BookmarkedPojo::getTargetPojo);
        }

        val idValue = argumentValue.get("id");
        if (idValue != null) {
            Class<?> paramClass = elementType.getCorrespondingClass();
            Optional<Bookmark> bookmarkIfAny;
            if(elementType.isAbstract()) {
                val logicalTypeName = argumentValue.get("logicalTypeName");
                if (logicalTypeName == null) {
                    throw new IllegalArgumentException(String.format(
                            "The 'logicalTypeName' is required along with the 'id', because the input type '%s' is abstract",
                            elementType.getLogicalTypeName()));
                }
                if(context.specificationLoader.specForLogicalTypeName(logicalTypeName).isEmpty()) {
                    throw new IllegalArgumentException(String.format(
                            "The 'logicalTypeName' of '%s' is unknown in the metamodel",
                            logicalTypeName));
                }

                 bookmarkIfAny = Optional.of(Bookmark.forLogicalTypeNameAndIdentifier(logicalTypeName, idValue));
            } else {
                bookmarkIfAny = context.bookmarkService.bookmarkFor(paramClass, idValue);
            }
            return bookmarkIfAny
                    .map(context.bookmarkService::lookup)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }
        throw new IllegalArgumentException("Either 'id' or 'ref' must be specified for a DomainObject input type");
    }

    /**
     * @param schemaStrategy
     * @param environment
     * @param objectAction
     * @param context
     * @return
     */
    public static Can<ManagedObject> argumentManagedObjectsFor(
            SchemaStrategy schemaStrategy,
            final Environment environment,
            final ObjectAction objectAction,
            final Context context) {
        Map<String, Object> argumentPojos = environment.getArguments();
        Can<ObjectActionParameter> parameters = objectAction.getParameters();
        return parameters
                .map(oap -> {
                    final ObjectSpecification elementType = oap.getElementType();
                    Object argumentValue = argumentPojos.get(oap.getId());
                    Object pojoOrPojoList;

                    switch (elementType.getBeanSort()) {

                        case VALUE:
                            return adaptValue(oap, argumentValue, context);

                        case ENTITY:
                        case VIEW_MODEL:
                            if (argumentValue == null) {
                                return ManagedObject.empty(elementType);
                            }
                            // fall through

                        case ABSTRACT:
                            // if the parameter is abstract, we still attempt to figure out the arguments.
                            // the arguments will need to either use 'ref' or else both 'id' AND 'logicalTypeName'
                            if (argumentValue instanceof List) {
                                val argumentValueList = (List<Object>) argumentValue;
                                pojoOrPojoList = argumentValueList.stream()
                                        .map(value -> asPojo(schemaStrategy, oap.getElementType(), value, environment, context))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList());
                            } else {
                                pojoOrPojoList = asPojo(schemaStrategy, oap.getElementType(), argumentValue, environment, context).orElse(null);
                            }
                            return ManagedObject.adaptParameter(oap, pojoOrPojoList);

                        case COLLECTION:
                        case MANAGED_BEAN_CONTRIBUTING:
                        case VETOED:
                        case MANAGED_BEAN_NOT_CONTRIBUTING:
                        case MIXIN:
                        case UNKNOWN:
                        default:
                            throw new IllegalArgumentException(String.format(
                                    "Cannot handle an input type for %s; beanSort is %s", elementType.getFullIdentifier(), elementType.getBeanSort()));
                    }
                });
    }

    private static ManagedObject adaptValue(
            final ObjectActionParameter oap,
            final Object argumentValue,
            final Context context) {

        val elementType = oap.getElementType();
        if (argumentValue == null) {
            return ManagedObject.empty(elementType);
        }

        val argPojo = context.typeMapper.unmarshal(argumentValue, elementType);
        return ManagedObject.adaptParameter(oap, argPojo);
    }

    public static String keyFor(String ref) {
        return GvqlActionUtils.class.getName() + "#" + ref;
    }
}

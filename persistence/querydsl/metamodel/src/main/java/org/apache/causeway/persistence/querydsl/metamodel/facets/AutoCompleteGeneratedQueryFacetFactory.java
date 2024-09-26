package org.apache.causeway.persistence.querydsl.metamodel.facets;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.jdo.annotations.PersistenceCapable;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;

import org.apache.causeway.persistence.querydsl.applib.annotation.AutoComplete;
import org.apache.causeway.persistence.querydsl.applib.annotation.AutoCompleteDomain;
import org.springframework.util.ReflectionUtils;


public class AutoCompleteGeneratedQueryFacetFactory extends FacetFactoryAbstract implements ObjectTypeFacetFactory {

    public AutoCompleteGeneratedQueryFacetFactory(MetaModelContext metaModelContext) {
        super(metaModelContext, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessObjectTypeContext processClassContext) {
        Optional<DomainObject> domainObject=processClassContext.synthesizeOnType(DomainObject.class);
        // TODO The next line makes this an JDO specific implementation that has to be generalized
        Optional<?> persistenceCapable=processClassContext.synthesizeOnType(PersistenceCapable.class);

        // Has the class the DomainObject or PersistenceCapable annotation?
        if(((domainObject.isPresent() && !(domainObject.get().autoCompleteRepository()==null)) || persistenceCapable.isPresent()) &&
                !processClassContext.getFacetHolder().containsFacet(AutoCompleteGeneratedQueryFacet.class)){

            // No repository set, possible candidate for query generation
            final List<Field> fields = new ArrayList<>();
            ReflectionUtils.doWithFields(processClassContext.getCls(), field -> {
                if(field.isAnnotationPresent(AutoComplete.class)) fields.add(field);
            });
            if(fields.isEmpty()) {

                // No fields with AutoComplete annotation found, search for string fields that have a getter
                ReflectionUtils.doWithFields(processClassContext.getCls(), field -> {
                    if( getClassCache().getterForField(processClassContext.getCls(), field).isPresent() &&
                            field.getType()==String.class)
                        fields.add(field);
                });
            }
            if(!fields.isEmpty()){
                // Is there a autoCompletePredicate method defined?
                Optional<AutoCompleteDomain> autoCompleteDomain=processClassContext.synthesizeOnType(AutoCompleteDomain.class);
                Object repository=null;
                Method method=null;
                Integer limit=null;
                if(autoCompleteDomain.isPresent() && autoCompleteDomain.get().repository()!=Object.class) {
                    Optional<?> result = lookupService(autoCompleteDomain.get().repository());
                    if(result.isPresent()) {
                        repository=result.get();
                        method = ReflectionUtils.findMethod(autoCompleteDomain.get().repository(), "autoCompletePredicate", String.class);
                        if (method == null || method.getReturnType() != Function.class) {
                            repository = null;
                            method = null;
                        }
                        limit=autoCompleteDomain.get().limitResults();
                    }
                }

                // Found everything to search, hence create facet
                addFacet(new AutoCompleteGeneratedQueryFacet(processClassContext.getCls(),
                        processClassContext.getFacetHolder(), fields, repository, method, limit));
            }
        }
    }
}

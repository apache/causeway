package org.apache.isis.extensions.bdd.fitnesse.internal.fixtures;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.fitnesse.internal.AbstractSubsetFixture;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.session.IsisSessionFactory;

public class CheckSpecificationsLoadedForFitNesse extends AbstractSubsetFixture {

    private static Set<ObjectSpecification> allSpecNames() {
        final Set<ObjectSpecification> specs = 
            new TreeSet<ObjectSpecification>(
                new Comparator<ObjectSpecification>() {
                public int compare(final ObjectSpecification o1,
                    final ObjectSpecification o2) {
                return o1.getFullName().compareTo(o2.getFullName());
                }
                        });

        final SpecificationLoader specificationLoader = getSessionFactory().getSpecificationLoader();
        for (final ObjectSpecification ObjectSpecification : specificationLoader
                .allSpecifications()) {
            specs.add(ObjectSpecification);
        }
        return specs;
    }

    public CheckSpecificationsLoadedForFitNesse(final AliasRegistry aliasesRegistry) {
        super(aliasesRegistry, CheckSpecificationsLoadedForFitNesse.allSpecNames());
    }

    // ///////////////////////////////////////////////////////////////
    // from context
    // ///////////////////////////////////////////////////////////////

    private static IsisSessionFactory getSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}

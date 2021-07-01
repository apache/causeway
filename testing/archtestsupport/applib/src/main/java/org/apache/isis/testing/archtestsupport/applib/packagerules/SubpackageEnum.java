package org.apache.isis.testing.archtestsupport.applib.packagerules;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import static org.apache.isis.testing.archtestsupport.applib.packagerules.SubpackageType.MANDATORY;
import static org.apache.isis.testing.archtestsupport.applib.packagerules.SubpackageType.OPTIONAL;

import lombok.Getter;


public enum SubpackageEnum implements Subpackage {

    parent(MANDATORY, emptyList(), "fixtures") {
        @Override
        public String packageIdentifierWithin(Class<?> moduleClass) {
            return moduleClass.getPackage().getName() + "..";
        }
    },

    dom(OPTIONAL, emptyList(), "fixtures"), // allow access to personas

    app(OPTIONAL, singletonList(dom)),

    menu(OPTIONAL, singletonList(dom)),

    contributions(OPTIONAL, singletonList(dom)),

    subscriptions(OPTIONAL, singletonList(dom)),

    restapi(OPTIONAL, singletonList(dom)),

    spi(OPTIONAL, singletonList(dom)),

    spiimpl(OPTIONAL, singletonList(dom)),

    fixtures(OPTIONAL, asList(dom, menu, contributions)),

    seed(OPTIONAL, asList(dom, fixtures)),

    integtests(OPTIONAL, asList(dom, fixtures, app, menu, contributions, subscriptions, restapi, spi, spiimpl, seed)),
    ;

    @Getter
    final SubpackageType subpackageType;
    final List<Subpackage> references;
    final List<String> softReferences;

    SubpackageEnum(SubpackageType subpackageType, List<Subpackage> references, String... softReferences) {
        this.subpackageType = subpackageType;
        this.references = references;
        this.softReferences = asList(softReferences);
    }

    public String getName() {
        return name();
    }

    public String packageIdentifierWithin(Class<?> moduleClass) {
        return moduleClass.getPackage().getName() + "." + name() + "..";
    }

    public boolean canReference(Subpackage subpackage) {
        return references.contains(subpackage) || softReferences.contains(subpackage.getName());
    }
}

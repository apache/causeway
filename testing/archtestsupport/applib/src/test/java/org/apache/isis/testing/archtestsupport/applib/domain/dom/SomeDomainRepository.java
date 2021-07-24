package org.apache.isis.testing.archtestsupport.applib.domain.dom;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface SomeDomainRepository {

    Optional<SomeDomainObject> find();
    List<SomeDomainObject> findAll();

    Map<String,SomeDomainObject> notFinder();

    // we allow these as an exception to the "finder" rule.
    SomeDomainObject findOrCreate();
}

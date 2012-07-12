package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class Persistence_findInstance {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void whenNoInstances() {
        iswf.beginTran();
        final PrimitiveValuedEntity entity = repo.findById(1);
        assertThat(entity, is(nullValue()));
        iswf.commitTran();
    }

    @Test
    public void whenAnInstance() throws Exception {
        
        iswf.beginTran();
        repo.newEntity().setId(1);
        iswf.commitTran();
        
        iswf.beginTran();
        final PrimitiveValuedEntity entity = repo.findById(1);
        assertThat(entity, is(not(nullValue())));
        assertThat(entity.getId(), is(1));
        iswf.commitTran();
    }


}

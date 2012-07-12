package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class Persistence_allInstances {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void whenNoInstances() {
        iswf.beginTran();
        final List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(0));
        iswf.commitTran();
    }

    @Test
    public void persist_dontBounce_listAll() throws Exception {
        
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        entity = repo.newEntity();
        entity.setId(2);
        iswf.commitTran();

        // don't bounce
        iswf.beginTran();
        List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persist_bounce_listAll() throws Exception {
        
        iswf.beginTran();
        repo.newEntity().setId(1);
        repo.newEntity().setId(2);
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }


}

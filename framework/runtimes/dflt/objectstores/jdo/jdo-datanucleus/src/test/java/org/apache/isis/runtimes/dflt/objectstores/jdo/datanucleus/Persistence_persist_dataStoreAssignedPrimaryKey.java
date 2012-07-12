package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.AutoAssignedEntity;
import org.apache.isis.tck.dom.scalars.AutoAssignedEntityRepository;

public class Persistence_persist_dataStoreAssignedPrimaryKey {

    private AutoAssignedEntityRepository repo = new AutoAssignedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("AUTOASSIGNEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void persistTwo() throws Exception {
        iswf.beginTran();
        repo.newEntity();
        repo.newEntity();
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<AutoAssignedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

}

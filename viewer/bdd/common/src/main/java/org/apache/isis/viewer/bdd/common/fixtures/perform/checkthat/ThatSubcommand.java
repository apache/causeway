package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformOwner;

public interface ThatSubcommand {

	ObjectAdapter that(PerformContext performContext) throws StoryBoundValueException;

    List<String> getSubkeys();

    void setOwner(PerformOwner owner);

}

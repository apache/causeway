package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat;

import java.util.List;

import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformOwner;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

public interface ThatSubcommand {

	ObjectAdapter that(PerformContext performContext) throws StoryBoundValueException;

    List<String> getSubkeys();

    void setOwner(PerformOwner owner);

}

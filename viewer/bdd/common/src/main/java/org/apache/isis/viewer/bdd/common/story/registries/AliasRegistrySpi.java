package org.apache.isis.viewer.bdd.common.story.registries;

import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.StoryValueException;

public interface AliasRegistrySpi extends AliasRegistry, Iterable<Map.Entry<String, ObjectAdapter>> {

	void alias(String alias, ObjectAdapter adapter);
	void aliasService(final String aliasAs, final String className) throws StoryValueException;

}

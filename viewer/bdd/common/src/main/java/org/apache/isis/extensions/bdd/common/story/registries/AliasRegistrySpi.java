package org.apache.isis.extensions.bdd.common.story.registries;

import java.util.Map;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.StoryValueException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

public interface AliasRegistrySpi extends AliasRegistry, Iterable<Map.Entry<String, ObjectAdapter>> {

	void alias(String alias, ObjectAdapter adapter);
	void aliasService(final String aliasAs, final String className) throws StoryValueException;

}

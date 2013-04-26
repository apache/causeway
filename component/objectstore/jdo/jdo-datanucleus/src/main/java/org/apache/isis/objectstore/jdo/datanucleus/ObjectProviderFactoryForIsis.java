package org.apache.isis.objectstore.jdo.datanucleus;

import org.datanucleus.ExecutionContext;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.state.ObjectProviderFactory;
import org.datanucleus.state.ObjectProviderFactoryImpl;

public class ObjectProviderFactoryForIsis extends ObjectProviderFactoryImpl
		implements ObjectProviderFactory {

	public ObjectProviderFactoryForIsis(NucleusContext nucCtx) {
		super(nucCtx);
	}

	@Override
	protected ObjectProvider getObjectProvider(ExecutionContext ec,
			AbstractClassMetaData cmd) {
		return new JDOStateManagerForIsis(ec, cmd);
	}
	
}

package org.apache.isis.objectstore.jdo.datanucleus;

import java.lang.reflect.Field;
import java.util.Map;

import org.datanucleus.NucleusContext;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.state.ObjectProviderFactory;

public class NucleusContextForIsis extends NucleusContext {

	public NucleusContextForIsis(String apiName, ContextType type,
			Map startupProps, PluginManager pluginMgr) {
		super(apiName, type, startupProps, pluginMgr);
	}

	public NucleusContextForIsis(String apiName, ContextType type,
			Map startupProps) {
		super(apiName, type, startupProps);
	}

	public NucleusContextForIsis(String apiName, Map startupProps,
			PluginManager pluginMgr) {
		super(apiName, startupProps, pluginMgr);
	}

	public NucleusContextForIsis(String apiName, Map startupProps) {
		super(apiName, startupProps);
	}

	/**
	 * Horrendous code...
	 */
	@Override
	public ObjectProviderFactory getObjectProviderFactory() {
		try {
			Field declaredField = NucleusContext.class.getDeclaredField("opFactory");
			declaredField.setAccessible(true);
			ObjectProviderFactory opf = (ObjectProviderFactory) declaredField.get(this);
			if(opf == null) {
				opf = newObjectProviderFactory();
				declaredField.set(this, opf);
			}
			return opf;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ObjectProviderFactoryForIsis newObjectProviderFactory() {
		return new ObjectProviderFactoryForIsis(this);
	}
	
}

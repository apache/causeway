package org.apache.isis.applib.plugins.classdiscovery.reflections;

import java.util.List;

import org.apache.isis.applib.internal.context._Context;
import org.apache.isis.applib.plugins.classdiscovery.ClassDiscovery;
import org.apache.isis.applib.plugins.classdiscovery.ClassDiscoveryPlugin;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

public class ClassDiscoveryUsingReflectionsPlugin implements ClassDiscoveryPlugin {

	@Override
	public ClassDiscovery discover(String packageNamePrefix) {
		ReflectManifest.prepareDiscovery(); 	//TODO [ahuber] REVIEW why is this required?
		return ReflectDiscovery.of(packageNamePrefix);
	}

	@Override
	public ClassDiscovery discover(List<String> packageNamePrefixes) {
		ReflectManifest.prepareDiscovery();	//TODO [ahuber] REVIEW why is this required?
		return ReflectDiscovery.of(packageNamePrefixes);
	}

	@Override
	public ClassDiscovery discoverFullscan(String packageNamePrefix) {
		ReflectManifest.prepareDiscovery();	//TODO [ahuber] REVIEW why is this required?
		return ReflectDiscovery.of(
				ClasspathHelper.forClassLoader(_Context.getDefaultClassLoader()),
				ClasspathHelper.forClass(Object.class),
				ClasspathHelper.forPackage(packageNamePrefix),
				new SubTypesScanner(false)
		);
	}

}

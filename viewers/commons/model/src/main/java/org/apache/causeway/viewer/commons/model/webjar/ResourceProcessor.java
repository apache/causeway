/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.commons.model.webjar;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.viewer.commons.model.webjar.WebjarEnumerator.WebjarResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

/**
 * Inspired by org.webjars.WebJarAssetLocator. However, allows alternative resource lookup paths.
 */
record ResourceProcessor(
		List<String> acceptedPaths) {
	record ResourceKey(
			String pathDiscriminator,
			String webJarName) {
		ResourceKey {
			Assert.isTrue(StringUtils.hasLength(pathDiscriminator), ()->"pathDiscriminator is required");
			Assert.isTrue(StringUtils.hasLength(webJarName), ()->"webJarName is required");
		}
		static ResourceKey forResource(final String discriminator, final Resource resource) {
			var noPrefix = resource.getPath().substring(discriminator.length() + 1);
            var webJarName = noPrefix.substring(0, noPrefix.indexOf('/'));
			return new ResourceKey(discriminator, webJarName);
		}
	}
	record ResourcePackage(
			ResourceKey key,
			ResourceList resources) {
		String pathDiscriminator() {
			return key.pathDiscriminator();
		}
		String webJarName() {
			return key.webJarName();
		}
		String webJarVersion() {
			if (resources.isEmpty())
				return null;
			final String aPath = resources.get(0).getPath();
			final String prefix = "%s/%s/".formatted(pathDiscriminator(), webJarName());
			if (aPath.startsWith(prefix)) {
				try {
					final String withoutName = aPath.substring(prefix.length());
					final String maybeVersion = withoutName.substring(0, withoutName.indexOf('/'));
					ResourceList withMaybeVersion = resources.filter(resource -> resource.getPath().startsWith(
							"%s%s/".formatted(prefix, maybeVersion)));
					if (withMaybeVersion.size() == resources.size())
						return maybeVersion;
					return null;
				} catch (Exception e) {
					return null;
				}
			}
			return null;
		}
		Optional<String> artifactId() {
        	final ClassGraph classGraph = new ClassGraph().overrideClasspath(classpathElementURI()).ignoreParentClassLoaders().acceptPaths("META-INF/maven");
            try (ScanResult scanResult = classGraph.scan()) {
                final ResourceList maybePomProperties = scanResult.getResourcesWithLeafName("pom.properties");
                if (maybePomProperties.size() == 1) {
                    try {
                        var properties = new Properties();
                        properties.load(maybePomProperties.get(0).open());
                        maybePomProperties.get(0).close();
                        return Optional.of(properties.getProperty("artifactId"));
                    } catch (IOException e) {
                        // ignored
                    }
                }
                return Optional.empty();
            }
        }
		private URI classpathElementURI() {
			return resources.get(0).getClasspathElementURI();
		}
	}
	Map<String, WebjarResource> processAll() {
		var classGraph = new ClassGraph().acceptPaths(acceptedPaths.stream().toArray(String[]::new));
    	try (var scanResult = classGraph.scan()) {

    		var map = _Multimaps.<ResourceKey, Resource>newListMultimap();
    		scanResult.getAllResources().stream()
	    		.forEach(resource->
	    			acceptedPaths().stream()
	    				.filter(discriminator->resource.getPath().startsWith(discriminator))
	    				.map(discriminator -> ResourceKey.forResource(discriminator, resource))
	    				.findFirst()
	    				.ifPresent(key->map.putElement(key, resource))
            	);

    		return map.entrySet().stream()
    			.map(entry->new ResourcePackage(entry.getKey(), new ResourceList(entry.getValue())))
    			.map(resourcePackage->new WebjarResource(
    					resourcePackage.pathDiscriminator(),
    					resourcePackage.artifactId().orElse(null),
    					resourcePackage.webJarVersion()))
    			.filter(WebjarResource::isValid)
            	.collect(Collectors.toMap(WebjarResource::path, UnaryOperator.identity()));
        }
	}
}

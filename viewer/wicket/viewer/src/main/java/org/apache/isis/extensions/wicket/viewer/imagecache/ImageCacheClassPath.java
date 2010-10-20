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


package org.apache.isis.extensions.wicket.viewer.imagecache;

import images.Images;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.isis.extensions.wicket.ui.app.imagecache.ImageCache;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.wicket.markup.html.PackageResource;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

/**
 * Caches images loaded up the <tt>images</tt> package (using the {@link Images} class).
 * 
 * <p>
 * Searches for a fixed set of suffixes: {@value #IMAGE_SUFFICES}. 
 */
@Singleton
public class ImageCacheClassPath implements ImageCache {

	public final static List<String> IMAGE_SUFFICES = Arrays.asList("png","gif","jpeg","jpg");

	private Map<String, PackageResource> imagesByName = Maps.newHashMap();

	public PackageResource findImage(ObjectSpecification noSpec) {
		String specFullName = noSpec.getFullName();
		PackageResource packageResource = findImage(specFullName);
		if (packageResource != null) {
			return packageResource;
		} else {
			return findAndCacheImage(noSpec);
		}
	}

	public PackageResource findImage(String imageName) {
		PackageResource packageResource = imagesByName.get(imageName);
		if (packageResource != null) {
			return packageResource;
		} else {
			return findAndCacheImage(imageName);
		}
	}

	private PackageResource findAndCacheImage(String imageName) {
		PackageResource packageResource = findImageSuffixed(imageName);
		if (packageResource != null) {
			imagesByName.put(imageName, packageResource);
		}
		return packageResource;
	}
	
	private PackageResource findImageSuffixed(String imageName) {
		for (String imageSuffix : IMAGE_SUFFICES) {
			String path = buildImagePath(imageName, imageSuffix);
			if (PackageResource.exists(Images.class, path, null, null)) {
				return PackageResource.get(Images.class, path);
			}
		}
		return null;
	}

	private synchronized PackageResource findAndCacheImage(ObjectSpecification noSpec) {
	    PackageResource packageResource = findImageSearchUpHierarchy(noSpec);
		imagesByName.put(noSpec.getFullName(), packageResource);
		return packageResource;
	}

	private PackageResource findImageSearchUpHierarchy(
			ObjectSpecification noSpec) {
		for(String imageSuffix : IMAGE_SUFFICES) {
			String fullName = noSpec.getFullName();
			String path = buildImagePath(fullName, imageSuffix);
			if (PackageResource.exists(Images.class, path, null, null)) {
				return PackageResource.get(Images.class, path);
			}
		}
		for (String imageSuffix : IMAGE_SUFFICES) {
			String shortName = noSpec.getShortName();
			String path = buildImagePath(shortName, imageSuffix);
			if (PackageResource.exists(Images.class, path, null, null)) {
				return PackageResource.get(Images.class, path);
			}
		}
		ObjectSpecification superSpec = noSpec.superclass();
		if (superSpec != null) {
			return findAndCacheImage(superSpec);
		}
		// fallback
		return PackageResource.get(Images.class, "Default.png");
	}

	private String buildImagePath(String name, String imageSuffix) {
		return name + "." + imageSuffix;
	}

}

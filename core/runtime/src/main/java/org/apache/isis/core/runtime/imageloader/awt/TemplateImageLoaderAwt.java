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

package org.apache.isis.core.runtime.imageloader.awt;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.Resources;
import org.apache.isis.core.runtime.imageloader.TemplateImage;
import org.apache.isis.core.runtime.imageloader.TemplateImageImpl;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;

/**
 * This class loads up file based images as resources (part of the classpath) or
 * from the file system. Images of type PNG, GIF and JPEG will be used. The
 * default directory is images.
 */
public class TemplateImageLoaderAwt implements TemplateImageLoader {

    private final static Logger LOG = Logger.getLogger(TemplateImageLoaderAwt.class);

    private static final String LOAD_IMAGES_FROM_FILES_KEY = ImageConstants.PROPERTY_BASE + "load-images-from-files";
    private static final String[] EXTENSIONS = { "png", "gif", "jpg", "jpeg", "svg" };
    private final static String IMAGE_DIRECTORY = "images";
    private final static String IMAGE_DIRECTORY_PARAM = ImageConstants.PROPERTY_BASE + "image-directory";
    private static final String SEPARATOR = "/";

    private boolean initialized;

    private boolean alsoLoadAsFiles;
    protected final MediaTracker mt = new MediaTracker(new Canvas());

    /**
     * A keyed list of core images, one for each name, keyed by the image path.
     */
    private final Hashtable<String, TemplateImage> loadedImages = new Hashtable<String, TemplateImage>();
    private final Vector<String> missingImages = new Vector<String>();
    private final IsisConfiguration configuration;
    private String directory;

    // ////////////////////////////////////////////////////////////
    // constructor
    // ////////////////////////////////////////////////////////////

    public TemplateImageLoaderAwt(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    // ////////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////////

    @Override
    public void init() {
        ensureNotInitialized();
        LOG.info("images to be loaded from " + directory());
        alsoLoadAsFiles = getConfiguration().getBoolean(LOAD_IMAGES_FROM_FILES_KEY, true);
        initialized = true;
    }

    @Override
    public void shutdown() {
    }

    private void ensureNotInitialized() {
        ensureThatState(initialized, is(false));
    }

    private void ensureInitialized() {
        ensureThatState(initialized, is(true));
    }

    // ////////////////////////////////////////////////////////////
    // getTemplateImage
    // ////////////////////////////////////////////////////////////

    /**
     * Returns an image template for the specified image (as specified by a path
     * to a file or resource).
     * 
     * <p>
     * If the path has no extension (<tt>.gif</tt>, <tt>.png</tt> etc) then all
     * valid {@link #EXTENSIONS extensions} are searched for.
     * 
     * <p>
     * This method attempts to load the image from the jar/zip file this class
     * was loaded from ie, your application, and then from the file system as a
     * file if can't be found as a resource. If neither method works the default
     * image is returned.
     * 
     * @return returns a {@link TemplateImage} for the specified image file, or
     *         null if none found.
     */
    @Override
    public TemplateImage getTemplateImage(final String name) {
        ensureInitialized();

        if (loadedImages.containsKey(name)) {
            return loadedImages.get(name);
        }

        if (missingImages.contains(name)) {
            return null;
        }

        final List<String> candidates = getCandidates(name);
        for (final String candidate : candidates) {
            final Image image = load(candidate);
            final TemplateImageImpl templateImage = TemplateImageImpl.create(image);
            if (templateImage != null) {
                loadedImages.put(name, templateImage);
                return templateImage;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("failed to find image for " + name);
        }
        missingImages.addElement(name);
        return null;
    }

    // ////////////////////////////////////////////////////////////
    // helpers: parsing path
    // ////////////////////////////////////////////////////////////

    private List<String> getCandidates(final String name) {
        boolean hasExtension = false;
        for (final String extension : EXTENSIONS) {
            hasExtension = hasExtension || name.endsWith(extension);
        }

        final List<String> candidates = new ArrayList<String>();
        if (hasExtension) {
            candidates.add(name);
        } else {
            for (final String extension : EXTENSIONS) {
                candidates.add(name + "." + extension);
            }
        }
        return candidates;
    }

    // ////////////////////////////////////////////////////////////
    // helpers: loading
    // ////////////////////////////////////////////////////////////

    private Image load(final String name) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searching for image " + name);
        }

        Image image = loadAsResource(name);
        if (image != null) {
            return image;
        }

        final String path = directory() + name;
        image = loadAsResource(path);
        if (image != null) {
            return image;
        }

        if (alsoLoadAsFiles) {
            image = loadAsFile(path);
            if (image != null) {
                return image;
            }
        }

        return null;
    }

    /**
     * Get an Image object from the jar/zip file that this class was loaded
     * from.
     */
    protected Image loadAsResource(final String path) {
        final URL url = Resources.getResourceURL(path);
        if (url == null) {
            LOG.debug("not found image in resources: " + path);
            return null;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(url);
        if (image != null) {
            mt.addImage(image, 0);
            try {
                mt.waitForAll();
            } catch (final Exception e) {
                e.printStackTrace();
            }

            if (mt.isErrorAny()) {
                LOG.error("found image but failed to load it from resources: " + url + " " + mt.getErrorsAny()[0]);
                mt.removeImage(image);
                image = null;
            } else {
                mt.removeImage(image);
                LOG.info("image loaded from resources: " + url);
            }
        }

        if (image == null) {
            throw new RuntimeException("null image");
        } else {
            if (image.getWidth(null) == -1) {
                throw new RuntimeException(image.toString());
            }
        }

        return image;
    }

    /**
     * Get an {@link Image} object from the specified file path on the file
     * system.
     */
    private Image loadAsFile(final String path) {
        final File file = new File(path);

        if (!file.exists()) {
            return null;
        }
        final Toolkit t = Toolkit.getDefaultToolkit();
        Image image = t.getImage(file.getAbsolutePath());

        if (image != null) {
            mt.addImage(image, 0);

            try {
                mt.waitForAll();
            } catch (final Exception e) {
                e.printStackTrace();
            }

            if (mt.isErrorAny()) {
                LOG.error("found image file but failed to load it: " + file.getAbsolutePath());
                mt.removeImage(image);
                image = null;
            } else {
                mt.removeImage(image);
                LOG.info("image loaded from file: " + file);
            }
        }
        return image;
    }

    private String directory() {
        if (directory == null) {
            directory = getConfiguration().getString(IMAGE_DIRECTORY_PARAM, IMAGE_DIRECTORY);
            if (!directory.endsWith(SEPARATOR)) {
                directory = directory.concat(SEPARATOR);
            }
        }
        return directory;
    }

    // ////////////////////////////////////////////////////////////
    // unused
    // ////////////////////////////////////////////////////////////

    /**
     * This code was commented out. I've reinstated it, even though it is
     * unused, because it looks interesting and perhaps useful.
     */
    @SuppressWarnings("unused")
    private Image createImage() {
        final byte[] pixels = new byte[128 * 128];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (byte) (i % 128);
        }

        final byte[] r = new byte[] { 0, 127 };
        final byte[] g = new byte[] { 0, 127 };
        final byte[] b = new byte[] { 0, 127 };
        final IndexColorModel colorModel = new IndexColorModel(1, 2, r, g, b);

        final MemoryImageSource producer = new MemoryImageSource(128, 128, colorModel, pixels, 0, 128);
        final Image image = Toolkit.getDefaultToolkit().createImage(producer);

        return image;
    }

    // ////////////////////////////////////////////////////////////
    // dependencies (from singleton)
    // ////////////////////////////////////////////////////////////

    private IsisConfiguration getConfiguration() {
        return configuration;
    }

}

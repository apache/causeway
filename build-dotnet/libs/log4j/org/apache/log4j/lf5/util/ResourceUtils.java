/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.util;

import java.io.InputStream;
import java.net.URL;

/**
 * ResourceUtils.  Provide a set of convenience methods for working with
 * Resources.
 *
 * @see org.apache.log4j.lf5.util.Resource
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public class ResourceUtils {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  /**
   * Get the InputStream for this resource.  Note: to convert an InputStream
   * into an InputReader, use: new InputStreamReader(InputStream).
   *
   * @param object   The object to grab the Classloader from.
   *                 This parameter is quite important from a
   *                 visibility of resources standpoint as the
   *                 hierarchy of Classloaders plays a role.
   *
   * @param resource The resource to load.
   *
   * @return If the Resource was found, the InputStream, otherwise null.
   *
   * @see Resource
   * @see #getResourceAsURL(Object,Resource)
   * @see InputStream
   */
  public static InputStream getResourceAsStream(Object object, Resource resource) {
    ClassLoader loader = object.getClass().getClassLoader();

    InputStream in = null;

    if (loader != null) {
      in = loader.getResourceAsStream(resource.getName());
    } else {
      in = ClassLoader.getSystemResourceAsStream(resource.getName());
    }

    return in;
  }

  /**
   * Get the URL for this resource.
   *
   * @param object   The object to grab the Classloader from.
   *                 This parameter is quite important from a
   *                 visibility of resources standpoint as the
   *                 hierarchy of Classloaders plays a role.
   *
   * @param resource The resource to load.
   *
   * @return If the Resource was found, the URL, otherwise null.
   *
   * @see Resource
   * @see #getResourceAsStream(Object,Resource)
   */
  public static URL getResourceAsURL(Object object, Resource resource) {
    ClassLoader loader = object.getClass().getClassLoader();

    URL url = null;

    if (loader != null) {
      url = loader.getResource(resource.getName());
    } else {
      url = ClassLoader.getSystemResource(resource.getName());
    }

    return (url);
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}







/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Resource encapsulates access to Resources via the Classloader.
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public class Resource {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------
  protected String _name;

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  /**
   * Default, no argument constructor.
   */
  public Resource() {
    super();
  }

  /**
   * Construct a Resource given a name.
   *
   * @see #setName(String)
   */
  public Resource(String name) {
    _name = name;
  }

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  /**
   * Set the name of the resource.
   * <p>
   * A resource is some data (images, audio, text, etc) that can be accessed
   * by class code in a way that is independent of the location of the code.
   * </p>
   * <p>
   * The name of a resource is a "/"-separated path name that identifies
   * the resource.
   * </p>
   *
   * @see #getName()
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Get the name of the resource.  Set setName() for a description of
   * a resource.
   *
   * @see #setName
   */
  public String getName() {
    return (_name);
  }

  /**
   * Get the InputStream for this Resource.  Uses the classloader
   * from this Resource.
   *
   * @see #getInputStreamReader
   * @see ResourceUtils
   */
  public InputStream getInputStream() {
    InputStream in = ResourceUtils.getResourceAsStream(this, this);

    return (in);
  }

  /**
   * Get the InputStreamReader for this Resource. Uses the classloader from
   * this Resource.
   *
   * @see #getInputStream
   * @see ResourceUtils
   */
  public InputStreamReader getInputStreamReader() {
    InputStream in = ResourceUtils.getResourceAsStream(this, this);

    if (in == null) {
      return null;
    }

    InputStreamReader reader = new InputStreamReader(in);

    return reader;
  }

  /**
   * Get the URL of the Resource.  Uses the classloader from this Resource.
   *
   * @see ResourceUtils
   */
  public URL getURL() {
    return (ResourceUtils.getResourceAsURL(this, this));
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







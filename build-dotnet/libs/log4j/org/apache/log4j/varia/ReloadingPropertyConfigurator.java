/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.varia;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.Configurator;
import java.net.URL;
import  org.apache.log4j.spi.LoggerRepository;

public class ReloadingPropertyConfigurator implements Configurator {


  PropertyConfigurator delegate = new PropertyConfigurator();

  
  public ReloadingPropertyConfigurator() {    
  }

  public
  void doConfigure(URL url, LoggerRepository repository) {
  }

}

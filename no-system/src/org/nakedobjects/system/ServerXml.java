package org.nakedobjects.system;

import org.nakedobjects.distribution.ObjectEncoder;
import org.nakedobjects.distribution.ServerDistribution;
import org.nakedobjects.distribution.SingleResponseUpdateNotifier;
import org.nakedobjects.distribution.java.JavaDataFactory;
import org.nakedobjects.distribution.xml.XServerListener;
import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;
import org.nakedobjects.object.transaction.TransactionPeerFactory;
import org.nakedobjects.reflector.java.reflect.JavaSpecificationLoader;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.InfoDebugFrame;


/**
 * Utility class to start a server, using the default configuration file: server.properties.
 */
public final class ServerXml extends AbstractXmlStoreSystem {
    public static void main(String[] args) {
        new ServerXml().init();       
    }


    private SingleResponseUpdateNotifier updateNotifier;
    
    public ServerXml() {
        updateNotifier = new SingleResponseUpdateNotifier();
    }

    protected void displayUserInterface() {
        ObjectEncoder encoder = new ObjectEncoder();
        encoder.setDataFactory(new JavaDataFactory());

   
        ServerDistribution sd = new ServerDistribution();
        sd.setEncoder(encoder);
        sd.setUpdateNotifier(updateNotifier);

        XServerListener serverListener = new XServerListener();
        serverListener.setServerDistribution(sd);

     //  NakedObjects.getObjectPersistor().addObjectChangedListener(updateNotifier);

        serverListener.start();
        

        InfoDebugFrame debugFrame = new InfoDebugFrame() {
            private static final long serialVersionUID = 1L;

            public void dialogClosing() {
                System.exit(0);
            }
        };
        DebugInfo[] debugInfo = new DebugInfo[] { NakedObjects.debug(), NakedObjects.getObjectPersistor(),
                NakedObjects.getObjectLoader(), NakedObjects.getConfiguration(), NakedObjects.getSpecificationLoader(),
                updateNotifier };
        debugFrame.setInfo(debugInfo);
        debugFrame.setBounds(10, 10, 1000, 800);
        debugFrame.refresh();
        debugFrame.show();
    }
    
    protected NakedObjectSpecificationLoader createReflector() {
        ReflectionPeerFactory[] factories = new ReflectionPeerFactory[] {
                new TransactionPeerFactory(),
        };
        JavaSpecificationLoader specificationLoader = new JavaSpecificationLoader();
        specificationLoader.setReflectionPeerFactories(factories);
        return specificationLoader;
    }
    
    protected NakedObjectPersistor createPersistor() {
        NakedObjectPersistor persistor = super.createPersistor();
        persistor.addObjectChangedListener(updateNotifier);
        return persistor;
    }
    
}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
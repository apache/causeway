
package org.nakedobjects.distribution.broadcast;

import org.nakedobjects.container.configuration.ConfigurationFactory;


class Parameters {
    private static final String PREFIX = "connection.simple";
    private static final String REQUEST_ADDRESS = PREFIX + ".address";
    private static final String MESSAGE_PORT = PREFIX + ".message-port";
    private static final String FILE_PORT = PREFIX + ".file-port";
    private final static String UPDATE_ADDRESS = PREFIX + ".udp-group";
    private final static String UPDATE_PORT = PREFIX + ".udp-port";
    private final static String UPDATE_PACKAGE_SIZE = PREFIX + ".udp-size";
    private final static String UPDATE_TTL = PREFIX + ".udp-ttl";
    private final static String DEFAULT_UPDATE_ADDRESS = "225.5.2.2";
    private final static int DEFAULT_PORT = 6561;
    private final static int DEFAULT_TTL = 4;
    private final static int DEFAULT_PACKAGE_SIZE = 8192;
    private static final int DEFAULT_REQUEST_PORT = 2401;
    private static final int DEFAULT_FILE_PORT = 2045;
    private final String host;
    private final int requestPort;
    private final int filePort;
    private final String updateAddress;
    private final int updatePort;
    private final int updatePackageSize;
    private final int UpdateTtl;

    Parameters() {
        ConfigurationFactory params = ConfigurationFactory.getConfiguration();

        host = params.getString(REQUEST_ADDRESS);

        requestPort = params.getInteger(MESSAGE_PORT, DEFAULT_REQUEST_PORT);
        filePort = params.getInteger(FILE_PORT, DEFAULT_FILE_PORT);

        updateAddress = params.getString(UPDATE_ADDRESS, DEFAULT_UPDATE_ADDRESS);
        updatePort = params.getInteger(UPDATE_PORT, DEFAULT_PORT);
        updatePackageSize = params.getInteger(UPDATE_PACKAGE_SIZE,
                DEFAULT_PACKAGE_SIZE);
        UpdateTtl = params.getInteger(UPDATE_TTL, DEFAULT_TTL);
    }

    public int getFilePort() {
        return filePort;
    }

    public String getHost() {
        return host;
    }

    public int getRequestPort() {
        return requestPort;
    }

    public String getUpdateAddress() {
        return updateAddress;
    }

    public int getUpdatePackageSize() {
        return updatePackageSize;
    }

    public int getUpdatePort() {
        return updatePort;
    }

    public int getUpdateTtl() {
        return UpdateTtl;
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General private License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General private License for more details.

You should have received a copy of the GNU General private License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
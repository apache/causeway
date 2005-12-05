package org.nakedobjects.example.movie.exploration;

import org.nakedobjects.example.movie.objectstore.ObjectStoreException;
import org.nakedobjects.example.movie.objectstore.SqlOid;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.persistence.OidGenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlOidGenerator implements OidGenerator {
    private int blockSize = 100;
    private int id;
    private int lastInBlock;

    public Oid next(Naked object) {
        if(id > lastInBlock) {
            nextBlock();
        }
        return new SqlOid(id ++);
    }

    public String name() {
        return "SQL OID Generator";
    }

    public void init() {
        nextBlock();
    }

    private void nextBlock() {
        Connection connection = null;
        String query = "select id from id_sequence";
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/movies", "rcm", "rcm");
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(query);
            if(rs.next()) {
                id = rs.getInt(1);
            } else {
                id = 0;
            }
            rs.close();
            s.close();
            
            lastInBlock = id + blockSize;
            
            PreparedStatement s2 = connection.prepareStatement("insert into id_sequence values(?)");
            s2.setInt(1, lastInBlock);
            s2.execute();
            s2.close();
        } catch (SQLException e) {
            throw new ObjectStoreException("Failed to initialise id sequence", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new ObjectStoreException("Failed to close connection", e);
                }
            }
        }
    }

    public void shutdown() {/**
    could check lastInBlock against DB and move DB back to id if the same - this would avoid two servers overwriting each other. 
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/movies", "rcm", "rcm");
            PreparedStatement s2 = connection.prepareStatement("insert into id values(?)");
            s2.setInt(1, id);
            s2.execute();
            s2.close();
        } catch (SQLException e) {
            throw new ObjectStoreException("Failed to update id sequence", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new ObjectStoreException("Failed to close connection", e);
                }
            }
        }
        */
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
    
    public int getBlockSize() {
        return blockSize;
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
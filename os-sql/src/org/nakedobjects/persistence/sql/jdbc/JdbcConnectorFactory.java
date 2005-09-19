package org.nakedobjects.persistence.sql.jdbc;

import org.nakedobjects.application.valueholder.Color;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.application.valueholder.FloatingPointNumber;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.Money;
import org.nakedobjects.application.valueholder.MultilineTextString;
import org.nakedobjects.application.valueholder.Option;
import org.nakedobjects.application.valueholder.Percentage;
import org.nakedobjects.application.valueholder.SerialNumber;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.application.valueholder.Time;
import org.nakedobjects.application.valueholder.WholeNumber;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.DatabaseConnectorFactory;
import org.nakedobjects.persistence.sql.ValueMapperLookup;


public class JdbcConnectorFactory implements DatabaseConnectorFactory {

    public JdbcConnectorFactory() {
        ValueMapperLookup lookup = ValueMapperLookup.getInstance();

        lookup.add(TextString.class, new JdbcGeneralValueHolderMapper("VARCHAR(65)"));
        lookup.add(Option.class, new JdbcGeneralValueHolderMapper("VARCHAR(65)"));
        lookup.add(MultilineTextString.class, new JdbcGeneralValueHolderMapper("VARCHAR(250)"));
        lookup.add(WholeNumber.class, new JdbcGeneralValueHolderMapper("INT"));
        lookup.add(SerialNumber.class, new JdbcGeneralValueHolderMapper("INT"));
        lookup.add(FloatingPointNumber.class, new JdbcGeneralValueHolderMapper("FLOAT"));
        lookup.add(Money.class, new JdbcGeneralValueHolderMapper("FLOAT"));
        lookup.add(Percentage.class, new JdbcGeneralValueHolderMapper("FLOAT"));
        lookup.add(Logical.class, new JdbcGeneralValueHolderMapper("VARCHAR(21)"));
        lookup.add(Color.class, new JdbcGeneralValueHolderMapper("INT"));
        
        
        lookup.add(String.class, new JdbcGeneralValueMapper("VARCHAR(65)"));
          
        lookup.add(Date.class, new JdbcDateMapper());
        lookup.add(Time.class, new JdbcTimeMapper());
 /*
        lookup.add(DateTime.class, new JdbcDateTimeMapper());
        lookup.add(TimeStamp.class, new JdbcTimestampMapper());
        lookup.add(DatePeriod.class, new JdbcGeneralValueMapper("VHARCHAR(21)"));
        
        */
    }

    public DatabaseConnector createConnector() {
        return new JdbcConnector();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
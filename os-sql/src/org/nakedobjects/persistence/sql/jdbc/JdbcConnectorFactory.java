package org.nakedobjects.persistence.sql.jdbc;

import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.DatePeriod;
import org.nakedobjects.object.defaults.value.DateTime;
import org.nakedobjects.object.defaults.value.FloatingPointNumber;
import org.nakedobjects.object.defaults.value.Logical;
import org.nakedobjects.object.defaults.value.Money;
import org.nakedobjects.object.defaults.value.MultilineTextString;
import org.nakedobjects.object.defaults.value.Option;
import org.nakedobjects.object.defaults.value.Percentage;
import org.nakedobjects.object.defaults.value.SerialNumber;
import org.nakedobjects.object.defaults.value.TextString;
import org.nakedobjects.object.defaults.value.Time;
import org.nakedobjects.object.defaults.value.TimeStamp;
import org.nakedobjects.object.defaults.value.WholeNumber;
import org.nakedobjects.persistence.sql.DatabaseConnector;
import org.nakedobjects.persistence.sql.DatabaseConnectorFactory;
import org.nakedobjects.persistence.sql.ValueMapperLookup;


public class JdbcConnectorFactory implements DatabaseConnectorFactory {

    public JdbcConnectorFactory() {
        ValueMapperLookup lookup = ValueMapperLookup.getInstance();

        lookup.add(TextString.class, new JdbcGeneralValueMapper("VARCHAR(65)"));
        lookup.add(Option.class, new JdbcGeneralValueMapper("VARCHAR(65)"));
        lookup.add(MultilineTextString.class, new JdbcGeneralValueMapper("VARCHAR(250)"));
        lookup.add(WholeNumber.class, new JdbcGeneralValueMapper("INT"));
        lookup.add(SerialNumber.class, new JdbcGeneralValueMapper("INT"));
        lookup.add(FloatingPointNumber.class, new JdbcGeneralValueMapper("FLOAT"));
        lookup.add(Money.class, new JdbcGeneralValueMapper("FLOAT"));
        lookup.add(Percentage.class, new JdbcGeneralValueMapper("FLOAT"));
        lookup.add(Date.class, new JdbcDateMapper());
        lookup.add(Time.class, new JdbcTimeMapper());
        lookup.add(DateTime.class, new JdbcDateTimeMapper());
        lookup.add(TimeStamp.class, new JdbcTimestampMapper());
        lookup.add(DatePeriod.class, new JdbcGeneralValueMapper("VHARCHAR(21)"));
        lookup.add(Logical.class, new JdbcGeneralValueMapper("VHARCHAR(21)"));
    }

    public DatabaseConnector createConnector() {
        return new JdbcConnector();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
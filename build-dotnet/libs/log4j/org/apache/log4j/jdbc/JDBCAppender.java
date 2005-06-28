package org.apache.log4j.jdbc;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;
import org.apache.log4j.PatternLayout;

import java.util.ArrayList;
import java.util.Iterator;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;


/**
   <p><b><font color="#FF2222">WARNING: This version of JDBCAppender
   is very likely to be completely replaced in the future. Moreoever,
   it does not log exceptions.</font></b> </p>

  The JDBCAppender provides for sending log events to a database.
  
  
  <p>Each append call adds to an <code>ArrayList</code> buffer.  When
  the buffer is filled each log event is placed in a sql statement
  (configurable) and executed.

  <b>BufferSize</b>, <b>db URL</b>, <b>User</b>, & <b>Password</b> are
  configurable options in the standard log4j ways.

  <p>The <code>setSql(String sql)</code> sets the SQL statement to be
  used for logging -- this statement is sent to a
  <code>PatternLayout</code> (either created automaticly by the
  appender or added by the user).  Therefore by default all the
  conversion patterns in <code>PatternLayout</code> can be used
  inside of the statement.  (see the test cases for examples)

  <p>Overriding the {@link #getLogStatement} method allows more
  explicit control of the statement used for logging.

  <p>For use as a base class:

    <ul>

    <li>Override <code>getConnection()</code> to pass any connection
    you want.  Typically this is used to enable application wide
    connection pooling.

     <li>Override <code>closeConnection(Connection con)</code> -- if
     you override getConnection make sure to implement
     <code>closeConnection</code> to handle the connection you
     generated.  Typically this would return the connection to the
     pool it came from.

     <li>Override <code>getLogStatement(LoggingEvent event)</code> to
     produce specialized or dynamic statements. The default uses the
     sql option value.

    </ul>

    @author: Kevin Steppe (<A HREF="mailto:ksteppe@pacbell.net">ksteppe@pacbell.net</A>)

*/
public class JDBCAppender extends org.apache.log4j.AppenderSkeleton
    implements org.apache.log4j.Appender {

  /**
   * URL of the DB for default connection handling
   */
  protected String databaseURL = "jdbc:odbc:myDB";

  /**
   * User to connect as for default connection handling
   */
  protected String databaseUser = "me";

  /**
   * User to use for default connection handling
   */
  protected String databasePassword = "mypassword";

  /**
   * Connection used by default.  The connection is opened the first time it
   * is needed and then held open until the appender is closed (usually at
   * garbage collection).  This behavior is best modified by creating a
   * sub-class and overriding the <code>getConnection</code> and
   * <code>closeConnection</code> methods.
   */
  protected Connection connection = null;

  /**
   * Stores the string given to the pattern layout for conversion into a SQL
   * statement, eg: insert into LogTable (Thread, Class, Message) values
   * ("%t", "%c", "%m")
   *
   * Be careful of quotes in your messages!
   *
   * Also see PatternLayout.
   */
  protected String sqlStatement = "";

  /**
   * size of LoggingEvent buffer before writting to the database.
   * Default is 1.
   */
  protected int bufferSize = 1;

  /**
   * ArrayList holding the buffer of Logging Events.
   */
  protected ArrayList buffer;

  /**
   * Helper object for clearing out the buffer
   */
  protected ArrayList removes;

  public JDBCAppender() {
    super();
    buffer = new ArrayList(bufferSize);
    removes = new ArrayList(bufferSize);
  }

  /**
   * Adds the event to the buffer.  When full the buffer is flushed.
   */
  public void append(LoggingEvent event) {
    buffer.add(event);

    if (buffer.size() >= bufferSize)
      flushBuffer();
  }

  /**
   * By default getLogStatement sends the event to the required Layout object.
   * The layout will format the given pattern into a workable SQL string.
   *
   * Overriding this provides direct access to the LoggingEvent
   * when constructing the logging statement.
   *
   */
  protected String getLogStatement(LoggingEvent event) {
    return getLayout().format(event);
  }

  /**
   *
   * Override this to provide an alertnate method of getting
   * connections (such as caching).  One method to fix this is to open
   * connections at the start of flushBuffer() and close them at the
   * end.  I use a connection pool outside of JDBCAppender which is
   * accessed in an override of this method.
   * */
  protected void execute(String sql) throws SQLException {

    Connection con = null;
    Statement stmt = null;

    try {
        con = getConnection();

        stmt = con.createStatement();
        stmt.executeUpdate(sql);
    } catch (SQLException e) {
       if (stmt != null)
	     stmt.close();
       throw e;
    }
    stmt.close();
    closeConnection(con);

    //System.out.println("Execute: " + sql);
  }


  /**
   * Override this to return the connection to a pool, or to clean up the
   * resource.
   *
   * The default behavior holds a single connection open until the appender
   * is closed (typically when garbage collected).
   */
  protected void closeConnection(Connection con) {
  }

  /**
   * Override this to link with your connection pooling system.
   *
   * By default this creates a single connection which is held open
   * until the object is garbage collected.
   */
  protected Connection getConnection() throws SQLException {
      if (!DriverManager.getDrivers().hasMoreElements())
	     setDriver("sun.jdbc.odbc.JdbcOdbcDriver");

      if (connection == null) {
        connection = DriverManager.getConnection(databaseURL, databaseUser,
					databasePassword);
      }

      return connection;
  }

  /**
   * Closes the appender, flushing the buffer first then closing the default
   * connection if it is open.
   */
  public void close()
  {
    flushBuffer();

    try {
      if (connection != null && !connection.isClosed())
          connection.close();
    } catch (SQLException e) {
        errorHandler.error("Error closing connection", e, ErrorCode.GENERIC_FAILURE);
    }
    this.closed = true;
  }

  /**
   * loops through the buffer of LoggingEvents, gets a
   * sql string from getLogStatement() and sends it to execute().
   * Errors are sent to the errorHandler.
   *
   * If a statement fails the LoggingEvent stays in the buffer!
   */
  public void flushBuffer() {
    //Do the actual logging
    removes.ensureCapacity(buffer.size());
    for (Iterator i = buffer.iterator(); i.hasNext();) {
      try {
        LoggingEvent logEvent = (LoggingEvent)i.next();
	    String sql = getLogStatement(logEvent);
	    execute(sql);
        removes.add(logEvent);
      }
      catch (SQLException e) {
	    errorHandler.error("Failed to excute sql", e,
			   ErrorCode.FLUSH_FAILURE);
      }
    }
    buffer.removeAll(removes);
    //buffer.clear();
  }


  /** closes the appender before disposal */
  public void finalize() {
    close();
  }


  /**
   * JDBCAppender requires a layout.
   * */
  public boolean requiresLayout() {
    return true;
  }


  /**
   *
   */
  public void setSql(String s) {
    sqlStatement = s;
    if (getLayout() == null) {
        this.setLayout(new PatternLayout(s));
    }
    else {
        ((PatternLayout)getLayout()).setConversionPattern(s);
    }
  }


  /**
   * Returns pre-formated statement eg: insert into LogTable (msg) values ("%m")
   */
  public String getSql() {
    return sqlStatement;
  }


  public void setUser(String user) {
    databaseUser = user;
  }


  public void setURL(String url) {
    databaseURL = url;
  }


  public void setPassword(String password) {
    databasePassword = password;
  }


  public void setBufferSize(int newBufferSize) {
    bufferSize = newBufferSize;
    buffer.ensureCapacity(bufferSize);
    removes.ensureCapacity(bufferSize);
  }


  public String getUser() {
    return databaseUser;
  }


  public String getURL() {
    return databaseURL;
  }


  public String getPassword() {
    return databasePassword;
  }


  public int getBufferSize() {
    return bufferSize;
  }


  /**
   * Ensures that the given driver class has been loaded for sql connection
   * creation.
   */
  public void setDriver(String driverClass) {
    try {
      Class.forName(driverClass);
    } catch (Exception e) {
      errorHandler.error("Failed to load driver", e,
			 ErrorCode.GENERIC_FAILURE);
    }
  }
}


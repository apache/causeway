


package org.apache.log4j.spi;


/**

   The <code>LogManager</code> uses one (and only one)
   <code>RepositorySelector</code> implementation to select the
   {@link LoggerRepository} for a particular application context.

   <p>It is the responsability of the <code>RepositorySelector</code>
   implementation to track the application context. Log4j makes no
   assumptions about the application context or on its management.

   <p>See also {@link org.apache.log4j.LogManager LogManager}.

   @author Ceki G&uuml;lc&uuml;
   @since 1.2

 */
public interface RepositorySelector {

  /**
     Returns a {@link LoggerRepository} depending on the
     context. Implementors must make sure that a valid (non-null)
     LoggerRepository is returned.
  */
  public
  LoggerRepository getLoggerRepository();
}


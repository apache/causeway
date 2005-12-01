package org.nakedobjects.utility.logging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.TriggeringEventEvaluator;


public class WebSnapshotAppender extends SnapshotAppender {
    private static class HttpQueryWriter extends OutputStreamWriter {

        private int parameter = 1;

        public HttpQueryWriter(OutputStream outputStream) throws UnsupportedEncodingException {
            super(outputStream, "ASCII");
        }

        public void addParameter(String name, String value) throws IOException {
            if (name == null || value == null) {
                return;
            }

            if (parameter > 1) {
                write("&");
            }
            parameter++;
            write(URLEncoder.encode(name));
            write("=");
            write(URLEncoder.encode(value));
        }

        public void close() throws IOException {
            write("\r\n");
            flush();
            super.close();
        }
    }
    private static final Logger LOG = Logger.getLogger(WebSnapshotAppender.class);
    private String proxyAddress;
    private int proxyPort = -1;

    private String url_spec = "http://development.nakedobjects.net/errors/log.php";

    /**
     * The default constructor will instantiate the appender with a {@link TriggeringEventEvaluator} that will
     * trigger on events with level ERROR or higher.
     */
    public WebSnapshotAppender() {
        this(new DefaultEvaluator());
    }

    public WebSnapshotAppender(TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyAddress(String proxyAddess) {
        this.proxyAddress = proxyAddess;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setUrl(String url) {
        url_spec = url;
    }

    protected void writeSnapshot(String message, String details) {
        try {
            URL url = proxyAddress == null ? new URL(url_spec) : new URL("http", proxyAddress, proxyPort, url_spec);
            LOG.info("connect to " + url);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            HttpQueryWriter out = new HttpQueryWriter(connection.getOutputStream());
            out.addParameter("error", message);
            out.addParameter("trace", details);
            out.close();

            InputStream in = connection.getInputStream();
            int c;
            StringBuffer result = new StringBuffer();
            while ((c = in.read()) != -1) {
                result.append((char) c);
            }
            LOG.info(result);

            in.close();

        } catch (UnknownHostException e) {
            LOG.info("could not find host (unknown host) to submit log to");
        } catch (IOException e) {
            LOG.debug("i/o problem submitting log", e);
        }
    }

}

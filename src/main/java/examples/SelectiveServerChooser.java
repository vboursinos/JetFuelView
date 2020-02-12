package examples;

import com.crankuptheamps.client.Authenticator;
import com.crankuptheamps.client.ConnectionInfo;
import com.crankuptheamps.client.ServerChooser;
import com.crankuptheamps.client.exception.DisconnectedException;

import java.util.Collections;
import java.util.Vector;

/**
 * Simple server chooser implementation that tracks the
 * number of failures for each URI and returns the URI
 * with the lowest failure count.
 */

class SelectiveServerChooser implements ServerChooser {


    /**
     * Construct a new server chooser.
     */

    public SelectiveServerChooser() {
    }

    /**
     * Add a new URI to the server chooser. When a URI is added,
     * the chooser also randomly chooses a URI as current, with
     * the newly added URI as one of the possible choices.
     *
     * @param uri the URI to add
     * @return this server chooser
     */
    public SelectiveServerChooser add(String uri) {
        _uris.add(uri);
        _failures.add(0);
        chooseNext();
        return this;
    }

    /**
     * Advance to the next URI. For this server chooser,
     * the next URI is whichever URI has the lowest fail count.
     * If the lowest fail count is the current URI, then the
     * adjacent URI will be selected.
     */
    public void chooseNext() {
        int minFailsIndex = _failures.indexOf(Collections.min(_failures));
        if (minFailsIndex != _currentURI) {
            _currentURI = minFailsIndex;
        } else
            _currentURI = (++_currentURI % _uris.size());
    }

    /**
     * Return the current URI. This method does not change the
     * current URI or advance to the next URI.
     *
     * @return the current URI
     */

    public String getCurrentURI() {
        return _uris.get(_currentURI);
    }

    /**
     * Always returns null. This implementation doesn't provide an
     * authenticator.
     *
     * @return always returns null
     */

    public Authenticator getCurrentAuthenticator() {
        return null;
    }


    /**
     * Handle connection failures. As with DefaultServerChooser, if
     * the client is simply disconnected, retry the same URI. If there is
     * a different error (for example, connection refused), choose another
     * URI.
     *
     * @param exception the exception for the failure
     * @param info      information about the connection, unused in this chooser
     */

    public void reportFailure(Exception exception, ConnectionInfo info) {
        // In either case we'll just move on to the next
        // server whenever we have a failure connecting.
        // If we just got disconnected, though, we'll retry.
        if (!(exception instanceof DisconnectedException)) {
            _failures.set(_currentURI, _failures.get(_currentURI) + 1);
            chooseNext();
        }
    }

    /**
     * Provide additional detail for an exception when the instance
     * isn't available -- not implemented in this chooser.
     */
    public String getError() {
        return "";
    }

    /**
     * Not used in this server chooser.
     *
     * @param info information about the connection, unused in this chooser
     */

    public void reportSuccess(ConnectionInfo info) {
        // This implementation does not use success.
    }

    // Vector to hold the failure count of each URI.
    private Vector<Integer> _failures = new Vector<Integer>();

    // Vector to hold the current list of URIs.  
    private Vector<String> _uris = new Vector<String>();

    // Index of the current URI.
    private int _currentURI = 0;
}


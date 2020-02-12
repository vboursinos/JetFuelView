package examples;

import com.crankuptheamps.client.Authenticator;
import com.crankuptheamps.client.ConnectionInfo;
import com.crankuptheamps.client.ServerChooser;
import com.crankuptheamps.client.exception.DisconnectedException;

import java.util.Random;
import java.util.Vector;

/**
 * Simple server chooser implementation that returns
 * a random server from the list of servers provided.
 * This implements a simple form of load balancing for
 * clients that place similar demands on the servers.
 */

class RandomServerChooser implements ServerChooser {


    /**
     * Construct a new server chooser.
     */

    public RandomServerChooser() {
        _random = new Random();
    }

    /**
     * Construct a new RandomServerChooser using a specific
     * seed for the random number generator. Using a seed
     * means that the chooser will generate URIs in the same
     * order each time.
     *
     * @param seed seed for the random number generator
     */

    public RandomServerChooser(long seed) {
        _random = new Random(seed);
    }

    /**
     * Add a new URI to the server chooser. When a URI is added,
     * the chooser also randomly chooses a URI as current, with
     * the newly added URI as one of the possible choices.
     *
     * @param uri the URI to add
     * @return this server chooser
     */
    public RandomServerChooser add(String uri) {
        _uris.add(uri);
        _current = _random.nextInt(_uris.size());
        return this;
    }

    /**
     * Advance to the next URI. For the random server chooser,
     * the next URI is whichever URI is random.
     */
    public void next() {

        // Protect against choosing the same URI twice in a row.
        int old = _current;
        while (old == _current) {
            _current = _random.nextInt(_uris.size());
        }
    }

    /**
     * Return the current URI. This method does not change the
     * current URI or advance to the next URI.
     *
     * @return the current URI
     */

    public String getCurrentURI() {
        return _uris.get(_current);
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
     * URI at random.
     *
     * @param exception the exception for the failure
     * @param info      information about the connection, unused in this chooser
     */

    public void reportFailure(Exception exception, ConnectionInfo info) {
        // In either case we'll just move on to the next
        // server whenever we have a failure connecting.
        // If we just got disconnected, though, we'll retry.
        if (!(exception instanceof DisconnectedException)) {
            next();
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

    // Random number generator.
    private Random _random;

    // Vector to hold the current list of URIs.  
    private Vector<String> _uris = new Vector<String>();

    // Index of the current URI.
    private int _current;
}

package headfront.amps;

import com.crankuptheamps.client.ConnectionInfo;
import com.crankuptheamps.client.DefaultServerChooser;
import headfront.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Created by Deepak on 09/07/2016.
 */
public class ServerChooser extends DefaultServerChooser {

    private long connectedTime = 0;

    private static final Logger LOG = LoggerFactory.getLogger(ServerChooser.class);
    private Consumer<Boolean> connectedListener = connected -> {
    };
    private Runnable forceStopListener;

    public ServerChooser(Consumer<Boolean> connectedListener, Runnable forceStopListener) {
        this.connectedListener = connectedListener;
        this.forceStopListener = forceStopListener;
    }

    @Override
    public void reportFailure(Exception exception, ConnectionInfo info) throws Exception {
        super.reportFailure(exception, info);
        String conNameNoPassword = StringUtils.removePassword(info.toString());
//        if (exception instanceof DisconnectedException){} if you want to knoq=w if we were disconnected
        LOG.error("Disconnected from amps  " + conNameNoPassword, exception);
        connectedListener.accept(false);
    }

    @Override
    public void reportSuccess(ConnectionInfo info) {
        super.reportSuccess(info);
        String conNameNoPassword = StringUtils.removePassword(info.toString());
        LOG.info("Connected to amps " + conNameNoPassword);
        connectedListener.accept(true);
        checkConnectionDisconnection();
        connectedTime = System.currentTimeMillis();
    }

    private void checkConnectionDisconnection() {
        long now = System.currentTimeMillis();
        if (now - connectedTime < 100) {
            LOG.info("Very quick connections and disconnections. Lets stop.");
            forceStopListener.run();
        }

    }
}

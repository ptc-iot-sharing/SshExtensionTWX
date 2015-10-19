package com.thingworx.extensions.sshExtension;

import ch.qos.logback.classic.Logger;
import com.thingworx.extensions.sshExtension.jsch.SshServerImpl;
import com.thingworx.logging.LogUtilities;
import org.joda.time.DateTime;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A ssh server that connects on demand, and disconnects on timeout
 * This regular checks when was the last message and, if a certain time has passed,
 * closes the connection.
 */
public class ManagedSshServer extends TimerTask {
    private static Logger LOGGER = LogUtilities.getInstance().getApplicationLogger(SshServerThing.class);

    private SshServer server;
    private SshConfiguration config;
    private DateTime lastCommand;

    public ManagedSshServer(SshConfiguration config) throws SshException {
        this.config = config;
        server = new SshServerImpl(config);
        new Timer("SshKeepAliveThread", true).scheduleAtFixedRate(this, config.getKeepAliveTimeout() / 2,
                config.getKeepAliveTimeout() / 2);
        lastCommand = new DateTime();
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        long lastCommandDelta = new DateTime().getMillis() - lastCommand.getMillis();
        if (lastCommandDelta > config.getKeepAliveTimeout() && !server.isDisconnected()) {
            try {
                server.close();
                LOGGER.info(String.format("Closed sshServer %s@%s no messages in last %d ms",
                        config.getUsername(), config.getHost(), lastCommandDelta));
            } catch (Exception e) {
                LOGGER.warn("Failed to close ssh server");
            }
        }
    }

    public SshServer getServer() throws SshException {
        if (server.isDisconnected()) {
            server = new SshServerImpl(config);
        }
        lastCommand = new DateTime();
        return server;
    }
}

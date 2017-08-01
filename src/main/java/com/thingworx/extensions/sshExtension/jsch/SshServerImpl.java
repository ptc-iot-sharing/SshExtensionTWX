package com.thingworx.extensions.sshExtension.jsch;

import ch.qos.logback.classic.Logger;
import com.jcraft.jsch.*;
import com.thingworx.common.utils.StringUtilities;
import com.thingworx.extensions.sshExtension.SshConfiguration;
import com.thingworx.extensions.sshExtension.SshServer;
import com.thingworx.extensions.sshExtension.SshServerThing;
import com.thingworx.logging.LogUtilities;

import java.io.IOException;
import java.io.InputStream;

/**
 * A implementation of the SshServer based on the Jsch library
 */
public class SshServerImpl implements SshServer {
    private static Logger LOGGER = LogUtilities.getInstance().getApplicationLogger(SshServerThing.class);

    private Session session;
    private ChannelExec channel;
    private SshConfiguration config;
    private boolean isDisconnected;

    public SshServerImpl(SshConfiguration config) {
        this.config = config;
    }

    /**
     * Executes a command on the remote server
     *
     * @param command command to execute
     * @return the exit-status
     */
    @Override
    public String executeCommand(String command) {
        try {
            StringBuilder result = new StringBuilder();
            JSch jSch = new JSch();
            // attempt to load the private key
            if (StringUtilities.isNonEmpty(config.getPrivateKey())) {
                // get the passphrase as a byte array
                byte[] passphraseBytes = config.getPassphrase() != null ? config.getPassphrase().getBytes() : null;
                // the private key as a byte array
                byte[] privateKeyBytes = config.getPrivateKey().getBytes();
                // add the identity
                jSch.addIdentity(config.getUsername(), privateKeyBytes, null, passphraseBytes);
            }
            // create a session
            session = jSch.getSession(config.getUsername(), config.getHost(), config.getPort());
            // accept all hosts, don't require a known_hosts list
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(config.getPassword());
            session.connect(config.getConnectionTimeout());
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            channel.setInputStream(System.in);
            channel.setInputStream(null);

            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    result.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    LOGGER.debug("For command " +  command + "exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            channel.disconnect();
            session.disconnect();
            return result.toString();
        } catch (JSchException | IOException e) {
            LOGGER.warn("Fail while executing command " + command, e);
        }
        return "";
    }

    /**
     * Closes the underlying channel and session
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        this.isDisconnected = true;
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }
}

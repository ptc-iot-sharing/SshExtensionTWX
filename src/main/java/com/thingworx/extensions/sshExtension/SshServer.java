package com.thingworx.extensions.sshExtension;

/**
 * An generic interface for interacting with a ssh remote server
 */
public interface SshServer extends AutoCloseable {
    /**
     * Executes a command on the remote server
     * @param command command to execute
     * @return the exit-status
     */
    String executeCommand(String command);

    boolean isDisconnected();
}
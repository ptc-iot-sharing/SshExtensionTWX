package com.thingworx.extensions.sshExtension;

/**
 * Exception that occurs using sftp operations
 */
public class SshException extends Exception {

    public SshException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SshException(String message) {
        super(message);
    }
}

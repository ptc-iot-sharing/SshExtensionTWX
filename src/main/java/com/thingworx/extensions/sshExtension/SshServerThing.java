package com.thingworx.extensions.sshExtension;


import ch.qos.logback.classic.Logger;
import com.thingworx.common.utils.StringUtilities;
import com.thingworx.entities.utils.ThingUtilities;
import com.thingworx.extensions.sshExtension.jsch.SshServerImpl;
import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.annotations.*;
import com.thingworx.things.Thing;
import com.thingworx.things.repository.FileRepositoryThing;


@ThingworxConfigurationTableDefinitions(
        tables = {@ThingworxConfigurationTableDefinition(
                name = "ConnectionInfo",
                description = "SFTP Server Connection Parameters",
                isMultiRow = false,
                ordinal = 0,
                dataShape = @ThingworxDataShapeDefinition(
                        fields = {@ThingworxFieldDefinition(
                                ordinal = 0,
                                name = "host",
                                description = "Server name",
                                baseType = "STRING",
                                aspects = {"defaultValue:localhost", "friendlyName:SFTP Server"}
                        ), @ThingworxFieldDefinition(
                                ordinal = 1,
                                name = "port",
                                description = "Server port",
                                baseType = "INTEGER",
                                aspects = {"defaultValue:22", "friendlyName:SFTP Server Port"}
                        ), @ThingworxFieldDefinition(
                                ordinal = 2,
                                name = "username",
                                description = "Username",
                                baseType = "STRING",
                                aspects = {"defaultValue:root", "friendlyName:SFTP User"}
                        ), @ThingworxFieldDefinition(
                                ordinal = 3,
                                name = "password",
                                description = "Password",
                                baseType = "PASSWORD",
                                aspects = {"friendlyName:SFTP Account Password"}
                        ), @ThingworxFieldDefinition(
                                ordinal = 6,
                                name = "connectionTimeout",
                                description = "Timeout (milliseconds) to establish a connection",
                                baseType = "INTEGER",
                                aspects = {"defaultValue:20000", "friendlyName:Connection Timeout"}
                        ), @ThingworxFieldDefinition(
                                ordinal = 7,
                                name = "keepAliveTimeout",
                                description = "Timeout (milliseconds) before closing a connection",
                                baseType = "INTEGER",
                                aspects = {"defaultValue:60000", "friendlyName:KeepAlive Timeout"}
                        )}
                )
        ),
                @ThingworxConfigurationTableDefinition(
                        name = "Keybasedauth",
                        description = "SFTP key based authentication",
                        isMultiRow = false,
                        ordinal = 1,
                        dataShape = @ThingworxDataShapeDefinition(
                                fields = {@ThingworxFieldDefinition(
                                        ordinal = 0,
                                        name = "sshServer",
                                        description = "The sshServer where the private key is.",
                                        baseType = "THINGNAME",
                                        aspects = {"friendlyName:Repository"}
                                ), @ThingworxFieldDefinition(
                                        ordinal = 1,
                                        name = "privateKey",
                                        description = "File on a sshServer with the private key.",
                                        baseType = "TEXT",
                                        aspects = {"friendlyName:Private key"}
                                ), @ThingworxFieldDefinition(
                                        ordinal = 2,
                                        name = "passphrase",
                                        description = "Passphrase",
                                        baseType = "PASSWORD",
                                        aspects = {"friendlyName: Private key passphrase"}
                                )}
                        )
                )}
)
public class SshServerThing extends Thing {
    private static Logger LOGGER = LogUtilities.getInstance().getApplicationLogger(SshServerThing.class);

    private SshConfiguration config = new SshConfiguration();

    @Override
    protected void initializeThing() throws Exception {
        // get values from the configuration table
        config.setHost((String) this.getConfigurationData().getValue("ConnectionInfo", "host"));
        config.setPort((Integer) this.getConfigurationData().getValue("ConnectionInfo", "port"));
        config.setPassphrase((String) this.getConfigurationData().getValue("Keybasedauth", "passphrase"));
        config.setPassword((String) this.getConfigurationData().getValue("ConnectionInfo", "password"));
        String privateKeyFile = (String) this.getConfigurationData().getValue("Keybasedauth", "privateKey");
        String fileRepo = (String) this.getConfigurationData().getValue("Keybasedauth", "sshServer");
        FileRepositoryThing repoThing = (FileRepositoryThing) ThingUtilities.findThing(fileRepo);
        if (repoThing != null) {
            try {
                String privateKey = repoThing.LoadText(privateKeyFile);
                config.setPrivateKey(privateKey);
            } catch (Exception ex) {
                LOGGER.warn("cannot load the private key file");
            }
        }
        config.setUsername((String) this.getConfigurationData().getValue("ConnectionInfo", "username"));
        config.setConnectionTimeout((Integer) this.getConfigurationData().getValue("ConnectionInfo", "connectionTimeout"));
        config.setKeepAliveTimeout((Integer) this.getConfigurationData().getValue("ConnectionInfo", "keepAliveTimeout"));
        LOGGER.info("Created a sftp thing with config:" + config);
    }

    @ThingworxServiceDefinition(
            name = "ExecuteCommand",
            description = "Executes a command on the remote filesystem",
            category = "Transfers"
    )
    @ThingworxServiceResult(
            name = "result",
            description = "Browse Results",
            baseType = "STRING",
            aspects = {"dataShape:FileSystemFile"}
    )
    public String ExecuteCommand(@ThingworxServiceParameter(
            name = "command",
            description = "Command to execute",
            baseType = "STRING"
    ) String command, @ThingworxServiceParameter(
            name = "username",
            description = "Command to execute",
            baseType = "STRING"
    ) String username, @ThingworxServiceParameter(
            name = "password",
            description = "Command to execute",
            baseType = "STRING"
    ) String password) throws Exception {
        try (SshServer server = new SshServerImpl(config)) {
            if (StringUtilities.isNonEmpty(username)) {
                config.setUsername(username);
            }
            if (StringUtilities.isNonEmpty(password)) {
                config.setPassword(password);
            }
            return server.executeCommand(command);
        } catch (SshException ex) {
            LOGGER.warn("Failed to execute command " + command, ex);
            throw ex;
        }
    }
}

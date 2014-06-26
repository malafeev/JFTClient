package org.jftclient.command;

import java.util.ArrayList;
import java.util.List;

import org.jftclient.ssh.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * @author sergei.malafeev
 */
public class CommandCopyFactory {
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(CommandCopyFactory.class);

    public CommandCopyFactory(Connection connection) {
        this.connection = connection;
    }


    public List<CommandCopy> buildCommands(boolean isSourceLocal, boolean isTargetLocal, String dst,
                                           List<String> srcPaths) {
        List<CommandCopy> commandCopies = new ArrayList<>();

        if (Strings.isNullOrEmpty(dst)) {
            logger.error("target is empty");
            return commandCopies;
        }

        if (srcPaths == null || srcPaths.size() == 0) {
            logger.error("sources are empty");
            return commandCopies;
        }

        if (isSourceLocal && isTargetLocal) {
            // Local to Local
            CommandCopy commandCopy = new CommandCopy("rsync");
            commandCopy.addArg("-a");
            for (String src : srcPaths) {
                commandCopy.addArg(src);
            }
            commandCopy.addArg(dst + "/");
            commandCopies.add(commandCopy);
        } else if (!isSourceLocal && !isTargetLocal) {
            // Remote to Remote
            CommandCopy commandCopy = new CommandCopy("cp");
            commandCopy.addArg("-rf");
            for (String src : srcPaths) {
                commandCopy.addArg("'" + src + "'");
            }
            commandCopy.addArg("'" + dst + "/'");
            commandCopies.add(commandCopy);
        } else {
            CommandCopy commandCopyStart = new CommandCopy("sshpass");
            commandCopyStart.addArg("-p");
            commandCopyStart.addArg(connection.getPassword());
            commandCopyStart.addArg("rsync");
            commandCopyStart.addArg("-e");
            commandCopyStart.addArg("ssh -q -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no");
            commandCopyStart.addArg("-aq");
            if (isSourceLocal && !isTargetLocal) {
                // Local to Remote
                CommandCopy commandCopy = new CommandCopy(commandCopyStart);
                for (String src : srcPaths) {
                    commandCopy.addArg(src);
                }
                commandCopy.addArg(connection.getUser() + "@" + connection.getRemoteHost() + ":'" + dst + "/'");
                commandCopies.add(commandCopy);
            } else if (!isSourceLocal && isTargetLocal) {
                // Remote to Local
                for (String src : srcPaths) {
                    CommandCopy commandCopy = new CommandCopy(commandCopyStart);
                    commandCopy.addArg(connection.getUser() + "@" + connection.getRemoteHost() + ":'" + src + "' ");
                    commandCopy.addArg(dst + "/");
                    commandCopies.add(commandCopy);
                }
            }
        }
        return commandCopies;
    }
}
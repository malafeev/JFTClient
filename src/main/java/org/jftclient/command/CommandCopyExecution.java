package org.jftclient.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.jftclient.JFTText;
import org.jftclient.OutputPanel;
import org.jftclient.ssh.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.text.Text;

import static org.jftclient.JFTText.getLocalHost;

/**
 * @author smalafeev
 */
public class CommandCopyExecution implements Callable<Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(CommandCopyExecution.class);
    private final CommandCopy commandCopy;
    private final String commandWithoutPassword;
    private Process process;
    private OutputPanel outputPanel;

    public CommandCopyExecution(CommandCopy commandCopy, Connection connection) {
        this.commandCopy = commandCopy;
        this.outputPanel = OutputPanel.getInstance();

        commandWithoutPassword = this.commandCopy.toString().replaceFirst("sshpass -p "
                + connection.getPassword() + " ", "").replaceFirst("-e ssh -q -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ", "");
    }

    @Override
    public Boolean call() {

        List<Text> output = new ArrayList<>();
        output.add(getLocalHost());
        output.add(JFTText.textBlack(commandWithoutPassword + "\n"));

        Process pr;
        try {
            pr = setProcess(new ProcessBuilder(commandCopy.toArray()).start());
        } catch (IOException ex) {
            logger.error("failed commandCopy: {}", commandWithoutPassword, ex);
            output.add(JFTText.failed());
            outputPanel.printlnOutputLater(output);
            return false;
        }
        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream(), Charset.defaultCharset()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream(), Charset.defaultCharset()))) {
            String s;

            while ((s = stdInput.readLine()) != null) {
                output.add(JFTText.textBlack(s + "\n"));
            }

            while ((s = stdError.readLine()) != null) {
                output.add(JFTText.textRed(s + "\n"));
            }
        } catch (IOException ex) {
            logger.error("failed {}", commandWithoutPassword, ex);
            output.add(JFTText.failed());
            outputPanel.printlnOutputLater(output);
            return false;
        }

        boolean res = false;
        output.add(JFTText.textBlack(commandWithoutPassword));
        try {
            if (pr.waitFor() != 0) {
                output.add(JFTText.failed());
            } else {
                output.add(JFTText.done());
                res = true;
            }
        } catch (InterruptedException e) {
            logger.warn("interrupted", e);
        }

        outputPanel.printlnOutputLater(output);

        return res;
    }


    public synchronized void destroy() {
        if (process != null) {
            process.destroy();
        }
    }

    private synchronized Process setProcess(Process process) {
        this.process = process;
        return this.process;
    }
}


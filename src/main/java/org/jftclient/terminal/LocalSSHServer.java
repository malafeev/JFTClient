package org.jftclient.terminal;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.keyprovider.ResourceKeyPairProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Singleton
 *
 * @author smalafeev
 */
@Component
public class LocalSSHServer {
    private static final Logger logger = LoggerFactory.getLogger(LocalSSHServer.class);
    private int sshdPort;
    private SshServer sshServer;
    private boolean running;

    public void start() {
        if (running) {
            return;
        }

        sshServer = SshServer.setUpDefaultServer();
        sshdPort = findFreePort();
        sshServer.setPort(sshdPort);

        EnumSet<ProcessShellFactory.TtyOptions> ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr);

        ProcessShellFactory processShellFactory = new ProcessShellFactory(new String[]{"/bin/bash", "-i", "-l"}, ttyOptions);
        sshServer.setShellFactory(processShellFactory);

        String sshdHostKeyPath = "sshd_host_key.pem";
        KeyPairProvider keyPairProvider = new ResourceKeyPairProvider(new String[]{sshdHostKeyPath}, null, Thread.currentThread().getContextClassLoader());
        sshServer.setKeyPairProvider(keyPairProvider);

        sshServer.setPasswordAuthenticator(new PasswordAuthenticator());
        sshServer.setPublickeyAuthenticator(null);

        Map<String, String> props = new HashMap<>();
        props.put(SshServer.IDLE_TIMEOUT, Integer.MAX_VALUE + "");

        sshServer.setProperties(props);

        try {
            sshServer.start();
            running = true;
        } catch (IOException e) {
            logger.error("failed to start ssh server", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (sshServer == null) {
            return;
        }
        try {
            sshServer.stop();
        } catch (InterruptedException e) {
            logger.warn("failed to stop sshd", e);
        }
    }

    private static int findFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            logger.error("cannot find free port");
            throw new RuntimeException(e);
        }
    }

    public int getSshdPort() {
        return sshdPort;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

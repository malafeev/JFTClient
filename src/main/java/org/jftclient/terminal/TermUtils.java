package org.jftclient.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author smalafeev
 */
public class TermUtils {
    private static Logger logger = LoggerFactory.getLogger(TermUtils.class);
    private static Map<Integer, byte[]> keyMap = new HashMap<>();

    public static void openLocalTerm(LocalSSHServer localSSHServer, TerminalPanel terminalPanel) throws JSchException, IOException {

        if (!localSSHServer.isRunning()) {
            localSSHServer.start();
        }

        JSch jsch = new JSch();
        Session session = jsch.getSession("user", "localhost", localSSHServer.getSshdPort());
        session.setPassword("");
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(5000);
        Channel channel = session.openChannel("shell");
        ((ChannelShell) channel).setPtyType("vt102");

        OutputStream inputToChannel = channel.getOutputStream();
        PrintStream printStream = new PrintStream(inputToChannel, true);

        terminalPanel.setPrintStream(printStream);

        ReentrantLock lock = new ReentrantLock();
        Runnable run = new TermOutputWatcher(channel.getExtInputStream(), lock, terminalPanel);
        Thread thread = new Thread(run);
        thread.start();

        Runnable run2 = new TermOutputWatcher(channel.getInputStream(), lock, terminalPanel);
        Thread thread2 = new Thread(run2);
        thread2.start();


        channel.connect();

        /*Terminal terminal = new Terminal();
        terminal.setHost(host);
        terminal.setPrintStream(printStream);
        terminal.setChannel(channel);
        terminal.setSession(session);
        terminal.getWatchers().add(thread);
        terminal.getWatchers().add(thread2);
        */

        //return terminal;
    }


    public static void openRemoteTerminal() {
        //TODO
    }

    public static byte[] getCode(KeyEvent event, TerminalPanel terminalPanel) {
        if (event.getCode() == KeyCode.BACK_SPACE) {
            return keyMap.get(72);
        } else if (event.getCode() == KeyCode.UP) {
            return keyMap.get(38);
        } else if (event.isControlDown() && event.getCode() == KeyCode.L) {
            terminalPanel.getPanel().clear();
            return keyMap.get(76);
        } else if (event.getCode() == KeyCode.SPACE) {
            return new byte[]{0x20};
        } else if (event.getCode() == KeyCode.TAB) {
            return keyMap.get(9);
        } else if (event.getCode() == KeyCode.ESCAPE) {
            return keyMap.get(27);
        }
        return null;
    }

    static {
        //ESC
        keyMap.put(27, new byte[]{(byte) 0x1b});
        //ENTER
        keyMap.put(13, new byte[]{(byte) 0x0d});
        //LEFT
        keyMap.put(37, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x44});
        //UP
        keyMap.put(38, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x41});
        //RIGHT
        keyMap.put(39, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x43});
        //DOWN
        keyMap.put(40, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x42});
        //DEL
        keyMap.put(8, new byte[]{(byte) 0x7f});
        //TAB
        keyMap.put(9, new byte[]{(byte) 0x09});
        //CTR
        keyMap.put(17, new byte[]{});
        //CTR-A
        keyMap.put(65, new byte[]{(byte) 0x01});
        //CTR-B
        keyMap.put(66, new byte[]{(byte) 0x02});
        //CTR-C
        keyMap.put(67, new byte[]{(byte) 0x03});
        //CTR-D
        keyMap.put(68, new byte[]{(byte) 0x04});
        //CTR-E
        keyMap.put(69, new byte[]{(byte) 0x05});
        //CTR-F
        keyMap.put(70, new byte[]{(byte) 0x06});
        //CTR-G
        keyMap.put(71, new byte[]{(byte) 0x07});
        //BACKSPACE
        keyMap.put(72, new byte[]{(byte) 0x08});
        //CTR-I
        keyMap.put(73, new byte[]{(byte) 0x09});
        //CTR-J
        keyMap.put(74, new byte[]{(byte) 0x0A});
        //CTR-K
        keyMap.put(75, new byte[]{(byte) 0x0B});
        //CTR-L
        keyMap.put(76, new byte[]{(byte) 0x0C});
        //CTR-M
        keyMap.put(77, new byte[]{(byte) 0x0D});
        //CTR-N
        keyMap.put(78, new byte[]{(byte) 0x0E});
        //CTR-O
        keyMap.put(79, new byte[]{(byte) 0x0F});
        //CTR-P
        keyMap.put(80, new byte[]{(byte) 0x10});
        //CTR-Q
        keyMap.put(81, new byte[]{(byte) 0x11});
        //CTR-R
        keyMap.put(82, new byte[]{(byte) 0x12});
        //CTR-S
        keyMap.put(83, new byte[]{(byte) 0x13});
        //CTR-T
        keyMap.put(84, new byte[]{(byte) 0x14});
        //CTR-U
        keyMap.put(85, new byte[]{(byte) 0x15});
        //CTR-V
        keyMap.put(86, new byte[]{(byte) 0x16});
        //CTR-W
        keyMap.put(87, new byte[]{(byte) 0x17});
        //CTR-X
        keyMap.put(88, new byte[]{(byte) 0x18});
        //CTR-Y
        keyMap.put(89, new byte[]{(byte) 0x19});
        //CTR-Z
        keyMap.put(90, new byte[]{(byte) 0x1A});
        //CTR-[
        keyMap.put(219, new byte[]{(byte) 0x1B});
        //CTR-]
        keyMap.put(221, new byte[]{(byte) 0x1D});
    }
}

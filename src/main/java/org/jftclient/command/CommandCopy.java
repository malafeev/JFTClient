package org.jftclient.command;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

/**
 * @author smalafeev
 */
public class CommandCopy {
    private List<String> args = new ArrayList<>();

    public CommandCopy(String arg) {
        args.add(arg);
    }

    public CommandCopy(CommandCopy commandCopy) {
        args = new ArrayList<>(commandCopy.args);
    }

    public void addArg(String arg) {
        if (!Strings.isNullOrEmpty(arg)) {
            args.add(arg);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String arg : args) {
            buf.append(arg);
            buf.append(" ");
        }
        return buf.toString().trim();
    }

    public String[] toArray() {
        return args.toArray(new String[args.size()]);
    }
}

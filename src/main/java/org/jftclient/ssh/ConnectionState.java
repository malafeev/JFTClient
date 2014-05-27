package org.jftclient.ssh;

/**
 * @author smalafeev
 */

public class ConnectionState {
    private boolean success;
    private String msg;

    public ConnectionState() {
        success = true;
    }

    public ConnectionState(Exception e, String error) {
        success = false;
        msg = error;
        if (e.getMessage() != null) {
            msg = error + ": " + e.getMessage();
        }
        msg = msg + "\n";
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

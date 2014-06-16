package org.jftclient.sshd;

import org.apache.sshd.server.session.ServerSession;

/**
 * @author sergei.malafeev
 */
public class PasswordAuthenticator implements org.apache.sshd.server.PasswordAuthenticator {

    @Override
    public boolean authenticate(String username, String password, ServerSession arg2) {
        return true;
    }
}

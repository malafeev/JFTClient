package org.jftclient.config;

/**
 * @author smalafeev
 */
public class Host implements Comparable<Host> {
    private String username;
    private String hostname;
    private String password;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Host host = (Host) o;

        if (hostname != null ? !hostname.equals(host.hostname) : host.hostname != null) return false;


        return true;
    }

    @Override
    public int hashCode() {
        int result = hostname != null ? hostname.hashCode() : 0;
        result = 31 * result;
        return result;
    }

    @Override
    public int compareTo(Host o) {
        return hostname.compareTo(o.getHostname());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

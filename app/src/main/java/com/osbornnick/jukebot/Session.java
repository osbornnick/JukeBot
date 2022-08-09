package com.osbornnick.jukebot;

public class Session {

    // session name = uid
    public String mSessionName;
    public String mSessionHost;

    public Session(String mSessionName, String mSessionHost) {
        this.mSessionName = mSessionName;
        this.mSessionHost = mSessionHost;
    }

    public Session(){

    }

    public String getmSessionName() {
        return mSessionName;
    }

    public void setmSessionName(String mSessionName) {
        this.mSessionName = mSessionName;
    }

    public String getmSessionHost() {
        return mSessionHost;
    }

    public void setmSessionHost(String mSessionHost) {
        this.mSessionHost = mSessionHost;
    }
}

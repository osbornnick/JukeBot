package com.osbornnick.jukebot;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Session)) return false;
        return Objects.equals(((Session) o).getmSessionName(), this.getmSessionName());
    }
}

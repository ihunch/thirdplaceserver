package org.thirdplace.bean;

import org.xmpp.packet.JID;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 30/12/14
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutUserDAO
{
    public final static String UserID_Column = "hangoutuserid";
    public final static String Username_Column = "username";
    public final static String JID_Column = "jid";
    public final static String GoingStatus_Column = "goingstatus";

    private long hangoutuserid;
    private String username;
    private JID jid;
    private String goingstatus;
    private long hangoutid;

    public long getHangoutuserid() {
        return hangoutuserid;
    }

    public void setHangoutuserid(long hangoutuserid) {
        this.hangoutuserid = hangoutuserid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JID getJid() {
        return jid;
    }

    public void setJid(JID jid) {
        this.jid = jid;
    }

    public String getGoingstatus() {
        return goingstatus;
    }

    public void setGoingstatus(String goingstatus) {
        this.goingstatus = goingstatus;
    }

    public long getHangoutid() {
        return hangoutid;
    }

    public void setHangoutid(long hangoutid) {
        this.hangoutid = hangoutid;
    }
}

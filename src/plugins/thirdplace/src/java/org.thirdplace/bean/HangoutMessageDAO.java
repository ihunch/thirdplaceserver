package org.thirdplace.bean;

import org.xmpp.packet.JID;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 30/12/14
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutMessageDAO
{
    private long messageid;
    private String content;
    private Date createTime;
    private JID  createUser;
    private Date updateTime;
    private JID  updateUser;
    private long hangoutid;

    public long getMessageid() {
        return messageid;
    }

    public void setMessageid(long messageid) {
        this.messageid = messageid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public JID getCreateUser() {
        return createUser;
    }

    public void setCreateUser(JID createUser) {
        this.createUser = createUser;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public JID getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(JID updateUser) {
        this.updateUser = updateUser;
    }

    public long getHangoutid() {
        return hangoutid;
    }

    public void setHangoutid(long hangoutid) {
        this.hangoutid = hangoutid;
    }
}

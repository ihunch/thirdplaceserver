package org.thirdplace.bean;

import org.xmpp.packet.JID;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 29/12/14
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutDAO
{
    private long hangoutid;
    private String description;
    private JID createUser;
    private Date createDate;
    private boolean closed;
    private boolean timeconfirmed;
    private boolean locationconfirmed;
    private String preferredlocation;
    private List<HangoutLocationDAO> locationDAOList;
    private List<HangoutMessageDAO>  messageDAOList;
    private List<HangoutUserDAO> userDAOList;
    private List<HangoutTimeDAO> timeDAOList;

    public List<HangoutLocationDAO> getLocationDAOList() {
        return locationDAOList;
    }

    public void setLocationDAOList(List<HangoutLocationDAO> locationDAOList) {
        this.locationDAOList = locationDAOList;
    }

    public List<HangoutMessageDAO> getMessageDAOList() {
        return messageDAOList;
    }

    public void setMessageDAOList(List<HangoutMessageDAO> messageDAOList) {
        this.messageDAOList = messageDAOList;
    }

    public List<HangoutUserDAO> getUserDAOList() {
        return userDAOList;
    }

    public void setUserDAOList(List<HangoutUserDAO> userDAOList) {
        this.userDAOList = userDAOList;
    }

    public List<HangoutTimeDAO> getTimeDAOList() {
        return timeDAOList;
    }

    public void setTimeDAOList(List<HangoutTimeDAO> timeDAOList) {
        this.timeDAOList = timeDAOList;
    }

    public long getHangoutid() {
        return hangoutid;
    }

    public void setHangoutid(long hangoutid) {
        this.hangoutid = hangoutid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JID getCreateUser() {
        return createUser;
    }

    public void setCreateUser(JID createUser) {
        this.createUser = createUser;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isTimeconfirmed() {
        return timeconfirmed;
    }

    public void setTimeconfirmed(boolean timeconfirmed) {
        this.timeconfirmed = timeconfirmed;
    }

    public boolean isLocationconfirmed() {
        return locationconfirmed;
    }

    public void setLocationconfirmed(boolean locationconfirmed) {
        this.locationconfirmed = locationconfirmed;
    }

    public String getPreferredlocation() {
        return preferredlocation;
    }

    public void setPreferredlocation(String preferredlocation) {
        this.preferredlocation = preferredlocation;
    }
}

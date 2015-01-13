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
public class HangoutLocationDAO
{
    public final static String LocationID_Column = "locationid";
    public final static String FourSquareID_Column = "foursquareLocationid";
    public final static String LocationConfirm_Column = "locationconfirm";
    public final static String CreateUser_Column = "createUser";
    public final static String CreateTime_Column = "createTime";
    public final static String Hangoutid_Column = "hangoutid";


    private long locationid;
    private long foursquare_locationid;
    private boolean locationconfirm;
    private long hangoutid;
    private Date createTime;
    private JID createUser;
    private Date updateTime;
    private JID  updateUser;

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

    public long getLocationid() {
        return locationid;
    }

    public void setLocationid(long locationid) {
        this.locationid = locationid;
    }

    public long getFoursquare_locationid() {
        return foursquare_locationid;
    }

    public void setFoursquare_locationid(long foursquare_locationid) {
        this.foursquare_locationid = foursquare_locationid;
    }

    public boolean isLocationconfirm() {
        return locationconfirm;
    }

    public void setLocationconfirm(boolean locationconfirm) {
        this.locationconfirm = locationconfirm;
    }

    public long getHangoutid() {
        return hangoutid;
    }

    public void setHangoutid(long hangoutid) {
        this.hangoutid = hangoutid;
    }
}

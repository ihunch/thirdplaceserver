package org.thirdplace.bean;

import org.xmpp.packet.JID;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 30/12/14
 * Time: 11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutTimeDAO
{
    public final static String HangoutTimeID_Column = "hangouttimeid";
    public final static String TimeDescription_Column = "timeDescription";
    public final static String StartDate_Column = "startdate";
    public final static String EndDate_Column = "enddate";
    public final static String CreateTime_Column = "createTime";
    public final static String CreateUser_Column = "createUser";
    public final static String TimeConfirmed_Column = "timeConfirmed";
    public final static String HangoutID_Column = "hangoutid";


    private long hangouttimeid;
    private String timeDescription;
    private Date startdate;
    private Date enddate;
    private Date createTime;
    private Date updateTime;
    private JID  createUser;
    private JID  updateUser;
    private boolean timeConfirmed;
    private long hangoutid;

    public long getHangouttimeid() {
        return hangouttimeid;
    }

    public void setHangouttimeid(long hangouttimeid) {
        this.hangouttimeid = hangouttimeid;
    }

    public String getTimeDescription() {
        return timeDescription;
    }

    public void setTimeDescription(String timeDescription) {
        this.timeDescription = timeDescription;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public JID getCreateUser() {
        return createUser;
    }

    public void setCreateUser(JID createUser) {
        this.createUser = createUser;
    }

    public JID getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(JID updateUser) {
        this.updateUser = updateUser;
    }

    public boolean isTimeConfirmed() {
        return timeConfirmed;
    }

    public void setTimeConfirmed(boolean timeConfirmed) {
        this.timeConfirmed = timeConfirmed;
    }

    public long getHangoutid() {
        return hangoutid;
    }

    public void setHangoutid(long hangoutid) {
        this.hangoutid = hangoutid;
    }
}

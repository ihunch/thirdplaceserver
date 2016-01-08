package org.thirdplace.provider;

import org.dom4j.Element;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.bean.*;
import org.thirdplace.util.DateAdditions;
import org.thirdplace.util.HangoutConstant;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 29/12/14
 * Time: 12:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutServiceProvider
{
    public static final String HANGOUT_ELEMET = "hangout";
    public static final String HANGOUTID_ELEMET = "hangoutid";
    public static final String HANGOUTDESCRIPTION_ELEMET = "description";

    public static final String HANGOUTUSERS_ELEMENT = "users";
    public static final String HANGOUTUSERS_SUBELEMENT = "user";
    public static final String HANGOUT_STARTDATE_ELEMENT = "startdate";
    public static final String HANGOUT_ENDDATE_ELEMENT = "enddate";
    public static final String HANGOUT_MESSAGE_ELEMENT = "message";
    public static final String HANGOUT_MESSAGEID_ELEMENT = "messageid";
    public static final String HANGOUT_TIMEDESCRIPTION_ELEMENT = "timedescription";
    public static final String HANGOUT_TIMECONFIRM_ELEMENT = "timeconfirm";
    public static final String HANGOUTLOCATION_ELEMENT = "locationid";
    public static final String HANGOUT_LOCATIONCONFIRM_ELEMENT = "locationconfirm";
    public static final String HANGOUT_GOINGSTATUS_ELEMENT = "goingstatus";
    public static final String HANGOUT_PERFERREDLOCATION_ELEMENT = "preferredlocation";

    public static final String HANGOUT_CREATEUSER = "createUser";
    public static final String HANGOUT_CREATETIME = "createTime";

    private static final Logger Log = LoggerFactory.getLogger(HangoutServiceProvider.class);

    private static final String CREATE_HANGOUT =
             "INSERT INTO thirdplaceHangout ("+
            "hangoutid, description, createUser, createDate, closed, timeconfirmed, locationconfirmed, preferredlocation)"
            + "VALUES (?,?,?,?,?,?,?,?)";
    private static final String CREATE_HANGOUTTIME =
             "INSERT INTO thirdplaceHangoutTime ("+
                "hangouttimeid, timeDescription, startdate, enddate, createTime, createUser, timeConfirmed, hangoutid)"
                + "VALUES (?,?,?,?,?,?,?,?)";
    private static final String CREATE_HANGOUTUSER =
            "INSERT INTO thirdplaceHangoutUser ("+
                            "hangoutuserid, username, jid, goingstatus, hangoutid)"
                            + "VALUES (?,?,?,?,?)";
    private static final String CREATE_HANGOUTMESSAGE =
            "INSERT INTO thirdplaceHangoutMessage ("+
                                "messageid, content, createTime, createUser, hangoutid)"
                                + "VALUES (?,?,?,?,?)";
    private static final String CREATE_HANGOUTLOCATION =
            "INSERT INTO thirdplaceHangoutLocation ("+
                                            "locationid, foursquareLocationid, locationconfirm,createUser, createTime, hangoutid)"
                                            + "VALUES (?,?,?,?,?,?)";

    private static final String Create_HANGOUTVERSION = "INSERT INTO thirdplaceHangoutVersion (" +
            "versionid, versiontime, hangoutid)" +
            "VALUES (?,?,?)";

    private static final String Update_HANGOUT_LOCATIONCONFIRM = "UPDATE thirdplaceHangout SET "
                + "locationconfirmed=? "
                + "WHERE hangoutid=?";

    private static final String Update_HANGOUT_CLOSED = "UPDATE thirdplaceHangout SET "
                    + "closed=? "
                    + "WHERE hangoutid=?";

    private static final String Update_HANGOUT_TIMECONFIRM = "UPDATE thirdplaceHangout SET "
                        + "timeconfirmed=? "
                        + "WHERE hangoutid=?";

    private static final String Update_UserGoingStatus = "UPDATE thirdplaceHangoutUser SET "
                   + "goingstatus=? "
                   + "WHERE jid=? AND hangoutid=?";

    private static final String Select_HangoutListID_BYJID = "SELECT hangoutuser.hangoutid from thirdplaceHangoutUser as hangoutuser join thirdplaceHangout as hangout " +
            "on hangoutuser.hangoutid = hangout.hangoutid where hangoutuser.jid=? order by hangoutuser.hangoutid desc";
    private static final String Select_HangoutListID_BYJID_Latest = "SELECT hangoutuser.hangoutid, hangout.preferredlocation, hangout.createUser, hangout.createDate, hangout.description " +
            "from thirdplaceHangoutUser as hangoutuser " +
            "join thirdplaceHangout as hangout on hangoutuser.hangoutid = hangout.hangoutid " +
            "join (select hangoutid, MAX(createTime) as LATESTCREATE, enddate from thirdplaceHangoutTime group by hangoutid) groupedtime " +
            "on  hangoutuser.hangoutid = groupedtime.hangoutid " +
            "where hangoutuser.jid = ? AND groupedtime.enddate >= ? order by hangoutuser.hangoutid desc";
    private static final String Select_HANGOUT_BY_ID = "SELECT * from thirdplaceHangout WHERE hangoutid=?";
    private static final String Select_HANGOUT_USER = "SELECT * from thirdplaceHangoutUser WHERE hangoutid=? AND jid=?";
    private static final String Select_HANGOUT_USERS = "SELECT * from thirdplaceHangoutUser WHERE hangoutid=?";
    private static final String Select_Hangout_Time_LATEST_BY_USER = "SELECT * from thirdplaceHangoutTime WHERE hangoutid=? AND createuser=? ORDER BY createTime DESC LIMIT 1";
    private static final String Select_Hangout_Location_LATEST_BY_USER = "SELECT * from thirdplaceHangoutLocation WHERE hangoutid=? AND createuser=? ORDER BY createTime DESC LIMIT 1";
    private static final String Select_Hangout_Message_LATEST_BY_USER = "SELECT * from thirdplaceHangoutMessage WHERE hangoutid=? AND createuser=? ORDER BY createTime DESC LIMIT 1";
    private static final String Select_Hangout_Time_LATEST = "SELECT * from thirdplaceHangoutTime WHERE hangoutid=? ORDER BY createTime DESC LIMIT 1";
    private static final String Select_Hangout_Location_LATEST = "SELECT * from thirdplaceHangoutLocation WHERE hangoutid=? ORDER BY createTime DESC LIMIT 1";
    private static final String Select_Hangout_Message_LATEST = "SELECT * from thirdplaceHangoutMessage WHERE hangoutid=? ORDER BY createTime DESC LIMIT 1";
    private static final String Select_Hangout_Message_ALL = "SELECT * from thirdplaceHangoutMessage WHERE hangoutid=? ORDER BY createTime DESC LIMIT 10";
    public HangoutDAO createHangout(IQ packet)
    {
        Element iq = packet.getChildElement();
        Element hangout = iq.element(HangoutServiceProvider.HANGOUT_ELEMET);
        Connection con = null;
        if (hangout != null)
        {
            try {
                con = DbConnectionManager.getTransactionConnection();
                con.setAutoCommit(false);
                Date now = new Date();
                long hangoutID = SequenceManager
                               .nextID(HangoutConstant.THIRDPLACE_HANGOOUT);
                HangoutDAO hangoutDAO = new HangoutDAO();
                hangoutDAO.setHangoutid(hangoutID);
                hangoutDAO.setCreateDate(now);
                hangoutDAO.setClosed(false);
                hangoutDAO.setLocationconfirmed(false);
                hangoutDAO.setTimeconfirmed(false);
                hangoutDAO.setLocationDAOList(new ArrayList<HangoutLocationDAO>());
                hangoutDAO.setTimeDAOList(new ArrayList<HangoutTimeDAO>());
                hangoutDAO.setMessageDAOList(new ArrayList<HangoutMessageDAO>());
                hangoutDAO.setUserDAOList(new ArrayList<HangoutUserDAO>());

                Element desElement = hangout.element(HangoutServiceProvider.HANGOUTDESCRIPTION_ELEMET);
                if (desElement != null) {
                    String des =  desElement.getText();
                    hangoutDAO.setDescription(des);
                }
                Element preferredlocationElement = hangout.element(HangoutServiceProvider.HANGOUT_PERFERREDLOCATION_ELEMENT);
                if (preferredlocationElement != null)
                {
                    String des = preferredlocationElement.getText();
                    hangoutDAO.setPreferredlocation(des);
                }

                String jidstr = packet.getElement().attributeValue("from");
                JID fromjid = new JID(jidstr);
                hangoutDAO.setCreateUser(fromjid);
                this.createHangout_private(con, hangoutDAO);
                //Hangout Time
                Element startdateElement = hangout.element(HangoutServiceProvider.HANGOUT_STARTDATE_ELEMENT);
                String startdatestr = startdateElement.getText();
                Element enddateElement = hangout.element(HangoutServiceProvider.HANGOUT_ENDDATE_ELEMENT);
                String enddatestr = null;
                if (enddateElement != null)
                {
                    enddatestr = enddateElement.getText();
                }

                Date sdate = DateAdditions.stringToDate(startdatestr, HangoutConstant.Hangout_DATEFORMAT);
                Date edate = DateAdditions.stringToDate(enddatestr, HangoutConstant.Hangout_DATEFORMAT);
                Element timedescription = hangout.element(HangoutServiceProvider.HANGOUT_TIMEDESCRIPTION_ELEMENT);
                long hangouttimeID = SequenceManager
                        .nextID(HangoutConstant.THIRDPLACE_HANGOUTTIME);
                HangoutTimeDAO timeDAO = new HangoutTimeDAO();
                timeDAO.setCreateUser(hangoutDAO.getCreateUser());
                timeDAO.setHangoutid(hangoutDAO.getHangoutid());
                timeDAO.setCreateTime(hangoutDAO.getCreateDate());
                timeDAO.setStartdate(sdate);
                timeDAO.setEnddate(edate);
                timeDAO.setTimeConfirmed(false);
                timeDAO.setHangouttimeid(hangouttimeID);
                timeDAO.setTimeDescription(timedescription.getText());
                this.createHangoutTime(con, timeDAO);
                hangoutDAO.getTimeDAOList().add(timeDAO);
                //HangoutUser
                Element users = hangout.element(HangoutServiceProvider.HANGOUTUSERS_ELEMENT);
                long hangoutuserID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOUTUSER);
                HangoutUserDAO inviter = new HangoutUserDAO();
                inviter.setHangoutid(hangoutID);
                inviter.setGoingstatus(HangoutConstant.GoingStatus);
                inviter.setJid(fromjid);
                inviter.setHangoutuserid(hangoutuserID);
                inviter.setUsername(fromjid.getNode());
                this.createHangoutUser(con,inviter);
                Iterator<Element> itr = users.elementIterator(HangoutServiceProvider.HANGOUTUSERS_SUBELEMENT);
                while (itr.hasNext())
                {
                    hangoutuserID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOUTUSER);
                    Element element = itr.next();
                    String tojidstr = element.getText();
                    JID tojid = new JID(tojidstr);
                    HangoutUserDAO user = new HangoutUserDAO();
                    user.setHangoutid(hangoutID);
                    user.setGoingstatus(HangoutConstant.PendingStatus);
                    user.setJid(tojid);
                    user.setUsername(tojid.getNode());
                    user.setHangoutuserid(hangoutuserID);
                    this.createHangoutUser(con,user);
                    hangoutDAO.getUserDAOList().add(user);
                }
                //Hangout Message
                Element messageContent = hangout.element(HangoutServiceProvider.HANGOUT_MESSAGE_ELEMENT);
                long hangoutMessageID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOOUTMESSAGE);
                HangoutMessageDAO messageDAO = new HangoutMessageDAO();
                messageDAO.setMessageid(hangoutMessageID);
                String messagecontent = null;
                if (messageContent != null)
                {
                    messagecontent = messageContent.getText();
                    messageDAO.setContent(messagecontent);
                }
                messageDAO.setCreateTime(hangoutDAO.getCreateDate());
                messageDAO.setHangoutid(hangoutDAO.getHangoutid());
                messageDAO.setCreateUser(hangoutDAO.getCreateUser());
                this.createHangoutMessage(con,messageDAO);
                hangoutDAO.getMessageDAOList().add(messageDAO);

//                //Hangout Location
//                Element location = hangout.element(HangoutServiceProvider.HANGOUTLOCATION_ELEMENT);
//                long hangoutLocationID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOOUTLOCATION);
//                HangoutLocationDAO locationDAO = new HangoutLocationDAO();
//                locationDAO.setLocationid(hangoutLocationID);
//                locationDAO.setFoursquare_locationid(Long.valueOf(location.getText()));
//                locationDAO.setCreateUser(hangoutDAO.getCreateUser());
//                locationDAO.setCreateTime(hangoutDAO.getCreateDate());
//                locationDAO.setHangoutid(hangoutDAO.getHangoutid());
//                this.createHangoutlocation(con, locationDAO);
//                hangoutDAO.getLocationDAOList().add(locationDAO);

                // HangoutVersion
                long hangoutversionID = SequenceManager
                                           .nextID(HangoutConstant.THIRDPLACE_HANGOUTVERSION);
                this.createHangoutVersion(con,hangoutID,hangoutversionID,hangoutDAO.getCreateDate());
                con.commit();
                return hangoutDAO;
            }
            catch (Exception e) {
                try {
                    con.rollback();
                }
                catch (SQLException e1)
                {
                    Log.error(e1.toString());
                }
                Log.error(e.toString());
            }
            finally
            {
                DbConnectionManager.closeConnection(con);
            }
        }
        return  null;
    }

    private void createHangout_private(Connection con, HangoutDAO hangoutDAO)  throws SQLException
    {
        PreparedStatement pstmt_hangout = null;
        pstmt_hangout = con.prepareStatement(CREATE_HANGOUT);
        String hangoutDes = hangoutDAO.getDescription();
        pstmt_hangout.setLong(1, hangoutDAO.getHangoutid());
        if (hangoutDes == null) {
            pstmt_hangout.setNull(2, Types.VARCHAR);
        } else {
            pstmt_hangout.setString(2, hangoutDes);
        }
        pstmt_hangout.setString(3, hangoutDAO.getCreateUser().toBareJID());
        pstmt_hangout.setString(4, String.valueOf(hangoutDAO.getCreateDate().getTime()));
        if (!hangoutDAO.isClosed()) {
            pstmt_hangout.setInt(5, 0);
        } else {
            pstmt_hangout.setInt(5, 1);
        }
        if (!hangoutDAO.isLocationconfirmed()) {
            pstmt_hangout.setInt(6, 0);
        } else {
            pstmt_hangout.setInt(6, 1);
        }
        if (!hangoutDAO.isTimeconfirmed()) {
            pstmt_hangout.setInt(7, 0);
        } else {
            pstmt_hangout.setInt(7, 1);
        }
        pstmt_hangout.setString(8, hangoutDAO.getPreferredlocation());
        pstmt_hangout.executeUpdate();
        DbConnectionManager.closeStatement(pstmt_hangout);
    }

    private void createHangoutTime(Connection connection, HangoutTimeDAO timeDAO) throws SQLException
    {
        PreparedStatement pstmt_hangouttime = null;
        pstmt_hangouttime = connection.prepareStatement(CREATE_HANGOUTTIME);
        pstmt_hangouttime.setLong(1, timeDAO.getHangouttimeid());
        pstmt_hangouttime.setString(2, timeDAO.getTimeDescription());
        pstmt_hangouttime.setString(3, String.valueOf(timeDAO.getStartdate().getTime()));
        if (timeDAO.getEnddate() != null)
        {
            pstmt_hangouttime.setString(4, String.valueOf(timeDAO.getEnddate().getTime()));
        }
        else
        {
            pstmt_hangouttime.setNull(4, Types.VARCHAR);
        }
        pstmt_hangouttime.setString(5, String.valueOf(timeDAO.getCreateTime().getTime()));
        pstmt_hangouttime.setString(6, timeDAO.getCreateUser().toBareJID());
        if (!timeDAO.isTimeConfirmed()) {
            pstmt_hangouttime.setInt(7, 0);
        } else {
            pstmt_hangouttime.setInt(7, 1);
        }
        pstmt_hangouttime.setLong(8, timeDAO.getHangoutid());
        pstmt_hangouttime.executeUpdate();
        DbConnectionManager.closeStatement(pstmt_hangouttime);
    }

    private void createHangoutUser(Connection connection, HangoutUserDAO userDAO)  throws SQLException
    {
        PreparedStatement pstmt_hangoutuser = null;
        pstmt_hangoutuser = connection.prepareStatement(CREATE_HANGOUTUSER);
        pstmt_hangoutuser.setLong(1, userDAO.getHangoutuserid());
        pstmt_hangoutuser.setString(2, userDAO.getUsername());
        pstmt_hangoutuser.setString(3, userDAO.getJid().toBareJID());
        pstmt_hangoutuser.setString(4, userDAO.getGoingstatus());
        pstmt_hangoutuser.setLong(5, userDAO.getHangoutid());
        pstmt_hangoutuser.executeUpdate();
        DbConnectionManager.closeStatement(pstmt_hangoutuser);
    }

    private void createHangoutMessage(Connection connection, HangoutMessageDAO messageDAO) throws SQLException
    {
        Log.debug("createHangoutMessage");
        PreparedStatement pstmt_hangoutMessage = null;
        pstmt_hangoutMessage = connection.prepareStatement(CREATE_HANGOUTMESSAGE);
        pstmt_hangoutMessage.setLong(1, messageDAO.getMessageid());
        if (messageDAO.getContent() != null)
        {
            pstmt_hangoutMessage.setString(2, messageDAO.getContent());
        }
        else
        {
            pstmt_hangoutMessage.setNull(2, Types.VARCHAR);
        }
        pstmt_hangoutMessage.setString(3, String.valueOf(messageDAO.getCreateTime().getTime()));
        pstmt_hangoutMessage.setString(4, messageDAO.getCreateUser().toBareJID());
        pstmt_hangoutMessage.setLong(5, messageDAO.getHangoutid());
        pstmt_hangoutMessage.executeUpdate();
        DbConnectionManager.closeStatement(pstmt_hangoutMessage);
    }

    private void createHangoutlocation(Connection connection, HangoutLocationDAO locationDAO) throws SQLException
    {
        Log.debug("createHangoutlocation");
        PreparedStatement pstmt_hangoutLocation = null;
        pstmt_hangoutLocation = connection.prepareStatement(CREATE_HANGOUTLOCATION);
        pstmt_hangoutLocation.setLong(1, locationDAO.getLocationid());
        pstmt_hangoutLocation.setLong(2, locationDAO.getFoursquare_locationid());
        if (!locationDAO.isLocationconfirm())
        {
            pstmt_hangoutLocation.setInt(3, 0);
        }
        else
        {
            pstmt_hangoutLocation.setInt(3, 1);
        }
        pstmt_hangoutLocation.setString(4, locationDAO.getCreateUser().toBareJID());
        pstmt_hangoutLocation.setString(5, String.valueOf(locationDAO.getCreateTime().getTime()));
        pstmt_hangoutLocation.setLong(6, locationDAO.getHangoutid());
        pstmt_hangoutLocation.executeUpdate();
        DbConnectionManager.closeStatement(pstmt_hangoutLocation);
    }

    private void createHangoutVersion(Connection connection, long Hangoutid, long HangoutVersionid, Date time) throws SQLException
    {
        Log.debug("createHangoutVersion");
        PreparedStatement pstmt_hangoutVersion = null;
        pstmt_hangoutVersion = connection.prepareStatement(Create_HANGOUTVERSION);
        pstmt_hangoutVersion.setLong(1, HangoutVersionid);
        pstmt_hangoutVersion.setString(2, String.valueOf(time.getTime()));
        pstmt_hangoutVersion.setLong(3, Hangoutid);
        pstmt_hangoutVersion.executeUpdate();
        DbConnectionManager.closeStatement(pstmt_hangoutVersion);
    }

    public HangoutDAO updateHangout(long hangoutid, IQ packet)
    {
        Element iq = packet.getChildElement();
        Element hangout = iq.element(HangoutServiceProvider.HANGOUT_ELEMET);
        Date now = new Date();
        Connection con = null;
        try
        {
            HangoutDAO existingHangout = this.selectHangoutByID(hangoutid);
            con = DbConnectionManager.getTransactionConnection();
            con.setAutoCommit(false);
            String jidstr = packet.getElement().attributeValue("from");
            JID fromjid = new JID(jidstr);

            existingHangout.setUserDAOList(new ArrayList<HangoutUserDAO>());
            // check the going status. adding this user is to check if the going status has changed,
            // if changed, we need to add into the userdao list
            Element userstatus = hangout.element("goingstatus");
            if (userstatus != null)
            {
                HangoutUserDAO user = this.selectHangoutUser(fromjid.toBareJID(),hangoutid);
                user.setGoingstatus(userstatus.getText());
                this.updateUserGoingStatus(con,user);
                existingHangout.getUserDAOList().add(user);
            }
            // we need to add other users in the XML Payload for the purpose of sending messages.
            Element users = hangout.element(HangoutServiceProvider.HANGOUTUSERS_ELEMENT);
            Iterator<Element> itr = users.elementIterator(HangoutServiceProvider.HANGOUTUSERS_SUBELEMENT);
            while (itr.hasNext())
            {
                Element element = itr.next();
                String tojidstr = element.getText();
                JID tojid = new JID(tojidstr);
                HangoutUserDAO user = this.selectHangoutUser(tojid.toBareJID(),hangoutid);
                existingHangout.getUserDAOList().add(user);
            }

            Element messageElement = hangout.element(HangoutServiceProvider.HANGOUT_MESSAGE_ELEMENT);
            if (messageElement != null)
            {
                long hangoutMessageID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOOUTMESSAGE);
                HangoutMessageDAO messageDAO = new HangoutMessageDAO();
                messageDAO.setMessageid(hangoutMessageID);
                String messagecontent = null;
                messagecontent = messageElement.getText();
                messageDAO.setContent(messagecontent);
                messageDAO.setCreateTime(now);
                messageDAO.setHangoutid(hangoutid);
                messageDAO.setCreateUser(fromjid);
                this.createHangoutMessage(con,messageDAO);
                existingHangout.setMessageDAOList(new ArrayList<HangoutMessageDAO>());
                existingHangout.getMessageDAOList().add(messageDAO);
            }

            Element timeElement = hangout.element("time");
            if (timeElement != null)
            {
                Element starttime = timeElement.element(HangoutServiceProvider.HANGOUT_STARTDATE_ELEMENT);
                Element endtime = timeElement.element(HangoutServiceProvider.HANGOUT_ENDDATE_ELEMENT);
                Date sdate = DateAdditions.stringToDate(starttime.getText(), HangoutConstant.Hangout_DATEFORMAT);
                Date edate = DateAdditions.stringToDate(endtime.getText(), HangoutConstant.Hangout_DATEFORMAT);

                Element timedescription = timeElement.element(HangoutServiceProvider.HANGOUT_TIMEDESCRIPTION_ELEMENT);
                Element timeconfirm = timeElement.element("timeconfirm");
                long hangouttimeID = SequenceManager
                              .nextID(HangoutConstant.THIRDPLACE_HANGOUTTIME);
                HangoutTimeDAO timeDAO = new HangoutTimeDAO();
                timeDAO.setCreateUser(fromjid);
                timeDAO.setHangoutid(hangoutid);
                timeDAO.setCreateTime(now);
                timeDAO.setStartdate(sdate);
                timeDAO.setEnddate(edate);
                if ( timeconfirm != null)
                {
                    Boolean isconfirm = Boolean.valueOf(timeconfirm.getText());
                    if (isconfirm)
                    {
                        timeDAO.setTimeConfirmed(true);
                        if (this.checkIfNeedUpdateHangoutTimeConfirmed(hangoutid,fromjid))
                        {
                            this.updateHangout_OverralTimeConfirm(con,hangoutid,true);
                        }
                    }
                    else
                    {
                        timeDAO.setTimeConfirmed(false);
                    }
                }
                else
                {
                    timeDAO.setTimeConfirmed(false);
                }
                timeDAO.setHangouttimeid(hangouttimeID);
                timeDAO.setTimeDescription(timedescription.getText());
                this.createHangoutTime(con, timeDAO);
                existingHangout.setTimeDAOList(new ArrayList<HangoutTimeDAO>());
                existingHangout.getTimeDAOList().add(timeDAO);
            }
            Element locationElement = hangout.element("location");
            if (locationElement != null)
            {
                Element islocationConfirmed = locationElement.element("locationconfirm");
                Element locationid = locationElement.element("locationid");
                long hangoutLocationID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOOUTLOCATION);

                HangoutLocationDAO locationDAO = new HangoutLocationDAO();
                locationDAO.setLocationid(hangoutLocationID);
                locationDAO.setFoursquare_locationid(Long.valueOf(locationid.getText()));
                locationDAO.setCreateUser(fromjid);
                locationDAO.setCreateTime(now);
                locationDAO.setHangoutid(hangoutid);
                if (islocationConfirmed != null)
                {
                    Boolean locationconfirmed = Boolean.valueOf(islocationConfirmed.getText());
                    if (locationconfirmed)
                    {
                        locationDAO.setLocationconfirm(true);
                        if (this.checkIfNeedUpdateHangoutLocationConfirmed(hangoutid,fromjid))
                        {
                            this.updateHangout_OverrallLocationConfirm(con, hangoutid, true);
                        }
                    }
                    else
                    {
                        locationDAO.setLocationconfirm(false);
                    }
                }
                this.createHangoutlocation(con, locationDAO);
                existingHangout.setLocationDAOList(new ArrayList<HangoutLocationDAO>());
                existingHangout.getLocationDAOList().add(locationDAO);
            }
            // HangoutVersion
            long hangoutversionID = SequenceManager
                                           .nextID(HangoutConstant.THIRDPLACE_HANGOUTVERSION);
            this.createHangoutVersion(con,hangoutid,hangoutversionID,now);
            con.commit();
            return existingHangout;
        }
        catch(Exception e)
        {
            try {
                con.rollback();
            }
            catch (SQLException e1)
            {
                Log.error(e1.toString());
            }
            Log.error(e.toString());
            return null;
        }
        finally
        {
            DbConnectionManager.closeConnection(con);
        }
    }

    public HangoutDAO closeHangout(long hangoutid, IQ packet)
    {
        Element iq = packet.getChildElement();
        Element hangout = iq.element(HangoutServiceProvider.HANGOUT_ELEMET);
        Connection con = null;
        try
        {
            HangoutDAO existingHangout = this.selectHangoutByID(hangoutid);
            con = DbConnectionManager.getConnection();
            con.setAutoCommit(false);
            existingHangout.setClosed(true);
            existingHangout.setUserDAOList(new ArrayList<HangoutUserDAO>());
            String jidstr = packet.getElement().attributeValue("from");
            JID fromjid = new JID(jidstr);
            HangoutUserDAO fromuser = this.selectHangoutUser(fromjid.toBareJID(),hangoutid);
            fromuser.setGoingstatus("notgoing");
            this.updateUserGoingStatus(con,fromuser);
            this.updateHangoutCloseStatus(con,existingHangout.getHangoutid(),true);
            Element users = hangout.element(HangoutServiceProvider.HANGOUTUSERS_ELEMENT);
            Iterator<Element> itr = users.elementIterator(HangoutServiceProvider.HANGOUTUSERS_SUBELEMENT);
            while (itr.hasNext()) {
                Element element = itr.next();
                String tojidstr = element.getText();
                JID tojid = new JID(tojidstr);
                HangoutUserDAO user = this.selectHangoutUser(tojid.toBareJID(), hangoutid);
                existingHangout.getUserDAOList().add(user);
            }
            con.commit();
            return existingHangout;
        }
        catch (Exception e)
        {
            try {
                con.rollback();
            }
            catch (SQLException e1)
            {
                Log.error(e1.toString());
            }
            Log.error(e.toString());
            return null;
        }
        finally
        {
            DbConnectionManager.closeConnection(con);
        }
    }

    public HangoutDAO queryHangout(long hangoutid)
    {
        return null;
    }

    private void updateHangout_OverrallLocationConfirm(Connection connection, long hangoutid, boolean value)  throws SQLException
    {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement(Update_HANGOUT_LOCATIONCONFIRM);
        if (value)
        {
            pstmt.setInt(1, 1);
        }
        else
        {
            pstmt.setInt(1, 0);
        }
        pstmt.setLong(2, hangoutid);
        pstmt.executeUpdate();
        DbConnectionManager.closeStatement(pstmt);
    }

    private void updateHangout_OverralTimeConfirm(Connection connection, long hangoutid, boolean value)  throws SQLException
    {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement(Update_HANGOUT_TIMECONFIRM);
        if (value)
        {
           pstmt.setInt(1, 1);
        }
        else
        {
           pstmt.setInt(1, 0);
        }
        pstmt.setLong(2, hangoutid);
        pstmt.executeUpdate();
        DbConnectionManager.closeStatement(pstmt);
    }



    private void updateUserGoingStatus(Connection connection, HangoutUserDAO user) throws SQLException
    {
        Log.debug("Update User Going Status");
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement(Update_UserGoingStatus);
        pstmt.setString(1, user.getGoingstatus());
        pstmt.setString(2, user.getJid().toBareJID());
        pstmt.setLong(3, user.getHangoutid());
        pstmt.executeUpdate();
        DbConnectionManager.closeStatement(pstmt);
    }

    private boolean checkIfNeedUpdateHangoutLocationConfirmed(long hangoutid, JID myself) throws SQLException
    {
        List<HangoutUserDAO> list = this.selectHangoutUsers(hangoutid);
        if (list != null)
        {
            for (HangoutUserDAO userDAO : list)
            {
                if (!userDAO.getJid().toBareJID().equals(myself.toBareJID()))
                {
                    HangoutLocationDAO locationDAO = this.selectLatestHangoutLocation_BYUSER(hangoutid, userDAO.getJid());
                    if (!locationDAO.isLocationconfirm())
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    private boolean checkIfNeedUpdateHangoutTimeConfirmed(long hangoutid, JID myself) throws  SQLException
    {
        List<HangoutUserDAO> list = this.selectHangoutUsers(hangoutid);
        if (list != null)
        {
            for (HangoutUserDAO userDAO : list)
            {
                if (!userDAO.getJid().toBareJID().equals(myself.toBareJID()))
                {
                    HangoutTimeDAO timeDAO = this.selectLatestHangoutTime_BYUSER(hangoutid, userDAO.getJid());
                    if (!timeDAO.isTimeConfirmed())
                    {
                         return false;
                    }
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    private void updateHangoutCloseStatus(Connection connection, long hangoutid, Boolean closestatus) throws SQLException
    {
        PreparedStatement pstmt = connection.prepareStatement(Update_HANGOUT_CLOSED);
        if (closestatus)
        {
            pstmt.setLong(1,1);
        }
        else
        {
            pstmt.setLong(1,0);
        }
        pstmt.setLong(2,hangoutid);
        pstmt.executeUpdate();
        DbConnectionManager.closeStatement(pstmt);
    }

    public HangoutDAO selectHangoutByID(long hangoutid)
    {
        Connection con = null;
        try {
            con = DbConnectionManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(Select_HANGOUT_BY_ID);
            pstmt.setLong(1, hangoutid);
            ResultSet result = pstmt.executeQuery();
            HangoutDAO hangout = null;
            if (result.next()) {
                hangout = new HangoutDAO();
                hangout.setHangoutid(hangoutid);
                String description = result.getString("description");
                hangout.setDescription(description);
                String jidstr = result.getString("createUser");
                JID jid = new JID(jidstr);
                hangout.setCreateUser(jid);
                String createDate = result.getString("createDate");
                hangout.setCreateDate(new Date(Long.valueOf(createDate)));
                int closedValue = result.getInt("closed");
                int timeconfirmedValue = result.getInt("timeconfirmed");
                int locationConfirmedValue = result.getInt("locationconfirmed");
                if (closedValue == 0) {
                    hangout.setClosed(false);
                } else {
                    hangout.setClosed(true);
                }
                if (timeconfirmedValue == 0) {
                    hangout.setTimeconfirmed(false);
                } else {
                    hangout.setTimeconfirmed(true);
                }
                if (locationConfirmedValue == 0) {
                    hangout.setLocationconfirmed(false);
                } else {
                    hangout.setLocationconfirmed(true);
                }
                hangout.setPreferredlocation(result.getString("preferredlocation"));
            }
            DbConnectionManager.closeStatement(pstmt);
            return hangout;
        } catch (SQLException e) {
            Log.error(e.toString());
            return  null;
        } finally {
            DbConnectionManager.closeConnection(con);
        }
    }

    public HangoutUserDAO selectHangoutUser(String JID, long hangoutid)
    {
        Log.debug("select Hangout User");
        Connection con = null;
        try {
            con = DbConnectionManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(Select_HANGOUT_USER);
            pstmt.setLong(1, hangoutid);
            pstmt.setString(2, JID);
            ResultSet result = pstmt.executeQuery();
            HangoutUserDAO user = null;
            if (result.next()) {
                user = new HangoutUserDAO();
                long hangoutuserid = result.getLong(HangoutUserDAO.UserID_Column);
                String usernameValue = result.getString(HangoutUserDAO.Username_Column);
                String jidValue = result.getString(HangoutUserDAO.JID_Column);
                String goingstatus = result.getString(HangoutUserDAO.GoingStatus_Column);
                user.setHangoutid(hangoutid);
                user.setHangoutuserid(hangoutuserid);
                user.setUsername(usernameValue);
                user.setJid(new JID(jidValue));
                user.setGoingstatus(goingstatus);
            }
            DbConnectionManager.closeStatement(pstmt);
            return user;
        } catch (SQLException e) {
            Log.error(e.toString());
            return null;
        } finally {
            DbConnectionManager.closeConnection(con);
        }
    }

    public List<HangoutUserDAO> selectHangoutUsers(long hangoutid)
    {
        Log.debug("select Hangout Users "+ hangoutid);
        Connection con = null;
        try {
            con = DbConnectionManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(Select_HANGOUT_USERS);
            pstmt.setLong(1, hangoutid);
            ResultSet result = pstmt.executeQuery();
            List<HangoutUserDAO> returndata = null;
            if (result.next()) {
                returndata = new ArrayList<HangoutUserDAO>();
                do {
                    HangoutUserDAO user = new HangoutUserDAO();
                    long hangoutuserid = result.getLong(HangoutUserDAO.UserID_Column);
                    String usernameValue = result.getString(HangoutUserDAO.Username_Column);
                    String jidValue = result.getString(HangoutUserDAO.JID_Column);
                    String goingstatus = result.getString(HangoutUserDAO.GoingStatus_Column);
                    user.setHangoutid(hangoutid);
                    user.setHangoutuserid(hangoutuserid);
                    user.setUsername(usernameValue);
                    user.setJid(new JID(jidValue));
                    user.setGoingstatus(goingstatus);
                    returndata.add(user);
                }while (result.next());
            }
            DbConnectionManager.closeStatement(pstmt);
            return returndata;
        }
        catch (SQLException e)
        {
           Log.error(e.toString());
           return null;
        }
        finally {
            DbConnectionManager.closeConnection(con);
        }
    }

    public HangoutLocationDAO selectLatestHangoutLocation(long hangoutid)
    {
        Log.debug("select Hangout Location");
        Connection con = null;
        try {
            con = DbConnectionManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(Select_Hangout_Location_LATEST);
            pstmt.setLong(1, hangoutid);
            ResultSet result = pstmt.executeQuery(); //the results are array, but we only get the top one
            HangoutLocationDAO locationDAO = null;
            if (result.next()) {
                locationDAO = new HangoutLocationDAO();
                locationDAO.setHangoutid(hangoutid);
                locationDAO.setCreateUser(new JID(result.getString(HangoutLocationDAO.CreateUser_Column)));
                String createtime = result.getString(HangoutLocationDAO.CreateTime_Column);
                locationDAO.setCreateTime(new Date(Long.valueOf(createtime)));
                int confirm = result.getInt(HangoutLocationDAO.LocationConfirm_Column);
                if (confirm == 0) {
                    //System.out.println("Location FALSE");
                    locationDAO.setLocationconfirm(false);
                } else {
                    //System.out.println("Location TRUE");
                    locationDAO.setLocationconfirm(true);
                }
                long foursquareLocationID = result.getLong(HangoutLocationDAO.FourSquareID_Column);
                locationDAO.setFoursquare_locationid(foursquareLocationID);
                long locationid = result.getLong(HangoutLocationDAO.LocationID_Column);
                locationDAO.setLocationid(locationid);
            }
            DbConnectionManager.closeStatement(pstmt);
            return locationDAO;
        } catch (SQLException e) {
            Log.error(e.toString());
            return null;
        } finally {
            DbConnectionManager.closeConnection(con);
        }
    }

    public HangoutLocationDAO selectLatestHangoutLocation_BYUSER(long hangoutid, JID user)
        {
            Log.debug("select Hangout Location" + user.toBareJID());
            Connection con = null;
            try {
                con = DbConnectionManager.getConnection();
                PreparedStatement pstmt = con.prepareStatement(Select_Hangout_Location_LATEST_BY_USER);
                pstmt.setLong(1, hangoutid);
                pstmt.setString(2, user.toBareJID());
                ResultSet result = pstmt.executeQuery(); //the results are array, but we only get the top one
                HangoutLocationDAO locationDAO = null;
                if (result.next()) {
                    locationDAO = new HangoutLocationDAO();
                    locationDAO.setHangoutid(hangoutid);
                    locationDAO.setCreateUser(user);
                    String createtime = result.getString(HangoutLocationDAO.CreateTime_Column);
                    locationDAO.setCreateTime(new Date(Long.valueOf(createtime)));
                    int confirm = result.getInt(HangoutLocationDAO.LocationConfirm_Column);
                    if (confirm == 0) {
                        //System.out.println("Location FALSE");
                        locationDAO.setLocationconfirm(false);
                    } else {
                        //System.out.println("Location TRUE");
                        locationDAO.setLocationconfirm(true);
                    }
                    long foursquareLocationID = result.getLong(HangoutLocationDAO.FourSquareID_Column);
                    locationDAO.setFoursquare_locationid(foursquareLocationID);
                    long locationid = result.getLong(HangoutLocationDAO.LocationID_Column);
                    locationDAO.setLocationid(locationid);
                }
                DbConnectionManager.closeStatement(pstmt);
                return locationDAO;
            } catch (SQLException e) {
                Log.error(e.toString());
                return null;
            } finally {
                DbConnectionManager.closeConnection(con);
            }
        }

    public HangoutTimeDAO selectLatestHangoutTime(long hangoutid)
    {
        Log.debug("select Hangout Time");
        Connection con = null;
        try {
            con = DbConnectionManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(Select_Hangout_Time_LATEST);
            pstmt.setLong(1, hangoutid);
            ResultSet result = pstmt.executeQuery(); //the results are array, but we only get the top one
            HangoutTimeDAO timeDAO = null;
            if (result.next()) {
                timeDAO = new HangoutTimeDAO();
                timeDAO.setHangoutid(hangoutid);
                timeDAO.setCreateUser(new JID(result.getString(HangoutTimeDAO.CreateUser_Column)));
                String createtime = result.getString(HangoutTimeDAO.CreateTime_Column);
                String startdate = result.getString(HangoutTimeDAO.StartDate_Column);
                String enddate = result.getString(HangoutTimeDAO.EndDate_Column);
                timeDAO.setStartdate(new Date(Long.valueOf(startdate)));
                timeDAO.setEnddate(new Date(Long.valueOf(enddate)));
                timeDAO.setCreateTime(new Date(Long.valueOf(createtime)));
                String timedes = result.getString(HangoutTimeDAO.TimeDescription_Column);
                timeDAO.setTimeDescription(timedes);
                long hangouttimeid = result.getLong(HangoutTimeDAO.HangoutTimeID_Column);
                timeDAO.setHangouttimeid(hangouttimeid);
                int confirm = result.getInt(HangoutTimeDAO.TimeConfirmed_Column);
                if (confirm == 0) {
                    //System.out.println("Time FALSE");
                    timeDAO.setTimeConfirmed(false);
                } else {
                    //System.out.println("Time true");
                    timeDAO.setTimeConfirmed(true);
                }
            }
            System.out.println(timeDAO);
            DbConnectionManager.closeStatement(pstmt);
            return timeDAO;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return null;
        } finally {
            DbConnectionManager.closeConnection(con);
        }
    }

    public HangoutTimeDAO selectLatestHangoutTime_BYUSER(long hangoutid, JID user)
      {
          Log.debug("select Hangout Time" + user.toBareJID());
          Connection con = null;
          try {
              con = DbConnectionManager.getConnection();
              PreparedStatement pstmt = con.prepareStatement(Select_Hangout_Time_LATEST_BY_USER);
              pstmt.setLong(1, hangoutid);
              pstmt.setString(2, user.toBareJID());
              ResultSet result = pstmt.executeQuery(); //the results are array, but we only get the top one
              HangoutTimeDAO timeDAO = null;
              if (result.next()) {
                  timeDAO = new HangoutTimeDAO();
                  timeDAO.setHangoutid(hangoutid);
                  timeDAO.setCreateUser(user);
                  String createtime = result.getString(HangoutTimeDAO.CreateTime_Column);
                  String startdate = result.getString(HangoutTimeDAO.StartDate_Column);
                  String enddate = result.getString(HangoutTimeDAO.EndDate_Column);
                  timeDAO.setStartdate(new Date(Long.valueOf(startdate)));
                  timeDAO.setEnddate(new Date(Long.valueOf(enddate)));
                  timeDAO.setCreateTime(new Date(Long.valueOf(createtime)));
                  String timedes = result.getString(HangoutTimeDAO.TimeDescription_Column);
                  timeDAO.setTimeDescription(timedes);
                  long hangouttimeid = result.getLong(HangoutTimeDAO.HangoutTimeID_Column);
                  timeDAO.setHangouttimeid(hangouttimeid);
                  int confirm = result.getInt(HangoutTimeDAO.TimeConfirmed_Column);
                  if (confirm == 0) {
                      //System.out.println("Time FALSE");
                      timeDAO.setTimeConfirmed(false);
                  } else {
                      //System.out.println("Time true");
                      timeDAO.setTimeConfirmed(true);
                  }
              }
              DbConnectionManager.closeStatement(pstmt);
              return timeDAO;
          } catch (SQLException e) {
              return null;
          } finally {
              DbConnectionManager.closeConnection(con);
          }
      }

    public HangoutMessageDAO selectLatestHangoutMessage(long hangoutid)
    {
        Log.debug("select Hangout Message");
        Connection con = null;
        try {
            con = DbConnectionManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(Select_Hangout_Message_LATEST);
            pstmt.setLong(1, hangoutid);
            ResultSet result = pstmt.executeQuery(); //the results are array, but we only get the top one
            HangoutMessageDAO messageDAO = null;
            if (result.next()) {
                messageDAO = new HangoutMessageDAO();
                messageDAO.setHangoutid(hangoutid);
                messageDAO.setCreateUser(new JID(result.getString(HangoutMessageDAO.CreateUser_Column)));
                messageDAO.setContent(result.getString(HangoutMessageDAO.Content_Column));
                String createtime = result.getString(HangoutTimeDAO.CreateTime_Column);
                messageDAO.setCreateTime(new Date(Long.valueOf(createtime)));
            }
            DbConnectionManager.closeStatement(pstmt);
            return messageDAO;
        } catch (SQLException e) {
            Log.error(e.toString()); return null;
        } finally {
            DbConnectionManager.closeConnection(con);
        }
    }


    public List<HangoutMessageDAO>  selectHangoutMessages(long hangoutid)
    {
        Log.debug("select Hangout Message");
              Connection con = null;
              try {
                  con = DbConnectionManager.getConnection();
                  PreparedStatement pstmt = con.prepareStatement(Select_Hangout_Message_ALL);
                  pstmt.setLong(1, hangoutid);
                  ResultSet result = pstmt.executeQuery(); //the results are array, but we only get the top one
                   List<HangoutMessageDAO> messages = new ArrayList<HangoutMessageDAO>();
                  while (result.next()) {
                      HangoutMessageDAO messageDAO = null;
                      messageDAO = new HangoutMessageDAO();
                      messageDAO.setHangoutid(hangoutid);
                      messageDAO.setMessageid(result.getLong(HangoutMessageDAO.HangoutMessageID_Column));
                      messageDAO.setCreateUser(new JID(result.getString(HangoutMessageDAO.CreateUser_Column)));
                      messageDAO.setContent(result.getString(HangoutMessageDAO.Content_Column));
                      String createtime = result.getString(HangoutTimeDAO.CreateTime_Column);
                      messageDAO.setCreateTime(new Date(Long.valueOf(createtime)));
                      messages.add(messageDAO);
                  }
                  DbConnectionManager.closeStatement(pstmt);
                  return messages;
              } catch (SQLException e) {
                  Log.error(e.toString()); return null;
              } finally {
                  DbConnectionManager.closeConnection(con);
              }
    }

    public Boolean containSenderStatus(List<HangoutUserDAO> list, JID sender)
    {
        for (HangoutUserDAO user : list) {
          if (user.getUsername().equals(sender.getNode()))
          {
              return true;
          }
        }
        return false;
    }

    public List<HangoutDAO> selectListOfHangout(JID user)
    {
        Log.debug("select hangout lists");
        Connection con = null;
        try
        {
            Date date= new Date();
            con = DbConnectionManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(Select_HangoutListID_BYJID_Latest);
            pstmt.setString(1, user.toBareJID());
            pstmt.setLong(2, date.getTime());
            ResultSet result = pstmt.executeQuery();
            List<HangoutDAO> returndata = new ArrayList<HangoutDAO>();
            if (result.next()) {
                do {
                    HangoutDAO hangoutDAO = new HangoutDAO();
                    long hangoutid = result.getLong("hangoutid");
                    String preferlocation = result.getString("preferredlocation");
                    String createUser = result.getString("createUser");
                    String createtime = result.getString("createDate");
                    String description = result.getString("description");
                    HangoutLocationDAO locationDAO = this.selectLatestHangoutLocation(hangoutid);
                    List<HangoutMessageDAO> messages = this.selectHangoutMessages(hangoutid);
                    HangoutTimeDAO timeDAO = this.selectLatestHangoutTime(hangoutid);
                    hangoutDAO.setLocationDAOList(new ArrayList<HangoutLocationDAO>());
                    hangoutDAO.setTimeDAOList(new ArrayList<HangoutTimeDAO>());
                    hangoutDAO.setUserDAOList(new ArrayList<HangoutUserDAO>());
                    hangoutDAO.setMessageDAOList(new ArrayList<HangoutMessageDAO>());
                    if (messages != null && messages.size()>0)
                    hangoutDAO.getMessageDAOList().addAll(messages);
                    if (timeDAO != null)
                    hangoutDAO.getTimeDAOList().add(timeDAO);
                    if (locationDAO != null)
                    hangoutDAO.getLocationDAOList().add(locationDAO);
                    hangoutDAO.setHangoutid(hangoutid);
                    List<HangoutUserDAO> users = this.selectHangoutUsers(hangoutid);
                    for (HangoutUserDAO u : users) {
                        hangoutDAO.getUserDAOList().add(u);
                    }
                    JID jid = new JID(createUser);
                    hangoutDAO.setPreferredlocation(preferlocation);
                    hangoutDAO.setCreateUser(jid);
                    hangoutDAO.setCreateDate(new Date(Long.valueOf(createtime)));
                    hangoutDAO.setDescription(description);
                    returndata.add(hangoutDAO);
                } while (result.next());
            }
            DbConnectionManager.closeStatement(pstmt);
            return returndata;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        } finally {
            DbConnectionManager.closeConnection(con);
        }
    }

    public HangoutDAO selectHangoutInDetail(long hangoutid)
    {
        HangoutDAO hangoutDAO = this.selectHangoutByID(hangoutid);
        hangoutDAO.setLocationDAOList(new ArrayList<HangoutLocationDAO>());
        hangoutDAO.setMessageDAOList(new ArrayList<HangoutMessageDAO>());
        hangoutDAO.setUserDAOList(new ArrayList<HangoutUserDAO>());
        hangoutDAO.setTimeDAOList(new ArrayList<HangoutTimeDAO>());
        HangoutLocationDAO locationDAO = this.selectLatestHangoutLocation(hangoutid);
        if (locationDAO != null)
            hangoutDAO.getLocationDAOList().add(locationDAO);
        HangoutMessageDAO messageDAO = this.selectLatestHangoutMessage(hangoutid);
        if (messageDAO != null)
            hangoutDAO.getMessageDAOList().add(messageDAO);
        HangoutTimeDAO timeDAO = this.selectLatestHangoutTime(hangoutid);
        if (timeDAO != null)
            hangoutDAO.getTimeDAOList().add(timeDAO);
        List<HangoutUserDAO> users = this.selectHangoutUsers(hangoutid);
        if (users != null)
            hangoutDAO.getUserDAOList().addAll(users);
        return hangoutDAO;
    }
}

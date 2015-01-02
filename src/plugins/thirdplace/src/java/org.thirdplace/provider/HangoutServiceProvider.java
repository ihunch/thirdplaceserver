package org.thirdplace.provider;

import org.dom4j.Element;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.bean.*;
import org.thirdplace.util.DateAdditions;
import org.thirdplace.util.HangoutConstant;
import org.thirdplace.util.StringAdditions;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import java.sql.*;
import java.util.Date;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 29/12/14
 * Time: 12:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutServiceProvider
{
    private static final Logger Log = LoggerFactory.getLogger(HangoutServiceProvider.class);

    private static final String CREATE_HANGOUT =
    "INSERT INTO thirdplaceHangout ("+
            "hangoutid, description, createUser, createDate, closed, timeconfirmed, locationconfirmed)"
            + "VALUES (?,?,?,?,?,?,?)";
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

    public HangoutDAO createHangout(IQ packet)
    {
        Element iq = packet.getChildElement();
        Element hangout = iq.element("hangout");
        Connection con = null;
        if (hangout != null)
        {
            try {
                con = DbConnectionManager.getTransactionConnection();
                con.setAutoCommit(false);
                long hangoutID = SequenceManager
                               .nextID(HangoutConstant.THIRDPLACE_HANGOOUT);
                HangoutDAO hangoutDAO = new HangoutDAO();
                hangoutDAO.setHangoutid(hangoutID);
                hangoutDAO.setCreateDate(new Date());
                hangoutDAO.setClosed(false);
                hangoutDAO.setLocationconfirmed(false);
                hangoutDAO.setTimeconfirmed(false);
                Element desElement = hangout.element("description");
                if (desElement != null) {
                    String des =  desElement.getText();
                    hangoutDAO.setDescription(des);
                }
                String jidstr = packet.getElement().attributeValue("from");
                JID fromjid = new JID(jidstr);
                hangoutDAO.setCreateUser(fromjid);
                this.createHangout_private(con, hangoutDAO);
                //Hangout Time
                Element startdateElement = hangout.element("startdate");
                String startdatestr = startdateElement.getText();
                Element enddateElement = hangout.element("enddate");
                String enddatestr = null;
                if (enddateElement != null)
                {
                    enddatestr = enddateElement.getText();
                }

                Date sdate = DateAdditions.stringToDate(startdatestr, HangoutConstant.Hangout_DATEFORMAT);
                Date edate = DateAdditions.stringToDate(enddatestr, HangoutConstant.Hangout_DATEFORMAT);
                Element timedescription = hangout.element("timedescription");
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

                //HangoutUser
                Element users = hangout.element("users");
                long hangoutuserID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOUTUSER);
                HangoutUserDAO inviter = new HangoutUserDAO();
                inviter.setHangoutid(hangoutID);
                inviter.setGoingstatus(HangoutConstant.GoingStatus);
                inviter.setJid(fromjid);
                inviter.setHangoutuserid(hangoutuserID);
                inviter.setUsername(StringAdditions.substringBeforeChar(fromjid.toBareJID(), "@"));
                this.createHangoutUser(con,inviter);
                Iterator<Element> itr = users.elementIterator("user");
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
                    user.setUsername(StringAdditions.substringBeforeChar(tojid.toBareJID(), "@"));
                    user.setHangoutuserid(hangoutuserID);
                    this.createHangoutUser(con,user);
                }
                //Hangout Message
                Element messageContent = hangout.element("message");
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

                //Hangout Location
                Element location = hangout.element("locationid");
                long hangoutLocationID = SequenceManager.nextID(HangoutConstant.THIRDPLACE_HANGOOUTLOCATION);
                HangoutLocationDAO locationDAO = new HangoutLocationDAO();
                locationDAO.setLocationid(hangoutLocationID);
                locationDAO.setFoursquare_locationid(Long.valueOf(location.getText()));
                locationDAO.setCreateUser(hangoutDAO.getCreateUser());
                locationDAO.setCreateTime(hangoutDAO.getCreateDate());
                locationDAO.setHangoutid(hangoutDAO.getHangoutid());
                this.createHangoutlocation(con, locationDAO);
                con.commit();
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
        else
        {

        }
        return null;
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
        System.out.println(timeDAO.getHangoutid());
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
        System.out.println("createHangoutMessage");
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
        System.out.println("createHangoutlocation");
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
}

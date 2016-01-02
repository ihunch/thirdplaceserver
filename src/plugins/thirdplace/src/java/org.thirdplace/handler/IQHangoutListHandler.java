package org.thirdplace.handler;

import com.notnoop.apns.ApnsService;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.HangoutComponent;
import org.thirdplace.bean.*;
import org.thirdplace.provider.HangoutServiceProvider;
import org.thirdplace.util.HangoutConstant;
import org.thirdplace.util.IQAdditions;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 28/12/14
 * Time: 10:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class IQHangoutListHandler implements IQHangoutHandler
{
    private final String QueryElement = "query";
    private final String ResultElment = "result";
    private HangoutServiceProvider provider = null;
    private static final Logger Log = LoggerFactory.getLogger(IQHangoutListHandler.class);
    private int First = 0;
    private ApnsService service = null;
    public IQHangoutListHandler()
    {
       this.init();
    }

    public IQ handleIQRequest(IQ packet) {
        IQ reply = null;
        try {
            Element iq = packet.getChildElement();
            String name = iq.getName();
            if (QueryElement.equals(name)) {
                String jidstr = packet.getElement().attributeValue("from");
                JID fromjid = new JID(jidstr);
                Document document = DocumentHelper.createDocument();
                Element result = document.addElement(ResultElment, HangoutComponent.HANGOUT_LIST);
                List<HangoutDAO> list = provider.selectListOfHangout(fromjid);
                for (HangoutDAO hangoutDAO : list) {
                    Element hangout = result.addElement(HangoutServiceProvider.HANGOUT_ELEMET);
                     Element createUser = hangout.addElement("createUser");
                    Element preferlocation = hangout.addElement("preferredlocation");
                    Element createTime = hangout.addElement("createTime");
                    Element description = hangout.addElement("description");
                    description.setText(hangoutDAO.getDescription());
                    preferlocation.setText(hangoutDAO.getPreferredlocation());
                    createUser.setText(hangoutDAO.getCreateUser().toBareJID());
                    DateFormat df = new SimpleDateFormat(HangoutConstant.Hangout_DATEFORMAT);
                    createTime.setText(df.format(hangoutDAO.getCreateDate()));
                    hangout.addAttribute("id", String.valueOf(hangoutDAO.getHangoutid()));
                    if (hangoutDAO.getLocationDAOList().size() > 0)
                    {
                        Element location = hangout.addElement("location");

                        HangoutLocationDAO locationDAO = hangoutDAO.getLocationDAOList().get(First);
                        Element locationid = location.addElement(HangoutServiceProvider.HANGOUTLOCATION_ELEMENT);
                        locationid.setText(String.valueOf(locationDAO.getFoursquare_locationid()));
                        Element createjid = location.addElement(HangoutServiceProvider.HANGOUT_CREATEUSER);
                        Element createtime = location.addElement(HangoutServiceProvider.HANGOUT_CREATETIME);
                        createjid.setText(locationDAO.getCreateUser().toBareJID());
                        createtime.setText(df.format(locationDAO.getCreateTime()));
                    }

                    if (hangoutDAO.getTimeDAOList().size() > 0)
                    {
                        Element time = hangout.addElement("time");
                        HangoutTimeDAO timeDAO = hangoutDAO.getTimeDAOList().get(First);
                        Element timedescription = time.addElement(HangoutServiceProvider.HANGOUT_TIMEDESCRIPTION_ELEMENT);
                        Element startdate = time.addElement(HangoutServiceProvider.HANGOUT_STARTDATE_ELEMENT);
                        Element enddate = time.addElement(HangoutServiceProvider.HANGOUT_ENDDATE_ELEMENT);
                        timedescription.setText(timeDAO.getTimeDescription());
                        startdate.setText(df.format(timeDAO.getStartdate()));
                        enddate.setText(df.format(timeDAO.getEnddate()));
                        Element createjid = time.addElement(HangoutServiceProvider.HANGOUT_CREATEUSER);
                        Element createtime = time.addElement(HangoutServiceProvider.HANGOUT_CREATETIME);
                        createjid.setText(timeDAO.getCreateUser().toBareJID());
                        createtime.setText(df.format(timeDAO.getCreateTime()));
                    }
                    if (hangoutDAO.getMessageDAOList().size() > 0) {
                        Element messages = hangout.addElement("messages");
                          for(HangoutMessageDAO messageDAO : hangoutDAO.getMessageDAOList())
                          {
                              Element message = messages.addElement("message");
                              Element content = message.addElement("content");
                              content.setText(messageDAO.getContent());
                              Element createjid = message.addElement(HangoutServiceProvider.HANGOUT_CREATEUSER);
                              Element createtime = message.addElement(HangoutServiceProvider.HANGOUT_CREATETIME);
                              createjid.setText(messageDAO.getCreateUser().toBareJID());
                              createtime.setText(df.format(messageDAO.getCreateTime()));
                          }
                       }
                    if (hangoutDAO.getUserDAOList().size() > 0) {
                        List<HangoutUserDAO> users = hangoutDAO.getUserDAOList();
                        Element usersElement = hangout.addElement("users");
                        for (HangoutUserDAO user : users) {
                            Element userElement = usersElement.addElement("user");
                            userElement.addAttribute("jid", user.getJid().toBareJID());
                            userElement.addAttribute("goingstatus", user.getGoingstatus());
                        }
                    }
                }
                reply = IQAdditions.createResultIQ(packet, result);
                return reply;
            } else {
                reply = IQ.createResultIQ(packet);
                reply.setError(PacketError.Condition.feature_not_implemented);
                return reply;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            reply = IQ.createResultIQ(packet);
            reply.setError(PacketError.Condition.internal_server_error);
            return reply;
        }
    }


    public void init()
    {
        provider = new HangoutServiceProvider();
    }

    public void destory() {
       provider = null;
    }

    public void setApnsServce(ApnsService service){
            this.service = service;
    }
}


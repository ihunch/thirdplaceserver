package org.thirdplace.handler;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import org.dom4j.*;
import org.jivesoftware.openfire.OfflineMessageStore;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.HangoutComponent;
import org.thirdplace.bean.*;
import org.thirdplace.provider.HangoutServiceProvider;
import org.thirdplace.util.HangoutConstant;
import org.thirdplace.util.HangoutMessagePacketWrapper;
import org.thirdplace.util.IQAdditions;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketError;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 28/12/14
 * Time: 10:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class IQHangoutDetailHandler implements IQHangoutHandler
{
    private static final Logger Log = LoggerFactory.getLogger(IQHangoutDetailHandler.class);

    private final String CreateElement = "create";
    private final String UpdateElement = "update";
    private final String CloseElement = "close";
    private final String QueryElement = "query";
    private final String HangoutID = "id";
    private final String ResultElment = "result";
    private final String DeviceToken = "TOKEN";
    private final String FormatedName = "FN";
    private UserManager userManager;
    private VCardManager vCardManager;
    private HangoutServiceProvider provider = null;
    private PacketRouter router;
    private OfflineMessageStore offlineMessageStore;
    private PresenceManager presenceManager;
    private int First = 0;
    private ApnsService service = null;
    public IQHangoutDetailHandler()
    {
        this.init();
    }

    public void init()
    {
        provider = new HangoutServiceProvider();
        userManager = UserManager.getInstance();
        vCardManager = VCardManager.getInstance();
        router = XMPPServer.getInstance().getPacketRouter();
        offlineMessageStore =  OfflineMessageStore.getInstance();
        presenceManager = XMPPServer.getInstance().getPresenceManager();
    }

    public void destory()
    {
        provider = null;
        router = null;
        offlineMessageStore= null;
        presenceManager=null;
    }

    public void setApnsServce(ApnsService service){
        this.service = service;
    }

    public IQ handleIQRequest(IQ packet)
    {
        IQ reply = null;
        Element iq = packet.getChildElement();
        String name = iq.getName();

        if (CreateElement.equals(name))
        {
           HangoutDAO hangout = this.CreateDetail(packet);
           Document document = DocumentHelper.createDocument();
           Element create = document.addElement(CreateElement,HangoutComponent.HANGOUT_DETAIL);
           create.setText(String.valueOf(hangout.getHangoutid()));
           reply = IQAdditions.createResultIQ(packet,create);
           for (HangoutUserDAO user : hangout.getUserDAOList())
           {
                if (userManager.isRegisteredUser(user.getUsername()))
                {
                    User toUser = null;
                    try {
                        toUser = userManager.getUser(user.getUsername());
                        HangoutMessagePacketWrapper messagePacketHandler = new HangoutMessagePacketWrapper(HangoutComponent.HANGOUT_MESSAGE);
                        messagePacketHandler.addHangoutDetailContent(hangout);
                        messagePacketHandler.setFrom(hangout.getCreateUser());
                        messagePacketHandler.setTo(user.getJid());
                        messagePacketHandler.setID(packet.getID());
                        if (presenceManager.isAvailable(toUser)) {
                            router.route(messagePacketHandler.getMessage());
                        } else
                        {
                            offlineMessageStore.addMessage((Message) messagePacketHandler.getMessage());
                        }
                        String tokendevice = vCardManager.getVCardProperty(user.getUsername(),DeviceToken);
                        String sendername = vCardManager.getVCardProperty(hangout.getCreateUser().getNode(),FormatedName);
                        if (tokendevice != null)
                        {
                            String plainmessage =  hangout.getMessageDAOList().get(First).getContent();
                            if (sendername != null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(sendername);
                                sb.append(": ");
                                sb.append(plainmessage);
                                this.pushNotification(tokendevice, sb.toString());
                            }
                            else
                            {
                                this.pushNotification(tokendevice, plainmessage);
                            }
                        }
                    } catch (UserNotFoundException e) {
                        Log.error(e.toString());
                    }
                }
               else
                {
                    //Not Implement at this stage. for SMS FOR EMAIL
                }
           }
           //Board
           return reply;
        }
        else if (UpdateElement.equals(name))
        {
            //Let's get the ID of Hangout
            Element hangoutelement = iq.element("hangout");
            Attribute attr = hangoutelement.attribute(HangoutID);
            String id = attr.getValue();
            Document document = DocumentHelper.createDocument();
            HangoutDAO hangoutDAO = this.UpdateDetail(Long.valueOf(id), packet);
            if (hangoutDAO != null)
            {
                Element update = document.addElement(UpdateElement,HangoutComponent.HANGOUT_DETAIL);
                update.addAttribute("hangoutid",String.valueOf(hangoutDAO.getHangoutid()));
                update.setText("success");
                reply = IQAdditions.createResultIQ(packet,update);
                String jidstr = packet.getElement().attributeValue("from");
                JID fromjid = new JID(jidstr);
                try {
                    HangoutUserDAO sender = provider.selectHangoutUser(fromjid.toBareJID(), hangoutDAO.getHangoutid());
                    //notify user's about update results
                    boolean isContainSender = provider.containSenderStatus(hangoutDAO.getUserDAOList(),sender.getJid());
                    for (HangoutUserDAO user : hangoutDAO.getUserDAOList()) {
                        if (!user.getUsername().equals(fromjid.getNode())) {

                            if (userManager.isRegisteredUser(user.getUsername()))
                            {
                                User toUser = null;
                                toUser = userManager.getUser(user.getUsername());
                                HangoutMessagePacketWrapper messagePacketHandler = new HangoutMessagePacketWrapper(HangoutComponent.HANGOUT_MESSAGE);
                                messagePacketHandler.addHangoutDetailContent(hangoutDAO);
                                messagePacketHandler.setFrom(fromjid);
                                messagePacketHandler.setTo(user.getJid());
                                messagePacketHandler.setID(packet.getID());
                                // create Messages
                                if (isContainSender) {
                                    messagePacketHandler.addSenderGoingStatus(sender.getGoingstatus());
                                }
                                if (presenceManager.isAvailable(toUser)) {
                                    router.route(messagePacketHandler.getMessage());
                                } else {
                                    offlineMessageStore.addMessage((Message) messagePacketHandler.getMessage());
                                }
                                String tokendevice = vCardManager.getVCardProperty(user.getUsername(), DeviceToken);
                                String sendername = vCardManager.getVCardProperty(sender.getUsername(),FormatedName);
                                if (tokendevice != null)
                                {
                                    String plainmessage =  hangoutDAO.getMessageDAOList().get(First).getContent();
                                    if (sendername != null) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(sendername);
                                        sb.append(": ");
                                        sb.append(plainmessage);
                                        this.pushNotification(tokendevice, sb.toString());
                                    }
                                    else
                                    {
                                        this.pushNotification(tokendevice, plainmessage);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (UserNotFoundException e1)
                {
                    Log.error(e1.toString());
                }
                return reply;
            }
            else
            {
                reply = IQ.createResultIQ(packet);
                reply.setError(PacketError.Condition.internal_server_error);
                return reply;
            }
        }
        else if (CloseElement.equals(name))
        {
             //Let's get the ID of Hangout
            Element hangoutelement = iq.element("hangout");
            Attribute attr = hangoutelement.attribute(HangoutID);
            String id = attr.getValue();
            Document document = DocumentHelper.createDocument();
            HangoutDAO hangoutDAO = this.CloseDetail(Long.valueOf(id), packet);
            String jidstr = packet.getElement().attributeValue("from");
            JID fromjid = new JID(jidstr);
            try {
                if (hangoutDAO != null) {
                    Element close = document.addElement(CloseElement, HangoutComponent.HANGOUT_DETAIL);
                    close.addAttribute("hangoutid", String.valueOf(hangoutDAO.getHangoutid()));
                    close.setText("success");
                    reply = IQAdditions.createResultIQ(packet, close);
                    for (HangoutUserDAO user : hangoutDAO.getUserDAOList()) {
                        if (userManager.isRegisteredUser(user.getUsername())) {
                            User toUser = null;
                            toUser = userManager.getUser(user.getUsername());
                            HangoutMessagePacketWrapper messagePacketHandler = new HangoutMessagePacketWrapper(HangoutComponent.HANGOUT_MESSAGE);
                            messagePacketHandler.setFrom(fromjid);
                            messagePacketHandler.setTo(user.getJid());
                            messagePacketHandler.setID(packet.getID());
                            messagePacketHandler.addHangoutCloseInformation(hangoutDAO);
                             // create Messages
                            if (presenceManager.isAvailable(toUser)) {
                                router.route(messagePacketHandler.getMessage());
                            } else
                            {
                                offlineMessageStore.addMessage((Message) messagePacketHandler.getMessage());
                            }
                            String tokendevice = vCardManager.getVCardProperty(user.getUsername(),DeviceToken);
                            String realname = vCardManager.getVCardProperty(user.getUsername(),FormatedName);
                            if (tokendevice != null && realname != null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(realname);
                                sb.append(" can not catch up this weekend");
                                this.pushNotification(tokendevice, sb.toString());
                            }
                        }
                    }
                    return reply;
                } else {
                    reply = IQ.createResultIQ(packet);
                    reply.setError(PacketError.Condition.internal_server_error);
                    return reply;
                }
            } catch (UserNotFoundException e1) {
                Log.error(e1.toString());
                return null;
            }
        }
        else if (QueryElement.equals(name))
        {
            Element hangoutelement = iq.element("hangout");
            Attribute attr = hangoutelement.attribute(HangoutID);
            String id = attr.getValue();
            String jidstr = packet.getElement().attributeValue("from");
            JID fromjid = new JID(jidstr);
            HangoutDAO hangoutDAO = this.FetchDetail(Long.valueOf(id));
            if (hangoutDAO != null)
            {
                Document document = DocumentHelper.createDocument();
                Element result = document.addElement(ResultElment, HangoutComponent.HANGOUT_DETAIL);
                Element hangout = result.addElement("hangout");
                Element idElement = hangout.addElement("id");
                idElement.setText(String.valueOf(hangoutDAO.getHangoutid()));
                if (hangoutDAO.getLocationDAOList().size() > 0) {
                    HangoutLocationDAO locationDAO = hangoutDAO.getLocationDAOList().get(First);
                    Element location = hangout.addElement(HangoutServiceProvider.HANGOUTLOCATION_ELEMENT);
                    location.setText(String.valueOf(locationDAO.getFoursquare_locationid()));
                    Element locationConfirm = hangout.addElement(HangoutServiceProvider.HANGOUT_LOCATIONCONFIRM_ELEMENT);
                    if (locationDAO.isLocationconfirm())
                    {
                        locationConfirm.setText("true");
                    }
                    else {
                        locationConfirm.setText("false");
                    }
                }
                if (hangoutDAO.getTimeDAOList().size() > 0) {
                    HangoutTimeDAO timeDAO = hangoutDAO.getTimeDAOList().get(First);
                    Element time = hangout.addElement(HangoutServiceProvider.HANGOUT_TIMEDESCRIPTION_ELEMENT);
                    time.setText(timeDAO.getTimeDescription());
                    Element timeConfirm = hangout.addElement(HangoutServiceProvider.HANGOUT_TIMECONFIRM_ELEMENT);
                    if (timeDAO.isTimeConfirmed())
                    {
                        timeConfirm.setText("true");
                    }
                    else
                    {
                        timeConfirm.setText("false");
                    }
                    DateFormat df = new SimpleDateFormat(HangoutConstant.Hangout_DATEFORMAT);
                    Element startdate = hangout.addElement(HangoutServiceProvider.HANGOUT_STARTDATE_ELEMENT);
                    startdate.setText(df.format(timeDAO.getStartdate()));
                    Element enddate = hangout.addElement(HangoutServiceProvider.HANGOUT_ENDDATE_ELEMENT);
                    enddate.setText(df.format(timeDAO.getEnddate()));
                }
                if (hangoutDAO.getMessageDAOList().size() > 0) {
                    HangoutMessageDAO messageDAO = hangoutDAO.getMessageDAOList().get(First);
                    Element message = hangout.addElement(HangoutServiceProvider.HANGOUT_MESSAGE_ELEMENT);
                    message.setText(messageDAO.getContent());
                }
                if (hangoutDAO.getUserDAOList().size() > 0) {
                    List<HangoutUserDAO> users = hangoutDAO.getUserDAOList();
                    Element usersElement = hangout.addElement(HangoutServiceProvider.HANGOUTUSERS_ELEMENT);
                    for (HangoutUserDAO user : users) {
                        if (!user.getJid().toBareJID().equals(fromjid.getNode()))
                        {
                            Element userElement = usersElement.addElement(HangoutServiceProvider.HANGOUTUSERS_SUBELEMENT);
                            userElement.addAttribute("jid", user.getJid().toBareJID());
                            userElement.addAttribute(HangoutServiceProvider.HANGOUT_GOINGSTATUS_ELEMENT, user.getGoingstatus());
                        }
                    }
                }
                reply = IQAdditions.createResultIQ(packet, result);
                return reply;
            }
            else
            {
                reply = IQ.createResultIQ(packet);
                reply.setError(PacketError.Condition.internal_server_error);
                return reply;
            }
        }
        else
        {
            reply = IQ.createResultIQ(packet);
            reply.setError(PacketError.Condition.internal_server_error);
            return reply;
        }
    }

    private HangoutDAO CreateDetail(IQ packet)
    {
        return provider.createHangout(packet);
    }

    private HangoutDAO UpdateDetail(long hangoutid, IQ packet)
    {
        return provider.updateHangout(hangoutid, packet);
    }

    private HangoutDAO CloseDetail(long hangoutid, IQ packet){
        return provider.closeHangout(hangoutid,packet);
    }

    private HangoutDAO FetchDetail(long hangoutid)
    {
        return provider.selectHangoutInDetail(hangoutid);
    }

    private void pushNotification(String tokendevice, String message)
    {
        if (tokendevice != null)
        {
            try {
                String payload = APNS.newPayload().alertBody(message).build();
                service.push(tokendevice, payload);
            }
            catch (Exception e)
            {
                Log.error(e.toString());
            }
        }
    }
}

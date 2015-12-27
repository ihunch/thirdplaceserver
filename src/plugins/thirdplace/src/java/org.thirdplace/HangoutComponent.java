package org.thirdplace;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import org.dom4j.Element;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.handler.IQHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.handler.IQHangoutHandler;
import org.thirdplace.handler.IQHangoutDetailHandler;
import org.thirdplace.handler.IQHangoutListHandler;
import org.thirdplace.util.SystemUtils;
import org.xmpp.component.AbstractComponent;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 9/12/14
 * Time: 12:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutComponent extends AbstractComponent
{
    public static final String HANGOUT_DETAIL 	= "hangout:iq:detail";
    public static final String HANGOUT_LIST = "hangout:iq:list";
    public static final String HANGOUT_MESSAGE = "hangout:message:detail";
    public static final String APSPATHKEY = "APSCertificate_path";
    public static final String APSCertificatePassword="APSCertificatePassword";
    private ApnsService service;
    private PluginManager pluginManager;
    private final Plugin plugin;
    private static final Logger Log = LoggerFactory.getLogger(HangoutComponent.class);

    public HangoutComponent(final Plugin plugin, PluginManager pluginManager)
    {
        this.plugin = plugin;
        this.pluginManager = pluginManager;
    }

    public void doStart()
    {
        try {
            String path = SystemUtils.loadProperty(APSPATHKEY);
            String password = SystemUtils.loadProperty(APSCertificatePassword);
            service =
                        APNS.newService()
                        .withCert(path, password)
                                .withProductionDestination()
                                .build();
        }
        catch (IOException e)
        {
            Log.error(e.toString());
        }

    }

    public void doStop()
    {
        service.stop();
    }

    @Override
    public String getDescription() {
        return pluginManager.getDescription(plugin);
    }

    @Override
    public String getName() {
        return pluginManager.getName(plugin);
    }
     @Override
     public String getDomain()
     {
         return XMPPServer.getInstance().getServerInfo().getXMPPDomain();
     }


    @Override
    protected String[] discoInfoFeatureNamespaces() {
        String[] ns = {HANGOUT_DETAIL,HANGOUT_LIST};
        return ns;
    }

    @Override
    protected IQ handleIQGet(IQ iq)
    {
        System.out.println("third place get iq");
        final Element element = iq.getChildElement();
        final String namespace = element.getNamespaceURI();
        IQHangoutHandler handler = null;
        try {
            if (HANGOUT_DETAIL.equals(namespace)) {
                handler = new IQHangoutDetailHandler();
                handler.setApnsServce(service);
                return handler.handleIQRequest(iq);

            } else if (HANGOUT_LIST.equals(namespace)) {
                handler = new IQHangoutListHandler();
                handler.setApnsServce(service);
                return handler.handleIQRequest(iq);
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.error(e.toString());
            return null;
        } finally {
            handler.destory();
        }
    }

    @Override
    protected IQ handleIQSet(IQ iq)
    {
        System.out.println("third place set iq");
        final Element element = iq.getChildElement();
        final String namespace = element.getNamespaceURI();
        IQHangoutHandler handler = null;
        if (HANGOUT_DETAIL.equals(namespace))
        {
            handler = new IQHangoutDetailHandler();
            handler.setApnsServce(service);
            return handler.handleIQRequest(iq);
        }
        else
        {
            return null;
        }
    }
}

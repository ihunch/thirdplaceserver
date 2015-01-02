package org.thirdplace;
/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 7/12/14
 * Time: 11:55 PM
 * To change this template use File | Settings | File Templates.
 */
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import java.io.File;

public class HangoutPlugin implements Plugin
{
    private ComponentManager componentManager;
    private static Logger logger = LoggerFactory.getLogger(HangoutPlugin.class);
    public static HangoutComponent component = null;
    private final String serviceName = "thirdplacehangout";

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        componentManager = ComponentManagerFactory.getComponentManager();
        component = new HangoutComponent(this,manager);
        try
        {
            componentManager.addComponent(serviceName, component);
        }
        catch (ComponentException e) {
            logger.error(e.getMessage(), e);
        }
        component.doStart();
    }

    public void destroyPlugin() {
        try {
            componentManager.removeComponent(serviceName);
            componentManager = null;
        }
        catch (ComponentException e) {
            logger.error("Could NOT Remove " + serviceName + " Component");
        }
        component.doStop();
    }

    public String getName() {
        return "Third Place Hangout";
    }

    public String getDescription() {
        return "Third Place Description";
    }
}

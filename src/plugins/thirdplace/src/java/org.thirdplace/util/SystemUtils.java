package org.thirdplace.util;

import org.jivesoftware.util.JiveGlobals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by Yang on 26/11/15.
 */
public class SystemUtils
{
    public static Properties properties = null;
    public static String loadProperty(String attribue) throws IOException
    {
        if (properties == null) {
            properties = new Properties();

            File file = new File(JiveGlobals.getHomeDirectory() + File.separator + "conf" + File.separator + "thirdplace.properties");
            if (!file.exists()) {
                throw new IOException("The file crowd.properties is missing from Openfire conf folder");
            }
            properties.load(new FileInputStream(file));
        }
        return (String)properties.getProperty(attribue);
    }
}

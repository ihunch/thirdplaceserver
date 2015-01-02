/**
 * $RCSfile$
 * $Revision: 1217 $
 * $Date: 2005-04-11 18:11:06 -0300 (Mon, 11 Apr 2005) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.container;

import java.io.File;

/**
 * Plugin interface. Plugins enhance the functionality of Openfire. They can:<ul>
 *
 *      <li>Act as {@link org.xmpp.component.Component Components} to implement
 *      additional features in the XMPP protocol.
 *      <li>Dynamically modify the admin console.
 *      <li>Use the Openfire API to add new functionality to the server.
 * </ul>
 *
 * Plugins live in the <tt>plugins</tt> directory of <tt>home</tt>. Plugins
 * that are packaged as JAR files will be automatically expanded into directories. A
 * org.hangout.org.thirdplace directory should have the following structure:
 *
 * <pre>[pluginDir]
 *    |-- org.hangout.org.thirdplace.xml
 *    |-- classes/
 *    |-- lib/</pre>
 *
 * The <tt>classes</tt> and <tt>lib</tt> directory are optional. Any files in the
 * <tt>classes</tt> directory will be added to the classpath of the org.hangout.org.thirdplace, as well
 * as any JAR files in the <tt>lib</tt> directory. The <tt>org.hangout.org.thirdplace.xml</tt> file is
 * required, and specifies the className of the Plugin implementation. The XML file
 * should resemble the following XML:
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;org.hangout.org.thirdplace&gt;
 *     &lt;class&gt;org.example.YourPlugin&lt;/class&gt;
 *     &lt;name&gt;Example Plugin&lt;/name&gt;
 *     &lt;description&gt;This is an example org.hangout.org.thirdplace.&lt;/description&gt;
 *     &lt;author&gt;Foo Inc.&lt;/author&gt;
 *     &lt;version&gt;1.0&lt;/version&gt;
 *     &lt;minServerVersion&gt;3.0.0&lt;/minServerVersion&gt;
 *     &lt;licenseType&gt;gpl&lt;/licenseType&gt;
 * &lt;/org.hangout.org.thirdplace&gt;</pre>
 * <p>
 * Each org.hangout.org.thirdplace will be loaded in its own class loader, unless the org.hangout.org.thirdplace is configured
 * with a parent org.hangout.org.thirdplace.</p>
 *
 * Please see the Plugin Developer Guide (available with the
 * Openfire documentation) for additional details about org.hangout.org.thirdplace development.
 *
 * @author Matt Tucker
 */
public interface Plugin {

    /**
     * Initializes the org.hangout.org.thirdplace.
     *
     * @param manager the org.hangout.org.thirdplace manager.
     * @param pluginDirectory the directory where the org.hangout.org.thirdplace is located.
     */
    public void initializePlugin(PluginManager manager, File pluginDirectory);

    /**
     * Destroys the org.hangout.org.thirdplace.<p>
     *
     * Implementations of this method must release all resources held
     * by the org.hangout.org.thirdplace such as file handles, database or network connections,
     * and references to core Openfire classes. In other words, a
     * garbage collection executed after this method is called must be able
     * to clean up all org.hangout.org.thirdplace classes.
     */
    public void destroyPlugin();

}
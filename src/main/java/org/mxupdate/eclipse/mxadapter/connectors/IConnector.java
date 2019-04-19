/*
 * Copyright 2008-2010 The MxUpdate Team
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
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.mxupdate.eclipse.mxadapter.connectors;

/**
 * Interface to define a connector between the Eclipse Plug-In and the MX
 * server.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public interface IConnector
{
    /**
     * Executes a 'dispatch' of the MxUdpate Update tools on the MX server.
     *
     * @param _arg1     first argument
     * @param _arg2     second argument
     * @param _arg3     third argument
     * @return returned value from the execution
     * @throws Exception if execute failed
     */
    String execute(final String _arg1,
                   final String _arg2,
                   final String _arg3)
        throws Exception;

    /**
     * Disconnects from the MX server.
     *
     * @throws Exception if disconnect failed
     */
    void disconnect()
        throws Exception;

    /**
     * Checks if the content of the configuration item file must be also
     * transferred or if it is enough that only a link is submitted.
     *
     * @return <i>true</i> if the file content to update must be also
     *         transfered; otherwise <i>false</i>
     */
    boolean isUpdateByFileContent();
}

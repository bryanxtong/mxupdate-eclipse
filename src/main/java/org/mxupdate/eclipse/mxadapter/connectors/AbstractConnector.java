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
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
abstract class AbstractConnector
    implements IConnector
{
    /**
     * Is in the client connector the update done by file contents?
     */
    private final boolean updateByFileContent;

    /**
     * Default constructor to initialize {@link #updateByFileContent}.
     *
     * @param _updateByFileContent  <i>true</i> if CI file content must be
     *                              transferred; otherwise <i>false</i>
     * @see #updateByFileContent
     */
    protected AbstractConnector(final boolean _updateByFileContent)
    {
        this.updateByFileContent = _updateByFileContent;
    }

    /**
     * {@inheritDoc}
     *
     * @see #updateByFileContent
     */
    public boolean isUpdateByFileContent()
    {
        return this.updateByFileContent;
    }
}

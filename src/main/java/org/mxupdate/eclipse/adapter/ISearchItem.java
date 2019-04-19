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

package org.mxupdate.eclipse.adapter;

/**
 * Defines the item of a result for a search.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public interface ISearchItem
{
    /**
     * Returns the type definition of this item.
     *
     * @return type definition
     */
    String getTypeDef();

    /**
     * Returns the name of this item.
     *
     * @return name
     */
    String getName();

    /**
     * Returns the file name of this item.
     *
     * @return file name
     */
    String getFileName();

    /**
     * Returns the file path of this item.
     *
     * @return file path
     */
    String getFilePath();
}

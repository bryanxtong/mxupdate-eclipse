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

import java.util.Set;

/**
 * Represents one node with sub (child) nodes of a type definition tree.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public interface ITypeDefNode
    extends ITypeDefRoot
{
    /**
     * Returns all type definitions which are represented by this type
     * definition tree. This includes also the related type definitions for sub
     * type definition trees.
     *
     * @return set of all assigned type definitions
     */
    Set<String> getTypeDefs();

    /**
     * Returns the (translated) label of the type definition tree.
     *
     * @return label of the type definition tree
     */
    String getLabel();
}

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

package org.mxupdate.eclipse.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;

/**
 * Adapter to the MX database.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public final class CommunicationUtil
{
    /**
     * Private constructor so that this utility could not be initialized.
     */
    private CommunicationUtil()
    {
    }

    /**
     * Encodes given <code>_object</code> to a string with <b>base64</b>.
     *
     * @param _object   object to encode
     * @return encoded string
     * @throws IOException if encode failed
     */
    public static String encode(final Object _object)
        throws IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(_object);
        oos.close();
        return new String(Base64.encodeBase64(out.toByteArray()));
    }

    /**
     * Decodes given string value to an object of given type
     * <code>&lt;T&gt;</code>. First the string is <b>base64</b> decoded, then
     * the object instance is extracted from the decoded bytes via the Java
     * &quot;standard&quot; feature of the {@link ObjectInputStream}.
     *
     * @param <T>   type of the object which must be decoded
     * @param _arg  string argument with encoded instance of
     *              <code>&lt;T&gt;</code>
     * @return decoded object instance of given type <code>&lt;T&gt;</code>
     * @throws IOException              if the value could not be decoded,
     *                                  the decoder stream could not be
     *                                  opened or the argument at given
     *                                  <code>_index</code> is not defined
     * @throws ClassNotFoundException   if the object itself could not be read
     *                                  from decoder stream
     */
    @SuppressWarnings("unchecked")
    public static <T> T decode(final String _arg)
        throws IOException, ClassNotFoundException
    {
        final byte[] bytes = Base64.decodeBase64(_arg.getBytes());
        final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(in);
        final T ret = (T) ois.readObject();
        ois.close();
        return ret;
    }
}

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.mxupdate.eclipse.Activator;
import org.mxupdate.eclipse.util.CommunicationUtil;
import org.osgi.framework.Bundle;

/**
 * Connector to MX via an URL with the MX Jar library (could be via web
 * application server, rmi server or via loaded shared library).
 *
 * @author The MxUpdate Team
 * @version $Id$
 * @see URLConnectorServer
 */
public class URLConnector
    extends AbstractConnector
{
    /**
     * Classes for the server which must be copied so that the server process
     * works. The Base64 stuff is copied from the Apache commons jar library
     * (because the complete Jar library is not necessary).
     *
     * @see #URLConnector(IProject, String, String, String, String, String, boolean)
     */
    private static final Set<Class<?>> SERVER_CLASSES = new HashSet<Class<?>>();
    static {
        URLConnector.SERVER_CLASSES.add(CommunicationUtil.class);
        URLConnector.SERVER_CLASSES.add(URLConnectorServer.class);
        URLConnector.SERVER_CLASSES.add(org.apache.commons.codec.binary.Base64.class);
        URLConnector.SERVER_CLASSES.add(org.apache.commons.codec.BinaryEncoder.class);
        URLConnector.SERVER_CLASSES.add(org.apache.commons.codec.BinaryDecoder.class);
        URLConnector.SERVER_CLASSES.add(org.apache.commons.codec.Decoder.class);
        URLConnector.SERVER_CLASSES.add(org.apache.commons.codec.DecoderException.class);
        URLConnector.SERVER_CLASSES.add(org.apache.commons.codec.Encoder.class);
        URLConnector.SERVER_CLASSES.add(org.apache.commons.codec.EncoderException.class);
    }

    /**
     * External Java process where the connection to MX is established.
     */
    private final Process process;

    /**
     * Output instance.
     */
    private final Writer out;

    /**
     * Handler for the input stream.
     *
     * @see InputStreamHandler
     */
    private final InputStreamHandler inHandler;

    /**
     * Handler for the error stream.
     *
     * @see ErrorStreamHandler
     */
    private final ErrorStreamHandler errHandler;

    /**
     * Flag to store that the connection was correct.
     */
    private final boolean connected;

    /**
     * Initializes the URL connector to MX database. First the required
     * {@link #SERVER_CLASSES Java classes} and {@link #CODEC_LIB codec library}
     * are copied to the <code>_project</code> temporary directory. Then the
     * Java {@link #process} {@link URLConnectorServer} is started which
     * handles the connection to the MX database.
     *
     * @param _project              related eclipse project for which the URL
     *                              connector is initialized
     * @param _javaPath             Java path
     * @param _mxJarPath            path of the MX Jar library
     * @param _url                  URL of the MX server
     * @param _user                 MX user
     * @param _passwd               MX password
     * @param _updateByFileContent  <i>true</i> if update is done with file
     *                              content
     * @throws Exception if connect to given MX server is not possible
     */
    public URLConnector(final IProject _project,
                        final String _javaPath,
                        final String _mxJarPath,
                        final String _url,
                        final String _user,
                        final String _passwd,
                        final boolean _updateByFileContent)
        throws Exception
    {
        this(Activator.getDefault().getStateLocation().append(_project.getName()).toFile(),
                Activator.getDefault().getBundle(),
                _javaPath,
                _mxJarPath,
                _url,
                _user,
                _passwd,
                _updateByFileContent);
    }

    /**
     * Initializes the connection to the MX server.
     *
     * @param _projectPath          path to the project temporary folder
     * @param _bundle               bundle of the plug-in to access the class
     *                              and required libraries for the server
     * @param _javaPath             path for the Java executable
     * @param _mxJarPath            path of the MX Jar library
     * @param _url                  URL of the MX server
     * @param _user                 name of the user on the MX server
     * @param _passwd               password of the user on the MX server
     * @param _updateByFileContent  <i>true</i> if update is done by
     *                              transmitting the file content; otherwise
     *                              <i>false</i>
     * @throws Exception if connection to the MX server could not be started
     */
    public URLConnector(final File _projectPath,
                        final Bundle _bundle,
                        final String _javaPath,
                        final String _mxJarPath,
                        final String _url,
                        final String _user,
                        final String _passwd,
                        final boolean _updateByFileContent)
        throws Exception
    {
        super(_updateByFileContent);

        // copy the required classes and JAR library to temporary project dir.
        if (!_projectPath.exists())  {
            _projectPath.mkdirs();
        }
        for (final Class<?> clazz : URLConnector.SERVER_CLASSES)  {
            // the URL must use slashes to be a valid URL (and
            // File.separatorChar delivers backslashes in Windows)
            final String clazzFileName = "/" + clazz.getName().replace('.', '/') + ".class";
            this.copy(
                    this.getClass().getClassLoader().getResource(clazzFileName),
                    new File(_projectPath, "/bin" + clazzFileName));
        }

        // prepare class path (and always slashes instead of backslashes)
        final StringBuilder classPath = new StringBuilder()
                .append("bin").append('/').append('.')
                .append(File.pathSeparatorChar)
                .append(_mxJarPath.replace('\\', '/'));

        // start process
        final ProcessBuilder pb = new ProcessBuilder(
                _javaPath,
                "-classpath", classPath.toString(),
                URLConnectorServer.class.getName(),
                _url,
                _user,
                _passwd);
        pb.directory(_projectPath);

        this.process = pb.start();

        this.out = new OutputStreamWriter(this.process.getOutputStream());
        this.inHandler = new InputStreamHandler(this.process.getInputStream());
        this.errHandler = new ErrorStreamHandler(this.process.getErrorStream());
        new Thread(this.inHandler).start();
        new Thread(this.errHandler).start();

        try  {
            this.testConnection();
            this.connected = true;
        } finally  {
            if (!this.connected)  {
                this.disconnect();
            }
        }
    }

    /**
     * Calls the test method on the server to be sure the connection to MX
     * works as expected.
     *
     * @throws Exception if connect failed or if execute of the test on the
     *                   server process failed
     * @see #execute(String, String, String, String)
     */
    private void testConnection()
        throws Exception
    {
        if (!"connect".equals(this.execute("test", null, null, null)))  {
            throw new Exception("connect failed");
        }
    }

    /**
     * {@inheritDoc}
     * Internally {@link #execute(String, String, String, String)} is executed
     * for the &quot;<code>dispatch</code> method.
     *
     * @see #execute(String, String, String, String)
     */
    public String execute(final String _arg1,
                          final String _arg2,
                          final String _arg3)
        throws Exception
    {
        return this.execute("dispatch", _arg1, _arg2, _arg3);
    }

    /**
     *
     * @param _method   name of the method on the server to call
     * @param _arg1     first argument
     * @param _arg2     second argument
     * @param _arg3     third argument
     * @return returns string from the server
     * @throws Exception {@link IOException} if the {@link #out} stream could
     *                   not be written,
     *                   {@link ClassNotFoundException} if returned value from
     *                   the server could not be decoded,
     *                   {@link InterruptedException} if the {@link #process}
     *                   is interrupted
     */
    protected String execute(final String _method,
                             final String _arg1,
                             final String _arg2,
                             final String _arg3)
        throws Exception
    {
        // prepare parameters
        final Map<Integer,String> args = new HashMap<Integer,String>();
        args.put(0, _method);
        args.put(1, _arg1);
        args.put(2, _arg2);
        args.put(3, _arg3);
        final String encoded = CommunicationUtil.encode(args);

        // write to output stream
        this.out.write(String.valueOf(encoded.length()));
        this.out.write(' ');
        this.out.write(encoded);
        this.out.write(' ');
        this.out.flush();

        // wait for return
        final int length = Integer.valueOf(this.readOneWord());
        final String value = this.readOneWord();
        if (value.length() != length)  {
            throw new Exception("wrong parameters");
        }
        final Map<String,?> bck = CommunicationUtil.decode(value);

        // return found values
        return (String) bck.get("ret");
    }

    /**
     * Reads next word from the input stream.
     *
     * @return read word
     * @throws Exception    if an error has occurred while reading next word
     *                      this error will be thrown
     */
    private String readOneWord()
        throws Exception
    {
        while (!this.inHandler.wordExists() && this.errHandler.isEmpty())  {
            Thread.sleep(1000);
        }
        if (!this.errHandler.isEmpty())  {
            throw new Exception(this.errHandler.read());
        }
        return this.inHandler.readWord();
    }


    /**
     * {@inheritDoc}
     *
     * @throws IOException if the {@link #out stream} to the MX server could
     *                     not be written
     * @throws InterruptedException if the thread sleep failed (for the loops
     *                     while waiting for the exit of the {@link #process})
     */
    public void disconnect()
        throws IOException, InterruptedException
    {
        final Map<Integer,String> args = new HashMap<Integer,String>();
        args.put(0, "exit");

        final String encoded = CommunicationUtil.encode(args);

        try  {
            if (this.connected)  {
                this.out.write(String.valueOf(encoded.length()));
                this.out.write(' ');
                this.out.write(encoded);
                this.out.write(' ');
                this.out.flush();
            }
        } finally  {
            // destroy process if "normal" exit does not work...
            IllegalThreadStateException ex = null;
            int count = 0;
            do {
                ex = null;
                try  {
                    this.process.exitValue();
                } catch (final IllegalThreadStateException e)  {
                    ex = e;
                    Thread.sleep(1000);
                    count++;
                }
            } while ((ex != null) && (count < 10));
            if (ex != null)  {
                this.process.destroy();
            }
        }
    }

    /**
     * Copy the content of the <code>_from</code> URL to the target
     * <code>_to</code> file.
     *
     * @param _from     URL of the file which must be copied
     * @param _to       target file to which must be copied
     * @throws IOException if the copy failed
     */
    private void copy(final URL _from,
                      final File _to)
        throws IOException
    {
        _to.getParentFile().mkdirs();
        if (!_to.exists())  {
            _to.createNewFile();
        }

        final InputStream in = _from.openStream();
        try  {
            final OutputStream fileOut = new FileOutputStream(_to);
            try {
                IOUtils.copy(in, fileOut);
            } catch (final Throwable e)  {
                e.printStackTrace();
            } finally {
                fileOut.close();
            }
        } finally  {
            in.close();
        }
    }

    /**
     * Handler for the error stream. The error stream is read within a separate
     * thread into {@link #buffer}.
     */
    private static final class ErrorStreamHandler
        extends Thread
    {
        /**
         * Error input stream.
         */
        private final InputStream in;

        /**
         * Used to store already read characters from the
         * {@link #in error input stream}.
         */
        private final StringBuilder buffer = new StringBuilder();

        /**
         * Initializes the {@in error input stream}.
         *
         * @param _in new input stream
         */
        public ErrorStreamHandler(final InputStream _in)
        {
            this.in = _in;
        }

        /**
         * <p>Reads from the {@link #in error input stream} current existing
         * characters and stores them in {@link #buffer}. If no characters
         * exists, this thread waits for 1s and starts again.</p>
         * <p>In the case of an thrown exception (e.g. {@link #in} is closed,
         * ...), an error will be thrown.</p>
         */
        @Override
        public void run()
        {
            try {
                while (true)  {
                    synchronized (this.in) {
                        synchronized(this.buffer)  {
                            while (this.in.available() > 0)  {
                                this.buffer.append((char) this.in.read());
                            }
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (final InterruptedException e) {
                throw new Error(e);
            } catch (final IOException e) {
                throw new Error(e);
            }
        }

        /**
         * Checks if {@link #buffer} is empty.
         *
         * @return <i>true</i> if {@link #buffer} contains no character;
         *          otherwise <i>false</i>
         *
         */
        public boolean isEmpty()
        {
            synchronized (this.buffer)  {
                return this.buffer.length() == 0;
            }
        }

        /**
         * Reads current read buffer. A synchronize on {@link #buffer} is done
         * so that the method waits until no characters are read anymore by
         * {@link #run()}.
         *
         * @return string
         * @see #buffer
         */
        public String read()
        {
            synchronized (this.buffer)  {
                final String ret = this.buffer.toString();
                this.buffer.delete(0, this.buffer.length());
                return ret.toString();
            }
        }
    }

    /**
     * Handler for the input stream of the process. The input stream is read
     * within a thread and stored in a buffer.
     */
    private static final class InputStreamHandler
        extends Thread
    {
        /**
         * Input stream of the process.
         */
        private final InputStream in;

        /**
         * Buffer to store read characters.
         */
        private final Vector<Integer> buffer = new Vector<Integer>();

        /**
         * Initializes the input stream handler.
         *
         * @param _in       input stream of the process
         */
        public InputStreamHandler(final InputStream _in)
        {
            this.in = _in;
        }

        /**
         * <p>Reads from the {@link #in input stream} current existing
         * characters and stores them in {@link #buffer}. If no characters
         * exists, this thread waits for 1s and starts again.</p>
         * <p>In the case of an thrown exception (e.g. {@link #in} is closed,
         * ...), an error will be thrown.</p>
         */
        @Override()
        public void run()
        {
            try {
                while (true)  {
                    while (this.in.available() > 0)  {
                        this.buffer.add(this.in.read());
                    }
                    Thread.sleep(1000);
                }
            } catch (final InterruptedException e) {
                throw new Error(e);
            } catch (final IOException e) {
                throw new Error(e);
            }
        }

        /**
         * Checks if a word exists.
         *
         * @return <i>true</i> if {@link #buffer} contains a space (and a
         *         complete word was returned); otherwise <i>false</i>
         * @see #buffer
         */
        public boolean wordExists()
        {
            return this.buffer.contains(32);
        }

        /**
         * Reads from {@link #buffer} the next word until a space
         * (character 32) is defined.
         *
         * @return read word
         */
        public String readWord()
        {
            final StringBuilder ret = new StringBuilder();
            int bck;
            while ((bck = this.buffer.remove(0)) != 32)  {
                ret.append((char) bck);
            }
            return ret.toString();
        }
    }
}

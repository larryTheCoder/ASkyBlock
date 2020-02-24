/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
 *
 * Permission is hereby granted to any persons and/or organizations
 * using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or
 * any derivatives of the work for commercial use or any other means to generate
 * income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing
 * and/or trademarking this software without explicit permission from larryTheCoder.
 *
 * Any persons and/or organizations using this software must disclose their
 * source code and have it publicly available, include this license,
 * provide sufficient credit to the original authors of the project (IE: larryTheCoder),
 * as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.json.simple.parser;

import java.io.IOException;

/**
 * A simplified and stoppable SAX-like content handler for stream processing of JSON text.
 *
 * @author FangYidong<fangyidong       @       yahoo.com.cn>
 * @see org.xml.sax.ContentHandler
 * @see org.json.simple.parser.JSONParser#parse(java.io.Reader, ContentHandler, boolean)
 */
public interface ContentHandler {
    /**
     * Receive notification of the beginning of JSON processing.
     * The parser will invoke this method only once.
     *
     * @throws ParseException - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     */
    void startJSON() throws ParseException, IOException;

    /**
     * Receive notification of the end of JSON processing.
     *
     * @throws ParseException
     */
    void endJSON() throws ParseException, IOException;

    /**
     * Receive notification of the beginning of a JSON object.
     *
     * @return false if the handler wants to stop parsing after return.
     * @throws ParseException - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     * @see #endJSON
     */
    boolean startObject() throws ParseException, IOException;

    /**
     * Receive notification of the end of a JSON object.
     *
     * @return false if the handler wants to stop parsing after return.
     * @throws ParseException
     * @see #startObject
     */
    boolean endObject() throws ParseException, IOException;

    /**
     * Receive notification of the beginning of a JSON object entry.
     *
     * @param key - Key of a JSON object entry.
     * @return false if the handler wants to stop parsing after return.
     * @throws ParseException
     * @see #endObjectEntry
     */
    boolean startObjectEntry(String key) throws ParseException, IOException;

    /**
     * Receive notification of the end of the value of previous object entry.
     *
     * @return false if the handler wants to stop parsing after return.
     * @throws ParseException
     * @see #startObjectEntry
     */
    boolean endObjectEntry() throws ParseException, IOException;

    /**
     * Receive notification of the beginning of a JSON array.
     *
     * @return false if the handler wants to stop parsing after return.
     * @throws ParseException
     * @see #endArray
     */
    boolean startArray() throws ParseException, IOException;

    /**
     * Receive notification of the end of a JSON array.
     *
     * @return false if the handler wants to stop parsing after return.
     * @throws ParseException
     * @see #startArray
     */
    boolean endArray() throws ParseException, IOException;

    /**
     * Receive notification of the JSON primitive values:
     * java.lang.String,
     * java.lang.Number,
     * java.lang.Boolean
     * null
     *
     * @param value - Instance of the following:
     *              java.lang.String,
     *              java.lang.Number,
     *              java.lang.Boolean
     *              null
     * @return false if the handler wants to stop parsing after return.
     * @throws ParseException
     */
    boolean primitive(Object value) throws ParseException, IOException;

}

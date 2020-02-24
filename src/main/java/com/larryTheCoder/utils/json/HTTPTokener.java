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

package com.larryTheCoder.utils.json;

/**
 * The HTTPTokener extends the JSONTokener to provide additional methods for the parsing of HTTP headers.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
class HTTPTokener extends JSONTokener {
    /**
     * Construct an HTTPTokener from a string.
     *
     * @param string A source string.
     */
    public HTTPTokener(final String string) {
        super(string);
    }

    /**
     * Get the next token or string. This is used in parsing HTTP headers.
     *
     * @return A String.
     * @throws JSONException
     */
    public String nextToken() throws JSONException {
        char c;
        char q;
        final StringBuilder sb = new StringBuilder();
        do {
            c = next();
        } while (Character.isWhitespace(c));
        if ((c == '"') || (c == '\'')) {
            q = c;
            for (; ; ) {
                c = next();
                if (c < ' ') {
                    throw syntaxError("Unterminated string.");
                }
                if (c == q) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
        for (; ; ) {
            if ((c == 0) || Character.isWhitespace(c)) {
                return sb.toString();
            }
            sb.append(c);
            c = next();
        }
    }
}

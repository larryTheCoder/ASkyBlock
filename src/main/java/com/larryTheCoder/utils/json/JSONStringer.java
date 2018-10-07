/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 larryTheCoder and contributors
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

import java.io.StringWriter;

/**
 * JSONStringer provides a quick and convenient way of producing JSON text. The texts produced strictly conform to JSON
 * syntax rules. No whitespace is added, so the results are ready for transmission or storage. Each instance of
 * JSONStringer can produce one JSON text.
 * <p>
 * A JSONStringer instance provides a <code>value</code> method for appending values to the text, and a <code>key</code>
 * method for adding keys before values in objects. There are <code>array</code> and <code>endArray</code> methods that
 * make and bound array values, and <code>object</code> and <code>endObject</code> methods which make and bound object
 * values. All of these methods return the JSONWriter instance, permitting cascade style. For example,
 *
 *
 * <pre>
 * myString = new JSONStringer().object().key(&quot;JSON&quot;).value(&quot;Hello,
 * World!&quot;).endObject().toString();
 * </pre>
 * <p>
 * which produces the string
 *
 *
 * <pre>
 * {"JSON":"Hello, World!"}
 * </pre>
 * <p>
 * The first method called must be <code>array</code> or <code>object</code>. There are no methods for adding commas or
 * colons. JSONStringer adds them for you. Objects and arrays can be nested up to 20 levels deep.
 * <p>
 * This can sometimes be easier than using a JSONObject to build a string.
 *
 * @author JSON.org
 * @version 2008-09-18
 */
class JSONStringer extends JSONWriter {
    /**
     * Make a fresh JSONStringer. It can be used to build one JSON text.
     */
    public JSONStringer() {
        super(new StringWriter());
    }

    /**
     * Return the JSON text. This method is used to obtain the product of the JSONStringer instance. It will return
     * <code>null</code> if there was a problem in the construction of the JSON text (such as the calls to
     * <code>array</code> were not properly balanced with calls to <code>endArray</code>).
     *
     * @return The JSON text.
     */
    @Override
    public String toString() {
        return mode == 'd' ? writer.toString() : null;
    }
}

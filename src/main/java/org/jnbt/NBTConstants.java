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

package org.jnbt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A class which holds constant values.
 *
 * @author Graham Edgecombe
 */
public final class NBTConstants {

    /**
     * The character set used by NBT (UTF-8).
     */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Tag type constants.
     */
    public static final int
            TYPE_END = 0x0,
            TYPE_BYTE = 0x1,
            TYPE_SHORT = 0x2,
            TYPE_INT = 0x3,
            TYPE_LONG = 0x4,
            TYPE_FLOAT = 0x5,
            TYPE_DOUBLE = 0x6,
            TYPE_BYTE_ARRAY = 0x7,
            TYPE_STRING = 0x8,
            TYPE_LIST = 0x9,
            TYPE_COMPOUND = 0xa;

    /**
     * Default private constructor.
     */
    private NBTConstants() {

    }

}
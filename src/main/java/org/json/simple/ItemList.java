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

/*
 * $Id: ItemList.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-3-24
 */
package org.json.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * |a:b:c| => |a|,|b|,|c|
 * |:| => ||,||
 * |a:| => |a|,||
 *
 * @author FangYidong<fangyidong       @       yahoo.com.cn>
 */
public class ItemList {
    List items = new ArrayList();
    private String sp = ",";


    public ItemList() {
    }


    public ItemList(String s) {
        this.split(s, sp, items);
    }

    public ItemList(String s, String sp) {
        this.sp = s;
        this.split(s, sp, items);
    }

    public ItemList(String s, String sp, boolean isMultiToken) {
        split(s, sp, items, isMultiToken);
    }

    public List getItems() {
        return this.items;
    }

    public String[] getArray() {
        return (String[]) this.items.toArray();
    }

    public void split(String s, String sp, List append, boolean isMultiToken) {
        if (s == null || sp == null)
            return;
        if (isMultiToken) {
            StringTokenizer tokens = new StringTokenizer(s, sp);
            while (tokens.hasMoreTokens()) {
                append.add(tokens.nextToken().trim());
            }
        } else {
            this.split(s, sp, append);
        }
    }

    public void split(String s, String sp, List append) {
        if (s == null || sp == null)
            return;
        int pos = 0;
        int prevPos = 0;
        do {
            prevPos = pos;
            pos = s.indexOf(sp, pos);
            if (pos == -1)
                break;
            append.add(s.substring(prevPos, pos).trim());
            pos += sp.length();
        } while (pos != -1);
        append.add(s.substring(prevPos).trim());
    }

    public void setSP(String sp) {
        this.sp = sp;
    }

    public void add(int i, String item) {
        if (item == null)
            return;
        items.add(i, item.trim());
    }

    public void add(String item) {
        if (item == null)
            return;
        items.add(item.trim());
    }

    public void addAll(ItemList list) {
        items.addAll(list.items);
    }

    public void addAll(String s) {
        this.split(s, sp, items);
    }

    public void addAll(String s, String sp) {
        this.split(s, sp, items);
    }

    public void addAll(String s, String sp, boolean isMultiToken) {
        this.split(s, sp, items, isMultiToken);
    }

    /**
     * @param i 0-based
     * @return
     */
    public String get(int i) {
        return (String) items.get(i);
    }

    public int size() {
        return items.size();
    }

    @Override
    public String toString() {
        return toString(sp);
    }

    public String toString(String sp) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < items.size(); i++) {
            if (i == 0)
                sb.append(items.get(i));
            else {
                sb.append(sp);
                sb.append(items.get(i));
            }
        }
        return sb.toString();

    }

    public void clear() {
        items.clear();
    }

    public void reset() {
        sp = ",";
        items.clear();
    }
}

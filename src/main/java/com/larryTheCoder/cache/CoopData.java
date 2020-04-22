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

package com.larryTheCoder.cache;

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.TeamManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores coop data.
 */
public class CoopData {

    private String leaderName;
    private String teamName;
    private List<String> members;

    public CoopData(String leaderName, String teamName, ArrayList<String> members) {
        this.leaderName = leaderName;
        this.teamName = teamName;
        this.members = members;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
        saveData();
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
        saveData();
    }

    public boolean isMember(String name) {
        return members.contains(name) || name.equalsIgnoreCase(leaderName);
    }

    public void addMembers(String members) {
        this.members.add(members);
        saveData();
    }

    public void removeMembers(String member) {
        this.members.remove(member);
        saveData();
    }

    void saveData() {
        TeamManager teamData = ASkyBlock.get().getTManager();
        teamData.storeCoopData(this);
    }

    @Override
    public int hashCode() {
        int i = 60;
        i += leaderName.hashCode();
        i += teamName.hashCode() / 32;

        return i + super.hashCode();
    }

    public List<String> getMembers() {
        return members;
    }
}

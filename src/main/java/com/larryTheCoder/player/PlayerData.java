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
package com.larryTheCoder.player;

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.*;

/**
 * @author larryTheCoder
 */
public class PlayerData implements Cloneable {

    // Player critical information data.
    private final int homes;
    private final String playerName;
    private final HashMap<String, Boolean> challengeList = new HashMap<>();
    private final HashMap<String, Integer> challengeListTimes = new HashMap<>();
    // Coop team for the player user.
    // #TBD
    public String teamLeader;
    String leader;
    private int resetleft;
    private int islandLevel;
    private ArrayList<String> banList = new ArrayList<>();
    private String pubLocale;
    public String teamIslandLocation;
    public boolean inTeam;
    public ArrayList<String> members = new ArrayList<>();
    public String name;

    public PlayerData(String playerName, int homes, int resetleft) {
        this.playerName = playerName;
        this.homes = homes;
        this.resetleft = resetleft;
        this.pubLocale = Settings.defaultLanguage;
        setupChallengeList();
    }

    public PlayerData(String playerName, int homes, ArrayList<String> members, String challenges, String challengesTime, int islandlvl, boolean inTeam, String teamleader, String teamIslandloc, int resetleft, ArrayList<String> banList, String locale, String teamName) {
        this.homes = homes;
        this.members = members;
        this.inTeam = inTeam;
        this.islandLevel = islandlvl;
        this.teamLeader = teamleader;
        this.teamIslandLocation = teamIslandloc;
        this.resetleft = resetleft;
        this.playerName = playerName;
        this.banList = banList;
        this.pubLocale = locale;
        this.name = teamName;
        encodeChallengeList(challenges, challengesTime); // Safe
    }

    public int getHomeNumber() {
        return homes;
    }

    /**
     * Gets the player name of this data.
     *
     * @return A player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Get the player reset
     *
     * @return the value of the player reset
     */
    public int getPlayerReset() {
        return resetleft;
    }

    /**
     * Gets the reset left for this user
     *
     * @param resetleft The value to be set
     */
    public void setPlayerReset(int resetleft) {
        this.resetleft = resetleft;
    }

    /**
     * Get the user's island level. Its basically
     * An XP Stats for SkyBlock
     *
     * @return The island level value
     */
    public int getIslandLevel() {
        return islandLevel;
    }

    /**
     * Get the user banned list for the SkyBlock users
     * This is more likely that this user hates that person.
     *
     * @return A list of string of player names
     */
    public ArrayList<String> getBanList() {
        return banList;
    }

    public String getLocale() {
        return pubLocale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(String locale) {
        this.pubLocale = locale;
    }

    /**
     * Checks if a challenge not exists in the player's challenge list
     *
     * @param challenge The challenge to be checked
     * @return true if challenge is listed in the player's challenge list,
     * otherwise false
     */
    public boolean challengeNotExists(final String challenge) {
        return !challengeList.containsKey(challenge.toLowerCase());
    }

    /**
     * Checks if a challenge is recorded as completed in the player's challenge
     * list or not
     *
     * @param challenge The challenge to be checked
     * @return true if the challenge is listed as complete, false if not
     */
    public boolean checkChallenge(final String challenge) {
        if (challengeList.containsKey(challenge.toLowerCase())) {
            // plugin.getLogger().info("DEBUG: " + challenge + ":" +
            // challengeList.get(challenge.toLowerCase()).booleanValue() );
            return challengeList.get(challenge.toLowerCase());
        }
        return false;
    }

    /**
     * Checks how many times a challenge has been done
     *
     * @param challenge The challenge to be checked
     * @return number of times
     */
    public int checkChallengeTimes(String challenge) {
        if (challengeListTimes.containsKey(challenge.toLowerCase())) {
            //Utils.sendDebug("DEBUG: check " + challenge + ":" + challengeListTimes.get(challenge.toLowerCase()));
            return challengeListTimes.get(challenge.toLowerCase());
        }
        return 0;
    }

    /**
     * Map of all of the known challenges and how many times each
     * one has been completed. This is a view of the challenges
     * map that only allows read operations.
     *
     * @return The list of all the challenges times
     */
    public Map<String, Integer> getChallengeTimes() {
        return Collections.unmodifiableMap(challengeListTimes);
    }

    public Map<String, Boolean> getChallengeStatus() {
        return Collections.unmodifiableMap(challengeList);
    }

    /**
     * Records the challenge as being complete in the player's list. If the
     * challenge is not listed in the player's challenge list already, then it
     * will be added.
     *
     * @param challenge The challenge name
     */
    public void completeChallenge(final String challenge) {
        // plugin.getLogger().info("DEBUG: Complete challenge");
        challengeList.put(challenge.toLowerCase(), true);
        // Count how many times the challenge has been done
        int times = 0;
        if (challengeListTimes.containsKey(challenge.toLowerCase())) {
            times = challengeListTimes.get(challenge.toLowerCase());
        }
        times++;
        challengeListTimes.put(challenge.toLowerCase(), times);
        // Utils.sendDebug(decodeChallengeList("clt"));
        // plugin.getLogger().info("DEBUG: complete " + challenge + ":" +
        // challengeListTimes.get(challenge.toLowerCase()).intValue() );
    }

    /**
     * Resets a specific challenge.
     * @param challenge the challenge name
     */
    public void resetChallenge(final String challenge) {
        //plugin.getLogger().info("DEBUG: reset challenge");
        challengeList.put(challenge, false);
        challengeListTimes.put(challenge, 0);
    }

    /**
     * Decode a challenge list that needs
     * to be saved into database got
     * a raw of data of it.
     *
     * @param type The type of the data needs to be decoded
     *             either its 'cl' or 'clt'
     * @return decoded data of the type
     */
    public String decodeChallengeList(String type) {
        StringBuilder buf = new StringBuilder();
        // Need to decode one of these
        if (type.equalsIgnoreCase("cl")) {
            challengeList.forEach((key, value) -> {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(key).append(":").append(value ? "1" : "0");
            });
        } else if (type.equalsIgnoreCase("clt")) {
            challengeListTimes.forEach((key, value) -> {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(key).append(":").append(value);
            });
        } else {
            Utils.send("&cUnknown challenge list: " + type + ", returning null...");
            buf.append("null");
        }
        return buf.toString();
    }

    private void encodeChallengeList(String challenges, String challengesTime) {
        setupChallengeList();
        try {
            // Challenges encode for PlayerData.challengeList
            String[] at = challenges.split(", ");
            for (String string : at) {
                String[] at2 = string.split(":");
                ArrayList<String> list = new ArrayList<>(Arrays.asList(at2));

                boolean value = list.get(1).equalsIgnoreCase("1");
                challengeList.put(list.get(0).toLowerCase(), value);
            }

            // Challenges encode for PlayerData.challengeListTimes
            at = challengesTime.split(", ");
            for (String string : at) {
                String[] at2 = string.split(":");
                ArrayList<String> list = new ArrayList<>(Arrays.asList(at2));

                challengeListTimes.put(list.get(0).toLowerCase(), Integer.parseInt(list.get(1)));
            }
        } catch (Exception ignored) {
            //Utils.sendDebug"Player data is outdated, resetting its data");
            ASkyBlock.get().getDatabase().savePlayerData(this);
        }
    }

    /**
     * Prepare the challenge list for the
     * first time to to the user
     */
    private void setupChallengeList() {
        for (String challenges : Settings.challengeList) {
            challengeList.put(challenges, false);
            challengeListTimes.put(challenges, 0);
        }
    }

    /**
     * Saves all of the data in this plugin
     * into database without trying to use
     * the hard way.
     */
    public void saveData() {
        ASkyBlock.get().getDatabase().savePlayerData(this);
    }
}

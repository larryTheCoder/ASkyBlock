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
import com.larryTheCoder.database.QueryInfo;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.sql2o.data.Row;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author larryTheCoder
 */
public class PlayerData implements Cloneable {

    // Player critical information data.
    @Getter
    public final String playerName;
    @Getter
    public final String playerXUID;

    @Setter
    @Getter
    private int resetLeft;

    private final HashMap<String, Boolean> challengeList = new HashMap<>();
    private final HashMap<String, Integer> challengeListTimes = new HashMap<>();

    @Getter
    @Setter
    private String locale;
    @Getter
    private List<String> banList;

    /**
     * Dummy PlayerData class that were used for challenges initialization.
     */
    public PlayerData() {
        playerName = null;
        playerXUID = null;

        setupChallengeList();
    }

    private PlayerData(String playerName, String playerXUID, String pubLocale, List<String> banList, int resetAttempts, Row clData) {
        this.playerName = playerName;
        this.playerXUID = playerXUID;

        this.locale = pubLocale;
        this.banList = banList;
        this.resetLeft = resetAttempts;

        if (clData == null) {
            setupChallengeList();
        } else {
            encodeChallengeList(clData.getString("challengesList"), clData.getString("challengesTimes"));
        }
    }

    public static PlayerData fromRows(Row dataRow, Row challengeData) {
        return new PlayerData(
                dataRow.getString("playerName"),
                dataRow.getString("playerUUID"),
                dataRow.getString("locale"),
                Utils.stringToArray(dataRow.getString("banList"), ":"),
                dataRow.getInteger("resetAttempts"),
                challengeData);
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

    @Override
    public String toString() {
        return String.format("PlayerData(playerName=%s, playerXUID=%s, resetLeft=%s, locale=%s)",
                playerName, playerXUID, resetLeft, locale);
    }

    /**
     * Save player data asynchronously.
     */
    public void saveData() {
        ASkyBlock.get().getDatabase().processBulkUpdate(new QueryInfo("UPDATE player SET locale = :locale, banList = :banList, resetAttempts = :resetLeft, lastLogin = :lastLogin WHERE playerName = :playerName")
                        .addParameter("playerName", playerName)
                        .addParameter("locale", locale)
                        .addParameter("banList", Utils.arrayToString(banList))
                        .addParameter("resetLeft", resetLeft)
                        .addParameter("lastLogin", Timestamp.from(Instant.now()).toString()),
                new QueryInfo("UPDATE challenges SET challengesList = :challengesList, challengesTimes = :challengesTimes WHERE player = :playerName")
                        .addParameter("playerName", playerName)
                        .addParameter("challengesList", decodeChallengeList("cl"))
                        .addParameter("challengesTimes", decodeChallengeList("clt")));
    }
}

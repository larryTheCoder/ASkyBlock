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

package com.larryTheCoder.storage;

/**
 * Caches information for an object.
 */
public class FastCache {

    // FastCache Theory:
    //    This FC, which is an abbreviation for FastCache, is used to fetch
    //    PlayerData and IslandData as fast as it could. In order to achieve this, a hierarchy
    //    of 0-5 is recorded in a table as follows where the data is tabulated after the player left the game:

    //    ID      INFO              TIME                                 DESCRIPTION
    //    0 - Extremely active [Less than 12h]                    This data loss rate is 0%
    //    1 - Casually active  [Average of 12-24h]      Data loss rate for this section is about 15%
    //    2 - Active           [Average of 24-48h]              30% chances of data loss rate
    //    3 - Less active      [Average of 48-96h]              55% chances of data loss rate
    //    4 - Not active       [Average of 96-144h]       70% chances for the data to destruct itself
    //    5 - Rarely active    [More than 144h]          95% overall chances of data to destruct itself

    //    DATA LOSS RATE: 1/10000s

    //    So in order to achieve this, a HashMap consisting of Level Name, Player name, and home UNIQUE were placed.
    //    The HashMap however, consisting of 6 HashMap functions, where those functions are aligned according to the
    //    theory above.

    //    In addition, if the data doesn't exists in the map, we need to fetch it from the database.
    //    But before that happens, the HashMap must store their data during startup. By this way, FC
    //    Can be used right after the server started, without having to query any database during in-game.

    //    Diagram of how this code executed:

    //    [From] --> {Asks for IslandData [Player, Home]} -->
}

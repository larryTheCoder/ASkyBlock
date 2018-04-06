/*
 * Copyright (C) 2017 Adam Matthew
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.larryTheCoder.network;

/**
 * The next generation of skyblock
 * interface with multi server control
 * <p>
 * Usage are only in server
 */
public class BBProtocol {

    public byte PLAYER_SEND_PACKET = 0x1;
    public byte SERVER_SEND_PACKET = 0x2;
    public byte SERVER_UUID_PACKET = 0x3;
    public byte SEND_PLAYER_DATA = 0x4;
    // TODO: More catch up on network protocols

    public BBProtocol() {
        // TODO: Server that respond this connection
    }
}

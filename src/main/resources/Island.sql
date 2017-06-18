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
 */
/**
 * Author:  Adam Matthew
 * Created: Feb 5, 2017
 */
CREATE TABLE IF NOT EXISTS `player` (`player` VARCHAR NOT NULL , 
                                    `homes` INTEGER NOT NULL , 
                                    `resetleft` INTEGER NOT NULL , 
                                    `banlist` VARCHAR , 
                                    `teamleader` VARCHAR , 
                                    `teamislandlocation` VARCHAR , 
                                    `inteam` BOOLEAN , 
                                    `members` VARCHAR , 
                                    `name` VARCHAR)
CREATE TABLE IF NOT EXISTS `worlds` (`world` VARCHAR)
CREATE TABLE IF NOT EXISTS `island` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , 
                                    `islandId` INTEGER NOT NULL , 
                                    `x` INTEGER NOT NULL , 
                                    `y` INTEGER NOT NULL , 
                                    `z` INTEGER NOT NULL , 
                                    `owner` VARCHAR NOT NULL , 
                                    `name` VARCHAR NOT NULL , 
                                    `world` VARCHAR NOT NULL , 
                                    `biome` VARCHAR NOT NULL , 
                                    `locked` INTEGER NOT NULL)


## ASkyBlock ##
[![PayPayl donate button](https://img.shields.io/badge/paypal-donate-yellow.svg)](http://www.paypal.me/DoubleCheese)
[![Circle-CI](https://circleci.com/gh/larryTheCoder/ASkyBlock-Nukkit.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/larryTheCoder/ASkyBlock-Nukkit)

__Special SkyBlock minigame build specially for Nukkit.__

## API
This plugin has an API for an example:

```java
public boolean openAPI(){
    Plugin plugin = this.getServer().getPluginManager().getPlugin("ASkyBlock");
    // Get the plugin
    if (plugin == null) {
    	// Report here
    	return false;
   	}
    // Get the plugin instance
    ASkyBlock block = (Plugin) ASkyBlock.get();
    this.instance = block;
    // Place the things you want to
    return true;
}
```

## Commands Help

There a lot of command to list here but you can use `/is help` in console or in game! But make sure you install this plugin first!

## Features
This plugin is best for `Single-world` production server.

Current features are:

* Schematic loader (Which paste and load schematic file)
* Island chest (Depends on your island schematic)
* Island team (Teammate are best than alone)
* Safe teleport (Afraid to teleport into void? don't worry!)
* Better Math (No collided island generated)
* Better config (Never get bored to see my config all day 24/7)
* Database (Implementation of MySql and Sqlite)
* More colorful (Nice and cozy)

## Installation
This plugin version support Nukkit Server v1.0.8.
This plugin also depends on these plugins:

* DbLib 0.2.x
* EconomyAPI

Please take attention, this plugin support Nukkit v1.0.8 (API 1.0.0) 
Due to getSide(I) API changes. This plugin no longer support lower than v1.0.8

### Releases
Pre-releases are considered **unsafe** for production servers.

Releases have a clean version number, has been tested, and should be safe for production servers.

### Note for developers
This is an actively developed project. Pull requests are accepted if they address a specific issue and are of high enough quality. The best way to do a pull request is to file an issue, then say that you'll fix it so we know it's coming and can coordinate with you.

### License

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

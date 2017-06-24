# ASkyBlock-Nukkit
[![PayPayl donate button](https://img.shields.io/badge/paypal-donate-yellow.svg)](paypal.me/DoubleCheese)

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
  
__Welcome developers to this Magic SkyBlock plugin builded by me! 
Its still in development but I really need your help to complete this plugin.
Build specially for Nukkit__

## Commands Help

Default command parameters and permissions:

| Command | Parameters | Info | Permission | Default |
| :-----: | :-------: | :---------: | :-------: | :-------: |
| `/is` | `args` | `Main island command` | `is.command` | `All` |
| `/is help` | `command` | `show help for island's command` | `none` | `All` |
| `/is generate` | `world name` | `Create a new island world` | `is.admin.generate` | `admin` |
| `/is accept` | `none` | `Accept an invitation from other player` | `is.command.accept` | `All` |
| `/is expel` | `player` | `Kick a player from your island` | `is.command.expel` | `All` |
| `/is kick` | `player` | `Kick a player from island world` | `is.admin.kick` | `admin` |

# API
This plugin has an api for an example:

    public void openAPI(){
        Plugin plugin = this.getServer().getPluginManager().getPlugin("ASkyBlock");
        // Get the plugin
        if (plugin == null) {
            // Disable this plugin
            return null;
        }
        // Get the plugin instance
        ASkyBlock block = (Plugin) ASkyBlock.get();
        this.instance = block;
        // Place the things you want to
    }

# Installation

This version depends on the following plugins:

* Nukkit MCPE v1.0.8++
* DbLib 0.2.x

**Note:** This plugin had been tested on Nukkit v1.0.4. The server under this MC version might not
supported due to *API implementation*

### Releases
Pre-releases are considered **unsafe** for production servers.

Releases have a clean version number, has been tested, and should be safe for production servers.

**Circle CI**: [Download link](https://circleci.com/gh/larryTheCoder/ASkyBlock-Nukkit)

## Config-files

*For experts only*

The plugin folder is a variables that contains DB, YAML, SQL. Please be informed that this plugin 
will try to re-update the old file into new one. The case is the player data might be gone, replaced
and corrupted. It is recommended to not using this procedure.

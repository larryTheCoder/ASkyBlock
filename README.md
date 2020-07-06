<h1>ASkyBlock<img src="https://cdn.discordapp.com/attachments/512987829970665482/729263817858744330/branding.gif" height="180" width="180" align="right"></img></h1>

[![Jenkins-CI](https://jenkins.potatohome.xyz/job/ASkyBlock/badge/icon)](https://jenkins.potatohome.xyz/job/ASkyBlock)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.me/Permeable)

    Made for HysteriaPE, Crafted with <3
    play.hysteria-pe.xyz:19132

Introduction
-------------
Based on Bukkit most famous plugin, ASkyBlock, a port of ASkyBlock which is now changed to 
[BentoBox](https://github.com/BentoBoxWorld/BentoBox). The port is not difficult to get done 
with the support of all Nukkit communities back in 2018. Now, I am bringing back the project
back alive.

All commands based on the original ASkyBlock plugin. Not all the commands taken from there 
but to provide best game experience, most of the commands taken from there.

### Why should you use this plugin?
Good question sir, let me explain, first, this plugin relies on Java and Cloudburst Server (formerly NukkitX),
which **brings an incredible amount of performance** against PocketMine-MP. 

Second, the amount of **features implemented in this plugin is far greater than any of the plugins available**
out there. Safe island teleportation, Island level calculations that is ran under asynchronous task and
multithreaded alike database operations (Further discussions required to ensure the database performance capability).

Third, it is easy to use this plugin, all islands created automatically and players will setup their island
with forms. Technically, all the operations for the plugin handled by forms. **Interface made easy**, another
differences within other open-sourced plugin out there. 

Lastly, this plugin can support **more than any type of islands**, you can create a custom
island by pasting a `.schematic` in schematics folder. Although you may need to configure it in `schematics/configuration.yml`, the values
will be set to default. Fallbacks also supported if you want to use default island generation from 2016.

Installation
-------------
For proper installation, **you must install DbLib** into your server.
It is a utility plugin that provides database classpath and utilities.

- DbLib: https://cloudburstmc.org/resources/dblib.12

#### Downloading
To download this plugin, you can either choose to download from [JenkinsCI](https://jenkins.potatohome.xyz/job/ASkyBlock) or from our verified [Cloudburst Plugins](https://cloudburstmc.org/resources/askyblock.79/) section.

**WARNING:** Downloading a build from Jenkins can result an unexpected error, or a crash.

#### Dependencies
Last but not least, this plugin can support multiple plugins as dependencies.
I have integrated a few plugins as requested by several players. You can install these if you want to, no heart feelings taken.

- EconomyAPI: https://cloudburstmc.org/resources/categories/economy.6
- LuckPerms v5.x: https://cloudburstmc.org/resources/luckperms.51
- ZChat: https://cloudburstmc.org/resources/zchat.282

Features
-------------
* Asynchronous island operations, support caching up to 100 days.
* Fantastic Gridding Technique proposed by tastybento.
* Safe teleportation and is guaranteed that you will not be teleported into lava.
* Supports many types of island in `.schematic` format.
* Supports more than 1 world in this plugin.
* No more 1 island in 1 world, every island shared distinctly.
* Supports Mysql* and Sqlite databases, cleaner and readable database.
* Island Settings, now you can configure what is needed for your island.
* Magic cobblestone generator, yes we have that too here.
* Per world configuration, you can configure your world settings in `world.yml`
* Island block calculations, scoring is now available. 

*Mysql operations may not be working for now.

Note
----
So that, if this plugin is useful for you, please consider starring this repo qwq.
*Just star it, I know you can*

It would help this plugin to brings back its popularity. Also I might need some help for translating the 
locale files. Feel free to open a Pull Request or ask me in discord, MrPotato101#0060 (Yup im still using this tag until today). 

I will be writing a [wiki page](https://github.com/larryTheCoder/ASkyBlock/wiki), any questions regarding the plugin and its API will be available there.


License
---------

    Adapted from the Wizardry License

    Copyright (c) 2016-2020 larryTheCoder and contributors

    Permission is hereby granted to any persons and/or organizations
    using this software to copy, modify, merge, publish, and distribute it.
    Said persons and/or organizations are not allowed to use the software or
    any derivatives of the work for commercial use or any other means to generate
    income, nor are they allowed to claim this software as their own.

    The persons and/or organizations are also disallowed from sub-licensing
    and/or trademarking this software without explicit permission from larryTheCoder.

    Any persons and/or organizations using this software must disclose their
    source code and have it publicly available, include this license,
    provide sufficient credit to the original authors of the project (IE: larryTheCoder),
    as well as provide a link to the original project.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
    PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
    TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
    USE OR OTHER DEALINGS IN THE SOFTWARE.
# Mob Stats

**Keep track of your users' PvE actions - MySQL or SQLite recommended**

This plugin will keep records of how many mob kills, deaths, kills in a row a player has, or vice versa.

***

## Features

- Records kills, deaths, max streaks, current streak, kill/death ratio

### Options

- MySQL support (highly recommended)
- SQLite support
- MobArena integration

***

## Dependencies

- Spigot - unclear which versions will work. Tested on 1.16, the plugin this is based on has been tested down to 1.13.
- My [Core](https://github.com/slipcor/Core) library (automatically added to the plugin before release)

***

## Downloads

- [spigotmc.org](https://www.spigotmc.org/resources/mobstats.90776/)
- [Discord - #pvpstats-builds](https://discord.gg/BNkk46vRKa)


***

## How to install

- Stop your server
- Place jar in plugins folder
- Run a first time to create config
- Configure database settings
- Reboot again, done!

***

## Documentation

- [API](doc/api.md)
- [Commands](doc/commands.md)
- [Permissions](doc/permissions.md)
- [Configuration](doc/configuration.md)
- [LeaderBoards](doc/leaderboards.md)

***

## Changelog

- v0.1.7 - use my Core API and the following upstream issues:
  - **FEAT** - add ability to run multiple commands on a streak
  - **FEAT** - allow to count pet kills
  - **FEAT** - support late loading of worlds
  - **FIX** - fix newbie check for deaths not from PvE
  - **FIX** - properly treat the "simple calculation" config node as boolean
  - **FIX** - replace several hardcoded messages with proper language settings
  - **FIX** - fix issue with MySQL table checks with limited permissions
- [read more](doc/changelog.md)

***

## Phoning home

By default, the server contacts www.bstats.org to notify that you are using my plugin.

Please refer to their website to learn about what they collect and how they handle the data.

If you want to disable the tracker, set "bStats.enabled" to false in the __config.yml__ !

***

## Support

I am developing this plugin in my free time, so if you have an issue, please create an issue describing the problem in detail. I will do my best to reply as soon as possible, but that can take a few days sometimes.

For some problems, it might be easier to join the [Discord](https://discord.gg/DSNfjYA) and open a ticket there. It allows for quicker transfer of log files, debug files and so on. Moreover, simple questions can be answered more quickly there rather than opening an issue for them.

Joining the Discord Server gets you early access to latest builds, and maybe it is your preferred method to interact with me? Be my guest!

***

## Credits

- Narcox for the idea


***

## Todos

- Move language nodes into proper block logic

***
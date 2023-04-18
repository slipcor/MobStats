# Command list

Click on a command to view more information about it. The shorthand can be used instead of the command name, E.g. `/mobstats !w` to wipe stats.

## Commands about getting stats
_Get your or other player's stats_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/mobstats show](commands/show.md) | !sh | show your own stats
[/mobstats show](commands/show.md) [player] | /stats !sh [player] | show the player's stats
[/mobstats top](commands/top.md) [amount] | /stats !t [amount] | show the top [amount] killers
[/mobstats top](commands/top.md) [type] [amount] | /stats !t [type] [amount] | show the top [amount] of a category
[/mobstats topplus](commands/topplus.md) [type] [days] {amount} | /stats !tp [type] [days] {amount} | show the top [amount] of a the top [type] players in the last [amount] days
[/mobstats topworkd](commands/topworld.md) [type] [world] [days] {amount} | /stats !tw [type] {amount} | show the top [amount] of a the top  [type] players in the last [amount] days in world [world]

Valid categories are: kills, deaths, streak

Note that the "topplus" command and the "topworld" command are made for database access and only use **kills**, **deaths** and **ratio**! YML will not work or be very slow!

***

## Commands about setting stats
_Manually set a certain value of a player's statistic_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/mobstats set](commands/set.md) | /stats !st [player] [type] [amount] | set a player's [type] statistic

Valid types are: kills, deaths, streak, currentstreak

***

## Database management commands
_Heavy maintenance_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/mobstats debug](commands/debug.md) [on/off] | /stats !d [on/off] | activate or deactivate debugging
[/mobstats debugkill](commands/debugkill.md) {killer} {victim} | /stats !dk {killer} {victim} | manually add a kill for debugging
[/mobstats migrate](commands/migrate.md) [from/to] [yml/sqlite/mysql] | migrate from / to other database method
[/mobstats purge](commands/purge.md) [standard/specific/both] [days] | /stats !p [standard/specific/both] [days] | remove entries older than [days], defaults to 30
[/mobstats reload](commands/reload.md) | /stats !r | reload config and language files
[/mobstats wipe](commands/wipe.md) {player} | /stats !w {player} | wipe a player's or all statistics

***

## Config management commands
_use with caution_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/mobstats config](commands/config.md) get [node] | /stats !c get ignoreworlds | get the value of a config node
[/mobstats config](commands/config.md) set [node] [value] | /stats !c set OPMessages false | set the value of a config node
[/mobstats config](commands/config.md) add [node] [value] | /stats !c add ignoreworlds Spawn | add an entry to a config list
[/mobstats config](commands/config.md) remove [node] [value] | /stats !c remove ignoreworlds pvparena | remove an entry from a config list

***
# [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) Placeholders

These are the placeholders you can use where PAPI Placeholders are supported:

**🔴 Shorthand needs to be enabled in the config: 🔴
`/mobstats config set shortPlaceholders true`**
Please note that you can only either use long OR short placeholders.

🟡 if you use [MVdWPlaceholderAPI](https://www.spigotmc.org/resources/mvdwplaceholderapi.11182/), you need to prefix the placeholders with `placeholderapi_` 🟡

## Player based statistics

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcormobstats_kills | sms_k | The player's kill count
slipcormobstats_deaths | sms_d | The player's death count
slipcormobstats_streak | sms_s | The player's current streak
slipcormobstats_maxstreak | sms_m | The player's highest streak
slipcormobstats_ratio | sms_r | The player's kill/death ratio

## Top X list

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcormobstats_top_kills_head_5 | sms_t_kills_h_5 | heading ("Top 5 Kills")
slipcormobstats_top_kills_1 | sms_t_kills_1 | Top player entry ("1. SLiPCoR: 100")
slipcormobstats_top_kills_2 | sms_t_kills_2 | Second player entry ("2. garbagemule: 70")
slipcormobstats_top_kills_3 | sms_t_kills_3 | ...
slipcormobstats_top_kills_4 | sms_t_kills_4 | ...
slipcormobstats_top_kills_5 | sms_t_kills_5 | ...

## Flop X list

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcormobstats_flop_kills_head_5 | sms_f_kills_h_5 | heading ("Flop 5 Kills")
slipcormobstats_flop_kills_1 | sms_f_kills_1 | Worst player entry ("1. SLiPCoR: 0")
slipcormobstats_flop_kills_2 | sms_f_kills_2 | Second worst player entry ("2. garbagemule: 10")
slipcormobstats_flop_kills_3 | sms_f_kills_3 | ...
slipcormobstats_flop_kills_4 | sms_f_kills_4 | ...
slipcormobstats_flop_kills_5 | sms_f_kills_5 | ...

---

Valid statistical entries instead of "kills" for the above lists are:
* **deaths** (🟡 sorting ascending by default! 🟡)
* **streak** (maximum streak)
* **k-d** (kill/death ratio, can be defined to fancy things in the config)

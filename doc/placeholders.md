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

## Top X list PLUS

### Top values in the last X days

Default Placeholder | Shorthand           | Meaning
------------- |---------------------| -------------
slipcormobstats_topplus_kills_head_5_30 | sms_tp_kills_h_5_30 | heading ("Top 5 Kills")
slipcormobstats_topplus_kills_1_30 | sms_tp_kills_1_30   | Top player entry ("1. SLiPCoR: 100")
slipcormobstats_topplus_kills_2_30 | sms_tp_kills_2_30   | Second player entry ("2. garbagemule: 70")
slipcormobstats_topplus_kills_3_30 | sms_tp_kills_3_30   | ...
slipcormobstats_topplus_kills_4_30 | sms_tp_kills_4_30   | ...
slipcormobstats_topplus_kills_5_30 | sms_tp_kills_5_30   | ...

## Top X list WORLD

### Top values in world 'world'  in the last 30 days

Default Placeholder | Shorthand                         | Meaning
------------- |-----------------------------------| -------------
slipcormobstats_topworld_kills_head_5_**world**_**3**0 | sms_tw_kills_h_5_**world**_**30** | heading ("Top 5 Kills")
slipcormobstats_topworld_kills_1_**world**_**3**0 | sms_tw_kills_1_**world**_**30**   | Top player entry ("1. SLiPCoR: 100")
slipcormobstats_topworld_kills_2_**world**_**3**0 | sms_tw_kills_2_**world**_**30**   | Second player entry ("2. garbagemule: 70")
slipcormobstats_topworld_kills_3_**world**_**3**0 | sms_tw_kills_3_**world**_**30**   | ...
slipcormobstats_topworld_kills_4_**world**_**3**0 | sms_tw_kills_4_**world**_**30**   | ...
slipcormobstats_topworld_kills_5_**world**_**3**0 | sms_tw_kills_5_**world**_**30**   | ...

---

Valid statistical entries instead of "kills" for the above lists are:
* **deaths** (🟡 sorting ascending by default! 🟡)
* **streak** (maximum streak)
* **k-d** (kill/death ratio, can be defined to fancy things in the config)

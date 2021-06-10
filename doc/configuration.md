# Configuration File

This is the default configuation file.

    # === [ MySQL Settings ] ===
    MySQL:
      # activate MySQL
      active: false
      # hostname to use to connect to the database, in most cases 'localhost'
      hostname: host
      # username to use to connect to the database
      username: user
      # password to use to connect to the database
      password: pw
      # database name to connect to
      database: db
      # database port to connect to
      port: 3306
      # general statistic table name
      table: mobstats
      # kill statistic table name
      killtable: mobkillstats
      # connection options
      options: ?autoReconnect=true
    # === [ SQLite Settings ] ===
    SQLite:
      # activate SQLite
      active: false
      # general statistic table name
      table: mobstats
      # kill statistic table name
      killtable: mobkillstats
    # === [ YML Database Emulation Settings ] ===
    YML:
      # general statistic table name
      table: mobstats
      # kill statistic table name
      killtable: mobkillstats
    # === [ Statistic Settings ] ===
    statistics:
      # save every kill - is never read internally, so only for web stats or alike
      collectPrecise: false
      # always reset a streak when a player disconnects
      resetKillstreakOnQuit: false
      # count dying from other sources than players towards death count and resetting of streaks
      countRegularDeaths: false
      # mathematical formula to calculate kill/death ratio
      killDeathCalculation: '&k/(&d+1)'
    # === [ Integration into other Plugins ] ===
    other:
      # count Mob Arena kills/deaths
      MobArena: false
    # world names where not to count statistics
    ignoreworlds:
      - doNotTrack
    # === [ Message Settings ] ===
    msg:
      overrides: false
      main:
        - '&cName: &7%n'
        - '&cKills: &7%k'
        - '&cDeaths: &7%d'
        - '&cRatio: &7%r'
        - '&cStreak: &7%s'
        - '&cMax Streak: &7%m'
    # locations of leaderboards
    leaderboards: []
    # === [ Updater Settings ] ===
    update:
      # phone home to inform slipcor that you use the plugin
      tracker: true
      # what to do? Valid values: disable, announce, download, both
      mode: both
      # which type of branch to get updates? Valid values: dev, alpha, beta, release
      type: beta

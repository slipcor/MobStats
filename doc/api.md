# How to use the API

This documentation is not extensive, but explains up the methods that I would deem safe to use by other plugins, and eventual caveats.

## net.slipcor.mobstats.api.DatabaseAPI

### AkilledB - emulate a kill

    Entity attacker; // can be null
    Entity victim;   // can be null

    // There will be checks for newbie status, whether one is a valid Entity object, etc
    DatabaseAPI.AkilledB(attacker, victim);

### setSpecificStat - set a specific statistic value

    Entity entity; // the Entity whose value to set
    String entry; // the attribute to set
    int value; // the value to set it to
    
    // valid values for entry: "kills", "deaths", "streak", "currentstreak"
    DatabaseAPI.setSpecificStat(entity.getUniqueEid(), entry, value)

## net.slipcor.mobstats.api.LeaderboardBuffer

### top - get the top stats 

    int value;     // how many entries to get (max: 10);
    String value;  // the information to get and to sort by
    
    // valid values for type: "kills", "deaths", "streak", "currentstreak"
    String[] lines = LeaderboardBuffer.top(value, type);
    
    // lines will be an array of formatted values, by default "1. {player}: {value}"


### flop - get the bottom stats

    int value;     // how many entries to get (max: 10);
    String value;  // the information to get and to sort by
    
    // valid values for type: "kills", "deaths", "streak", "currentstreak"
    String[] lines = LeaderboardBuffer.flop(value, type);
    
    // lines will be an array of formatted values, by default "1. {player}: {value}"

## net.slipcor.mobstats.api.MobStatsEntityKillEvent

    // do not forget to register listeners in your main plugin!
    @EventHandler
    public void onMobKill(final MobStatsEntityKillEvent event) {
        Entity killer = event.getKiller();
        Entity victim = event.getVictim();
        
        if (killer != null && killer.equals(victim)) {
            event.setCancelled(true);
        }
    }

## net.slipcor.mobstats.api.EntityStatisticsBuffer

All methods do **not** alter the database. Some of them do query the database if there is not a value loaded yet.


### remove temporary values to have them reload from the database

    Entity entity; // the Entity whose values to access
    
    // clear an Entity's statistics as a whole
    EntityStatisticsBuffer.clear(entity.getUniqueId());
    
    // clear an Entity's deaths
    EntityStatisticsBuffer.clearDeaths(entity.getUniqueId());
    
    // clear an Entity's kills
    EntityStatisticsBuffer.clearKills(entity.getUniqueId());
    
    // clear an Entity's max streak
    EntityStatisticsBuffer.clearMaxStreak(entity.getUniqueId());
    
    // clear an Entity's current streak
    EntityStatisticsBuffer.clearStreak(entity.getUniqueId());
    
### retrieve buffered values, query database if needed

    Entity entity; // the Entity whose values to access
    
    // read an Entity's deaths, query the database only if needed
    int deaths = EntityStatisticsBuffer.getDeaths(entity.getUniqueId());
    
    // read an Entity's kills, query the database only if needed
    int kills = EntityStatisticsBuffer.getKills(entity.getUniqueId());
    
    // read an Entity's max streak, query the database only if needed
    int maxstreak = EntityStatisticsBuffer.getMaxStreak(entity.getUniqueId());
    
    // read an Entity's current streak, query the database only if needed
    int streak = EntityStatisticsBuffer.getStreak(entity.getUniqueId());
    
    // read an Entity's kill/death ratio, query the database only if needed
    int ratio = EntityStatisticsBuffer.getRatio(entity.getUniqueId());

### miscellaneous

    Entity entity; // the Entity whose values to access
    
    // preload all Entity's values. Query the database if needed
    EntityStatisticsBuffer.loadEntity(entity.getUniqueId());
    
    // clear all statistics and let them reload from the database
    EntityStatisticsBuffer.refresh();
package net.slipcor.mobstats.commands;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.core.Config;
import net.slipcor.mobstats.core.Language;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandConfig extends AbstractCommand {
    final List<Config.Entry> accessibleLists = new ArrayList<>();

    public CommandConfig() {
        super(new String[]{"mobstats.config"});

        accessibleLists.add(Config.Entry.IGNORE_WORLDS);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_NOPERMCONFIGSET.toString());
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("set")) {
            if (!argCountValid(sender, args, new Integer[]{4})) {
                return;
            }

            //           0         1      2      3
            // /mobstats config    set    [node] [value]
            set(sender, args[2], args[3]);
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("get")) {
            if (!argCountValid(sender, args, new Integer[]{3})) {
                return;
            }

            //           0         1      2
            // /mobstats config    get    [node]
            get(sender, args[2]);
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("add")) {
            if (!argCountValid(sender, args, new Integer[]{4})) {
                return;
            }

            //           0         1      2      3
            // /mobstats config    add    [node] [value]
            add(sender, args[2], args[3]);
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("remove")) {
            if (!argCountValid(sender, args, new Integer[]{4})) {
                return;
            }

            //           0         1      2      3
            // /mobstats config    add    [node] [value]
            remove(sender, args[2], args[3]);
            return;
        }

        MobStats.getInstance().sendPrefixed(sender, getShortInfo());
    }

    private Config.Entry getFullNode(String part) {

        boolean foundEntry = false;
        Config.Entry completedEntry = null;

        for (Config.Entry entry : Config.Entry.values()) {
            if (entry.getNode().toLowerCase().contains(part.toLowerCase()) && entry.getNode().length() != part.length()) {
                if (foundEntry) {
                    // found a second match, let us not autocomplete this!
                    foundEntry = false;
                    completedEntry = null;
                    break;
                }
                foundEntry = true;
                completedEntry = entry;
            }
        }

        return completedEntry;
    }

    private void add(final CommandSender sender, final String node, final String value) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            add(sender, completedEntry.getNode(), value);
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_UNKNOWN.toString(node));
            return;
        }
        final Class type = entry.getType();

        Config config = MobStats.getInstance().config();

        if (type.equals(ObjectUtils.Null.class)) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_SET_GROUP.toString(node));
            return;
        } else if (List.class.equals(type)) {
            List<String> newList = new ArrayList<>(config.getList(entry));
            if (newList.contains(value)) {
                MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_ADD.toString(node, value));
                return;
            }
            newList.add(value);
            config.setValue(entry, newList);
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGADDED.toString(node, value));
        } else {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_NO_LIST_NODE.toString(node));
            return;
        }
        config.save();
    }

    private void get(final CommandSender sender, final String node) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            get(sender, completedEntry.getNode());
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_UNKNOWN.toString(node));
            return;
        }
        if (entry.secret) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_SECRET.toString(node));
            return;
        }
        final Class type = entry.getType();

        Config config = MobStats.getInstance().config();

        if (type.equals(ObjectUtils.Null.class)) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_GET_GROUP.toString(node));
        } else if (List.class.equals(type)) {
            StringBuffer value = new StringBuffer();
            List<String> list = config.getList(entry);
            for (String item : list) {
                value.append("\n");
                value.append(item);
            }
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGGET.toString(node, value.toString()));
        } else if (Boolean.class.equals(type)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGGET.toString(node, String.valueOf(config.getBoolean(entry))));
        } else if (String.class.equals(type)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGGET.toString(node, config.get(entry)));
        } else if (Integer.class.equals(type)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGGET.toString(node, String.valueOf(config.getInt(entry))));
        } else if (Double.class.equals(type)) {
            MobStats.getInstance().sendPrefixed(sender,
                    Language.MSG_CONFIGGET.toString(node, String.format("%.2f", config.getDouble(entry))));
        } else {
            MobStats.getInstance().sendPrefixed(sender,
                    Language.ERROR_CONFIG_TYPE_UNKNOWN.toString(String.valueOf(type)));
        }
    }

    private void remove(final CommandSender sender, final String node, final String value) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            remove(sender, completedEntry.getNode(), value);
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_UNKNOWN.toString(node));
            return;
        }
        final Class type = entry.getType();

        Config config = MobStats.getInstance().config();

        if (type.equals(ObjectUtils.Null.class)) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_SET_GROUP.toString(node));
            return;
        } else if (List.class.equals(type)) {
            List<String> newList = new ArrayList<>(config.getList(entry));
            if (!newList.contains(value)) {
                MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_REMOVE.toString(node, value));
                return;
            }
            newList.remove(value);
            config.setValue(entry, newList);
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGREMOVED.toString(node, value));
        } else {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_NO_LIST_NODE.toString(node));
            return;
        }
        config.save();
    }

    private void set(final CommandSender sender, final String node, final String value) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            set(sender, completedEntry.getNode(), value);
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_UNKNOWN.toString(node));
            return;
        }
        final Class type = entry.getType();

        Config config = MobStats.getInstance().config();

        if (type.equals(ObjectUtils.Null.class)) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_SET_GROUP.toString(node));
            return;
        } else if (List.class.equals(type)) {
            MobStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIG_SET_LIST.toString(node));
            return;
        } else if (Boolean.class.equals(type)) {
            if ("true".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.TRUE);
                MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, "true"));
            } else if ("false".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.FALSE);
                MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, "false"));
            } else {
                MobStats.getInstance().sendPrefixed(sender,
                        Language.ERROR_COMMAND_ARGUMENT.toString(value, "boolean (true|false)"));
                return;
            }
        } else if (String.class.equals(type)) {
            config.setValue(entry, String.valueOf(value));
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, value));
        } else if (Integer.class.equals(type)) {
            final int iValue;

            try {
                iValue = Integer.parseInt(value);
            } catch (final Exception e) {
                MobStats.getInstance().sendPrefixed(sender, Language.ERROR_INVALID_NUMBER.toString(value));
                return;
            }
            config.setValue(entry, iValue);
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, String.valueOf(iValue)));
        } else if (Double.class.equals(type)) {
            final double dValue;

            try {
                dValue = Double.parseDouble(value);
            } catch (final Exception e) {
                MobStats.getInstance().sendPrefixed(sender,
                        Language.ERROR_COMMAND_ARGUMENT.toString(value, "double (e.g. 12.00)"));
                return;
            }
            config.setValue(entry, dValue);
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node,
                            String.valueOf(dValue)));
        } else {
            MobStats.getInstance().sendPrefixed(sender,
                    Language.ERROR_CONFIG_TYPE_UNKNOWN.toString(String.valueOf(type)));
            return;
        }
        config.save();
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();
        //           0         1      2      3
        // /mobstats config    add    [node] [value]

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("get");
            results.add("set");
            results.add("add");
            results.add("remove");
        } else if (args.length == 2) {
            // second argument!
            addIfMatches(results, "get", args[1]);
            addIfMatches(results, "set", args[1]);
            addIfMatches(results, "add", args[1]);
            addIfMatches(results, "remove", args[1]);
        } else {
            // args is >= 3

            if (
                    !args[1].equalsIgnoreCase("get") &&
                            !args[1].equalsIgnoreCase("set") &&
                            !args[1].equalsIgnoreCase("add") &&
                            !args[1].equalsIgnoreCase("remove")
            ) {
                return results;
            }

            if (args[2].equals("")) {
                // list actual argument possibilities
                for (Config.Entry entry : Config.Entry.values()) {

                    if (args[1].equalsIgnoreCase("get")) {
                        if (entry.getType().equals(ObjectUtils.Null.class)) {
                            continue;
                        }
                    } else if (args[1].equalsIgnoreCase("set")) {
                        if (entry.getType().equals(ObjectUtils.Null.class) || entry.getType().equals(List.class)) {
                            continue;
                        }
                    } else {
                        if (entry.getType().equals(ObjectUtils.Null.class) || !accessibleLists.contains(entry)) {
                            continue;
                        }
                    }
                    results.add(entry.getNode());
                }
                return results;
            }

            if (args.length > 3) {
                return results; // don't go too far!
            }

            for (Config.Entry entry : Config.Entry.values()) {
                if (args[1].equalsIgnoreCase("get")) {
                    if (entry.getType().equals(ObjectUtils.Null.class)) {
                        continue;
                    }
                } else if (args[1].equalsIgnoreCase("set")) {
                    if (entry.getType().equals(ObjectUtils.Null.class) || entry.getType().equals(List.class)) {
                        continue;
                    }
                } else {
                    if (entry.getType().equals(ObjectUtils.Null.class) || !accessibleLists.contains(entry)) {
                        continue;
                    }
                }

                addIfMatches(results, entry.getNode(), args[2]);
            }
        }

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("config");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!c");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats config get [node] - get a config value\n" +
                "/mobstats config set [node] [value] - set a config value\n" +
                "/mobstats config add [node] [value] - add a value to a config list\n" +
                "/mobstats config remove [node] [value] - remove a value from a config list";
    }
}

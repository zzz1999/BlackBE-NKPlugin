package xyz.blackbe.blackbeplugin.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import xyz.blackbe.blackbeplugin.BlackBEMain;
import xyz.blackbe.blackbeplugin.constant.BlackBEApiConstants;
import xyz.blackbe.blackbeplugin.task.CheckBlacklistByNameTask;
import xyz.blackbe.blackbeplugin.task.QueryBEServerStatusTask;
import xyz.blackbe.blackbeplugin.task.QueryJEServerStatusTask;
import xyz.blackbe.blackbeplugin.task.QueryXUIDTask;
import xyz.blackbe.blackbeplugin.util.BlacklistCacheManager;

public class BlackBECommand extends Command {
    public BlackBECommand() {
        super("blackbe", "BlackBE 云黑插件", "/blackbe help");
        this.setPermission("blackbe.command.default");
        this.setPermissionMessage("你没有权限使用此命令,所需权限<permission>");
        this.commandParameters.clear();
        this.commandParameters.put("check", new CommandParameter[]{
                CommandParameter.newEnum("check", new String[]{"check"}),
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newType("xuid", true, CommandParamType.STRING)
        });
        this.commandParameters.put("xuid", new CommandParameter[]{
                CommandParameter.newEnum("xuid", new String[]{"xuid"}),
                CommandParameter.newType("gamertag", CommandParamType.STRING)
        });
        this.commandParameters.put("motdpe", new CommandParameter[]{
                CommandParameter.newEnum("motdpe", new String[]{"motdpe"}),
                CommandParameter.newType("host", CommandParamType.STRING),
                CommandParameter.newType("port", true, CommandParamType.INT)
        });
        this.commandParameters.put("motdpc", new CommandParameter[]{
                CommandParameter.newEnum("motdpc", new String[]{"motdpc"}),
                CommandParameter.newType("host", CommandParamType.STRING),
                CommandParameter.newType("port", true, CommandParamType.INT)
        });
        this.commandParameters.put("cacheClean", new CommandParameter[]{
                CommandParameter.newEnum("cacheClean", new String[]{"cacheClean"}),
        });
        this.commandParameters.put("help", new CommandParameter[]{
                CommandParameter.newEnum("help", new String[]{"help"}),
        });
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (testPermission(sender)) {
            if (args.length == 0) {
                sender.sendMessage("本服务器使用BlackBE云黑限制违禁玩家,API版本:" + BlackBEApiConstants.API_VERSION + ". BlackBE云黑(https://blackbe.xyz)致力于维护MCBE的服务器环境,用最简单粗暴的方式,让广大服主开服省心、放心.");
                return false;
            } else {
                switch (args[0]) {
                    case "check": {
                        String playerName = args[1].replace("&", " ");
                        CheckBlacklistByNameTask task;
                        if (args.length > 2) {
                            task = new CheckBlacklistByNameTask(playerName, args[1], sender);
                        } else {
                            task = new CheckBlacklistByNameTask(playerName, sender);
                        }
                        BlackBEMain.getAsyncTaskWorker().submitTask(task);
                        break;
                    }
                    case "xuid": {
                        String gamertag = args[1].replace("&", " ");
                        QueryXUIDTask task = new QueryXUIDTask(gamertag, sender);
                        BlackBEMain.getAsyncTaskWorker().submitTask(task);
                        break;
                    }
                    case "motdbe":
                    case "motdpe": {
                        String host = args[1];
                        QueryBEServerStatusTask task;
                        if (args.length > 2) {
                            task = new QueryBEServerStatusTask(host, Integer.parseInt(args[2]), sender);
                        } else {
                            task = new QueryBEServerStatusTask(host, sender);
                        }
                        BlackBEMain.getAsyncTaskWorker().submitTask(task);
                        break;
                    }
                    case "motdje":
                    case "motdpc": {
                        String host = args[1];
                        QueryJEServerStatusTask task;
                        if (args.length > 2) {
                            task = new QueryJEServerStatusTask(host, Integer.parseInt(args[2]), sender);
                        } else {
                            task = new QueryJEServerStatusTask(host, sender);
                        }
                        BlackBEMain.getAsyncTaskWorker().submitTask(task);
                        break;
                    }
                    case "cacheclean":
                    case "cacheClean": {
                        BlacklistCacheManager.clear();
                        sender.sendMessage("黑名单缓存清理完成!");
                        break;
                    }
                    case "help":
                    default: {
                        sendHelpInfo(sender);
                    }
                }
            }
        }
        return true;
    }

    public void sendHelpInfo(CommandSender sender) {
        sender.sendMessage(TextFormat.GREEN + "云黑插件命令:");
        sender.sendMessage(TextFormat.GREEN + "/blackbe check <name> [xuid] | 查询某玩家是否在云黑名单中(可以使用&号代替空格)");
        sender.sendMessage(TextFormat.GREEN + "/blackbe xuid <gamertag> | 通过Xbox玩家代号查询XUID(可以使用&号代替空格)");
        sender.sendMessage(TextFormat.GREEN + "/blackbe motdpe <host> [port=19132] | 获取BE版服务器状态");
        sender.sendMessage(TextFormat.GREEN + "/blackbe motdpc <host> [port=19132] | 获取JE版服务器状态");
        sender.sendMessage(TextFormat.GREEN + "/blackbe cacheClean | 清空黑名单缓存");
    }
}

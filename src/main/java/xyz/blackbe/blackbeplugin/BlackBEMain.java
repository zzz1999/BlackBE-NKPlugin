package xyz.blackbe.blackbeplugin;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import xyz.blackbe.blackbeplugin.command.BlackBECommand;
import xyz.blackbe.blackbeplugin.constant.BlackBEApiConstants;
import xyz.blackbe.blackbeplugin.runnable.AsyncTaskWorker;

public class BlackBEMain extends PluginBase {

    private static BlackBEMain instance;
    private static final AsyncTaskWorker asyncTaskWorker = new AsyncTaskWorker();

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getServer().getCommandMap().register("blackbe", new BlackBECommand());
        this.getLogger().info(TextFormat.GREEN + "BlackBE云黑插件加载完成,当前API版本" + BlackBEApiConstants.API_VERSION);

        asyncTaskWorker.start();
    }

    public static BlackBEMain getInstance() {
        return instance;
    }

    public static AsyncTaskWorker getAsyncTaskWorker() {
        return asyncTaskWorker;
    }
}
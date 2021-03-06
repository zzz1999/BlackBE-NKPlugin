package xyz.blackbe.blackbeplugin.task;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.permission.BanEntry;
import com.google.gson.Gson;
import xyz.blackbe.blackbeplugin.BlackBEMain;
import xyz.blackbe.blackbeplugin.constant.BlackBEApiConstants;
import xyz.blackbe.blackbeplugin.data.BlackBEBlacklistCheckData;
import xyz.blackbe.blackbeplugin.event.BlackBEBanPlayerEvent;
import xyz.blackbe.blackbeplugin.event.BlackBEKickPlayerEvent;
import xyz.blackbe.blackbeplugin.util.BlackBEUtils;
import xyz.blackbe.blackbeplugin.util.BlacklistCacheManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static xyz.blackbe.blackbeplugin.constant.BlackBEApiConstants.BLACKBE_API_HOST;

@SuppressWarnings({"DuplicatedCode", "unused"})
public class CheckBlacklistTask implements BlackBETask {
    private static final Gson GSON = new Gson();
    private final Player player;
    private BlackBEBlacklistCheckData data;
    private boolean checkSuccess = false;

    public CheckBlacklistTask(Player player) {
        this.player = player;
    }

    @Override
    public void invoke() {
        BufferedReader bufferedReader = null;
        HttpsURLConnection httpsURLConnection = null;
        try {
            URL url = new URL(String.format(BLACKBE_API_HOST + "check?name=%s&xuid=%s", URLEncoder.encode(player.getName(), StandardCharsets.UTF_8.name()), player.getLoginChainData().getXUID()));
            httpsURLConnection = BlackBEUtils.initHttpsURLConnection(url, 5000, 5000);
            httpsURLConnection.connect();

            if (httpsURLConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                String inputLine;
                StringBuilder sb = new StringBuilder(147);
                while ((inputLine = bufferedReader.readLine()) != null) {
                    sb.append(inputLine);
                }

                this.data = GSON.fromJson(sb.toString(), BlackBEBlacklistCheckData.class);
                this.checkSuccess = true;

                switch (this.data.getStatus()) {
                    case BlackBEApiConstants.CHECK_STATUS_IN_BLACKLIST: {
                        StringBuilder reasonStringBuilder = new StringBuilder("?????????????????????????????????,??????????????????????????????.??????????????????:\n");
                        if (this.data.getCheckData() != null) {
                            List<BlackBEBlacklistCheckData.Data.InfoBean> infoList = this.data.getCheckData().getInfo();
                            for (BlackBEBlacklistCheckData.Data.InfoBean infoBean : infoList) {
                                reasonStringBuilder.append("    ????????????:").append(infoBean.getLevel());
                                reasonStringBuilder.append(",name:").append(infoBean.getName());
                                reasonStringBuilder.append(",black_id:").append(infoBean.getBlackId());
                                reasonStringBuilder.append(",QQ:").append(infoBean.getQQ());
                                reasonStringBuilder.append(",XUID:").append(infoBean.getXUID());
                                reasonStringBuilder.append(",\n    UUID:").append(infoBean.getUUID());
                                reasonStringBuilder.append(",\n    ????????????:").append(new String(infoBean.getInfo().getBytes(), StandardCharsets.UTF_8));
                                reasonStringBuilder.append("\n");
                            }
                        }
                        BlackBEKickPlayerEvent blackBEKickPlayerEvent = new BlackBEKickPlayerEvent(this.player, BlackBEKickPlayerEvent.Reason.IN_BLACKLIST, true);
                        Server.getInstance().getPluginManager().callEvent(blackBEKickPlayerEvent);
                        if (!blackBEKickPlayerEvent.isCancelled() && blackBEKickPlayerEvent.isAutoKick()) {
                            BlackBEUtils.kickPlayer(this.player, reasonStringBuilder.toString(), reasonStringBuilder.toString());
                        }
                        BlackBEBanPlayerEvent blackBEBanPlayerEvent = new BlackBEBanPlayerEvent(this.player, true);
                        Server.getInstance().getPluginManager().callEvent(blackBEBanPlayerEvent);
                        if (!blackBEBanPlayerEvent.isCancelled() && blackBEBanPlayerEvent.isBanned()) {
                            BanEntry banEntry = new BanEntry(this.player.getName());
                            banEntry.setCreationDate(new Date());
                            banEntry.setReason("?????????????????????,??????:" + this.data.getCheckData().getInfo().get(0).getInfo());
                            banEntry.setSource("????????????");
                            Server.getInstance().getNameBans().add(banEntry);
                        }
                        break;
                    }
                    case BlackBEApiConstants.CHECK_STATUS_NOT_IN_BLACKLIST: {
                        // ????????????, ???????????????
                        break;
                    }
                    case BlackBEApiConstants.CHECK_STATUS_LACK_PARAM: {
                        BlackBEKickPlayerEvent blackBEKickPlayerEvent = new BlackBEKickPlayerEvent(this.player, BlackBEKickPlayerEvent.Reason.LACK_PARAM, true);
                        Server.getInstance().getPluginManager().callEvent(blackBEKickPlayerEvent);
                        if (!blackBEKickPlayerEvent.isCancelled() && blackBEKickPlayerEvent.isAutoKick()) {
                            BlackBEUtils.kickPlayer(this.player, "??????????????????Api??????????????????,???????????????????????????????????????.URL=" + url.toExternalForm(), "??????????????????Api??????????????????");
                        }
                        break;
                    }
                    case BlackBEApiConstants.CHECK_STATUS_SERVER_ERROR: {
                        BlackBEKickPlayerEvent blackBEKickPlayerEvent = new BlackBEKickPlayerEvent(this.player, BlackBEKickPlayerEvent.Reason.SERVER_ERROR, true);
                        Server.getInstance().getPluginManager().callEvent(blackBEKickPlayerEvent);
                        if (!blackBEKickPlayerEvent.isCancelled() && blackBEKickPlayerEvent.isAutoKick()) {
                            BlackBEUtils.kickPlayer(this.player, "??????????????????????????????,?????????????????????????????????.", "??????????????????????????????");
                        }
                        break;
                    }
                    default: {
                        BlackBEMain.getInstance().getLogger().notice("????????????????????????????????????:" + this.data.getStatus());
                        BlackBEKickPlayerEvent blackBEKickPlayerEvent = new BlackBEKickPlayerEvent(this.player, BlackBEKickPlayerEvent.Reason.UNKNOWN_STATUS, true);
                        Server.getInstance().getPluginManager().callEvent(blackBEKickPlayerEvent);
                        if (!blackBEKickPlayerEvent.isCancelled() && blackBEKickPlayerEvent.isAutoKick()) {
                            BlackBEUtils.kickPlayer(this.player, "??????????????????????????????,?????????????????????????????????.", "???????????????" + this.data.getStatus());
                        }
                    }
                    BlacklistCacheManager.putRecord(this.player, this.data);
                }
            } else {
                BlackBEMain.getInstance().getLogger().error("?????????????????????????????????????????????,?????????=" + httpsURLConnection.getResponseCode() + ",??????URL=" + url.toExternalForm());
                BlackBEKickPlayerEvent blackBEKickPlayerEvent = new BlackBEKickPlayerEvent(this.player, BlackBEKickPlayerEvent.Reason.URL_CONNECTION_ERROR, true);
                Server.getInstance().getPluginManager().callEvent(blackBEKickPlayerEvent);
                if (!blackBEKickPlayerEvent.isCancelled() && blackBEKickPlayerEvent.isAutoKick()) {
                    BlackBEUtils.kickPlayer(player, "????????????????????????,???????????????????????????????????????.", "????????????????????????");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            BlackBEKickPlayerEvent blackBEKickPlayerEvent = new BlackBEKickPlayerEvent(this.player, BlackBEKickPlayerEvent.Reason.PLUGIN_EXCEPTION, true);
            Server.getInstance().getPluginManager().callEvent(blackBEKickPlayerEvent);
            if (!blackBEKickPlayerEvent.isCancelled() && blackBEKickPlayerEvent.isAutoKick()) {
                BlackBEUtils.kickPlayer(this.player, "????????????????????????,???????????????????????????????????????.", "?????????????????????????????????");
            }
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public BlackBEBlacklistCheckData getData() {
        return data;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }
}

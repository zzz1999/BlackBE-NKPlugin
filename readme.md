# BlackBE NKPlugin
云黑平台(blackbe.xyz)的第三方Nukkit插件

# Commands
* /blackbe 查询所使用的BlackBEAPI版本
* /blackbe check \<name> [xuid] | 查询某玩家是否在云黑名单中(可以使用&号代替空格)
* /blackbe xuid \<gamertag> | 通过Xbox玩家代号查询XUID(可以使用&号代替空格)
* /blackbe motdpe \<host> [port=19132] | 获取BE版服务器状态
* /blackbe motdpc \<host> [port=19132] | 获取JE版服务器状态
* /blackbe cacheClean | 清空黑名单缓存

# Events
* BlackBEKickPlayerEvent
> 当玩家因为在云黑名单中而被踢出时触发
* BlackBEBanPlayerEvent
> 当玩家因为在云黑名单中而被Ban时触发
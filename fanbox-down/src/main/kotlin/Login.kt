import util.Message

class Login (
    private val userName: String? = null,
    private val passWord: String? = null
) {
    init {
        if (userName == null) {
            if (!Cookies.contain("cf_clearance")) {
                Message.printlnError("Cookies 字段中缺少 cf_clearance，请尝试手动导入")
                throw Exception()
            }
            if (!Cookies.isEfficient()) {
                Message.printlnError("Cookies 已经过期或者没有加载，尝试重新导入或使用用户名和密码")
                throw Exception()
            }
        } else { //通过用户名和密码登录逻辑
            throw Exception("暂不支持通过用户名和密码登录")
        }
    }

    fun start () =
        if (ConfigManager.config.renameMode) RenameMode.start()
        else DownLoadMode.start()
}
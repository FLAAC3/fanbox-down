import modle.CookieStoreModel
import modle.Headers
import org.apache.commons.io.FileUtils
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.apache.hc.client5.http.cookie.BasicCookieStore
import org.apache.hc.core5.http.MessageHeaders
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.net.URIBuilder
import res.ConnectionManager
import res.FileManager
import util.CookieParser
import util.Message
import java.nio.charset.StandardCharsets

/**
 * __cf_bm 和 cf_clearance 等是必要的人机验证 cookies
 * 继承 BasicCookieStore 添加自定义方法
 * */
object Cookies: BasicCookieStore() {
    private val cookiesInitFile = FileManager.cookiesInitPath.toFile() //初始化导入 cookies
    private val cookiesFile = FileManager.cookiesPath.toFile() //存储 cookies 的文件路径
    private val cookiesSaveFile = FileManager.cookiesSavePath.toFile() //导出 cookies 的文件路径

    init {
        if (cookiesInitFile.exists()) { //初次加载 Json 格式的 Cookies
            FileUtils.readFileToString(cookiesInitFile, StandardCharsets.UTF_8)
                .let(CookieStoreModel::of)
                .forEachCookie(::addCookie)
            save();cookiesInitFile.delete() //覆盖了 cookiesFile 的内容
            Message.printlnInfo("文件 ${cookiesInitFile.name} 已加载")

        } else if (cookiesFile.exists()) { //从 cookiesFile 中读取 Cookies
            FileUtils.readLines(cookiesFile, StandardCharsets.UTF_8)
                .forEach { addCookie(CookieParser.strToBasicClientCookie(it)) }
            Message.printlnInfo("文件 ${cookiesFile.name} 已加载")
        }
    }

    /**
     * 是否包含指定的字段
     * */
    fun contain (name: String): Boolean = cookies.any { it.name == name }

    /**
     * 检查 cookies 是否有效
     * */
    fun isEfficient (): Boolean {
        var out = false
        val uri = URIBuilder("https://api.fanbox.cc/creator.listFollowing").build()
        val httpGet = HttpGet(uri)
        Headers.getAPIHeaders(Headers.H1).forEach(httpGet::addHeader)
        addCookieForRequest(httpGet)
        ConnectionManager.getHttpClient().execute(httpGet) { response ->
            out = response.code == 200
            EntityUtils.consume(response.entity)
            updateByResponse(response)
        }
        return out
    }

    /**
     * 给传入的 Request 添加 Cookie 标头
     * */
    fun addCookieForRequest (uriRequest: HttpUriRequestBase) {
        val conformCookies = //查找所有符合条件的 cookie
            cookies.takeWhile {
                if (it.isSecure && uriRequest.scheme != "https") return@takeWhile false
                if (it.domain == null) true else
                Regex("${it.domain}$").containsMatchIn(uriRequest.uri.host)
            }
        if (conformCookies.isNotEmpty()) { //转换成 Cookie 请求头
            val cookieStr = conformCookies.joinToString(";") { "${it.name}=${it.value}" }
            uriRequest.removeHeaders("Cookie") //先移除，防止重复添加
            uriRequest.addHeader("Cookie", cookieStr)
        }
    }

    /**
     * 根据服务器响应更新 cookie 并且保存到文件中
     * */
    fun updateByResponse (messageHeaders: MessageHeaders) {
        var changed = false
        messageHeaders.getHeaders("Set-Cookie").forEach {
            changed = true
            val cookie = CookieParser.strToBasicClientCookie(it.value)
            addCookie(cookie)
            Message.printlnInfo("更新 Cookie [${cookie.name} = value]")
        }
        if (changed) save()
    }

    /**
     * 把当前储存的所有 cookie 覆盖导出到 Json 文件中
     * */
    @Synchronized
    fun saveAsJson () {
        FileUtils.writeStringToFile(cookiesSaveFile, toJsonString(), StandardCharsets.UTF_8)
        Message.printlnInfo("当前 cookies 均已导出到 ${cookiesSaveFile.name}")
    }

    /**
     * 把当前储存的所有 cookie 覆盖保存到文件中
     * */
    @Synchronized
    fun save () {
        FileUtils.writeStringToFile(cookiesFile, toString(), StandardCharsets.UTF_8)
        Message.printlnInfo("文件 ${cookiesFile.name} 已更新")
    }

    /**
     * 序列化 BasicCookieStore 包含的信息为 Json 格式
     * */
    fun toJsonString (): String = CookieStoreModel.of(this).toString()

    /**
     * 序列化 BasicCookieStore 包含的信息
     * */
    override fun toString (): String =
        StringBuilder().apply {
            cookies.forEach { append(CookieParser.cookieToStr(it)).append("\r\n") }
        }.toString()
}
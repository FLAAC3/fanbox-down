package modle

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.apache.hc.client5.http.cookie.Cookie
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie
import util.CookieParser
import util.MyJson

@Serializable
data class CookieModel (
    val name: String,
    val value: String,
    val domain: String? = null,
    @Serializable(MyJson.DoubleSerializer::class) //使用自定义序列化器
    val expirationDate: Double? = null,
    val httpOnly: Boolean? = null,
    val path: String? = null,
    //val sameSite: String? = null, //暂时忽略
    val secure: Boolean? = null
) {
    fun toBasicClientCookie (): BasicClientCookie =
        BasicClientCookie(name, value).also { basicCookie ->
            basicCookie.domain = domain
            basicCookie.setExpiryDate(CookieParser.unixDouToInstant(expirationDate))
            httpOnly?.let { basicCookie.isHttpOnly = it }
            basicCookie.path = path
            secure?.let { basicCookie.isSecure = it }
        }

    override fun toString(): String = MyJson.json.encodeToString(this)

    companion object {
        /**
         * Json字符串 转换成 CookieModel 的方法
         * */
        fun of (string: String) = MyJson.json.decodeFromString<CookieModel>(string)

        /**
         * cookie对象 转换成 CookieModel 的方法
         * */
        fun of (cookie: Cookie) =
            CookieModel(cookie.name,
                cookie.value,
                cookie.domain,
                CookieParser.instantToUnixDou(cookie.expiryInstant),
                cookie.isHttpOnly,
                cookie.path,
                cookie.isSecure
            )
    }
}
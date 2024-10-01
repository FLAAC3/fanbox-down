package util

import org.apache.hc.client5.http.cookie.Cookie
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object CookieParser {
    private val mainFormatter = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneId.of("GMT"))
    private val secondFormatter = DateTimeFormatter
        .ofPattern("EEE, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneId.of("GMT"))

    /**
     * 把 Set-Cookie 的值解析成 BasicClientCookie 对象
     * */
    fun strToBasicClientCookie (cookieValue: String): BasicClientCookie {
        var out: BasicClientCookie? = null
        cookieValue.split(";").forEach {
            val keyAndValue = it.split("=").map(String::trim)
            if (out == null) out = BasicClientCookie(keyAndValue[0], keyAndValue[1]) else {
                when (keyAndValue[0].lowercase()) {
                    "expires" -> out!!.setExpiryDate(strToInstant(keyAndValue[1]))
                    "max-age" -> {} //忽略字段
                    "path" -> out!!.path = keyAndValue[1]
                    "domain" -> out!!.domain = keyAndValue[1]
                    "secure" -> out!!.isSecure = true
                    "httponly" -> out!!.isHttpOnly = true
                    "samesite" -> {} //忽略字段
                    else -> Message.printlnWaring("CookieParser.toBasicClientCookie 方法中 ${keyAndValue[0]} 属性被忽略")
                }
            }
        }
        return out!!
    }

    /**
     * cookie 序列化成 Set-Cookie 值的方法
     * */
    fun cookieToStr (cookie: Cookie): String {
        val out = StringBuilder(cookie.name).append('=').append(cookie.value)
        cookie.expiryInstant?.let {
            out.append(';').append("expires").append('=').append(instantToStr(it))
        }
        cookie.path?.let {
            out.append(';').append("path").append('=').append(it)
        }
        cookie.domain?.let {
            out.append(';').append("domain").append('=').append(it)
        }
        if (cookie.isSecure) out.append(';').append("secure")
        if (cookie.isHttpOnly) out.append(';').append("HttpOnly")
        return out.toString()
    }

    /**
     * 把表示时间的指定格式字符串（服务器返回的两种时间格式）解析成 Instant 对象
     * */
    fun strToInstant (timeStr: String): Instant =
        if (timeStr.contains('-')) ZonedDateTime.parse(timeStr, secondFormatter).toInstant()
        else ZonedDateTime.parse(timeStr, mainFormatter).toInstant()

    /**
     * 把 Instant 对象转换成主要格式的字符串
     * */
    fun instantToStr (instant: Instant): String =
        LocalDateTime.ofInstant(instant, ZoneOffset.UTC).format(mainFormatter)

    /**
     * 把 Unix时间戳值 解析成 Instant 对象
     * */
    fun unixDouToInstant (unixDouble: Double?): Instant? = if (unixDouble == null) null
        else Instant.ofEpochSecond(unixDouble.toLong(), (unixDouble % 1 * 1_000_000_000).toLong())

    /**
     * 把 Instant 对象转换成 Unix 时间戳值
     * */
    fun instantToUnixDou (instant: Instant?): Double? = if (instant == null) null
        else instant.epochSecond.toDouble() + instant.nano / 1_000_000_000
}
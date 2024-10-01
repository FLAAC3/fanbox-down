package modle

import ConfigManager
import org.apache.hc.core5.http.HttpHeaders
import org.apache.hc.core5.http.message.BasicHeader

/**
 * 快速创建 BasicHeader 对象
 * */
enum class Headers (private val headerName: String, private val value: String) {
    H1(HttpHeaders.USER_AGENT, ConfigManager.config.ua),
    H1_1(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5410.0 Safari/537.36"),
    H1_2(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0"),
    H1_3(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:79.0) Gecko/20100101 Firefox/79.0"),
    H1_4(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.5304.107 Safari/537.36"),
    H1_5(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.5137.4 Safari/537.36"),
    H1_6(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5384.2 Safari/537.36"),
    H1_7(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4919.0 Safari/537.36"),
    H1_8(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10; rv:81.0) Gecko/20100101 Firefox/81.0"),
    H1_9(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.3; rv:82.0) Gecko/20100101 Firefox/82.0"),

    H2(HttpHeaders.ACCEPT,"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"),
    H2_1(HttpHeaders.ACCEPT, "application/json, text/plain, */*"),
    H2_2(HttpHeaders.ACCEPT, "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"),
    H2_3(HttpHeaders.ACCEPT, "*/*"),

    H3(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br, zstd"),
    H4(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,zh-TW;q=0.8,ja;q=0.7"),
    H5(HttpHeaders.CONNECTION, "keep-alive"),
    H6(HttpHeaders.REFERER, "https://www.fanbox.cc/"),
    H7(HttpHeaders.CACHE_CONTROL, "max-age=0"),
    H8("Origin", "https://www.fanbox.cc"),
    ;
    companion object {
        private val UAs = listOf(H1_1, H1_2, H1_3, H1_4, H1_5, H1_6, H1_7, H1_8, H1_9)
        /**
         * 用于下载图片文件
         * */
        fun getImgHeaders (uaHeader: Headers) = collect(uaHeader, H2_2, H3, H4, H6)

        /**
         * 向 API 请求 Json 数据时使用的 Headers
         * */
        fun getAPIHeaders (uaHeader: Headers) = collect(uaHeader, H2_1, H3, H4, H6, H8)

        /**
         * 随机取一个 UA
         * */
        fun randomUA () = UAs[(Math.random() * 9).toInt()]

        /**
         * 收集所选的 Headers 转换成 List<BasicHeader>
         * */
        private fun collect (vararg headers: Headers) = headers.map(Headers::toBasicHeader)
    }

    fun toBasicHeader () = BasicHeader(headerName, value)
}
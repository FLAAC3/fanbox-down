package modle

import kotlinx.serialization.encodeToString
import org.apache.hc.client5.http.cookie.BasicCookieStore
import org.apache.hc.client5.http.cookie.Cookie
import util.MyJson

data class CookieStoreModel (
    val cookieModels: LinkedHashSet<CookieModel>
) {
    /**
     * 内联函数，提供遍历 Cookie 的快捷方法
     * */
    inline fun forEachCookie (accept: (Cookie) -> Unit) {
        for (element in cookieModels) { accept(element.toBasicClientCookie()) }
    }

    override fun toString(): String = MyJson.json.encodeToString(cookieModels)

    companion object {
        /**
         * Json字符串 转换成 CookieStoreModel 的方法
         * */
        fun of (string: String) =
            CookieStoreModel(MyJson.json.decodeFromString<LinkedHashSet<CookieModel>>(string))

        /**
         * basicCookieStore对象 转换成 CookieStoreModel 的方法
         * */
        fun of (basicCookieStore: BasicCookieStore) =
            CookieStoreModel(LinkedHashSet(basicCookieStore.cookies.map(CookieModel::of)))
    }
}
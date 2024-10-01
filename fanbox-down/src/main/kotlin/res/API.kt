package res

import Cookies
import modle.Headers
import modle.api.creator.Creator
import modle.api.article.Article
import modle.api.workpages.WorkPages
import modle.api.works.Works
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.net.URIBuilder
import util.Message
import java.io.InputStream
import java.net.URI

object API {
    /**
     * 搜索作者名
     * */
    fun searchCreator (keyword: String): String {
        Message.printlnInfo("调用 searchCreator API")
        val uri = URIBuilder("https://api.fanbox.cc/creator.search")
            .addParameter("q", keyword)
            .addParameter("page", "0")
            .build()
        val httpGet = HttpGet(uri)
        Headers.getAPIHeaders(Headers.randomUA()).forEach(httpGet::addHeader)
        return ConnectionManager.getHttpClient()
            .getResponseStr(httpGet, "searchCreator API 请求失败")
    }

    /**
     * 获取作者主页信息
     * */
    fun getCreator (creatorId: String): Creator {
        Message.printlnInfo("调用 getCreator API")
        val uri = URIBuilder("https://api.fanbox.cc/creator.get")
            .addParameter("creatorId", creatorId)
            .build()
        val httpGet = HttpGet(uri)
        Headers.getAPIHeaders(Headers.randomUA()).forEach(httpGet::addHeader)
        return ConnectionManager.getHttpClient()
            .getResponseStr(httpGet, "getCreator API 请求失败")
            .let(Creator::of)
    }

    /**
     * 获取作者投稿作品的所有分页
     * */
    fun getWorkPages (creatorId: String): WorkPages {
        Message.printlnInfo("调用 getWorkPages API")
        val uri = URIBuilder("https://api.fanbox.cc/post.paginateCreator")
            .addParameter("creatorId", creatorId)
            .build()
        val httpGet = HttpGet(uri)
        Headers.getAPIHeaders(Headers.randomUA()).forEach(httpGet::addHeader)
        return ConnectionManager.getHttpClient()
            .getResponseStr(httpGet, "getWorkPages API 请求失败")
            .let(WorkPages::of)
    }

    /**
     * 获取作者投稿作品（根据最大ID、最新上传时间来选择 limit 条数据）
     * maxPublishedDatetime 和 maxId 必须同时为 null 或 非null
     * */
    fun getWorks (
        creatorId: String,
        limit: Int, //一般是 10
        maxPublishedDatetime: String? = null, //格式 2024-09-16 20:01:57 (日本标准时间)
        maxId: String? = null
    ): Works =
        URIBuilder("https://api.fanbox.cc/post.listCreator")
            .addParameter("creatorId", creatorId)
            .apply {
                if (maxPublishedDatetime != null && maxId != null) {
                    addParameter("maxPublishedDatetime", maxPublishedDatetime)
                    addParameter("maxId", maxId)
                }
            }
            .addParameter("limit", limit.toString())
            .build()
            .let(::getWorks)

    /**
     * 传入已经构建好的 URI 获取 Works
     * */
    fun getWorks (uri: URI): Works {
        Message.printlnInfo("调用 getWorks API")
        val httpGet = HttpGet(uri)
        Headers.getAPIHeaders(Headers.randomUA()).forEach(httpGet::addHeader)
        return ConnectionManager.getHttpClient()
            .getResponseStr(httpGet, "getWorks API 请求失败")
            .let(Works::of)
    }
    fun getWorks (url: String) = getWorks(URI.create(url))

    /**
     * 查看作者投稿作品
     * */
    fun getArticle (postId: String): Article {
        Message.printlnInfo("调用 getArticle API")
        val uri = URIBuilder("https://api.fanbox.cc/post.info")
            .addParameter("postId", postId)
            .build()
        val httpGet = HttpGet(uri)
        Headers.getAPIHeaders(Headers.H1).forEach(httpGet::addHeader)
        return ConnectionManager.getHttpClient()
            .getResponseStr(httpGet, "getArticle API 请求失败", Cookies)
            .let(Article::of)
    }

    /**
     * 使用不带 Cookies 的 Get 请求图片文件
     * */
    inline fun getImg (uri: URI, crossinline accept: (suffix: String, inputStream: InputStream) -> Unit) {
        Message.printlnInfo("调用 getImg 方法")
        val httpGet = HttpGet(uri)
        Headers.getImgHeaders(Headers.randomUA()).forEach(httpGet::addHeader)
        ConnectionManager.getHttpClient().execute(httpGet) { response ->
            val entity = response.entity
            if (response.code == 200) {
                try {
                    accept(uri.getSuffix()!!, entity.content)
                } catch (e: Exception) {
                    Message.printlnError("getImg 方法传入的函数运行出错")
                    e.printStackTrace()
                } finally {
                    EntityUtils.consume(entity)
                }
            } else {
                Message.printlnError("getImg 方法请求失败，状态：${response.code}")
                EntityUtils.consume(entity)
            }
        }
    }

    /**
     * 使用带 Cookies 的 Get 请求图片文件
     * */
    inline fun getImgWithCookies (uri: URI, crossinline accept: (suffix: String, inputStream: InputStream) -> Unit) {
        Message.printlnInfo("调用 getImgWithCookies 方法")
        val httpGet = HttpGet(uri)
        Headers.getImgHeaders(Headers.H1).forEach(httpGet::addHeader)
        Cookies.addCookieForRequest(httpGet)
        ConnectionManager.getHttpClient().execute(httpGet) { response ->
            Cookies.updateByResponse(response)
            val entity = response.entity
            if (response.code == 200) {
                try {
                    accept(uri.getSuffix()!!, entity.content)
                } catch (e: Exception) {
                    Message.printlnError("getImgWithCookies 方法传入的函数运行出错")
                    e.printStackTrace()
                } finally {
                    EntityUtils.consume(entity)
                }
            } else {
                Message.printlnError("getImgWithCookies 方法请求失败，状态：${response.code}")
                EntityUtils.consume(entity)
            }
        }
    }

    /**
     * 我关注的创作者
     * */
    fun getCreatorFollowing (): String {
        Message.printlnInfo("调用 getCreatorFollowing API")
        val uri = URIBuilder("https://api.fanbox.cc/creator.listFollowing")
            .build()
        val httpGet = HttpGet(uri)
        Headers.getAPIHeaders(Headers.H1).forEach(httpGet::addHeader)
        return ConnectionManager.getHttpClient()
            .getResponseStr(httpGet, "getCreatorFollowing API 请求失败", Cookies)
    }

    /**
     * 获得响应内容字符串
     * */
    private fun CloseableHttpClient.getResponseStr (
        request: HttpUriRequestBase,
        errMessage: String,
        cookies: Cookies? = null
    ): String {
        var out = ""
        cookies?.addCookieForRequest(request) //加上 Cookie 请求头
        execute(request) { response ->
            val entity = response.entity
            out = EntityUtils.toString(entity)
            EntityUtils.consume(entity)
            cookies?.updateByResponse(response) //根据服务器响应更新 Cookies

            if (response.code != 200) {
                Message.printlnError("$errMessage，返回数据：$out")
                throw Exception()
            }
        }
        return out
    }

    /**
     * 包含后缀名的文件直链才会返回，否则返回 null
     * */
    fun URI.getSuffix (): String? = Regex("""\..{1,4}$""").find(rawPath)?.value
}
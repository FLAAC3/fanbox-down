import modle.PostIdRange
import modle.api.article.Article
import modle.api.article.WorkInfo
import res.API
import res.Cache
import res.FileManager
import util.Message
import java.time.LocalDateTime
import java.util.LinkedHashSet
import java.util.concurrent.CompletableFuture

object DownLoadMode {
    @Volatile private var shutdown = false //是否要提前退出下载

    //用于存储 withPostIdRangeSet 返回的数据
    private class Content (val fileManager: FileManager, val workInfo: WorkInfo?)

    /**
     * 开始爬虫，启动！！！
     * */
    fun start () {
        Thread {
            readlnOrNull() //输入了什么不重要，重要的是输入了
            shutdown = true
            while (true) {
                Message.printlnInfo("正在等待当前作品爬虫完成，请不要提前关闭")
                Thread.sleep(2000)
            }
        }.apply { isDaemon = true }.start()

        val creators = ConfigManager.config.creators
        for (creatorId in creators.keys) { //遍历配置文件中的 creatorId
            val creatorConfig = creators[creatorId] //看有没有给作者单独配置
            if (creatorConfig == null) {
                startByProgress(creatorId)
            } else {
                if (creatorConfig.postId != null) {
                    startByPostId(creatorId, creatorConfig.postId)
                } else if (creatorConfig.publishedDate != null) {
                    startByTime(creatorId, creatorConfig.publishedDatetime()!!)
                } else {
                    startByProgress(creatorId)
                }
            }
            Message.printlnInf1("作者", Cache.getCreatorName(creatorId), "的作品爬虫完成")
        }
        Message.printlnInfo("爬虫程序运行结束")
        Message.printlnWaring("如果当前是在配置中指定作品ID或者发布时间爬虫的，下次恢复爬虫必须删除这些信息")
    }

    /**
     * 大保底，最后看看进度存储文件中有没有信息
     * */
    private fun startByProgress (creatorId: String) {
        val pages = API.getWorkPages(creatorId)
        val postIdRangeSet = ProgressManager.getPostIdRangeSet(creatorId)

        if (postIdRangeSet == null) { //什么信息都没有，直接从最后开始爬
            withPostIdRangeSet(pages.getPostIdRangeSet(6))
        } else {
            var content: Content? = null
            if (!postIdRangeSet.first().isComplete())
                content = withPostIdRangeSet(postIdRangeSet)
            if (!shutdown) {
                if (content == null) { //上次爬虫已经完成
                    val postId = postIdRangeSet.first().endId!!
                    val next = API.getArticle(postId).nextPost
                    if (next != null) withPostIdRangeSet(
                        pages.getPostIdRangeSet(6, next.id.toInt())!!
                    )
                } else if (content.workInfo != null) withPostIdRangeSet(
                    pages.getPostIdRangeSet(6, content.workInfo!!.id.toInt())!!,
                    content.fileManager
                )
            }
        }
    }

    /**
     * 爬虫作者指定时间之后的文章
     * */
    private fun startByTime (creatorId: String, localDateTime: LocalDateTime) {
        val postIdRangeSet = API.getWorkPages(creatorId)
            .getPostIdRangeSet(6, localDateTime)
        if (postIdRangeSet != null) withPostIdRangeSet(postIdRangeSet)
    }

    /**
     * 根据 postId 爬虫这个文章以及之后的所有图片
     * */
    private fun startByPostId (creatorId: String, postId: String) {
        val postIdRangeSet = API.getWorkPages(creatorId)
            .getPostIdRangeSet(6, postId.toInt())
        if (postIdRangeSet != null) withPostIdRangeSet(postIdRangeSet)
    }

    /**
     * 传入 postIdRangeSet 开始多线程爬虫，返回一些可以复用的数据
     * */
    private fun withPostIdRangeSet (
        postIdRangeSet: LinkedHashSet<PostIdRange>,
        fileManager: FileManager? = null
    ): Content {
        val article = API.getArticle(postIdRangeSet.first().startId) //网络请求文章的所有信息
        val theFileManager = fileManager ?: FileManager(article) //此作者的文件管理器

        /**
         * 异步执行，线程返回结果是处理的最后一篇 Article（不一定就是范围内最后一篇）
         * */
        fun withInitArticle (article: Article, postIdRange: PostIdRange): CompletableFuture<Article> =
            CompletableFuture.supplyAsync {
                var theArticle = article
                while (!shutdown && !postIdRange.isComplete()) {
                    val fullPath = theFileManager.getFullPath(theArticle)
                    FileManager.saveImagesToPath(1, fullPath, theArticle)
                    if (postIdRange.isOnlyOne()) postIdRange.setNext("") else {
                        postIdRange.setNext(theArticle.nextPost!!.id)
                        theArticle = API.getArticle(postIdRange.startId)
                    }
                }
                theArticle //如果提前结束，返回的结果不可用
            }

        fun default (threadId: Int, postIdRange: PostIdRange): CompletableFuture<Article> =
            CompletableFuture.supplyAsync {
                while (!shutdown && !postIdRange.isComplete()) {
                    val theArticle = API.getArticle(postIdRange.startId)
                    val fullPath = theFileManager.getFullPath(theArticle)
                    FileManager.saveImagesToPath(threadId, fullPath, theArticle)
                    if (postIdRange.isOnlyOne()) {
                        postIdRange.setNext("")
                        return@supplyAsync theArticle
                    } else postIdRange.setNext(theArticle.nextPost!!.id)
                }
                if (shutdown) article else API.getArticle(postIdRange.endId!!) //如果提前结束，返回的结果不可用
            }

        val iterator = postIdRangeSet.iterator()
        val completableFutureArr = Array(postIdRangeSet.size) {
            if (it == 0) withInitArticle(article, iterator.next())
            else default(it + 1, iterator.next())
        }
        CompletableFuture.allOf(*completableFutureArr).join() //等所有线程运行结束
        val content = if (!shutdown) {
            val lastArticle = completableFutureArr.last().get()
            theFileManager.renamePath(lastArticle)
            Content(theFileManager, lastArticle.nextPost)
        } else Content(theFileManager, null)
        ProgressManager.update(article.creatorId, postIdRangeSet) //保存进度
        return content
    }
}
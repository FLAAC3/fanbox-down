package modle.api.workpages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import modle.PostIdRange
import modle.api.works.Item
import res.API
import util.MyJson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class WorkPages(
    @SerialName("body")
    val pageUrlArray: Array<String> //每一页数据的 API 链接（API.getWorks）（从最新到最旧排序）
) {
    private val dateList by lazy { //和 API 链接一一对应的日期列表（懒加载）
        pageUrlArray.map {
            val oldStr = Regex("(?<=maxPublishedDatetime=)[^&]+").find(it)!!.value
            val newStr = URLDecoder.decode(oldStr, StandardCharsets.UTF_8)
            LocalDateTime.parse(newStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }
    }
    private val postIdList by lazy { //和 API 链接一一对应的作品ID列表（懒加载）
        pageUrlArray.map {
            Regex("(?<=maxId=)\\d+").find(it)!!.value.toInt()
        }
    }

    //辅助函数 (涉及到网络请求)
    private fun getItemsByI (indexI: Int) = API.getWorks(pageUrlArray[indexI]).items

    /**
     * threadCount 是切割线程的最大数量，获取每个线程应该处理的文章范围
     * 根据 发布时间 来筛选
     * */
    fun getPostIdRangeSet (threadCount: Int, startTime: LocalDateTime): LinkedHashSet<PostIdRange>? {
        for (i in dateList.lastIndex downTo 0) {
            val date = dateList[i]
            if (date.isAfter(startTime)) {
                val items = getItemsByI(i)
                for (j in items.lastIndex downTo 0) {
                    val item = items[j]
                    if (item.publishedDatetime.isAfter(startTime.atZone(ZoneId.systemDefault()).toOffsetDateTime()))
                        return getPostIdRangeSet(threadCount, i, j, items)
                }
                return null
            }
        }
        return null
    }

    /**
     * 根据 作品ID 来筛选
     * */
    fun getPostIdRangeSet (threadCount: Int, startPostId: Int): LinkedHashSet<PostIdRange>? {
        for (i in postIdList.lastIndex downTo 0) { //i 遍历每一页
            val id = postIdList[i]
            if (id >= startPostId) {
                val items = getItemsByI(i)
                for (j in items.lastIndex downTo 0) { //j 遍历每一页的元素
                    val item = items[j]
                    if (item.id.toInt() == startPostId) //找到了对应的文章
                        return getPostIdRangeSet(threadCount, i, j, items)
                }
                return null
            }
        }
        return null
    }

    /**
     * 不做筛选，从头开始
     * */
    fun getPostIdRangeSet (threadCount: Int): LinkedHashSet<PostIdRange> {
        val i = pageUrlArray.lastIndex
        val items = getItemsByI(i)
        return getPostIdRangeSet(threadCount, i, items.lastIndex, items)
    }

    /**
     * 根据坐标直接定位到具体文章
     * */
    private fun getPostIdRangeSet (
        threadCount: Int,
        indexI: Int,
        indexJ: Int,
        items: Array<Item>
    ): LinkedHashSet<PostIdRange> {
        val out = LinkedHashSet<PostIdRange>()

        //辅助数据
        class Content (val indexI: Int, val items: Array<Item>, val indexJ: Int) {
            fun getId () = this.items[this.indexJ].id
            fun callAble () = this.indexI != 0 || this.indexJ != 0
        }

        /**
         * 辅助函数，itemSize 是指当前页面可以取多少元素，可以取 0
         * */
        fun findEnd (indexI: Int, items: Array<Item>, itemSize: Int, size: Int): Content {
            if (size > itemSize) { //回溯一页又一页
                val i = indexI - 1
                return if (i == -1) Content(0, items, 0) else {
                    val its = getItemsByI(i)
                    findEnd(i, its, its.size, size - itemSize)
                }
            }
            return Content(indexI, items, itemSize - size)
        }

        /**
         * size 是设置每个 PostIdRange 包含元素的最大数量，至少是 2
         * */
        fun split2 (indexI: Int, items: Array<Item>, indexJ: Int, size: Int) {
            val startId = items[indexJ].id
            val content = findEnd(indexI, items, indexJ + 1, size)
            if (content.callAble()) {
                out.add(PostIdRange(startId, content.getId()))
                return if (content.indexJ > 0) {
                    split2(content.indexI, content.items, content.indexJ - 1, size)
                } else {
                    val i = content.indexI - 1
                    val its = getItemsByI(i)
                    split2(i, its, its.size - 1, size)
                }
            }
            out.add(PostIdRange(startId, content.getId()))
        }

        /**
         * 每个 PostIdRange 包含元素的数量恒为 1
         * */
        fun split1 (indexI: Int, items: Array<Item>, indexJ: Int) {
            for (j in indexJ downTo 0) {
                out.add(PostIdRange(items[j].id))
            }
            if (indexI > 0) {
                val i = indexI - 1
                val its = getItemsByI(i)
                split1(i, its, its.size - 1)
            } else {
                val last = out.last()
                last.endId = last.startId //列表最后一个要保存总体进度
            }
        }

        val totalSize = indexI * 10 + indexJ + 1 //计算出总共有多少篇文章
        val size = //计算每个线程分配的文章数
            if (totalSize % threadCount == 0) totalSize / threadCount
            else (totalSize / threadCount) + 1
        if (size == 1) split1(indexI, items, indexJ)
        else split2(indexI, items, indexJ, size)
        return out
    }


    companion object {
        fun of (string: String) = MyJson.json.decodeFromString<WorkPages>(string)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WorkPages
        return pageUrlArray.contentEquals(other.pageUrlArray)
    }

    override fun hashCode(): Int {
        return pageUrlArray.contentHashCode()
    }
}

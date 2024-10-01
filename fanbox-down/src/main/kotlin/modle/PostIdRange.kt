package modle

import kotlinx.serialization.Serializable

/**
 * 左闭右闭区间，全部都是没有完成的文章ID
 * */
@Serializable
data class PostIdRange (
    var startId: String,
    var endId: String? = null //如果只有一篇文章，则 end 为 null（除了列表中最后一个要记录总体进度）
) {
    fun isComplete () = startId == "" //如果 startId=""，则说明当前范围已经全部爬虫完成
    fun isOnlyOne () = endId == null || startId == endId //此范围是不是只有一篇文章需要爬虫
    fun setNext (postId: String) { startId = postId }
}
package modle.config

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class CreatorConfig (
    val postId: String? = null,
    val publishedDate: String? = null, //格式 2011-12-03
    val datePattern: String? = null,
    val firstPath: String? = null,
    val secondPath: String? = null,
    val newFirstPath: String? = null,
    val newSecondPath: String? = null
) {
    /**
     * 指定开始爬虫的投稿日期
     * */
    fun publishedDatetime (): LocalDateTime? =
        if (publishedDate != null) {
            LocalDate.parse(publishedDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(0, 0, 0)
        } else null
}

package modle.api.article

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import modle.PathNameData
import modle.ProvideImgUrl
import modle.ProvidePathNameData
import modle.api.creator.User
import util.Message
import util.MyJson
import java.time.OffsetDateTime

@Serializable
data class Article (
    val title: String,
    @Serializable(MyJson.OffsetDateTimeSerializer::class) //使用自定义序列化器
    val publishedDatetime: OffsetDateTime, //发布时间
    val likeCount: Int,
    val commentCount: Int,
    val user: User,
    val creatorId: String,
    val coverImageUrl: String?, //封面图
    @SerialName("body")
    val articleContent: ArticleContent?, //文章内容（为 null 直接报错）
    val nextPost: WorkInfo?, //下一篇文章
    val prevPost: WorkInfo? //上一篇文章
): ProvidePathNameData, ProvideImgUrl {

    private var fCount = 0 //记录当前应该获取第几个 freeImage
    override var freeSize: Int = 0
    private var count = 0 //记录当前应该获取第几个 image（在 imageMap 中保存图片 Url，在 blocks 中保存图片先后顺序）
    override var size: Int = 0

    init {
        if (articleContent == null) {
            Message.printlnError("通过API请求文章具体信息时被拒绝，请检查是否触发了人机验证，或者配置文件中含有未赞助的作者")
            throw Exception()
        }

        val arr = articleContent.freeImageUrlArray
        freeSize = if (arr == null) coverImageUrl?.let { 1 } ?: 0
                    else coverImageUrl?.let { arr.size + 1 } ?: arr.size
        articleContent.blocks?.let { size = it.size }
    }

    companion object {
        fun of (string: String): Article = Regex("""^\{[^{]*|\s*}$""")
            .replace(string, "")
            .let { MyJson.json.decodeFromString<Article>(it) }
    }

    override fun nextFreeImgUrl(): String =
        if (coverImageUrl == null) {
            articleContent!!.freeImageUrlArray!![fCount].apply { fCount++ }
        } else {
            if (fCount == 0) { fCount++; coverImageUrl }
            else articleContent!!.freeImageUrlArray!![fCount - 1].apply { fCount++ }
        }

    override fun nextImgUrl(): String = articleContent!!.run { imageMap!![blocks!![count]]!! }.apply { count++ }

    override fun getPathData(): PathNameData = PathNameData(title, publishedDatetime, likeCount, commentCount, user.name)
}
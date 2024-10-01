package modle.api.works

import kotlinx.serialization.Serializable
import modle.PathNameData
import modle.ProvidePathNameData
import modle.api.creator.User
import util.MyJson
import java.time.OffsetDateTime

@Serializable
data class Item (
    val id: String,
    val title: String,
    @Serializable(MyJson.OffsetDateTimeSerializer::class) //使用自定义序列化器
    val publishedDatetime: OffsetDateTime, //日本标准时间
    val likeCount: Int,
    val commentCount: Int,
    val user: User
): ProvidePathNameData {
    override fun getPathData(): PathNameData = PathNameData(title, publishedDatetime, likeCount, commentCount, user.name)
}
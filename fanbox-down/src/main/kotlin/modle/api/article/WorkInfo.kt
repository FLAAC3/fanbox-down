package modle.api.article

import kotlinx.serialization.Serializable
import util.MyJson
import java.time.OffsetDateTime

@Serializable
data class WorkInfo (
    val id: String, //作品ID（postId）
    val title: String,
    @Serializable(MyJson.OffsetDateTimeSerializer::class) //使用自定义序列化器
    val publishedDatetime: OffsetDateTime //发布时间
)

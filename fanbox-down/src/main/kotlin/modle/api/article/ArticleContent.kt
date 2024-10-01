package modle.api.article

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

@Serializable
data class ArticleContent (
    @Serializable(BlocksSerializer::class)
    val blocks: Array<String>? = null, //记录图片ID在文中出现的顺序
    @Serializable(ImageMapSerializer::class)
    val imageMap: HashMap<String, String>? = null, //key 是图片ID，value 是图片链接（需要 cookies）
    @SerialName("images")
    @Serializable(FreeImageSerializer::class)
    val freeImageUrlArray: Array<String>? = null //文章内容的图片链接（不需要 cookies）
) {
    companion object {
        object BlocksSerializer: KSerializer<Array<String>?> {
            private val serializer = serializer<Array<String>?>() //获取默认的序列化器
            override val descriptor: SerialDescriptor = serializer.descriptor

            override fun serialize(encoder: Encoder, value: Array<String>?) =
                serializer.serialize(encoder, value)

            override fun deserialize (decoder: Decoder): Array<String>? {
                decoder as JsonDecoder
                val out = ArrayList<String>()
                val jsonElement = decoder.decodeJsonElement()
                if (jsonElement is JsonNull) return null
                val jsonElements = jsonElement.jsonArray as List<JsonElement>
                jsonElements.forEach { it as JsonObject
                    if ((it["type"] as JsonPrimitive).content == "image") {
                        out.add((it["imageId"] as JsonPrimitive).content)
                    }
                }
                return out.toTypedArray()
            }
        }

        object ImageMapSerializer: KSerializer<HashMap<String, String>?> {
            private val serializer = serializer<HashMap<String, String>?>() //获取默认的序列化器
            override val descriptor: SerialDescriptor = serializer.descriptor

            override fun serialize(encoder: Encoder, value: HashMap<String, String>?) =
                serializer.serialize(encoder, value)

            override fun deserialize (decoder: Decoder): HashMap<String, String>? {
                decoder as JsonDecoder
                val jsonElement = decoder.decodeJsonElement()
                if (jsonElement is JsonNull) return null
                val map = HashMap<String, String>()
                jsonElement.jsonObject.values.forEach { it as JsonObject
                    map[(it["id"] as JsonPrimitive).content] = (it["originalUrl"] as JsonPrimitive).content
                }
                return map
            }
        }

        object FreeImageSerializer: KSerializer<Array<String>> {
            private val serializer = serializer<Array<String>>() //获取默认的序列化器
            override val descriptor: SerialDescriptor = serializer.descriptor

            override fun serialize (encoder: Encoder, value: Array<String>) =
                serializer.serialize(encoder, value)

            override fun deserialize (decoder: Decoder): Array<String> {
                decoder as JsonDecoder
                val jsonElements = decoder.decodeJsonElement().jsonArray as List<JsonElement>
                return jsonElements.map {
                    (it.jsonObject["originalUrl"] as JsonPrimitive).content
                }.toTypedArray()
            }
        }
    }

    override fun equals (other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ArticleContent
        if (imageMap != other.imageMap) return false
        if (freeImageUrlArray != null) {
            if (other.freeImageUrlArray == null) return false
            if (!freeImageUrlArray.contentEquals(other.freeImageUrlArray)) return false
        } else if (other.freeImageUrlArray != null) return false

        return true
    }

    override fun hashCode (): Int {
        var result = imageMap?.hashCode() ?: 0
        result = 31 * result + (freeImageUrlArray?.contentHashCode() ?: 0)
        return result
    }
}

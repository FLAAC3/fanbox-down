package modle.api.works

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import util.MyJson

@Serializable
data class Works (
    @SerialName("body")
    val items: Array<Item>
) {
    companion object {
        fun of (string: String) = MyJson.json.decodeFromString<Works>(string)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Works
        return items.contentEquals(other.items)
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }
}

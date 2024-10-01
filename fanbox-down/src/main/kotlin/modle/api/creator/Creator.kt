package modle.api.creator

import kotlinx.serialization.Serializable
import util.MyJson

@Serializable
data class Creator (
    val user: User,
    val coverImageUrl: String
) {
    companion object {
        fun of (string: String) = Regex("""^\{[^{]*|\s*}$""")
            .replace(string, "")
            .let { MyJson.json.decodeFromString<Creator>(it) }
    }
}
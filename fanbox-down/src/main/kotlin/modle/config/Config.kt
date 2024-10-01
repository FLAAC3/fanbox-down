package modle.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
data class Config (
    val outPath: String,
    val renameMode: Boolean,
    val datePattern: String = "yyyy-MM-dd",
    val ua: String,
    val firstPath: String,
    val secondPath: String,
    val newFirstPath: String? = null,
    val newSecondPath: String? = null,
    val creators: HashMap<String, CreatorConfig?> //key 是 creatorId
) {
    companion object {
        fun of (string: String) = Yaml.default.decodeFromString<Config>(string)
    }
}

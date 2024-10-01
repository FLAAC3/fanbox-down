package modle.api.creator

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val userId: String,
    val name: String,
    val iconUrl: String
)
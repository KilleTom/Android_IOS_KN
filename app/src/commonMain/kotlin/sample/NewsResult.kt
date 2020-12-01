package sample

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
 class NewsResult(
    @SerialName("data")
    var data: List<NewsData>,
    @SerialName("stat")
    var stat: String = ""
)


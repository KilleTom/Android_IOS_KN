package sample


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    @SerialName("error_code")
    var errorCode: Int,
    @SerialName("reason")
    var reason: String?,
    @SerialName("result")
    var newsResult: NewsResult?
)
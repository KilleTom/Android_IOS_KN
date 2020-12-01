package sample


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsData(

    @SerialName("author_name")
    var authorName: String?,
    @SerialName("category")
    var category: String?,

    @SerialName("date")
    var date: String = "",

    @SerialName("thumbnail_pic_s")
    var thumbnailPicS: String = "",
    @SerialName("thumbnail_pic_s02")
    var thumbnailPicS02: String = "",
    @SerialName("thumbnail_pic_s03")
    var thumbnailPicS03: String = "",
    @SerialName("title")
    var title: String?,
    @SerialName("uniquekey")
    var uniquekey: String?,
    @SerialName("url")
    var url: String?
)
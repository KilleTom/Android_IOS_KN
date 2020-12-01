package sample

import io.ktor.client.request.*
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal expect val APIDispatcher: CoroutineDispatcher

class Api private constructor() {

//    聚合数据的ApiKey
    private var newsApiKey = "13728f03ef29af183184d6d30dc6ae43"

    private suspend fun getNews(type: String = "top"): List<NewsData> {

        val clientManager = NetClientManager.instarnce


        val client = clientManager.client

        val response = client.post<NewsResponse>(HttpRequestBuilder().apply {

            url.host = "v.juhe.cn"
            url.protocol = URLProtocol.HTTPS
            url.encodedPath = "/toutiao/index"
            url.parameters.append("type", type)
            url.parameters.append("key", newsApiKey)

        })

        if (response.reason == "成功的返回") {

            val result = response.newsResult ?: throw ApiResultException("news result null")
            val datas = result.data

            return datas
        }

        throw ApiResultException(response.reason ?: "unknown error")
    }

    fun getNewsResult(
        type: String = "",
        dataAction: (List<NewsData>) -> Unit,
        errorAction: (Exception) -> Unit,
        dispatcher: CoroutineDispatcher = APIDispatcher
    ) {
        GlobalScope.apply {

            launch(dispatcher) {

                try {

                    val result = getNews(type)
                    dataAction.invoke(result)

                } catch (e: Exception) {

                    errorAction.invoke(e)
                }
            }
        }
    }

    companion object {

        val instance by lazy { Api() }
    }

}
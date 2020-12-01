package sample

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer

class NetClientManager private constructor() {


    val client: HttpClient

    init {

        client = HttpClient() {

            install(JsonFeature) {
                serializer = KotlinxSerializer()

            }


        }
    }

    companion object {
        val instarnce by lazy { NetClientManager() }
    }
}
package edu.brunteless.timeline.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.ContentEncodingConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


fun createHttpClient(engine: HttpClientEngine) = HttpClient(engine) {

    install(ContentEncoding) {
        mode = ContentEncodingConfig.Mode.DecompressResponse
        gzip()
        deflate()
        identity()
    }

    install(ContentNegotiation) {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
        register(ContentType.Text.Html, KotlinxSerializationConverter(json))
        json(json = json)
    }

}
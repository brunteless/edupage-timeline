@file:OptIn(ExperimentalSerializationApi::class)
package edu.brunteless.timeline.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.glance.state.GlanceStateDefinition
import edu.brunteless.timeline.models.RenderTimeline
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime


object TimelineGlanceStateDefinition : GlanceStateDefinition<RenderTimeline> {

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<RenderTimeline> {
        return DataStoreFactory.create(
            serializer = TimelineSerializer,
            produceFile = {
                getLocation(context, fileKey)
            }
        )
    }

    override fun getLocation(context: Context, fileKey: String) =
        File(context.filesDir, "timeline_$fileKey.json")


    private object TimelineSerializer : Serializer<RenderTimeline> {
        override val defaultValue: RenderTimeline = RenderTimeline(
            lessons = emptyList(),
            currentIndex = 0,
            day = LocalDateTime.now(),
            credentials = null
        )

        override suspend fun readFrom(input: InputStream) =
            Json.decodeFromStream<RenderTimeline>(input)

        override suspend fun writeTo(t: RenderTimeline, output: OutputStream) =
            Json.encodeToStream(t, output)

    }
}
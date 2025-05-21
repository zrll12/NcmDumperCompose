package cc.vastsea.zrll.ncmdumpercompose.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class NCMMetadata(
    val musicName: String,
    @Serializable(with = ArtistNameListSerializer::class)
    val artist: List<String>,
    val album: String,
    val musicId: Long,
    val format: String
)

@Serializable
data class MusicLyrics(val lyric: String = "")

object ArtistNameListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        ListSerializer(String.serializer()).descriptor

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This serializer can only be used with Json format")
        val jsonArray = jsonDecoder.decodeJsonElement().jsonArray

        return jsonArray.map { element ->
            element.jsonArray[0].jsonPrimitive.content
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        val jsonArray = buildJsonArray {
            value.forEach { name ->
                add(buildJsonArray {
                    add(name)
                })
            }
        }
        (encoder as JsonEncoder).encodeJsonElement(jsonArray)
    }
}
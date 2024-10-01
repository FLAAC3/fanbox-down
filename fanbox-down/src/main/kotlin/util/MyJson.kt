package util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinProperty

object MyJson {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        explicitNulls = false
    }

    /**
     * 自定义 Double 的序列化器
     * */
    object DoubleSerializer: KSerializer<Double> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("kotlin.Double")

        /**
         * 最多截取 6 位小数
         * */
        override fun serialize (encoder: Encoder, value: Double) {
            val composer = Class.forName("kotlinx.serialization.json.internal.StreamingJsonEncoder")
                .getDeclaredField("composer").kotlinProperty!!.apply { isAccessible = true }

            Class.forName("kotlinx.serialization.json.internal.Composer")
                .getMethod("print", String::class.java)
                .invoke(composer.call(encoder), String.format("%.6f", value).trimEnd('0').trimEnd('.'))
        }

        /**
         * 一路获取到初始字符串并且转换成 Double（其实这本来就是默认行为，但是跳过了数据验证）
         * */
        override fun deserialize (decoder: Decoder): Double {
            decoder as JsonDecoder
            val jsonElement = decoder.decodeJsonElement()
            jsonElement as JsonPrimitive
            return jsonElement.content.toDouble()
        }
    }

    /**
     * 自定义 OffsetDateTime 的序列化器
     * */
    object OffsetDateTimeSerializer: KSerializer<OffsetDateTime> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("java.time.OffsetDateTime")

        override fun serialize (encoder: Encoder, value: OffsetDateTime) =
            encoder.encodeString(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value)
            )

        override fun deserialize (decoder: Decoder): OffsetDateTime =
            OffsetDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
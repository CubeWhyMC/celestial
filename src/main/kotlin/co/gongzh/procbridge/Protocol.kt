package co.gongzh.procbridge

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.AbstractMap.SimpleEntry
import kotlin.math.min

internal object Protocol {
    private val FLAG = byteArrayOf('p'.code.toByte(), 'b'.code.toByte())


    private fun read(stream: InputStream): Map.Entry<StatusCode, JsonObject> {
        // 1. FLAG
        var b = stream.read()
        if (b == -1 || b != FLAG[0].toInt()) throw ProtocolException(ProtocolException.UNRECOGNIZED_PROTOCOL)
        b = stream.read()
        if (b == -1 || b != FLAG[1].toInt()) throw ProtocolException(ProtocolException.UNRECOGNIZED_PROTOCOL)

        // 2. VERSION
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        if (b != Versions.CURRENT[0].toInt()) throw ProtocolException(ProtocolException.INCOMPATIBLE_VERSION)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        if (b != Versions.CURRENT[1].toInt()) throw ProtocolException(ProtocolException.INCOMPATIBLE_VERSION)

        // 3. STATUS CODE
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        val statusCode: StatusCode = StatusCode.fromRawValue(b)
            ?: throw ProtocolException(ProtocolException.INVALID_STATUS_CODE)

        // 4. RESERVED BYTES (2 bytes)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)

        // 5. LENGTH (little endian)
        var bodyLen = b
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        bodyLen = bodyLen or (b shl 8)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        bodyLen = bodyLen or (b shl 16)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        bodyLen = bodyLen or (b shl 24)

        // 6. JSON OBJECT
        val buffer = ByteArrayOutputStream()
        var readCount: Int
        var restCount = bodyLen
        var buf = ByteArray(
            min(bodyLen.toDouble(), (1024 * 1024).toDouble()).toInt()
        )
        while ((stream.read(buf, 0, min(buf.size.toDouble(), restCount.toDouble()).toInt())
                .also { readCount = it }) != -1
        ) {
            buffer.write(buf, 0, readCount)
            restCount -= readCount
            if (restCount == 0) {
                break
            }
        }

        if (buffer.size() != bodyLen) {
            throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        }

        buffer.flush()
        buf = buffer.toByteArray()

        try {
            val jsonText = String(buf, StandardCharsets.UTF_8)
            val body = JsonParser.parseString(jsonText).asJsonObject
            return SimpleEntry(statusCode, body)
        } catch (ex: Exception) {
            throw ProtocolException(ProtocolException.INVALID_BODY)
        }
    }


    private fun write(stream: OutputStream, statusCode: StatusCode, body: JsonObject) {
        // 1. FLAG 'p', 'b'
        stream.write(FLAG)

        // 2. VERSION
        stream.write(Versions.CURRENT)

        // 3. STATUS CODE
        stream.write(statusCode.rawValue)

        // 4. RESERVED BYTES (2 bytes)
        stream.write(0)
        stream.write(0)

        // make json object
        val buf = body.toString().toByteArray(StandardCharsets.UTF_8)

        // 5. LENGTH (4-byte, little endian)
        val len = buf.size
        val b0 = len and 0xff
        val b1 = (len and 0xff00) shr 8
        val b2 = (len and 0xff0000) shr 16
        val b3 = (len and -0x1000000) shr 24
        stream.write(b0)
        stream.write(b1)
        stream.write(b2)
        stream.write(b3)

        // 6. JSON OBJECT
        stream.write(buf)

        stream.flush()
    }


    fun readRequest(stream: InputStream): Map.Entry<String, Any> {
        val entry = read(stream)
        val statusCode = entry.key
        val body = entry.value
        if (statusCode != StatusCode.REQUEST) {
            throw ProtocolException(ProtocolException.INVALID_STATUS_CODE)
        }
        val method = body[Keys.METHOD].asString
        val payload: Any = body[Keys.PAYLOAD]
        return SimpleEntry(method, payload)
    }


    fun readResponse(stream: InputStream): Map.Entry<StatusCode, Any> {
        val entry = read(stream)
        val statusCode = entry.key
        val body = entry.value
        return when (statusCode) {
            StatusCode.GOOD_RESPONSE -> {
                SimpleEntry<StatusCode, Any>(StatusCode.GOOD_RESPONSE, body[Keys.PAYLOAD])
            }
            StatusCode.BAD_RESPONSE -> {
                SimpleEntry<StatusCode, Any>(StatusCode.BAD_RESPONSE, body[Keys.MESSAGE].asString)
            }
            else -> {
                throw ProtocolException(ProtocolException.INVALID_STATUS_CODE)
            }
        }
    }


    fun writeRequest(stream: OutputStream, method: String?, payload: Any?) {
        val body = JsonObject()
        if (method != null) {
            body.addProperty(Keys.METHOD, method)
        }
        if (payload != null) {
            body.addProperty(Keys.PAYLOAD, payload as String?)
        }
        write(stream, StatusCode.REQUEST, body)
    }


    fun writeGoodResponse(stream: OutputStream, payload: Any?) {
        val body = JsonObject()
        if (payload != null) {
            body.add(Keys.PAYLOAD, Gson().toJsonTree(payload, Map::class.java))
        }
        write(stream, StatusCode.GOOD_RESPONSE, body)
    }

    fun writeBadResponse(stream: OutputStream, message: String?) {
        val body = JsonObject()
        if (message != null) {
            body.addProperty(Keys.MESSAGE, message)
        }
        write(stream, StatusCode.BAD_RESPONSE, body)
    }
}

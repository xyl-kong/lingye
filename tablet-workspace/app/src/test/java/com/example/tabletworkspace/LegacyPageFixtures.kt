package com.example.tabletworkspace

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/**
 * Builds minimal, non-user-data pages in historical formats. Real backups were used during
 * the audit to validate the decoder, but the regression suite must not embed user page content.
 */
object LegacyPageFixtures {
    fun bytes(version: Int): ByteArray {
        require(version in 4..24) { "unsupported legacy fixture version $version" }
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use { out ->
            out.writeInt(version)
            out.writeUTF("11111111-1111-1111-1111-111111111111")
            out.writeUTF("Legacy v$version")
            writeString(out, "body")
            out.writeFloat(10f)
            out.writeFloat(20f)
            out.writeInt(0) // strokes
            out.writeInt(1) // blocks
            out.writeUTF("paragraph")
            writeString(out, "hello")
            out.writeBoolean(false)
            out.writeInt(1) // shapes
            out.writeUTF("roundRect")
            out.writeFloat(30f)
            out.writeFloat(40f)
            out.writeFloat(100f)
            out.writeFloat(50f)
            out.writeInt(0) // connectors
            out.writeInt(0xffffd866.toInt()) // shape color
            out.writeInt(0) // images
            // version 4+ stroke styles: no strokes
            if (version >= 5) {
                out.writeBoolean(false) // block bold
                out.writeBoolean(false) // block italic
                out.writeBoolean(false) // block underline
                writeString(out, "") // folder
                out.writeBoolean(false) // trashed
            }
            if (version >= 6) {
                out.writeUTF("shape-1")
                out.writeUTF("")
            }
            if (version >= 8) {
                writeString(out, "Title")
                writeString(out, "Body")
                out.writeInt(0) // templates
            }
            if (version >= 13) {
                out.writeInt(1)
                out.writeUTF("shape-1")
            }
            if (version >= 18) {
                out.writeInt(0xffd9deec.toInt())
                out.writeUTF("solid")
                out.writeFloat(1f)
                out.writeFloat(12f)
                out.writeInt(0xff20293a.toInt())
            }
            if (version >= 19) out.writeBoolean(false)
            if (version >= 20) {
                out.writeUTF("")
                out.writeFloat(16f)
                out.writeUTF("left")
            }
            if (version >= 21) {
                out.writeInt(0) // folders
                out.writeInt(0) // tags
                out.writeBoolean(false) // favorite
            }
            if (version >= 23) writeString(out, "")
            if (version >= 24) out.writeUTF("box")
        }
        return output.toByteArray()
    }

    private fun writeString(out: DataOutputStream, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        out.writeInt(bytes.size)
        out.write(bytes)
    }
}

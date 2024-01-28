package org.cubewhy.celestial.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object FileUtils {
    private val log: Logger = LoggerFactory.getLogger(FileUtils::class.java)

    
    fun inputStreamFromClassPath(path: String): InputStream? {
        return FileUtils::class.java.getResourceAsStream(path)
    }

    
    
    fun readBytes(inputStream: InputStream): ByteArray {
        return inputStream.readAllBytes()
    }

    
    
    fun unzipNatives(nativesZip: File, baseDir: File?) {
        log.info("Unzipping natives")
        val dir = File(baseDir, "natives")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        unZip(nativesZip, dir)
        log.info("Natives unzipped.")
    }

    
    fun unZip(input: File, outputDir: File?) {
        val zipFile = ZipFile(input)
        val zipInputStream = ZipInputStream(Files.newInputStream(input.toPath()))

        var entry: ZipEntry?
        while ((zipInputStream.nextEntry.also { entry = it }) != null) {
            val out = File(outputDir, entry!!.name)
            if (entry!!.isDirectory) {
                out.mkdirs()
            } else {
                out.createNewFile()
                val entryInputStream = zipFile.getInputStream(entry!!)
                FileOutputStream(out).use { fileOutPutStream ->
                    fileOutPutStream.write(entryInputStream.readAllBytes())
                }
            }
        }
        zipFile.close()
    }
}

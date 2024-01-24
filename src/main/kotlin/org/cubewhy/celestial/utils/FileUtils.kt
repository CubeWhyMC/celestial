package org.cubewhy.celestial.utils

import org.cubewhy.celestial.utils.TextUtils.dumpTrace
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object FileUtils {
    private val log: Logger = LoggerFactory.getLogger(FileUtils::class.java)

    @JvmStatic
    fun inputStreamFromClassPath(path: String): InputStream? {
        return FileUtils::class.java.getResourceAsStream(path)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readBytes(inputStream: InputStream): ByteArray {
        return inputStream.readAllBytes()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun unzipNatives(nativesZip: File, baseDir: File?) {
        log.info("Unzipping natives")
        val dir = File(baseDir, "natives")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        unZip(nativesZip, dir)
        log.info("Natives unzipped.")
    }

    @Throws(IOException::class)
    fun unZip(input: File, outputDir: File?) {
        val zipFile = ZipFile(input)
        val zipInputStream = ZipInputStream(Files.newInputStream(input.toPath()))

        var entry: ZipEntry
        while ((zipInputStream.nextEntry.also { entry = it }) != null) {
            val out = File(outputDir, entry.name)
            if (entry.isDirectory) {
                out.mkdirs()
            } else {
                out.createNewFile()
                val entryInputStream = zipFile.getInputStream(entry)
                FileOutputStream(out).use { fileOutPutStream ->
                    fileOutPutStream.write(entryInputStream.readAllBytes())
                }
            }
        }
        zipFile.close()
    }

    fun deleteDir(folder: File): Boolean {
        try {
            Files.walkFileTree(
                folder.toPath(),
                EnumSet.noneOf(FileVisitOption::class.java),
                Int.MAX_VALUE,
                object : SimpleFileVisitor<Path>() {
                    @Throws(IOException::class)
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        Files.delete(file)
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun postVisitDirectory(dir: Path, exc: IOException): FileVisitResult {

                        return FileVisitResult.CONTINUE
                    }
                })
        } catch (e: IOException) {
            log.error(dumpTrace(e))
            return false
        }
        return true
    }
}

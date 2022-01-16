package org.fkirill

import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

class SoundRecorder(
    private val format: AudioFormat,
    private val recordDuration: Duration,
    private val outputFile: File
) : Runnable {
    private var stopping: Boolean = false

    private val thread = Thread(this)

    init {
        thread.isDaemon = false
        thread.name = "Capture Microphone"
    }

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.interrupt()
        stopping = true
    }

    fun join() {
        thread.join()
    }

    override fun run() {
        val line: TargetDataLine
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!AudioSystem.isLineSupported(info)) {
            throw IllegalStateException("Line not supported")
        }
        try {
            line = AudioSystem.getLine(info) as TargetDataLine
            line.open(format, line.bufferSize)
        } catch (ex: Exception) {
            throw IllegalStateException("Error opening line", ex)
        }
        val startTime = System.nanoTime()
        try {
            ByteArrayOutputStream().use { out ->
                line.use { line ->
                    val rawAudioData = recordAudio(line, recordDuration)
                    val audioStream = AudioInputStream(ByteArrayInputStream(rawAudioData), format, rawAudioData.size.toLong()/format.frameSize)
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun recordAudio(
        line: TargetDataLine,
        recordDuration: Duration
    ): ByteArray {
        val frameSizeInBytes = format.frameSize
        val bufferLengthInFrames = line.bufferSize / 8
        val bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes
        val data = ByteArray(bufferLengthInBytes)
        var numBytesRead: Int
        val out = ByteArrayOutputStream()
        val endTime = System.nanoTime() + recordDuration.toNanos()
        line.start()
        while (System.nanoTime() < endTime && !stopping) {
            if (line.read(data, 0, bufferLengthInBytes).also { numBytesRead = it } == -1) {
                break
            }
            out.write(data, 0, numBytesRead)
        }
        return out.toByteArray()
    }
}
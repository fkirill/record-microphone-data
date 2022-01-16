package org.fkirill

import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat

object App {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val format = buildAudioFormatInstance()
        while(true) {
            val outputFile = File("test_${LocalDateTime.now().toString()}.wav")
            val recordDuration = Duration.ofMinutes(15)
            val soundRecorder = SoundRecorder(format, recordDuration, outputFile)
            println("Start recording...")
            soundRecorder.start()
            println("Waiting doe recording to complete ($recordDuration)...")
            soundRecorder.join()
            println("audio written to file $outputFile")
        }
    }

    fun buildAudioFormatInstance(): AudioFormat {
        val encoding = AudioFormat.Encoding.PCM_SIGNED
        val rate = 44100.0f
        val channels = 1
        val sampleSize = 16
        val bigEndian = true
        return AudioFormat(encoding, rate, sampleSize, channels, sampleSize / 8 * channels, rate, bigEndian)
    }
}
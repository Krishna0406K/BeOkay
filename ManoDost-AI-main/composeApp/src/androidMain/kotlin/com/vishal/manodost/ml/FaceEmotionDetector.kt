package com.vishal.manodost.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FaceEmotionDetector(context: Context) {
    
    // Configure ML Kit Face Detector
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .enableTracking()
        .build()
    
    private val detector = FaceDetection.getClient(options)
    
    suspend fun detectEmotion(imageProxy: ImageProxy): EmotionResult = suspendCoroutine { continuation ->
        try {
            val bitmap = imageProxyToBitmap(imageProxy)
            val inputImage = InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
            
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0] // Use first detected face
                        val emotion = analyzeFaceEmotion(face)
                        println("[ML-KIT] ✅ Face detected! Emotion: ${emotion.emotion} (confidence: ${emotion.confidence})")
                        println("[ML-KIT] Smile: ${face.smilingProbability}, Left Eye: ${face.leftEyeOpenProbability}, Right Eye: ${face.rightEyeOpenProbability}")
                        continuation.resume(emotion)
                    } else {
                        println("[ML-KIT] ⚠️ No face detected in frame")
                        continuation.resume(EmotionResult("Neutral", 0.5f, System.currentTimeMillis()))
                    }
                }
                .addOnFailureListener { e ->
                    println("[ML-KIT] ❌ Face detection failed: ${e.message}")
                    continuation.resume(EmotionResult("Neutral", 0.5f, System.currentTimeMillis()))
                }
        } catch (e: Exception) {
            println("[ML-KIT] ❌ Error in detectEmotion: ${e.message}")
            e.printStackTrace()
            continuation.resume(EmotionResult("Neutral", 0.5f, System.currentTimeMillis()))
        }
    }
    
    private fun analyzeFaceEmotion(face: Face): EmotionResult {
        val smilingProb = face.smilingProbability ?: 0.5f
        val leftEyeOpen = face.leftEyeOpenProbability ?: 0.5f
        val rightEyeOpen = face.rightEyeOpenProbability ?: 0.5f
        
        // Analyze facial features to determine emotion
        val emotion: String
        val confidence: Float
        
        when {
            // Happy: High smile probability
            smilingProb > 0.7f -> {
                emotion = "Happy"
                confidence = smilingProb
            }
            // Sad: Low smile, eyes slightly closed
            smilingProb < 0.2f && (leftEyeOpen < 0.6f || rightEyeOpen < 0.6f) -> {
                emotion = "Sad"
                confidence = 1.0f - smilingProb
            }
            // Anxious: Eyes wide open, low smile
            (leftEyeOpen > 0.8f && rightEyeOpen > 0.8f) && smilingProb < 0.4f -> {
                emotion = "Anxious"
                confidence = (leftEyeOpen + rightEyeOpen) / 2
            }
            // Surprised: Eyes very wide open
            leftEyeOpen > 0.9f && rightEyeOpen > 0.9f -> {
                emotion = "Surprised"
                confidence = (leftEyeOpen + rightEyeOpen) / 2
            }
            // Neutral: Everything in middle range
            else -> {
                emotion = "Neutral"
                confidence = 0.6f
            }
        }
        
        return EmotionResult(
            emotion = emotion,
            confidence = confidence,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    
    fun close() {
        detector.close()
    }
}

data class EmotionResult(
    val emotion: String,
    val confidence: Float,
    val timestamp: Long
)

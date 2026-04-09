package com.vishal.manodost.ml

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EmotionAnalyzer(private val context: Context) {
    
    private val faceEmotionDetector = FaceEmotionDetector(context)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val analysisScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _currentEmotion = MutableStateFlow<EmotionResult?>(null)
    val currentEmotion: StateFlow<EmotionResult?> = _currentEmotion
    
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var isAnalyzing = false
    
    fun startAnalysis(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onEmotionDetected: (EmotionResult) -> Unit
    ) {
        println("[EMOTION-ANALYZER] Starting camera analysis...")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                println("[EMOTION-ANALYZER] Camera provider obtained")
                
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            analyzeImage(imageProxy, onEmotionDetected)
                        }
                    }
                
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                println("[EMOTION-ANALYZER] ✅ Camera bound successfully, analysis started")
            } catch (e: Exception) {
                println("[EMOTION-ANALYZER] ❌ Error starting camera: ${e.message}")
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    private fun analyzeImage(imageProxy: ImageProxy, onEmotionDetected: (EmotionResult) -> Unit) {
        // Skip if already analyzing to prevent backlog
        if (isAnalyzing) {
            imageProxy.close()
            return
        }
        
        isAnalyzing = true
        
        analysisScope.launch {
            try {
                val result = faceEmotionDetector.detectEmotion(imageProxy)
                _currentEmotion.value = result
                
                withContext(Dispatchers.Main) {
                    onEmotionDetected(result)
                }
                
                println("[EMOTION-ANALYZER] ✅ Emotion callback triggered: ${result.emotion} (${result.confidence})")
            } catch (e: Exception) {
                println("[EMOTION-ANALYZER] ❌ Error analyzing image: ${e.message}")
                e.printStackTrace()
            } finally {
                imageProxy.close()
                isAnalyzing = false
            }
        }
    }
    
    fun stopAnalysis() {
        imageAnalysis?.clearAnalyzer()
        camera = null
        println("[EMOTION-ANALYZER] Analysis stopped")
    }
    
    fun shutdown() {
        stopAnalysis()
        faceEmotionDetector.close()
        analysisScope.cancel()
        cameraExecutor.shutdown()
        println("[EMOTION-ANALYZER] Shutdown complete")
    }
}

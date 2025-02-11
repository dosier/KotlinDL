/*
 * Copyright 2020-2022 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package examples.onnx.objectdetection.efficientdet

import examples.transferlearning.getFileFromResource
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import org.jetbrains.kotlinx.dl.dataset.image.ImageConverter
import org.jetbrains.kotlinx.dl.visualization.swing.createDetectedObjectsPanel
import org.jetbrains.kotlinx.dl.visualization.swing.showFrame
import java.io.File

fun main() {
    val modelHub = ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = ONNXModels.ObjectDetection.EfficientDetD2.pretrainedModel(modelHub)

    model.use { detectionModel ->
        println(detectionModel)

        val file = getFileFromResource("datasets/detection/image3.jpg")
        val image = ImageConverter.toBufferedImage(file)
        val detectedObjects = detectionModel.detectObjects(image)

        detectedObjects.forEach {
            println("Found ${it.classLabel} with probability ${it.probability}")
        }

        showFrame("Detection result for ${file.name}", createDetectedObjectsPanel(image, detectedObjects))
    }
}

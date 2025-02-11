/*
 * Copyright 2022 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.kotlinx.dl.api.inference.posedetection

import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject

/** This data class represents a few detected poses on the given image. */
public data class MultiPoseDetectionResult(
    /** The list of pairs DetectedObject - DetectedPose. */
    val multiplePoses: List<Pair<DetectedObject, DetectedPose>>
)

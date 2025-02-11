/*
 * Copyright 2020-2022 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.kotlinx.dl.dataset.preprocessing

import android.graphics.Bitmap
import org.jetbrains.kotlinx.dl.dataset.shape.TensorShape
import org.jetbrains.kotlinx.dl.dataset.preprocessing.bitmap.Resize
import org.jetbrains.kotlinx.dl.dataset.preprocessing.bitmap.Rotate

/**
 * The data preprocessing pipeline presented as Kotlin DSL on receivers.
 */

/** Applies [ConvertToFloatArray] operation to convert the [Bitmap] to a float array. */
public fun <I> Operation<I, Bitmap>.toFloatArray(block: ConvertToFloatArray.() -> Unit): Operation<I, Pair<FloatArray, TensorShape>> {
    return PreprocessingPipeline(this, ConvertToFloatArray().apply(block))
}

/** Applies [Resize] operation to resize the [Bitmap] to a specific size. */
public fun <I> Operation<I, Bitmap>.resize(block: Resize.() -> Unit): Operation<I, Bitmap> {
    return PreprocessingPipeline(this, Resize().apply(block))
}

/** Applies [Rotate] operation to rotate the [Bitmap] by an arbitrary angle (specified in degrees). */
public fun <I> Operation<I, Bitmap>.rotate(block: Rotate.() -> Unit): Operation<I, Bitmap>  {
    return PreprocessingPipeline(this, Rotate().apply(block))
}

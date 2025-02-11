package org.jetbrains.kotlinx.dl.api.inference.onnx;

import org.jetbrains.kotlinx.dl.api.inference.InferenceModel
import org.jetbrains.kotlinx.dl.api.inference.imagerecognition.InputType
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.ModelHub
import org.jetbrains.kotlinx.dl.api.inference.onnx.classification.ImageRecognitionModel
import org.jetbrains.kotlinx.dl.api.inference.onnx.objectdetection.SSDLikeModel
import org.jetbrains.kotlinx.dl.api.inference.onnx.objectdetection.SSDLikeModelMetadata
import org.jetbrains.kotlinx.dl.api.inference.onnx.posedetection.SinglePoseDetectionModel
import org.jetbrains.kotlinx.dl.dataset.Imagenet
import org.jetbrains.kotlinx.dl.dataset.image.ColorMode
import org.jetbrains.kotlinx.dl.dataset.preprocessing.*
import org.jetbrains.kotlinx.dl.dataset.shape.TensorShape

/**
 * Set of pretrained mobile-friendly ONNX models
 */
public object ONNXModels {
    /** Image classification models */
    public sealed class CV<T : InferenceModel>(
        override val modelRelativePath: String,
        override val channelsFirst: Boolean,
        override val inputColorMode: ColorMode = ColorMode.RGB,
    ) : OnnxModelType<T, ImageRecognitionModel> {
        override fun pretrainedModel(modelHub: ModelHub): ImageRecognitionModel {
            return ImageRecognitionModel(modelHub.loadModel(this) as OnnxInferenceModel, this)
        }

        /**
         * Image classification model based on EfficientNet-Lite architecture.
         * Trained on ImageNet 1k dataset.
         * (labels are available via [org.jetbrains.kotlinx.dl.dataset.Imagenet.labels] method).
         *
         * EfficientNet-Lite 4 is the largest variant and most accurate of the set of EfficientNet-Lite model.
         * It is an integer-only quantized model that produces the highest accuracy of all the EfficientNet models.
         * It achieves 80.4% ImageNet top-1 accuracy, while still running in real-time (e.g. 30ms/image) on a Pixel 4 CPU.
         *
         * The model have
         * - an input with the shape (1x224x224x3)
         * - an output with the shape (1x1000)
         *
         * @see <a href="https://arxiv.org/abs/1905.11946">
         *     EfficientNet: Rethinking Model Scaling for Convolutional Neural Networks</a>
         * @see <a href="https://github.com/onnx/models/tree/main/vision/classification/efficientnet-lite4">
         *    Official EfficientNet4Lite model from ONNX Github.</a>
         */
        public class EfficientNet4Lite : CV<OnnxInferenceModel>("efficientnet_lite4", channelsFirst = false) {
            override val preprocessor: Operation<Pair<FloatArray, TensorShape>, Pair<FloatArray, TensorShape>>
                get() = InputType.TF.preprocessing(channelsLast = !channelsFirst)
        }

        /**
         * Image classification model based on MobileNetV1 architecture.
         * Trained on ImageNet 1k dataset.
         * (labels are available via [org.jetbrains.kotlinx.dl.dataset.Imagenet.labels] method).
         *
         * MobileNetV1 is small, low-latency, low-power model and can be run efficiently on mobile devices
         *
         * The model have
         * - an input with the shape (1x224x224x3)
         * - an output with the shape (1x1001)
         *
         * @see <a href="https://arxiv.org/abs/1905.11946">
         *     EfficientNet: Rethinking Model Scaling for Convolutional Neural Networks</a>
         * @see <a href="https://github.com/onnx/models/tree/main/vision/classification/efficientnet-lite4">
         *    Official EfficientNet4Lite model from ONNX Github.</a>
         */
        public class MobilenetV1 : CV<OnnxInferenceModel>("mobilenet_v1", channelsFirst = false) {
            override val preprocessor: Operation<Pair<FloatArray, TensorShape>, Pair<FloatArray, TensorShape>>
                get() = pipeline<Pair<FloatArray, TensorShape>>()
                    .rescale { scalingCoefficient = 255f }
                    .normalize {
                        mean = floatArrayOf(0.5f, 0.5f, 0.5f)
                        std = floatArrayOf(0.5f, 0.5f, 0.5f)
                        channelsLast = !channelsFirst
                    }

            override fun pretrainedModel(modelHub: ModelHub): ImageRecognitionModel {
                return ImageRecognitionModel(
                    modelHub.loadModel(this),
                    this,
                    Imagenet.V1001.labels()
                )
            }
        }
    }

    /** Pose detection models. */
    public sealed class PoseDetection<T : InferenceModel, U : InferenceModel>(
        override val modelRelativePath: String,
        override val channelsFirst: Boolean = true,
        override val inputColorMode: ColorMode = ColorMode.RGB
    ) : OnnxModelType<T, U> {
        /**
         * This model is a convolutional neural network model that runs on RGB images and predicts human joint locations of a single person.
         * (edges are available in [org.jetbrains.kotlinx.dl.api.inference.onnx.posedetection.edgeKeyPointsPairs]
         * and keypoints are in [org.jetbrains.kotlinx.dl.api.inference.onnx.posedetection.keyPoints]).
         *
         * Model architecture: MobileNetV2 image feature extractor with Feature Pyramid Network decoder (to stride of 4)
         * followed by CenterNet prediction heads with custom post-processing logics. Lightning uses depth multiplier 1.0.
         *
         * The model have an input tensor with type INT32 and shape `[1, 192, 192, 3]`.
         *
         * The model has 1 output:
         * - output_0 tensor with type FLOAT32 and shape `[1, 1, 17, 3]` with 17 rows related to the following keypoints
         * `[nose, left eye, right eye, left ear, right ear, left shoulder, right shoulder, left elbow, right elbow, left wrist, right wrist, left hip, right hip, left knee, right knee, left ankle, right ankle]`.
         * Each row contains 3 numbers: `[y, x, confidence_score]` normalized in `[0.0, 1.0]` range.
         *
         * @see <a href="https://blog.tensorflow.org/2021/05/next-generation-pose-detection-with-movenet-and-tensorflowjs.html">
         *     Detailed description of MoveNet architecture in TensorFlow blog.</a>
         * @see <a href="https://tfhub.dev/google/movenet/singlepose/lightning/4">
         *    TensorFlow Model Hub with the MoveNetLighting model converted to ONNX.</a>
         */
        public object MoveNetSinglePoseLighting :
            PoseDetection<OnnxInferenceModel, SinglePoseDetectionModel>("movenet_singlepose_lighting_13") {
            override fun pretrainedModel(modelHub: ModelHub): SinglePoseDetectionModel {
                return SinglePoseDetectionModel(modelHub.loadModel(this))
            }
        }

        /**
         * This model is a convolutional neural network model that runs on RGB images and predicts human joint locations of a single person.
         * (edges are available in [org.jetbrains.kotlinx.dl.api.inference.onnx.posedetection.edgeKeyPointsPairs]
         * and keypoints are in [org.jetbrains.kotlinx.dl.api.inference.onnx.posedetection.keyPoints]).
         *
         * Model architecture: MobileNetV2 image feature extractor with Feature Pyramid Network decoder (to stride of 4)
         * followed by CenterNet prediction heads with custom post-processing logics. Lightning uses depth multiplier 1.0.
         *
         * The model have an input tensor with type INT32 and shape `[1, 192, 192, 3]`.
         *
         * The model has 1 output:
         * - output_0 tensor with type FLOAT32 and shape `[1, 1, 17, 3]` with 17 rows related to the following keypoints
         * `[nose, left eye, right eye, left ear, right ear, left shoulder, right shoulder, left elbow, right elbow, left wrist, right wrist, left hip, right hip, left knee, right knee, left ankle, right ankle]`.
         * Each row contains 3 numbers: `[y, x, confidence_score]` normalized in `[0.0, 1.0]` range.
         *
         * @see <a href="https://blog.tensorflow.org/2021/05/next-generation-pose-detection-with-movenet-and-tensorflowjs.html">
         *     Detailed description of MoveNet architecture in TensorFlow blog.</a>
         * @see <a href="https://tfhub.dev/google/movenet/singlepose/thunder/4">
         *    TensorFlow Model Hub with the MoveNetLighting model converted to ONNX.</a>
         */
        public object MoveNetSinglePoseThunder :
            PoseDetection<OnnxInferenceModel, SinglePoseDetectionModel>("movenet_thunder") {
            override fun pretrainedModel(modelHub: ModelHub): SinglePoseDetectionModel {
                return SinglePoseDetectionModel(modelHub.loadModel(this))
            }
        }
    }

    /** Object detection models and preprocessing. */
    public sealed class ObjectDetection<T : InferenceModel, U : InferenceModel>(
        override val modelRelativePath: String,
        override val channelsFirst: Boolean = true,
        override val inputColorMode: ColorMode = ColorMode.RGB
    ) : OnnxModelType<T, U> {
        /**
         * This model is a real-time neural network for object detection that detects 90 different classes
         * (labels are available in [org.jetbrains.kotlinx.dl.dataset.Coco.V2017]).
         *
         * SSD-MobilenetV1 is an object detection model that uses a Single Shot MultiBox Detector (SSD) approach
         * to predict object classes for boundary boxes.
         *
         * SSD is a CNN that enables the model to only need to take one single shot to detect multiple objects in an image,
         * and MobileNet is a CNN base network that provides high-level features for object detection.
         * The combination of these two model frameworks produces an efficient,
         * high-accuracy detection model that requires less computational cost.
         *
         * The model have an input with the shape is (1x300x300x3).
         *
         * The model has 4 outputs:
         * - num_detections: the number of detections.
         * - detection_boxes: a list of bounding boxes. Each list item describes a box with top, left, bottom, right relative to the image size.
         * - detection_scores: the score for each detection with values between 0 and 1 representing probability that a class was detected.
         * - detection_classes: Array of 10 integers (floating point values) indicating the index of a class label from the COCO class.
         *
         * @see <a href="https://arxiv.org/abs/1512.02325">
         *     SSD: Single Shot MultiBox Detector.</a>
         * @see <a href="https://github.com/onnx/models/tree/master/vision/object_detection_segmentation/ssd-mobilenetv1">
         *    Detailed description of SSD model and its pre- and postprocessing in onnx/models repository.</a>
         */
        public object SSDMobileNetV1 :
            ObjectDetection<OnnxInferenceModel, SSDLikeModel>("ssd_mobilenet_v1") {

            private val METADATA = SSDLikeModelMetadata(
                "TFLite_Detection_PostProcess",
                "TFLite_Detection_PostProcess:1",
                "TFLite_Detection_PostProcess:2",
                0, 1
            )

            override fun pretrainedModel(modelHub: ModelHub): SSDLikeModel {
                return SSDLikeModel(modelHub.loadModel(this), METADATA)
            }
        }

        /**
         * This model is a real-time neural network for object detection that detects 90 different classes
         * (labels are available in [org.jetbrains.kotlinx.dl.dataset.Coco.V2017]).
         *
         * Internally it uses the EfficientNetLite as backbone network.
         *
         * The model have an input with the shape is (1x320x320x3).
         *
         * The model has 4 outputs:
         * - num_detections: the number of detections.
         * - detection_boxes: a list of bounding boxes. Each list item describes a box with top, left, bottom, right relative to the image size.
         * - detection_scores: the score for each detection with values between 0 and 1 representing probability that a class was detected.
         * - detection_classes: Array of 10 integers (floating point values) indicating the index of a class label from the COCO class.
         *
         * NOTE: The detections are limited to 25.
         *
         * @see <a href="https://arxiv.org/abs/1911.09070">
         *     EfficientDet: Scalable and Efficient Object Detection.</a>
         * @see <a href="https://github.com/google/automl/tree/master/efficientdet">
         *    Detailed description of EfficientDet model in google/automl repository.</a>
         * @see <a href="https://github.com/onnx/tensorflow-onnx/blob/master/tutorials/efficientdet.ipynb">
         *    Tutorial which shows how to covert the EfficientDet models to ONNX using tf2onnx.</a>
         */
        public object EfficientDetLite0 :
            ObjectDetection<OnnxInferenceModel, SSDLikeModel>("efficientdet_lite0") {

            private val METADATA = SSDLikeModelMetadata(
                "StatefulPartitionedCall:3",
                "StatefulPartitionedCall:2",
                "StatefulPartitionedCall:1",
                0, 1
            )

            override fun pretrainedModel(modelHub: ModelHub): SSDLikeModel {
                return SSDLikeModel(modelHub.loadModel(this), METADATA)
            }
        }
    }
}

import os
import sys
import random
import math
import re
import time
import numpy as np
import cv2
import matplotlib
import matplotlib.pyplot as plt

# Root directory of the project
ROOT_DIR = os.path.abspath("../../")

# Import Mask RCNN
sys.path.append(ROOT_DIR)  # To find local version of the library
from mrcnn_ys.config import Config
from mrcnn_ys import utils
import mrcnn_ys.model as modellib
from mrcnn_ys import visualize
from mrcnn_ys.model import log


# Directory to save logs and trained model
MODEL_DIR = os.path.join(ROOT_DIR, "logs")

# Local path to trained weights file
COCO_MODEL_PATH = os.path.join(ROOT_DIR, "mask_rcnn_coco.h5")
# Download COCO trained weights from Releases if needed
if not os.path.exists(COCO_MODEL_PATH):
    utils.download_trained_weights(COCO_MODEL_PATH)


class ArmsConfig(Config):
    """Configuration for training on the toy shapes dataset.
    Derives from the base Config class and overrides values specific
    to the toy shapes dataset.
    """
    # Give the configuration a recognizable name
    NAME = "arms"

    # Train on 1 GPU and 8 images per GPU. We can put multiple images on each
    # GPU because the images are small. Batch size is 8 (GPUs * images/GPU).
    GPU_COUNT = 1
    IMAGES_PER_GPU = 4

    # Number of classes (including background)
    # NUM_CLASSES = 1 # background + 3 shapes

    # Use small images for faster training. Set the limits of the small side
    # the large side, and that determines the image shape.
    IMAGE_MIN_DIM = 64
    IMAGE_MAX_DIM = 64

    # Use smaller anchors because our image and objects are small
    RPN_ANCHOR_SCALES = (4, 8, 16, 32, 64)  # anchor side in pixels

    # Reduce training ROIs per image because the images are small and have
    # few objects. Aim to allow ROI sampling to pick 33% positive ROIs.
    TRAIN_ROIS_PER_IMAGE = 32

    # Use a small epoch since the data is simple
    STEPS_PER_EPOCH = 4000

    # use small validation steps since the epoch is small
    VALIDATION_STEPS = 5

    def __init__(self, num_classes):
        self.NUM_CLASSES = num_classes + 1
        super().__init__()


config = ArmsConfig(1)
config.display()


def get_ax(rows=1, cols=1, size=8):
    """Return a Matplotlib Axes array to be used in
    all visualizations in the notebook. Provide a
    central point to control graph sizes.

    Change the default size attribute to control the size
    of rendered images
    """
    _, ax = plt.subplots(rows, cols, figsize=(size * cols, size * rows))
    return ax


class ArmsDataset(utils.Dataset):
    img_list = []
    count = 0

    def load_list(self, train_path, anno_path):
        img_name_list = os.listdir(train_path)
        for name in img_name_list:
            self.img_list.append([train_path + name, anno_path + name])
            self.count += 1

        print(str(self.count) + "개의 데이터셋 로드 완료")

    def load_first(self, train_path, anno_path, height, width):
        """Generate the requested number of synthetic images.
        count: number of images to generate.
        height, width: the size of the generated images.
        """
        # Add classes
        self.load_list(train_path, anno_path)
        self.add_class("arms", 1, "arm_class")

        # Add images
        # Generate random specifications of images (i.e. color and
        # list of shapes sizes and locations). This is more compact than
        # actual images. Images are generated on the fly in load_image().
        for i in range(self.count):
            self.add_image("arms", image_id=i, path=None,
                           width=width, height=height)

    def load_image(self, image_id):
        """Generate an image from the specs of the given image ID.
        Typically this function loads the image from a file, but
        in this case it generates the image on the fly from the
        specs in image_info.
        """
        image_name = self.img_list[image_id][0]
        image = cv2.imread(image_name, cv2.IMREAD_COLOR)
        return image

    def image_reference(self, image_id):
        """Return the shapes data of the image."""
        # info = self.image_info[image_id]
        # if info["source"] == "shapes":
        #    return info["shapes"]
        # else:
        #    super(self.__class__).image_reference(self, image_id)

    def load_mask(self, image_id):
        """Generate instance masks for shapes of the given image ID.
        """
        mask_name = self.img_list[image_id][1]
        mask = cv2.imread(mask_name, cv2.IMREAD_GRAYSCALE)
        mask = np.expand_dims(mask, axis=-1)
        # print(mask.shape)
        # print(mask)

        # Map class names to class IDs.
        class_ids = np.array([1])

        return mask.astype(np.bool), class_ids.astype(np.int32)


class InferenceConfig(ArmsConfig):
    GPU_COUNT = 1
    IMAGES_PER_GPU = 4

inference_config = InferenceConfig(1)

# Recreate the model in inference mode
model = modellib.MaskRCNN(mode="inference",
                          config=inference_config,
                          model_dir=MODEL_DIR)

# Get path to saved weights
# Either set a specific path or find last trained weights
# model_path = os.path.join(ROOT_DIR, ".h5 file name here")
model_path = model.find_last()

# Load trained weights
print("Loading weights from ", model_path)
model.load_weights(model_path, by_name=True)

# Training dataset
dataset_test = ArmsDataset()
dataset_test.load_first("../../../data/Test_images/test_images/","../../../data/Test_parsing_annotation/test_segmentations/",64,64)
dataset_test.prepare()

image_ids = dataset_test.image_ids

APs = []
for image_id in image_ids:
    # Load image and ground truth data
    image, image_meta, gt_class_id, gt_bbox, gt_mask = \
        modellib.load_image_gt(dataset_test, inference_config,
                               image_id, use_mini_mask=False)
    molded_images = np.expand_dims(modellib.mold_image(image, inference_config), 0)
    # Run object detection
    results = model.detect([image], verbose=0)
    r = results[0]
    # Compute AP
    AP, precisions, recalls, overlaps = \
        utils.compute_ap(gt_bbox, gt_class_id, gt_mask,
                         r["rois"], r["class_ids"], r["scores"], r['masks'])
    APs.append(AP)

print("mAP: ", np.mean(APs))

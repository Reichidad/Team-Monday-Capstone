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
    IMAGES_PER_GPU = 8

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

# Training dataset
dataset_train = ArmsDataset()
dataset_train.load_first("../../../data/TrainVal_images/train_images/","../../../data/TrainVal_parsing_annotations/train_segmentations/",64,64)
dataset_train.prepare()

# Training dataset
dataset_val = ArmsDataset()
dataset_val.load_first("../../../data/TrainVal_images/val_images/","../../../data/TrainVal_parsing_annotations/val_segmentations/",64,64)
dataset_val.prepare()


# # Load and display random samples
# image_ids = np.random.choice(dataset_train.image_ids, 4)
# for image_id in image_ids:
#     image = dataset_train.load_image(image_id)
#     mask, class_ids = dataset_train.load_mask(image_id)
#
#
#     visualize.display_top_masks(image, mask, class_ids, dataset_train.class_names)

# Create model in training mode
model = modellib.MaskRCNN(mode="training", config=config,
                          model_dir=MODEL_DIR)

# Which weights to start with?
init_with = "coco"  # imagenet, coco, or last

if init_with == "imagenet":
    model.load_weights(model.get_imagenet_weights(), by_name=True)
elif init_with == "coco":
    # Load weights trained on MS COCO, but skip layers that
    # are different due to the different number of classes
    # See README for instructions to download the COCO weights
    model.load_weights(COCO_MODEL_PATH, by_name=True,
                       exclude=["mrcnn_class_logits", "mrcnn_bbox_fc",
                                "mrcnn_bbox", "mrcnn_mask"])
elif init_with == "last":
    # Load the last model you trained and continue training
    model.load_weights(model.find_last(), by_name=True)

# Fine tune all layers
# Passing layers="all" trains all layers. You can also
# pass a regular expression to select which layers to
# train by name pattern.
model.train(dataset_train, dataset_val,
            learning_rate=config.LEARNING_RATE / 10,
            epochs=1000,
            layers="all")
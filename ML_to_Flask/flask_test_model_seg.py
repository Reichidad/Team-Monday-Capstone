import os,sys
import logging
import pickle
from flask import request, jsonify, send_file
from keras.backend.tensorflow_backend import set_session
from keras.backend import clear_session

import tensorflow as tf

import flask
import numpy as np
import cv2
import io
import torch

from mrcnn_ys.config import Config
from mrcnn_ys import utils
import mrcnn_ys.model as modellib
from PIL import Image

app = flask.Flask(__name__)
logging.basicConfig(level=logging.DEBUG)


@app.route('/')
def hello_world():

    app.logger.info('Processing default request')


    return send_file("mask.png", mimetype='image/png')

@app.route('/test', methods=["POST"])
def test():
    data = {}
    if request.method == "POST":
        app.logger.info('Processing Depth request')
        app.logger.info('DL Model execute..')

        print(flask.request.files)
        print(flask.request.files.get('image'))

        input_img = request.files['image']
        img_bytes = input_img.read()
        image = Image.open(io.BytesIO(img_bytes))


        #our_image = cv2.imread("frame8_left.png", cv2.IMREAD_COLOR)
        our_image = np.asarray(image)

        print("-------------", flush=True)
        our_image = cv2.resize(dsize=(64, 64), src=our_image, interpolation=cv2.INTER_AREA)

        results = model.detect([our_image], verbose=0)

        mask = results[0]['masks'].astype(int) * 120
        cv2.imwrite("mask.png", mask)
        print(len(mask))



        file_dir = "mask.png"


    return send_file(file_dir, mimetype='image/png')


class ArmsConfig(Config):
    """Configuration for training on the toy shapes dataset.
    Derives from the base Config class and overrides values specific
    to the toy shapes dataset.
    """
    # Give the configuration a recognizable name
    NAME = "arms"
    BACKBONE = "resnet50"

    # Train on 1 GPU and 8 images per GPU. We can put multiple images on each
    # GPU because the images are small. Batch size is 8 (GPUs * images/GPU).
    GPU_COUNT = 3
    IMAGES_PER_GPU = 1

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


class InferenceConfig(ArmsConfig):
    GPU_COUNT = 1
    IMAGES_PER_GPU = 1

if __name__ == '__main__':
    clear_session()
    config = tf.ConfigProto()
    config.gpu_options.allow_growth = True  # dynamically grow the memory used on the GPU
    config.log_device_placement = True  # to log device placement (on which device the operation ran)
    sess = tf.Session(config=config)
    set_session(sess)  # set this TensorFlow session as the default session for Keras


    ROOT_DIR = os.path.abspath(".")
    MODEL_DIR = os.path.join(ROOT_DIR, "logs")
    inf_config = InferenceConfig(1)
    print(ROOT_DIR, flush=True)

    MY_MODEL_DIR = os.path.join(ROOT_DIR+"/model/", "mask_rcnn_arms_0068.h5")



    global model
    model = modellib.MaskRCNN(mode="inference", config=inf_config,
                              model_dir=MODEL_DIR)
    model.load_weights(MY_MODEL_DIR, by_name=True)
    model.keras_model._make_predict_function()


    app.logger.debug("Server start")

    app.run(host='0.0.0.0',debug=True,port=5001)
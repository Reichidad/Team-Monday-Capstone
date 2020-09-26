import cv2
import numpy as np
import os
import time


def crop_all_dataset():
    print("<< Executing crop_all_dataset >>")
    start = time.time()
    LEFT_ARM = 14;
    RIGHT_ARM = 15;

    seg_train_src = 'dataset/TrainVal_parsing_annotations/TrainVal_parsing_annotations/train_segmentations'
    ori_train_src = 'dataset/TrainVal_images/TrainVal_images/train_images'
    seg_val_src = 'dataset/TrainVal_parsing_annotations/TrainVal_parsing_annotations/val_segmentations'
    ori_val_src = 'dataset/TrainVal_images/TrainVal_images/val_images'

    seg_train_dst = 'crop/TrainVal_parsing_annotations/train_segmentations'
    ori_train_dst = 'crop/TrainVal_images/train_images'
    seg_val_dst = 'crop/TrainVal_parsing_annotations/val_segmentations'
    ori_val_dst = 'crop/TrainVal_images/val_images'

    train_images = os.listdir(seg_train_src)
    val_images = os.listdir(seg_val_src)

    for train_image in train_images:
        current_seg_image = cv2.imread(seg_train_src + '/' + train_image, 1)
        current_ori_image = cv2.imread(ori_train_src + '/' + train_image.replace(".png", ".jpg"), 1)

        if LEFT_ARM in np.unique(current_seg_image[np.nonzero(current_seg_image)]):
            l_top = get_top(current_seg_image, LEFT_ARM);
            l_bottom = get_bottom(current_seg_image, LEFT_ARM);
            l_left = get_left(current_seg_image, LEFT_ARM);
            l_right = get_right(current_seg_image, LEFT_ARM);

            crop_left_seg_image = current_seg_image.copy()[l_top:l_bottom, l_left:l_right]
            crop_left_ori_image = current_ori_image.copy()[l_top:l_bottom, l_left:l_right]
            cv2.imwrite(seg_train_dst + '/left_' + train_image, crop_left_seg_image)
            cv2.imwrite(ori_train_dst + '/left_' + train_image, crop_left_ori_image)

        if RIGHT_ARM in np.unique(current_seg_image[np.nonzero(current_seg_image)]):
            r_top = get_top(current_seg_image, RIGHT_ARM);
            r_bottom = get_bottom(current_seg_image, RIGHT_ARM);
            r_left = get_left(current_seg_image, RIGHT_ARM);
            r_right = get_right(current_seg_image, RIGHT_ARM);

            crop_right_seg_image = current_seg_image.copy()[r_top:r_bottom, r_left:r_right]
            crop_right_ori_image = current_ori_image.copy()[r_top:r_bottom, r_left:r_right]
            cv2.imwrite(seg_train_dst + '/right_' + train_image, crop_right_seg_image)
            cv2.imwrite(ori_train_dst + '/right_' + train_image, crop_right_ori_image)

    for val_image in val_images:
        current_seg_image = cv2.imread(seg_val_src + '/' + val_image, cv2.IMREAD_COLOR)
        current_ori_image = cv2.imread(ori_val_src + '/' + val_image.replace(".png", ".jpg"), cv2.IMREAD_COLOR)

        if LEFT_ARM in np.unique(current_seg_image[np.nonzero(current_seg_image)]):
            l_top = get_top(current_seg_image, LEFT_ARM);
            l_bottom = get_bottom(current_seg_image, LEFT_ARM);
            l_left = get_left(current_seg_image, LEFT_ARM);
            l_right = get_right(current_seg_image, LEFT_ARM);

            crop_left_seg_image = current_seg_image.copy()[l_top:l_bottom, l_left:l_right]
            crop_left_ori_image = current_ori_image.copy()[l_top:l_bottom, l_left:l_right]
            cv2.imwrite(seg_val_dst + '/left_' + val_image, crop_left_seg_image)
            cv2.imwrite(ori_val_dst + '/left_' + val_image, crop_left_ori_image)

        if RIGHT_ARM in np.unique(current_seg_image[np.nonzero(current_seg_image)]):
            r_top = get_top(current_seg_image, RIGHT_ARM);
            r_bottom = get_bottom(current_seg_image, RIGHT_ARM);
            r_left = get_left(current_seg_image, RIGHT_ARM);
            r_right = get_right(current_seg_image, RIGHT_ARM);

            crop_right_seg_image = current_seg_image.copy()[r_top:r_bottom, r_left:r_right]
            crop_right_ori_image = current_ori_image.copy()[r_top:r_bottom, r_left:r_right]
            cv2.imwrite(seg_val_dst + '/right_' + val_image, crop_right_seg_image)
            cv2.imwrite(ori_val_dst + '/right_' + val_image, crop_right_ori_image)

    end = time.time()
    print("<< crop_all_dataset Done | Elapsed time : ", end - start, " >>")


def get_top(image, num):
    top = np.sort(np.where(num == image)[0])[0] - 15
    return top if top > 0 else 0


def get_bottom(image, num):
    bottom = np.sort(np.where(num == image)[0])[-1] + 15
    max_bottom = image.shape[0]
    return bottom if bottom < max_bottom else max_bottom


def get_left(image, num):
    left = np.sort(np.where(num == image)[1])[0] - 15
    return left if left > 0 else 0


def get_right(image, num):
    right = np.sort(np.where(num == image)[1])[-1] + 15
    max_right = image.shape[1]
    return right if right < max_right else max_right


crop_all_dataset()


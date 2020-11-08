from scipy import io
import os
import gc
import numpy as np
import cv2
import glob
from PIL import Image


def read_samples(cameras_dir, read_f, num_cameras=4):
    cameras = []
    for camera_id in range(1, num_cameras+1):
        camera_path = os.path.join(cameras_dir, f'camera{camera_id:02d}')
        camera_data = read_f(camera_path)
        if camera_data is None:
            return None
        cameras.append(np.asarray(camera_data))
        gc.collect()
    return np.asarray(cameras)


def read_depths(files_dir):
    file = os.path.join(files_dir, 'depth.mat')
    if os.path.exists(file):
        depth = io.loadmat(file)
        return [v for k, v in sorted(depth.items()) if k.startswith('depth')]


def read_segmentation(files_dir):
    segmentation_path = os.path.join(files_dir,'*.png')
    files = glob.glob(segmentation_path)
    if files:
        return [np.asarray(Image.open(file).convert('RGB')) for file in sorted(files)]


def get_top(image):
    top = np.sort(np.where(image == 100)[0])[0] - 15
    return top if top > 0 else 0


def get_bottom(image):
    bottom = np.sort(np.where(image == 100)[0])[-1] + 15
    max_bottom = image.shape[0]
    return bottom if bottom < max_bottom else max_bottom


def get_left(image):
    left = np.sort(np.where(image == 100)[1])[0] - 15
    return left if left > 0 else 0


def get_right(image):
    right = np.sort(np.where(image == 100)[1])[-1] + 15
    max_right = image.shape[1]
    return right if right < max_right else max_right


def save_imgs(seg_img, depth_img, boundary, seg_dir, depth_dir):
    r_mask_0 = cv2.inRange(seg_img, boundary[0], boundary[0])
    r_res_0 = cv2.bitwise_and(seg_img, seg_img, mask=r_mask_0)
    r_mask_1 = cv2.inRange(seg_img, boundary[1], boundary[1])
    r_res_1 = cv2.bitwise_and(seg_img, seg_img, mask=r_mask_1)
    r_mask_2 = cv2.inRange(seg_img, boundary[2], boundary[2])
    r_res_2 = cv2.bitwise_and(seg_img, seg_img, mask=r_mask_2)

    r_img = r_res_0 + r_res_1 + r_res_2
    r_img = np.where(r_img > 255, 255, r_img)
    r_gray = cv2.cvtColor(r_img, cv2.COLOR_BGR2GRAY)
    r_gray = np.where(r_gray == 0, 0, 100)
    if 100 not in np.unique(r_gray):
        return
    r_t = get_top(r_gray)
    r_b = get_bottom(r_gray)
    r_l = get_left(r_gray)
    r_r = get_right(r_gray)
    cv2.imwrite(seg_dir, r_gray[r_t:r_b, r_l:r_r])
    cv2.imwrite(depth_dir, depth_img[r_t:r_b, r_l:r_r])


# depth_name = 'depth_mat/woman01/03_02_walk_uneven_terrain'
# segmentation_name = 'segmentation_body/woman01/03_02_walk_uneven_terrain'
# depths = read_samples(depth_name, read_depths)
# segmentation = read_samples(segmentation_name, read_segmentation)
# right_arms = np.array([[128, 0, 128], [128, 128, 255], [255, 128, 128]])
# left_arms = np.array([[0, 0, 255], [128, 128, 0], [0, 128, 0]])
#
# for i in range(len(segmentation)):
#     for j in range(len(segmentation[i])):
#         seg_right_dir = 'segmentation_body/woman01/03_02_walk_uneven_terrain_process/camera0'\
#                         + str(i+1) + '/frame' + str(j+1) + '_right.png'
#         depth_right_dir = 'depth_mat/woman01/03_02_walk_uneven_terrain_process/camera0'\
#                           + str(i+1) + '/frame' + str(j+1) + '_right.png'
#         seg_left_dir = 'segmentation_body/woman01/03_02_walk_uneven_terrain_process/camera0'\
#                        + str(i + 1) + '/frame' + str(j + 1) + '_left.png'
#         depth_left_dir = 'depth_mat/woman01/03_02_walk_uneven_terrain_process/camera0' \
#                          + str(i + 1) + '/frame' + str(j + 1) + '_left.png'
#         save_imgs(segmentation[i][j], depths[i][j], right_arms, seg_right_dir, depth_right_dir)
#         save_imgs(segmentation[i][j], depths[i][j], left_arms, seg_left_dir, depth_left_dir)

img = cv2.imread('depth_mat/woman01/03_02_walk_uneven_terrain_process/camera01/frame21_left.png')
print(np.unique(img))

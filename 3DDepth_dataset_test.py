from scipy import io
import os
import gc
import numpy as np
import cv2
import glob
from PIL import Image


def read_rgbs(files_dir):
    rgbs_path = os.path.join(files_dir, '*', '*.jpg')
    files = glob.glob(rgbs_path)
    if files:
        return [np.asarray(Image.open(file)) for file in sorted(files)]


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

    return [r_t, r_b, r_l, r_r]


depth_name = '3DPeople_sample/depth_mat/woman17/02_04_jump'
segmentation_name = '3DPeople_sample/segmentation_body/woman17/02_04_jump'
depths = read_samples(depth_name, read_depths)
segmentation = read_samples(segmentation_name, read_segmentation)
right_arms = np.array([[128, 0, 128], [128, 128, 255], [255, 128, 128]])
left_arms = np.array([[0, 0, 255], [128, 128, 0], [0, 128, 0]])
crop_shape = (depths.shape[0], depths.shape[1], 2, 4)
crop_array = np.zeros(crop_shape)

for i in range(len(segmentation)):
    for j in range(len(segmentation[i])):
        seg_right_dir = '3DPeople_sample/segmentation_body/woman17/process/camera0'\
                        + str(i+1) + '/frame' + str(j+1) + '_right.png'
        depth_right_dir = '3DPeople_sample/depth_mat/woman17/process/camera0'\
                          + str(i+1) + '/frame' + str(j+1) + '_right.png'
        seg_left_dir = '3DPeople_sample/segmentation_body/woman17/process/camera0'\
                       + str(i + 1) + '/frame' + str(j + 1) + '_left.png'
        depth_left_dir = '3DPeople_sample/depth_mat/woman17/process/camera0' \
                         + str(i + 1) + '/frame' + str(j + 1) + '_left.png'
        crop_array[i][j][0] = save_imgs(segmentation[i][j], depths[i][j], right_arms, seg_right_dir, depth_right_dir)
        crop_array[i][j][1] = save_imgs(segmentation[i][j], depths[i][j], left_arms, seg_left_dir, depth_left_dir)

del depths
del segmentation
rgb_name = '3DPeople_sample/rgb/woman17/02_04_jump'
rgbs = read_samples(rgb_name, read_rgbs)
for i in range(len(rgbs)):
    for j in range(len(rgbs[i])):
        rgb_right_dir = '3DPeople_sample/rgb/woman17/process/camera0'\
                        + str(i+1) + '/frame' + str(j+1) + '_right.png'
        rgb_left_dir = '3DPeople_sample/rgb/woman17/process/camera0'\
                        + str(i+1) + '/frame' + str(j+1) + '_left.png'
        rights = crop_array[i][j][0]
        lefts = crop_array[i][j][1]

        if np.isfinite(rights).all():
            rights = rights.astype(int)
            right_img = cv2.cvtColor(rgbs[i][j][rights[0]:rights[1], rights[2]:rights[3]], cv2.COLOR_RGB2BGR)
            cv2.imwrite(rgb_right_dir, right_img)
        if np.isfinite(lefts).all():
            lefts = lefts.astype(int)
            left_img = right_img = cv2.cvtColor(rgbs[i][j][lefts[0]:lefts[1], lefts[2]:lefts[3]], cv2.COLOR_RGB2BGR)
            cv2.imwrite(rgb_left_dir, left_img)
# img = cv2.imread('depth_mat/woman01/03_02_walk_uneven_terrain_process/camera01/frame21_left.png')
# print(np.unique(img))

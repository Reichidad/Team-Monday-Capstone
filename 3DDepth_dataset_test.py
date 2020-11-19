from scipy import io
import os
import gc
import numpy as np
import cv2
import glob
from PIL import Image
import copy

# MemoryError 해결하기 위해서 전체가 아닌 일부 카메라 데이터를 나눠서 가공함
START = 3
END = 4
COMMON_DIR = 'woman01/02_04_jump'


# read_something
# 3DPeople Dataset의 샘플 코드에 위의 START, END를 활용함
def read_samples(cameras_dir, read_f, start=START, num_cameras=END):
    cameras = []
    for camera_id in range(start, num_cameras+1):
        camera_path = os.path.join(cameras_dir, f'camera{camera_id:02d}')
        camera_data = read_f(camera_path)
        if camera_data is None:
            return None
        cameras.append(np.asarray(camera_data))
        gc.collect()
    return np.asarray(cameras)


def read_rgbs(files_dir):
    rgbs_path = os.path.join(files_dir, '*', '*.jpg')
    files = glob.glob(rgbs_path)
    if files:
        return [np.asarray(Image.open(file)) for file in sorted(files)]


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


# get_something
# 이미지 크롭을 위한 4개의 끝점을 판별하는 함수
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


# img_mask
# 이미지에 특정 구간 값에 대한 threshold 를 지정하는 함수
def img_mask(img, lower, upper):
    mask = cv2.inRange(img, lower, upper)
    return cv2.bitwise_and(img, img, mask=mask)


# save_imgs
# segmentation_body에서 팔 부분만을 남김
# 가공한 segmentation data : 이 데이터와 segmentation_clothes의 피부 부분의 공통 구간
# 해당 구간에 대해서 크롭을 위한 4개의 끝점을 구함
# 가공한 depth data : 가공한 segmentation data 위치의 depth data만을 남긴 것
# 각각의 가공한 data를 4개의 끝점으로 크롭하여 저장
def save_imgs(seg_img, depth_img, cloth_img, seg_dir, depth_dir, ignore):
    # 팔 부분만 남기고 나머지 라벨 제거
    seg_img_copy = copy.deepcopy(seg_img)
    # ignore : 반대쪽 팔 라벨
    for ig in ignore:
        seg_img_copy -= img_mask(seg_img_copy, ig-61, ig+61)
    # erase_labels : 양 팔을 제외한 모든 라벨
    erase_labels = np.array([[128, 0, 0], [153, 153, 153], [128, 64, 0], [128, 255, 128], [255, 255, 128], [255, 0, 255],
                    [0, 0, 128], [0, 128, 128], [255, 128, 0], [0, 0, 0], [64, 64, 64], [128, 128, 128], [192, 192, 192],
                             [255, 255, 255]])
    # segmentation data에서 해당 라벨들을 모두 제거
    for label in erase_labels:
        seg_img_copy -= img_mask(seg_img_copy, label-61, label+61)
    # clothes segmentation의 피부 부분을 마스킹
    skin = np.array([100, 210, 149])
    cloth_res = img_mask(cloth_img, skin, skin)
    cloth_res = cv2.cvtColor(cloth_res, cv2.COLOR_RGB2GRAY)
    # 가공한 segmentation data에서 피부 부분만을 남김
    tmp = cv2.cvtColor(seg_img_copy, cv2.COLOR_RGB2GRAY)
    tmp = np.where(cloth_res == 0, 0, tmp)
    tmp = np.where(tmp <= 0, 0, 100)

    # 피부 부분이 존재할 경우 4개의 끝점을 찾아서 자르고 저장
    if 100 in np.unique(tmp):
        r_t = get_top(tmp)
        r_b = get_bottom(tmp)
        r_l = get_left(tmp)
        r_r = get_right(tmp)

        cv2.imwrite(seg_dir, tmp[r_t:r_b, r_l:r_r])
        depth_tmp = np.where(tmp == 0, 0, depth_img)
        cv2.imwrite(depth_dir, depth_tmp[r_t:r_b, r_l:r_r])

        return [r_t, r_b, r_l, r_r]
    else:
        return


# 원본 경로
depth_name = 'depth_mat/' + COMMON_DIR
segmentation_name = 'segmentation_body/' + COMMON_DIR
clothes_name = 'segmentation_clothes/' + COMMON_DIR
# 데이터 로드
depths = read_samples(depth_name, read_depths)
segmentation = read_samples(segmentation_name, read_segmentation)
clothes = read_samples(clothes_name, read_segmentation)
# 각 팔의 라벨들을 저장하는 numpy array
right_arms = np.array([[128, 0, 128], [128, 128, 255], [255, 128, 128]])
left_arms = np.array([[0, 0, 255], [128, 128, 0], [0, 128, 0]])
# 4개의 끝점들을 저장하는 numpy array
crop_shape = (depths.shape[0], depths.shape[1], 2, 4)
crop_array = np.zeros(crop_shape)

# save_imgs()를 활용하는 loop
for i in range(len(segmentation)):
    for j in range(len(segmentation[i])):
        # 자른 이미지를 저장할 경로들
        seg_right_dir = 'own_dataset/segmentation_body/' + COMMON_DIR + '/camera0'\
                        + str(i + START) + '/frame' + str(j + 1) + '_right.png'
        depth_right_dir = 'own_dataset/depth_mat/' + COMMON_DIR + '/camera0'\
                          + str(i + START) + '/frame' + str(j + 1) + '_right.png'
        seg_left_dir = 'own_dataset/segmentation_body/' + COMMON_DIR + '/camera0'\
                       + str(i + START) + '/frame' + str(j + 1) + '_left.png'
        depth_left_dir = 'own_dataset/depth_mat/' + COMMON_DIR + '/camera0' \
                         + str(i + START) + '/frame' + str(j + 1) + '_left.png'
        # save_imgs() 호출 및 4개의 끝점들을 저장
        crop_array[i][j][0] = save_imgs(segmentation[i][j], depths[i][j], clothes[i][j], seg_right_dir,
                                        depth_right_dir, left_arms)
        crop_array[i][j][1] = save_imgs(segmentation[i][j], depths[i][j], clothes[i][j], seg_left_dir,
                                        depth_left_dir, right_arms)
# MemoryError를 해결하기 위해 다 사용한 원본 데이터를 del
del clothes
del depths
del segmentation
# 원본 경로
rgb_name = 'rgb/' + COMMON_DIR
# 데이터 로드
rgbs = read_samples(rgb_name, read_rgbs)
# rgb data를 자르는 loop
for i in range(len(rgbs)):
    for j in range(len(rgbs[i])):
        # 자른 이미지를 저장할 경로들
        rgb_right_dir = 'own_dataset/rgb/' + COMMON_DIR + '/camera0'\
                        + str(i + START) + '/frame' + str(j + 1) + '_right.png'
        rgb_left_dir = 'own_dataset/rgb/' + COMMON_DIR + '/camera0'\
                        + str(i + START) + '/frame' + str(j + 1) + '_left.png'
        # 왼팔, 오른팔에 해당하는 4개의 끝점
        rights = crop_array[i][j][0]
        lefts = crop_array[i][j][1]

        # nan체크이후 nan이 아니면 잘라서 저장
        if np.isfinite(rights).all():
            rights = rights.astype(int)
            right_img = cv2.cvtColor(rgbs[i][j][rights[0]:rights[1], rights[2]:rights[3]], cv2.COLOR_RGB2BGR)
            cv2.imwrite(rgb_right_dir, right_img)
        if np.isfinite(lefts).all():
            lefts = lefts.astype(int)
            left_img = cv2.cvtColor(rgbs[i][j][lefts[0]:lefts[1], lefts[2]:lefts[3]], cv2.COLOR_RGB2BGR)
            cv2.imwrite(rgb_left_dir, left_img)
# 다 사용한 원본 데이터를 del
del rgbs
import cv2
import numpy as np
import os
import warnings
import time
import matplotlib.pyplot as plt

warnings.filterwarnings(action='ignore')


def png_to_npy(directory, files):
    for f in files:
        filename = os.path.splitext(f)[0]
        img = cv2.imread(directory + '/' + f, cv2.IMREAD_GRAYSCALE)

        np.save('test_npy/' + filename, img)


def accuracy(origin, target, seg, confidence):
    if origin.size != target.size:
        print("size diff")
        return 0
    else:
        sub = np.abs(origin - target)
        sub_arm = sub[np.where(seg == 0)]
        hit = np.count_nonzero(sub_arm <= confidence)

        return hit/sub_arm.size


start = time.time()
dataset_dir = 'own_dataset/depth_mat'
result_dir = 'test_result'

# result_files = os.listdir(result_dir)
# png_to_npy(result_dir, result_files)

result_npy_dir = 'test_npy'
result_files = os.listdir(result_npy_dir)
accuracies = [[] for i in range(5)]

for file in result_files:
    filename = os.path.splitext(file)[0]
    result_file = result_npy_dir + '/' + file
    data_file = dataset_dir + '/' + file
    seg_file = 'own_dataset/segmentation_body/' + filename + '.png'
    result = np.load(result_npy_dir + '/' + file, allow_pickle=True)
    data = np.load(dataset_dir + '/' + file, allow_pickle=True)
    seg_data = cv2.imread(seg_file, cv2.IMREAD_GRAYSCALE)

    data_resize = cv2.resize(data, dsize=(112, 112), interpolation=cv2.INTER_AREA)
    seg_data_resize = cv2.resize(seg_data, dsize=(112, 112), interpolation=cv2.INTER_AREA)

    accuracies[0].append(accuracy(data_resize, result, seg_data_resize, 5))
    accuracies[1].append(accuracy(data_resize, result, seg_data_resize, 10))
    accuracies[2].append(accuracy(data_resize, result, seg_data_resize, 15))
    accuracies[3].append(accuracy(data_resize, result, seg_data_resize, 20))
    accuracies[4].append(accuracy(data_resize, result, seg_data_resize, 25))

end = time.time()
print("<<<Evaluation Finished>>>")
print("Elapsed time :", end - start, "seconds")
print("Number of files :", len(result_files))
print("Average Accuracy with confidence 5 :", np.mean(accuracies[0]) * 100, "%")
print("Average Accuracy with confidence 10 :", np.mean(accuracies[1]) * 100, "%")
print("Average Accuracy with confidence 15 :", np.mean(accuracies[2]) * 100, "%")
print("Average Accuracy with confidence 20 :", np.mean(accuracies[3]) * 100, "%")
print("Average Accuracy with confidence 25 :", np.mean(accuracies[4]) * 100, "%")



import cv2
import numpy as np
import os
import warnings
import time
import matplotlib.pyplot as plt

warnings.filterwarnings(action='ignore')


# def pixel_accr(origin, target):
#     if origin.size != target.size:
#         return 0
#     return origin[origin == target].size / origin.size


def precision_recall_accuracy(origin, target):
    if origin.size != target.size:
        return 0, 0
    # target arm found index
    origin_1d = origin.flatten()
    target_1d = target.flatten()

    target_found = [np.where(target_1d != 0)]
    target_not_found = [np.where(target_1d == 0)]
    origin_found = [np.where(origin_1d != 0)]
    origin_not_found = [np.where(origin_1d == 0)]

    # true_positive : dataset arm / model segmentation arm -> Hit
    # false_positive : dataset not arm / model segmentation arm -> Miss
    # true_negative : dataset not arm / model segmentation not arm -> Hit
    # false_negative : dataset arm / model segmentation not arm -> Miss
    true_positive = np.sum(np.isin(target_found, origin_found))
    false_positive = np.sum(np.isin(target_found, origin_not_found))
    true_negative = np.sum(np.isin(target_not_found, origin_not_found))
    false_negative = np.sum(np.isin(target_not_found, origin_found))

    # precision : hit / model arm(positive)
    # recall : hit / data set arm
    # accuracy : hit / all
    precision = 0
    recall = 0
    accuracy = 0
    if true_positive + false_positive != 0:
        precision = true_positive / (true_positive + false_positive)
    if true_positive + false_negative != 0:
        recall = true_positive / (true_positive + false_negative)
    if true_positive + false_positive + true_negative + false_negative != 0:
        accuracy = (true_positive + true_negative) / (true_positive + false_positive + true_negative + false_negative)
    # return all statistics values
    return precision, recall, accuracy


start = time.time()
dataset_directory = 'crop/Test_parsing_annotations/test_segmentations'
result_directory = 'test_result'
result_files = os.listdir(result_directory)


# lists for save each values
precisions = []
recalls = []
accuracies = []
file_not_found = []

for file in result_files:
    result_image = cv2.imread(result_directory + '/' + file, 1)
    dataset_image = cv2.imread(dataset_directory + '/' + file, 1)

    if dataset_image is None:
        file_not_found.append(file)
        pass
    else:
        dataset_image_resize = cv2.resize(dataset_image, dsize=(64, 64), interpolation=cv2.INTER_AREA)

        p, r, a = precision_recall_accuracy(dataset_image_resize, result_image)
        print(p, r, a)
        precisions.append(p)
        recalls.append(r)
        accuracies.append(a)


end = time.time()
# print statistics
print("<<<Evaluation Finished>>>")
print("Elapsed time :", end - start, "seconds")
print("Number of files :", len(result_files))
print("average precision :", np.mean(precisions) * 100, "%")
print("average recall :", np.mean(recalls) * 100, "%")
print("average accuracy :", np.mean(accuracies) * 100, "%")
print("file not found in dataset")
print(file_not_found)

# # Example PR Curve with plt
# plt.figure(1, figsize=(8,8))
# plt.title("Precision-Recall Curve")
# plt.xlabel("Recall")
# plt.ylabel("Precision")
# plt.plot(recalls, precisions)
# plt.show()

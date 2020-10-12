import cv2
import numpy as np
import os
import warnings
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
    target_found = [np.where(target != 0)]
    target_not_found = [np.where(target == 0)]

    # true_positive : dataset arm / model segmentation arm -> Hit
    # false_positive : dataset not arm / model segmentation arm -> Miss
    # true_negative : dataset not arm / model segmentation not arm -> Hit
    # false_negative : dataset arm / model segmentation not arm -> Miss

    true_positive = np.sum(target[target_found] == origin[target_found])
    false_positive = np.sum(target[target_found] != origin[target_found])
    true_negative = np.sum(target[target_not_found] == origin[target_not_found])
    false_negative = np.sum(target[target_not_found] != origin[target_not_found])

    # precision : hit / model arm(positive)
    # recall : hit / data set arm
    # accuracy : hit / all
    precision = true_positive / (true_positive + false_positive)
    recall = true_positive / (true_positive + false_negative)
    accuracy = (true_positive + true_negative) / (true_positive + false_positive + true_negative + false_negative)

    # return all statistics values
    return precision, recall, accuracy


directory = 'seg_accr_test_images'

imgs = os.listdir(directory)
img1 = cv2.imread(directory + '/' + imgs[0], 1)
img2 = cv2.imread(directory + '/' + imgs[1], 1)

# lists for save each values
imgs1 = [img1, img2, img1]
imgs2 = [img1, img2, img2]
precisions = []
recalls = []
accuracies = []

for i in range(len(imgs1)):
    p, r, a = precision_recall_accuracy(imgs1[i], imgs2[i])
    precisions.append(p)
    recalls.append(r)
    accuracies.append(a)

# print statistics
print("precision :", precisions)
print("recall :", recalls)
print("accuracy :", accuracies)

# Example PR Curve with plt
plt.figure(1, figsize=(8,8))
plt.title("Precision-Recall Curve")
plt.xlabel("Recall")
plt.ylabel("Precision")
plt.plot(recalls, precisions)
plt.show()

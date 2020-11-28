import os
import argparse

import torch
import torch.nn as nn
import torch.nn.functional as F
import numpy as np
import cv2
import scipy.io

from torch.utils.data import Dataset
from torch.utils.data import DataLoader
import torch.optim as optim

from skimage import io
from torch.autograd import Variable
from torch.utils.tensorboard import SummaryWriter
from torch.utils.data import random_split
from torchvision import  models
from torch.optim import lr_scheduler
from torchvision import transforms

from depth_network_3 import DepthNetwork


parser = argparse.ArgumentParser(description='CRAFT reimplementation')


parser.add_argument('--resume', default=None, type=str,
                    help='Checkpoint state_dict file to resume training from')
parser.add_argument('--batch_size', default=128, type = int,
                    help='batch size of training')
#parser.add_argument('--cdua', default=True, type=str2bool,
                    #help='Use CUDA to train model')
parser.add_argument('--lr', '--learning-rate', default=3.2768e-5, type=float,
                    help='initial learning rate')
parser.add_argument('--momentum', default=0.9, type=float,
                    help='Momentum value for optim')
parser.add_argument('--weight_decay', default=5e-4, type=float,
                    help='Weight decay for SGD')
parser.add_argument('--gamma', default=0.1, type=float,
                    help='Gamma update for SGD')
parser.add_argument('--num_workers', default=32, type=int,
                    help='Number of workers used in dataloading')


args = parser.parse_args()


class DepthDataset(Dataset):
    def __init__(self,dir,transform=None): #data_len = array
        self.seg_dir = dir+"/segmentation_body/"
        self.img_dir = dir + "/rgb/"
        self.depth_dir = dir + "/depth_mat/"

        self.img_list = os.listdir(self.img_dir)
        self.transform = transform

    def to_str_name(num):
        if num == 0:
            return

    def __len__(self):
        return len(self.img_list)

    def __getitem__(self, idx):
        img_name = self.img_list[idx]

        image = io.imread(self.img_dir+img_name)
        segment = io.imread(self.seg_dir+img_name)
        depth = np.load(self.depth_dir+img_name[:-4]+".npy")


        np_img = np.array(image)
        np_img = cv2.resize(np_img,(224,224),interpolation=cv2.INTER_AREA)
        np_seg = cv2.resize(np.array(segment), (224, 224), interpolation=cv2.INTER_AREA)
        np_dep = cv2.resize(np.array(depth), (224, 224), interpolation=cv2.INTER_AREA)
        depth_for_loss = cv2.resize(np.array(depth), (112, 112), interpolation=cv2.INTER_AREA)
        np_img = np_img.transpose(2, 0, 1)
        np_seg_extend = np_seg.reshape((1,) + np_seg.shape)
        np_img_seg = np.concatenate((np_img,np_seg_extend),axis=0)
        np_img_seg = np_img_seg.transpose(1, 2, 0)






        if self.transform:
            np_img_seg = self.transform(np_img_seg)
            #np_seg = self.transform(np_seg)
            #np_dep = self.transform(np_dep)
            #depth_for_loss = self.transform(depth_for_loss)

        x = torch.FloatTensor(np_img_seg)
        y = torch.FloatTensor(np_seg)
        #y = y.reshape((1,) + y.shape)

        z = torch.FloatTensor(np_dep)
        #z = z.reshape((1,) + z.shape)

        gt = torch.FloatTensor(depth_for_loss)
        gt = gt.reshape((1,) + gt.shape)

        return [x, y, z,gt]



if __name__ == "__main__":
    writer = SummaryWriter('runs/depth_exper_img_seg_1')

    trans = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406,0.5],
                             std=[0.229, 0.224, 0.225,0.5])
    ])
    init_dataset = DepthDataset("../maskrcnn/data/3d_dataset/",transform=trans)

    lengths = [int(len(init_dataset) * 0.8), int(len(init_dataset) * 0.2)+1]
    train_dataset, val_dataset = random_split(init_dataset, lengths)

    dataloader = DataLoader(train_dataset, batch_size=1, shuffle=True)
    val_dataloader = DataLoader(val_dataset, batch_size=1,shuffle=True)

    # GPU 할당 변경하기
    GPU_NUM = 2  # 원하는 GPU 번호 입력
    device = torch.device(f'cuda:{GPU_NUM}' if torch.cuda.is_available() else 'cpu')
    torch.cuda.set_device(device)  # change allocation of current GPU
    print('Current cuda device ', torch.cuda.current_device())  # check

    # Additional Infos
    if device.type == 'cuda':
        print(torch.cuda.get_device_name(GPU_NUM))
        print('Memory Usage:')
        print('Allocated:', round(torch.cuda.memory_allocated(GPU_NUM) / 1024 ** 3, 1), 'GB')
        print('Cached:   ', round(torch.cuda.memory_cached(GPU_NUM) / 1024 ** 3, 1), 'GB')

    ###


    model_ft = DepthNetwork().to(device)
    model_ft.load_state_dict(torch.load("img_seg_model.pth"))
    print("@@@@@")



    nb_epochs = 1000
    num_batches = len(dataloader)

    print("len loader--")
    print(len(dataloader))

    IMG_DIR = "./test_image/"

    for i, samples in enumerate(dataloader):
        # 입력을 받은 후,
        model_ft.eval()

        img_seg,seg,depth,gt = samples
        inputs = img_seg
        labels = depth
        gt = Variable(gt).to(device)

        # Variable로 감싸고
        inputs, labels = Variable(inputs).to(device), Variable(labels).to(device)
        # 변화도 매개변수를 0으로 만든 후
        outputs = model_ft(inputs).to(device)


        outputs = outputs.squeeze()
        outputs = outputs.squeeze()
        print(outputs.shape)
        cv2.imwrite(IMG_DIR+init_dataset.img_list[i],outputs.cpu().detach().numpy())

        if i>2000 : break





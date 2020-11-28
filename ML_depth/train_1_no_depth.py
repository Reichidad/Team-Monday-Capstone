import os
import argparse

import torch
import torch.nn as nn
import torch.nn.functional as F
import numpy as np
import cv2

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

from depth_network import DepthNetwork


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


        if self.transform:
            #np_img = self.transform(np_img)
            np_seg = self.transform(np_seg)
            np_dep = self.transform(np_dep)
            #depth_for_loss = self.transform(depth_for_loss)

        x = torch.FloatTensor(np_img.transpose(2,0,1))
        y = torch.FloatTensor(np_seg)
        #y = y.reshape((1,) + y.shape)

        z = torch.FloatTensor(np_dep)
        #z = z.reshape((1,) + z.shape)

        gt = torch.FloatTensor(depth_for_loss)
        gt = gt.reshape((1,) + gt.shape)

        return [x, y, z,gt]



if __name__ == "__main__":
    writer = SummaryWriter('runs/depth_exper_2_no_norm')

    trans = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485],
                             std=[0.229])
    ])
    init_dataset = DepthDataset("../maskrcnn/data/3d_dataset/",transform=trans)

    lengths = [int(len(init_dataset) * 0.8), int(len(init_dataset) * 0.2)+1]
    train_dataset, val_dataset = random_split(init_dataset, lengths)

    dataloader = DataLoader(train_dataset, batch_size=6, shuffle=True)
    val_dataloader = DataLoader(val_dataset, batch_size=1,shuffle=True)

    # GPU 할당 변경하기
    GPU_NUM = 0  # 원하는 GPU 번호 입력
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

    criterion = nn.MSELoss(reduce=False, size_average=False)
    optimizer = optim.Adam(model_ft.parameters(), lr=args.lr, weight_decay=args.weight_decay)

    # 7 에폭마다 0.1씩 학습률 감소
    exp_lr_scheduler = lr_scheduler.StepLR(optimizer, step_size=7, gamma=0.1)

    ###



    nb_epochs = 1000
    num_batches = len(dataloader)

    print("len loader--")
    print(len(dataloader))



    for epoch in range(nb_epochs + 1):

        running_loss = 0.0
        running_corrects = 0

        for i, samples in enumerate(dataloader):
            # 입력을 받은 후,
            img,seg,depth,gt = samples
            inputs = seg
            labels = depth
            gt = Variable(gt).to(device)

            # Variable로 감싸고
            inputs, labels = Variable(inputs).to(device), Variable(labels).to(device)
            # 변화도 매개변수를 0으로 만든 후


            model_ft.train()
            outputs = model_ft(inputs).to(device)
            # _, preds = torch.max(outputs, 1)

            loss = criterion(outputs, gt)
            scalar_loss = torch.mean(loss.view(-1))
            optimizer.zero_grad()
            scalar_loss.backward()
            optimizer.step()


            running_loss += scalar_loss.item()
            # running_corrects += torch.sum(preds == labels.data)
            #
            #
            # data_check = [0,0,0,0]
            # data_check_pos = [0,0,0,0]
            # data_check_label = [0,0,0,0]

            model_ft.eval()
            if (i + 1) % 900 == 0 and i != 0:  # every 100 mini-batches
                with torch.no_grad():
                    val_loss = 0.0
                    for j, val in enumerate(val_dataloader):
                        val_x, val_seg,_,gt = val
                        val_seg = Variable(val_seg).to(device)
                        val_output = model_ft(val_seg).to(device)
                        gt = Variable(gt).to(device)

                        v_loss = criterion(val_output, gt)
                        v_loss = torch.mean(v_loss.view(-1))
                        val_loss += v_loss





                print("epoch: {}/{} | step: {}/{} | trn loss: {:.4f} | val loss: {:.4f}".format(
                    epoch + 1, nb_epochs, i + 1, num_batches, running_loss /i, val_loss/len(val_dataloader))
                )


                print(epoch)
                print(i)
                val_output = val_output.squeeze(dim=0)
                gt = gt.squeeze(dim=0)
                
                writer.add_image("val_depth",val_output,epoch * len(dataloader) + i)
                writer.add_image("val_gt",gt,epoch * len(dataloader) + i)
                writer.add_scalar('training loss',
                                  running_loss /i,
                                  epoch * len(dataloader) + i)

                writer.add_scalar('val loss',
                                  val_loss/len(val_dataset),
                                  epoch * len(dataloader) + i)


            print('Epoch {:4d}/{} Batch {}/{} Loss: {:.6f}'.format(
                epoch, nb_epochs, i + 1, len(dataloader),
                scalar_loss.item()
            ))
        torch.save(model_ft.state_dict(), "./training/model_depth_1/"+str(epoch)+".pth")


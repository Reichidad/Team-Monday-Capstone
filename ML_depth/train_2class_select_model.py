import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import transforms

import numpy as np
import cv2

from torch.utils.data import Dataset
from torch.utils.data import DataLoader
import torch.optim as optim

from data_utils import load_info
from skimage import io
from torch.autograd import Variable
from torch.utils.tensorboard import SummaryWriter
from torchvision import  models
from torch.optim import lr_scheduler


class FlowDataset(Dataset):
    def __init__(self,list_dir,img_dir,transform=None): #data_len = array
        self.list_dir = list_dir
        self.img_dir = img_dir
        self.info_list = load_info(list_dir)
        self.transform = transform

    def to_str_name(num):
        if num == 0:
            return

    def __len__(self):
        return len(self.info_list)

    def __getitem__(self, idx):
        info = self.info_list[idx]
        img_name = info[0]
        image = io.imread(self.img_dir+img_name)
        np_img = np.array(image)
        np_img = cv2.resize(np_img,(224,224),interpolation=cv2.INTER_AREA)
        x = torch.FloatTensor(np_img.transpose(2,0,1))

        if self.transform:
            x = self.transform(x)

        val = 0
        if int(info[1]) > 2:
            val = 2
        else :
            val = 1
        y = torch.LongTensor(np.array(float(val-1)))

        return [x, y]


if __name__ == "__main__":
    writer = SummaryWriter('runs/vgg_experiment_1')

    transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406],
                             std=[0.229, 0.224, 0.225])
    ])


    train_dataset = FlowDataset("train.txt","./flow_images/")
    dataloader = DataLoader(train_dataset,batch_size=6,shuffle=True)

    val_dataset = FlowDataset("val.txt", "./flow_images/")
    val_dataloader = DataLoader(val_dataset, batch_size=1,shuffle=True)

    # GPU 할당 변경하기
    GPU_NUM = 1  # 원하는 GPU 번호 입력
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
    backbone = "vgg16"

    if backbone == "resnet18":
        model_ft = models.resnet18(pretrained=True)
        num_ftrs = model_ft.fc.in_features
        model_ft.fc = nn.Linear(num_ftrs, 2)
        model_ft = model_ft.to(device)
    elif backbone == "vgg16":
        vgg16 = models.vgg16_bn()
        num_features = vgg16.classifier[6].in_features
        features = list(vgg16.classifier.children())[:-1]  # Remove last layer
        features.extend([nn.Linear(num_features, 2)])  # Add our layer with 4 outputs
        vgg16.classifier = nn.Sequential(*features)  # Replace the model classifier
        print(vgg16)
        model_ft = vgg16.to(device
                            )




    criterion = nn.CrossEntropyLoss()
    optimizer_ft = optim.SGD(model_ft.parameters(), lr=0.001, momentum=0.9)

    # 7 에폭마다 0.1씩 학습률 감소
    exp_lr_scheduler = lr_scheduler.StepLR(optimizer_ft, step_size=7, gamma=0.1)

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
            inputs, labels = samples

            # Variable로 감싸고
            inputs, labels = Variable(inputs).to(device), Variable(labels).to(device)
            # 변화도 매개변수를 0으로 만든 후


            model_ft.train()

            outputs = model_ft(inputs).to(device)
            _, preds = torch.max(outputs, 1)

            loss = criterion(outputs, labels)
            optimizer_ft.zero_grad()
            loss.backward()
            optimizer_ft.step()


            running_loss += loss.item()
            running_corrects += torch.sum(preds == labels.data)


            data_check = [0,0,0,0]
            data_check_pos = [0,0,0,0]
            data_check_label = [0,0,0,0]

            model_ft.eval()
            if (i + 1) % 1000 == 0 and i != 0:  # every 100 mini-batches
                with torch.no_grad():
                    val_loss = 0.0
                    for j, val in enumerate(val_dataloader):
                        val_x, val_label = val
                        val_x = Variable(val_x).to(device)
                        val_label = Variable(val_label).to(device)
                        val_output = model_ft(val_x).to(device)
                        v_loss = criterion(val_output, val_label)
                        val_loss += v_loss

                        data_check_label[val_label] += 1
                        max = 0
                        for k in range(len(val_output[0])):
                            if val_output[0][k].item() > max:
                                max = k
                        data_check[max] += 1
                        if val_label == max:
                            data_check_pos[max] += 1
                print("data check")
                print(data_check)
                print(data_check_label)
                print(data_check_pos)


                writer.add_scalar('data1',
                                  data_check[0],
                                  epoch * len(dataloader) + i)
                writer.add_scalar('data2',
                                  data_check[1],
                                  epoch * len(dataloader) + i)
                writer.add_scalar('data3',
                                  data_check[2],
                                  epoch * len(dataloader) + i)
                writer.add_scalar('data4',
                                  data_check[3],
                                  epoch * len(dataloader) + i)

                for n in range(0,4):
                    writer.add_scalar('data_pos_'+str(n),
                                      data_check_pos[n],
                                      epoch * len(dataloader) + i)

                train_acc = running_corrects.double() / (i*6)
                val_acc = np.sum(data_check_pos) / np.sum(data_check_label)

                print("train accuracy : "  + str(train_acc))
                print("test accuracy : " + str(val_acc))

                print("epoch: {}/{} | step: {}/{} | trn loss: {:.4f} | val loss: {:.4f}".format(
                    epoch + 1, nb_epochs, i + 1, num_batches, running_loss /i, val_loss/len(val_dataloader))
                )

                writer.add_scalar('training accuracy',
                                  train_acc,
                                  epoch * len(dataloader) + i)

                writer.add_scalar('val accuracy',
                                  val_acc,
                                  epoch * len(dataloader) + i)

                print(epoch * len(dataloader) + i)
                print(epoch)
                print(i)


                writer.add_scalar('training loss',
                                  running_loss /i,
                                  epoch * len(dataloader) + i)


                writer.add_scalar('val loss',
                                  val_loss/len(val_dataset),
                                  epoch * len(dataloader) + i)


            # print('Epoch {:4d}/{} Batch {}/{} Loss: {:.6f}'.format(
            #     epoch, nb_epochs, i + 1, len(dataloader),
            #     loss.item()
            # ))
        torch.save(model_ft.state_dict(), "./training/model_resnet_2class/"+str(epoch)+".pth")

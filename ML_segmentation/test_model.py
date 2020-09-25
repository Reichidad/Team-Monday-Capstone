from torchvision import models

maskrcnn = torchvision.models.detection.maskrcnn_resnet50_fpn(pretrained=True)
maskrcnn.eval()


import torchvision.transforms as T
from PIL import Image

image = Image.open('Dir')

transform = T.Compose([T.Resize(400), T.CenterCrop(224), T.ToTensor(),
                 T.Normalize(mean=[0.485, 0.456, 0.406],
                             std=[0.229, 0.224, 0.225])])
# Resize, Centercrop 등 ToTensor 를 제외한 수치들은 Image 에 맞게 바꿔주시면 됩니다.

img = transform(image)



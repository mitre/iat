import math
import torch
import numpy as np
import cv2
import torch
import torch.nn as nn
from torchvision import models
from torchvision.transforms import Compose, ToTensor, Normalize
from network import NestedSharedAtrousResUNet, NestedSharedAtrousAttentionResUNetIN
from PIL import Image

class fclayer(nn.Module):
    def __init__(self, in_h = 8, in_w = 10, out_n = 6):
        super().__init__()
        self.in_h = in_h
        self.in_w = in_w
        self.out_n = out_n
        self.fc_list = []
        self.fc_list = nn.ModuleList(nn.Linear(in_h*in_w, 1) for i in range(out_n))
    def forward(self, x):
        x = x.reshape(-1, self.out_n, self.in_h, self.in_w)
        outs = []
        for i, fc_layer in enumerate(self.fc_list):
            outs.append(fc_layer(x[:, i, :, :].reshape(-1, self.in_h*self.in_w)))
        out = torch.cat(outs, 1)
        return out

class conv(nn.Module):
    def __init__(self, in_channels=512, out_n = 6):
        super().__init__()
        self.conv = nn.Conv2d(in_channels, out_n, kernel_size=1, stride=1, padding='same')
    def forward(self, x):
        x = self.conv(x)
        return x
    
class CircleMaskFinder(object):
    def __init__(self, mask_net_path = "./models/nestedsharedatrousresunet-156-0.026496-maskIoU-0.943222.pth", circle_net_path = './models/convnext_tiny-1076-0.030622-maskIoU-0.938355.pth', eyelid_net_path = "./models/nestedsharedatrousresunet-1935-0.036982-maskIoU-0.946174-insideeyelid.pth", device=torch.device('cpu')):
        self.circle_net_path = circle_net_path
        self.mask_net_path = mask_net_path
        self.eyelid_net_path = eyelid_net_path
        self.device = device
        self.NET_INPUT_SIZE = (320,240)
        self.eyelid_model = NestedSharedAtrousResUNet(1, 1, width=64, resolution=(self.NET_INPUT_SIZE[1], self.NET_INPUT_SIZE[0]))
        try:
            self.eyelid_model.load_state_dict(torch.load(self.eyelid_net_path, map_location=self.device))
        except AssertionError:
                print("assertion error")
                self.eyelid_model.load_state_dict(torch.load(self.eyelid_net_path,
                    map_location = lambda storage, loc: storage))
        self.eyelid_model = self.eyelid_model.to(self.device)
        self.eyelid_model.eval()
        self.circle_model = models.convnext_tiny()
        self.circle_model.avgpool = conv(in_channels=768, out_n=6)
        self.circle_model.classifier = fclayer(in_h=7, in_w=10, out_n=6)
        try:
            self.circle_model.load_state_dict(torch.load(self.circle_net_path, map_location=self.device))
        except AssertionError:
                print("assertion error")
                self.circle_model.load_state_dict(torch.load(self.circle_net_path,
                    map_location = lambda storage, loc: storage))
        self.circle_model = self.circle_model.to(self.device)
        self.circle_model.eval()
        self.mask_model = NestedSharedAtrousResUNet(1, 1, width=64, resolution=(self.NET_INPUT_SIZE[1], self.NET_INPUT_SIZE[0]))
        try:
            self.mask_model.load_state_dict(torch.load(self.mask_net_path, map_location=self.device))
        except AssertionError:
                print("assertion error")
                self.mask_model.load_state_dict(torch.load(self.mask_net_path,
                    map_location = lambda storage, loc: storage))
        self.mask_model = self.mask_model.to(self.device)
        self.mask_model.eval()
        self.input_transform = Compose([
            ToTensor(),
            Normalize(mean=(0.5,), std=(0.5,))
        ])
        self.ISO_RES = (640,480)
    
    def fix_image(self, image):
        w, h = image.size
        aspect_ratio = float(w)/float(h)
        if aspect_ratio >= 1.333 and aspect_ratio <= 1.334:
            result_im = image
            w_pad = 0
            h_pad = 0
        elif aspect_ratio < 1.333:
            w_new = h * (4.0/3.0)
            w_pad = (w_new - w) / 2
            result_im = Image.new(image.mode, (int(w_new), h), 127)
            result_im.paste(image, (int(w_pad), 0))
            h_pad = 0
        else:
            h_new = w * (3.0/4.0)
            h_pad = (h_new - h) / 2
            result_im = Image.new(image.mode, (w, int(h_new)), 127)
            result_im.paste(image, (0, int(h_pad)))
            w_pad = 0
        return result_im, w_pad, h_pad
        
    def segmentAndCircApprox(self, image):
           
        w,h = image.size

        image = cv2.resize(np.array(image), self.NET_INPUT_SIZE, cv2.INTER_LINEAR_EXACT)
        with torch.inference_mode():
            inp_t = self.input_transform(image).unsqueeze(0).to(self.device)
            mask_logit_t = torch.sigmoid(self.mask_model(inp_t))
            eyelid_logit_t = torch.sigmoid(self.eyelid_model(inp_t))
            back_mask_t = torch.where(eyelid_logit_t > 0.5, 255, 0)
            back_mask = back_mask_t.cpu().numpy()[0][0]
            mask_t = torch.where(mask_logit_t > 0.5, 255, 0)
            mask = mask_t.cpu().numpy()[0][0]
            inp_xyr_t = self.circle_model(inp_t.repeat(1,3,1,1))
            inp_xyr = inp_xyr_t.tolist()[0]

        #Mask
        mask = cv2.resize(mask, (w, h), interpolation=cv2.INTER_NEAREST_EXACT)
        back_mask = cv2.resize(back_mask, (w, h), interpolation=cv2.INTER_NEAREST_EXACT)

        #Circle params
        diag = math.sqrt(w**2 + h**2)
        pupil_x = inp_xyr[0] * w
        pupil_y = inp_xyr[1] * h
        pupil_r = inp_xyr[2] * 0.5 * 0.8 * diag
        iris_x = inp_xyr[3] * w
        iris_y = inp_xyr[4] * h
        iris_r = inp_xyr[5] * 0.5 * diag

        return Image.fromarray(mask.astype(np.uint8), "L"), Image.fromarray(back_mask.astype(np.uint8), "L"), np.array([pupil_x,pupil_y,pupil_r]), np.array([iris_x,iris_y,iris_r])
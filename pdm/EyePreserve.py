import numpy as np
import torch
import torch
from torch import nn
from torchvision.transforms import Compose, ToTensor, Normalize, Resize
from PIL import Image
import cv2
from kornia.geometry.transform import rotate as tensor_rotate
from io import StringIO
from scipy import io
import math
from math import pi
from torchvision import transforms
import gc

def create_circular_mask(h, w, center=None, radius=None):

    if center is None: # use the middle of the image
        center = (int(w/2), int(h/2))
    if radius is None: # use the smallest distance between the center and image walls
        radius = min(center[0], center[1], w-center[0], h-center[1])

    Y, X = np.ogrid[:h, :w]
    dist_from_center = np.sqrt((X - center[0])**2 + (Y-center[1])**2)

    mask = np.where(dist_from_center <= radius, 255, 0)
    return mask

def crop_iris_w_b(img_tensors, mask_tensors, back_mask_tensors, pupil_xyr, iris_xyr, res=256):
    cropped_imgs = []
    cropped_masks = []
    cropped_back_masks = []
    pupil_xyr_new = pupil_xyr.clone().detach()
    iris_xyr_new = iris_xyr.clone().detach()

    b,c,h,w = img_tensors.shape

    for i in range(img_tensors.shape[0]):
        ix = int(round(iris_xyr[i, 0].item()))
        iy = int(round(iris_xyr[i, 1].item()))
        ir = iris_xyr[i, 2].item()

        ir_ratio = 16/14
        x_min = (ix - int(round(ir_ratio*ir)))
        x_max = (ix + int(round(ir_ratio*ir)))
        y_min = (iy - int(round(ir_ratio*ir)))
        y_max = (iy + int(round(ir_ratio*ir)))
        
        if x_min >= 0 and x_max < w and y_min >= 0 and y_min < h:
            cropped_img = img_tensors[i, :, y_min:y_max, x_min:x_max].unsqueeze(0)
            cropped_mask = mask_tensors[i, :, y_min:y_max, x_min:x_max].unsqueeze(0)
            cropped_back_mask = back_mask_tensors[i, :, y_min:y_max, x_min:x_max].unsqueeze(0)
        else:
            if x_min < 0:
                if y_min < 0:
                    cropped_img = img_tensors[i, :, 0:y_max, 0:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, 0:y_max, 0:x_max].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, 0:y_max, 0:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (-x_min, 0, -y_min, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (-x_min, 0, -y_min, 0), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (-x_min, 0, -y_min, 0), mode='constant', value=0)
                elif y_max >= h:
                    cropped_img = img_tensors[i, :, y_min:h, 0:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:h, 0:x_max].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, y_min:h, 0:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (-x_min, 0, 0, (y_max - h)), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (-x_min, 0, 0, (y_max - h)), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (-x_min, 0, 0, (y_max - h)), mode='constant', value=0)
                else:
                    cropped_img = img_tensors[i, :, y_min:y_max, 0:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:y_max, 0:x_max].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, y_min:y_max, 0:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (-x_min, 0, 0, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (-x_min, 0, 0, 0), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (-x_min, 0, 0, 0), mode='constant', value=0)
            elif x_max >= w:
                if y_min < 0:
                    cropped_img = img_tensors[i, :, 0:y_max, x_min:w].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, 0:y_max, x_min:w].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, 0:y_max, x_min:w].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, (x_max - w), -y_min, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, (x_max - w), -y_min, 0), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (0, (x_max - w), -y_min, 0), mode='constant', value=0)
                elif y_max >= h:
                    cropped_img = img_tensors[i, :, y_min:h, x_min:w].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:h, x_min:w].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, y_min:h, x_min:w].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, (x_max - w), 0, (y_max - h)), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, (x_max - w), 0, (y_max - h)), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (0, (x_max - w), 0, (y_max - h)), mode='constant', value=0)
                else:
                    cropped_img = img_tensors[i, :, y_min:y_max, x_min:w].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:y_max, x_min:w].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, y_min:y_max, x_min:w].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, (x_max - w), 0, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, (x_max - w), 0, 0), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (0, (x_max - w), 0, 0), mode='constant', value=0)
            else:
                if y_min < 0:
                    cropped_img = img_tensors[i, :, 0:y_max, x_min:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, 0:y_max, x_min:x_max].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, 0:y_max, x_min:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, 0, -y_min, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, 0, -y_min, 0), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (0, 0, -y_min, 0), mode='constant', value=0)
                elif y_max >= h:
                    cropped_img = img_tensors[i, :, y_min:h, x_min:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:h, x_min:x_max].unsqueeze(0)
                    cropped_back_mask = back_mask_tensors[i, :, y_min:h, x_min:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, 0, 0, (y_max - h)), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, 0, 0, (y_max - h)), mode='constant', value=0)
                    cropped_back_mask = nn.functional.pad(cropped_back_mask, (0, 0, 0, (y_max - h)), mode='constant', value=0)
   

        cropped_img = nn.functional.interpolate(cropped_img, size=(res, res), mode='bilinear')
        cropped_mask = nn.functional.interpolate(cropped_mask, size=(res, res), mode='nearest')
        cropped_back_mask = nn.functional.interpolate(cropped_back_mask, size=(res, res), mode='nearest')
        
        mult = res/(ir_ratio*ir * 2)
        pupil_xyr_new[i, 0] = (res/2) - mult * (ir - (pupil_xyr[i, 0] - ix + ir))
        pupil_xyr_new[i, 1] = (res/2) - mult * (ir - (pupil_xyr[i, 1] - iy + ir))
        pupil_xyr_new[i, 2] = mult * pupil_xyr[i, 2]
        iris_xyr_new[i, 0] = res/2
        iris_xyr_new[i, 1] = res/2
        iris_xyr_new[i, 2] = ((res/2) - (res/16))
        
        cropped_imgs.append(cropped_img)
        cropped_masks.append(cropped_mask)
        cropped_back_masks.append(cropped_back_mask)
    cropped_imgs = torch.cat(cropped_imgs, 0)
    cropped_masks = torch.cat(cropped_masks, 0)
    cropped_back_masks = torch.cat(cropped_back_masks, 0)
    return cropped_imgs, cropped_masks, cropped_back_masks, pupil_xyr_new, iris_xyr_new

def rotate_tensor_batch_w_b(tensors, masks, back_masks, shifts, pupil_xyr, iris_xyr):
    rotated_tensors = []
    rotated_masks = []
    rotated_back_masks = []
    pupil_xyr_new = pupil_xyr.clone().detach()
    iris_xyr_new = iris_xyr.clone().detach()
    for i in range(tensors.shape[0]):
        cx = iris_xyr[i, 0].item()
        cy = iris_xyr[i, 1].item()
        rot_center = torch.tensor([int(round(cx)), int(round(cy))]).reshape(1,2).float()
        tensor = tensors[i, :, :, :].clone().detach().unsqueeze(0)
        mask = masks[i, :, :, :].clone().detach().unsqueeze(0)
        back_mask = back_masks[i, :, :, :].clone().detach().unsqueeze(0)
        rot_degree = torch.tensor((shifts[i]/512)*360).float()
        rotated_tensor = tensor_rotate(tensor, -rot_degree, center=rot_center, mode='bilinear', padding_mode='border')
        rotated_tensors.append(rotated_tensor)
        rotated_mask = tensor_rotate(mask, -rot_degree, center=rot_center, mode='nearest', padding_mode='zeros')
        rotated_masks.append(rotated_mask)
        rotated_back_mask = tensor_rotate(back_mask, -rot_degree, center=rot_center, mode='nearest', padding_mode='zeros')
        rotated_back_masks.append(rotated_back_mask)
        rot_radian = (shifts[i]/512) * 2 * math.pi
        pupil_xyr_new[i, 0] = (pupil_xyr[i, 0] - cx) * math.cos(-rot_radian) + (pupil_xyr[i, 1] - cy) * math.sin(-rot_radian) + cx
        pupil_xyr_new[i, 1] = (pupil_xyr[i, 1] - cy) * math.cos(-rot_radian) - (pupil_xyr[i, 0] - cx) * math.sin(-rot_radian) + cy
        iris_xyr_new[i, 0] = (iris_xyr[i, 0] - cx) * math.cos(-rot_radian) + (iris_xyr[i, 1] - cy) * math.sin(-rot_radian) + cx
        iris_xyr_new[i, 1] = (iris_xyr[i, 1] - cy) * math.cos(-rot_radian) - (iris_xyr[i, 0] - cx) * math.sin(-rot_radian) + cy
    rotated_tensors = torch.cat(rotated_tensors, dim=0)
    rotated_masks = torch.cat(rotated_masks, dim=0)
    rotated_back_masks = torch.cat(rotated_back_masks, dim=0)
    return rotated_tensors, rotated_masks, rotated_back_masks, pupil_xyr_new, iris_xyr_new

def crop_iris(img_tensors, mask_tensors, pupil_xyr, iris_xyr, res=256):
    cropped_imgs = []
    cropped_masks = []
    pupil_xyr_new = pupil_xyr.clone().detach()
    iris_xyr_new = iris_xyr.clone().detach()

    b,c,h,w = img_tensors.shape

    for i in range(img_tensors.shape[0]):
        ix = int(round(iris_xyr[i, 0].item()))
        iy = int(round(iris_xyr[i, 1].item()))
        ir = iris_xyr[i, 2].item()

        ir_ratio = 16/14
        x_min = (ix - int(round(ir_ratio*ir)))
        x_max = (ix + int(round(ir_ratio*ir)))
        y_min = (iy - int(round(ir_ratio*ir)))
        y_max = (iy + int(round(ir_ratio*ir)))
        
        if x_min >= 0 and x_max < w and y_min >= 0 and y_min < h:
            cropped_img = img_tensors[i, :, y_min:y_max, x_min:x_max].unsqueeze(0)
            cropped_mask = mask_tensors[i, :, y_min:y_max, x_min:x_max].unsqueeze(0)
        else:
            if x_min < 0:
                if y_min < 0:
                    cropped_img = img_tensors[i, :, 0:y_max, 0:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, 0:y_max, 0:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (-x_min, 0, -y_min, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (-x_min, 0, -y_min, 0), mode='constant', value=0)
                elif y_max >= h:
                    cropped_img = img_tensors[i, :, y_min:h, 0:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:h, 0:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (-x_min, 0, 0, (y_max - h)), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (-x_min, 0, 0, (y_max - h)), mode='constant', value=0)
                else:
                    cropped_img = img_tensors[i, :, y_min:y_max, 0:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:y_max, 0:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (-x_min, 0, 0, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (-x_min, 0, 0, 0), mode='constant', value=0)
            elif x_max >= w:
                if y_min < 0:
                    cropped_img = img_tensors[i, :, 0:y_max, x_min:w].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, 0:y_max, x_min:w].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, (x_max - w), -y_min, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, (x_max - w), -y_min, 0), mode='constant', value=0)
                elif y_max >= h:
                    cropped_img = img_tensors[i, :, y_min:h, x_min:w].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:h, x_min:w].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, (x_max - w), 0, (y_max - h)), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, (x_max - w), 0, (y_max - h)), mode='constant', value=0)
                else:
                    cropped_img = img_tensors[i, :, y_min:y_max, x_min:w].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:y_max, x_min:w].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, (x_max - w), 0, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, (x_max - w), 0, 0), mode='constant', value=0)
            else:
                if y_min < 0:
                    cropped_img = img_tensors[i, :, 0:y_max, x_min:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, 0:y_max, x_min:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, 0, -y_min, 0), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, 0, -y_min, 0), mode='constant', value=0)
                elif y_max >= h:
                    cropped_img = img_tensors[i, :, y_min:h, x_min:x_max].unsqueeze(0)
                    cropped_mask = mask_tensors[i, :, y_min:h, x_min:x_max].unsqueeze(0)
                    cropped_img = nn.functional.pad(cropped_img, (0, 0, 0, (y_max - h)), mode='replicate')
                    cropped_mask = nn.functional.pad(cropped_mask, (0, 0, 0, (y_max - h)), mode='constant', value=0)
   

        cropped_img = nn.functional.interpolate(cropped_img, size=(res, res), mode='bilinear')
        cropped_mask = nn.functional.interpolate(cropped_mask, size=(res, res), mode='nearest') 
        
        mult = res/(ir_ratio*ir * 2)
        pupil_xyr_new[i, 0] = (res/2) - mult * (ir - (pupil_xyr[i, 0] - ix + ir))
        pupil_xyr_new[i, 1] = (res/2) - mult * (ir - (pupil_xyr[i, 1] - iy + ir))
        pupil_xyr_new[i, 2] = mult * pupil_xyr[i, 2]
        iris_xyr_new[i, 0] = res/2
        iris_xyr_new[i, 1] = res/2
        iris_xyr_new[i, 2] = ((res/2) - (res/16))
        
        cropped_imgs.append(cropped_img)
        cropped_masks.append(cropped_mask)
    cropped_imgs = torch.cat(cropped_imgs, 0)
    cropped_masks = torch.cat(cropped_masks, 0)
    return cropped_imgs, cropped_masks, pupil_xyr_new, iris_xyr_new

def calculate_shift(anchor_code, anchor_mask, inp_code, inp_mask, max_shift=16):
    with torch.no_grad():
        scores = []
        anchor_code_binary = torch.where(anchor_code[:, :12, :, :] > 0, True, False) # b x filt_size x 64 x 512
        inp_code_binary = torch.where(inp_code[:, :12, :, :] > 0, True, False)  # b x filt_size x 64 x 512
        for shift in range(-max_shift, max_shift+1):
            andMasks = torch.logical_and(anchor_mask, torch.roll(inp_mask, shift, -1))  # b x 1 x  64 x 512
            xorCodes = torch.logical_xor(anchor_code_binary, torch.roll(inp_code_binary, shift, -1)) # b x filt_size x 64 x 512
            xorCodesMasked = torch.logical_and(xorCodes, andMasks.repeat(1, xorCodes.shape[1], 1, 1)) # b x filt_size x 64 x 512
            scores.append((torch.sum(xorCodesMasked, dim=(1,2,3)) / (torch.sum(andMasks, dim=(1,2,3)) * 12)).unsqueeze(-1)) # b * (max_shift+1)*2
        scores = torch.cat(scores, 1) # b x (max_shift+1)*2
        scores_index = torch.argmin(scores, dim=1)
        shifts = scores_index - max_shift
    return shifts.cpu().numpy().tolist()

def rotate_tensor_batch(tensors, masks, shifts, pupil_xyr, iris_xyr):
    rotated_tensors = []
    rotated_masks = []
    pupil_xyr_new = pupil_xyr.clone().detach()
    iris_xyr_new = iris_xyr.clone().detach()
    for i in range(tensors.shape[0]):
        cx = iris_xyr[i, 0].item()
        cy = iris_xyr[i, 1].item()
        rot_center = torch.tensor([int(round(cx)), int(round(cy))]).reshape(1,2).float()
        tensor = tensors[i, :, :, :].clone().detach().unsqueeze(0)
        mask = masks[i, :, :, :].clone().detach().unsqueeze(0)
        rot_degree = torch.tensor((shifts[i]/512)*360).float()
        rotated_tensor = tensor_rotate(tensor, -rot_degree, center=rot_center, mode='bilinear', padding_mode='border')
        rotated_tensors.append(rotated_tensor)
        rotated_mask = tensor_rotate(mask, -rot_degree, center=rot_center, mode='bilinear', padding_mode='zeros')
        rotated_masks.append(rotated_mask)
        rot_radian = (shifts[i]/512) * 2 * math.pi
        pupil_xyr_new[i, 0] = (pupil_xyr[i, 0] - cx) * math.cos(-rot_radian) + (pupil_xyr[i, 1] - cy) * math.sin(-rot_radian) + cx
        pupil_xyr_new[i, 1] = (pupil_xyr[i, 1] - cy) * math.cos(-rot_radian) - (pupil_xyr[i, 0] - cx) * math.sin(-rot_radian) + cy
        iris_xyr_new[i, 0] = (iris_xyr[i, 0] - cx) * math.cos(-rot_radian) + (iris_xyr[i, 1] - cy) * math.sin(-rot_radian) + cx
        iris_xyr_new[i, 1] = (iris_xyr[i, 1] - cy) * math.cos(-rot_radian) - (iris_xyr[i, 0] - cx) * math.sin(-rot_radian) + cy
    rotated_tensors = torch.cat(rotated_tensors, dim=0)
    rotated_masks = torch.cat(rotated_masks, dim=0)
    return rotated_tensors, rotated_masks, pupil_xyr_new, iris_xyr_new

class SIFLayerMaskOSIRIS(nn.Module):
    def __init__(self, polar_height, polar_width, filter_mat1, filter_mat2, osiris_filters, device, channels = 1):
        super().__init__()
        with torch.inference_mode():
            self.device = device
            self.polar_height = polar_height
            self.polar_width = polar_width
            self.channels = channels
            
            self.filter1_size = filter_mat1.shape[0]
            self.num_filters1 = filter_mat1.shape[2]
            self.filter1 = torch.FloatTensor(filter_mat1).to(self.device)
            self.filter_mat1 = filter_mat1
            self.filter1 = torch.moveaxis(self.filter1.unsqueeze(0), 3, 0).detach().requires_grad_(False)
            
            self.filter2_size = filter_mat2.shape[0]
            self.num_filters2 = filter_mat2.shape[2]
            self.filter2 = torch.FloatTensor(filter_mat2).to(self.device)
            self.filter_mat2 = filter_mat2
            self.filter2 = torch.moveaxis(self.filter2.unsqueeze(0), 3, 0).detach().requires_grad_(False)
            
            self.np_filters = []
            with open(osiris_filters, 'r') as osirisFile:
                string_inp = ''
                for line in osirisFile:
                    if not line.strip() == 'end':
                        string_inp += line.strip() + '\n'
                    else:
                        c = StringIO(string_inp)
                        self.np_filters.append(np.loadtxt(c))
                        string_inp = ''
            
            self.torch_filters = []
            
            self.torch_filters.append(self.filter1)
            self.torch_filters.append(self.filter2)
            
            self.max_filter_size = max(self.filter1.shape[2], self.filter2.shape[2])
            
            for np_filter in self.np_filters:
                torch_filter = torch.FloatTensor(np_filter).to(self.device)
                torch_filter = torch_filter.unsqueeze(0).unsqueeze(0).detach().requires_grad_(False)
                self.max_filter_size = max(self.max_filter_size, torch_filter.shape[2])
                self.torch_filters.append(torch_filter)
            
            print('Max Filter Size:', self.max_filter_size)
            self.n_filters = len(self.torch_filters)
        
    def grid_sample(self, input, grid, interp_mode='bilinear'):

        N, C, H, W = input.shape
        gridx = grid[:, :, :, 0]
        gridy = grid[:, :, :, 1]
        gridx = ((gridx + 1) / 2 * W - 0.5) / (W - 1) * 2 - 1
        gridy = ((gridy + 1) / 2 * H - 0.5) / (H - 1) * 2 - 1
        newgrid = torch.stack([gridx, gridy], dim=-1).requires_grad_(False)
        return torch.nn.functional.grid_sample(input, newgrid, mode=interp_mode, padding_mode='zeros')

    def cartToPol(self, image, mask, pupil_xyr, iris_xyr):
        with torch.inference_mode():
            batch_size = image.shape[0]
            width = image.shape[3]
            height = image.shape[2]
    
            polar_height = self.polar_height
            polar_width = self.polar_width
    
            pupil_xyr = pupil_xyr.clone().detach().requires_grad_(False)
            iris_xyr = iris_xyr.clone().detach().requires_grad_(False)
            
            theta = (2*pi*torch.linspace(0,polar_width-1,polar_width)/polar_width).requires_grad_(False).to(self.device)
    
            pxCirclePoints = pupil_xyr[:, 0].reshape(-1, 1) + pupil_xyr[:, 2].reshape(-1, 1) @ torch.cos(theta).reshape(1, polar_width) #b x 512
            pyCirclePoints = pupil_xyr[:, 1].reshape(-1, 1) + pupil_xyr[:, 2].reshape(-1, 1) @ torch.sin(theta).reshape(1, polar_width)  #b x 512
            
            ixCirclePoints = iris_xyr[:, 0].reshape(-1, 1) + iris_xyr[:, 2].reshape(-1, 1) @ torch.cos(theta).reshape(1, polar_width)  #b x 512
            iyCirclePoints = iris_xyr[:, 1].reshape(-1, 1) + iris_xyr[:, 2].reshape(-1, 1) @ torch.sin(theta).reshape(1, polar_width)  #b x 512
    
            radius = (torch.linspace(1,polar_height,polar_height)/polar_height).reshape(-1, 1).requires_grad_(False).to(self.device)  #64 x 1
            
            pxCoords = torch.matmul((1-radius), pxCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
            pyCoords = torch.matmul((1-radius), pyCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
            
            ixCoords = torch.matmul(radius, ixCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
            iyCoords = torch.matmul(radius, iyCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
    
            x = (pxCoords + ixCoords).float()
            x_norm = ((x-1)/(width-1))*2 - 1 #b x 64 x 512
    
            y = (pyCoords + iyCoords).float()
            y_norm = ((y-1)/(height-1))*2 - 1  #b x 64 x 512
    
            grid_sample_mat = torch.cat([x_norm.unsqueeze(-1), y_norm.unsqueeze(-1)], dim=-1).detach().requires_grad_(False)
            
            if mask is not None:
                if torch.is_tensor(mask):
                    mask_t = mask.clone().detach().to(self.device)
                else: 
                    mask_t = torch.tensor(mask).float().to(self.device)
                mask_polar = self.grid_sample(mask_t, grid_sample_mat, interp_mode='nearest')
                mask_polar = (mask_polar - mask_polar.min()) / mask_polar.max()
                mask_polar = torch.where(mask_polar > 0.5, 1.0, 0.0)
            else:
                mask_polar = None
                
        image_polar = self.grid_sample(image, grid_sample_mat, interp_mode='bilinear')
        return image_polar, mask_polar
    
    def cartToPolIrisCenter(self, image, mask, pupil_xyr, iris_xyr):
        with torch.inference_mode():
            batch_size = image.shape[0]
            width = image.shape[3]
            height = image.shape[2]
    
            polar_height = self.polar_height
            polar_width = self.polar_width
    
            pupil_xyr = pupil_xyr.clone().detach().requires_grad_(False)
            iris_xyr = iris_xyr.clone().detach().requires_grad_(False)
            
            theta = (2*pi*torch.linspace(0,polar_width-1,polar_width)/polar_width).requires_grad_(False)
    
            pxCirclePoints = iris_xyr[:, 0].reshape(-1, 1) + pupil_xyr[:, 2].reshape(-1, 1) @ torch.cos(theta).reshape(1, polar_width) #b x 512
            pyCirclePoints = iris_xyr[:, 1].reshape(-1, 1) + pupil_xyr[:, 2].reshape(-1, 1) @ torch.sin(theta).reshape(1, polar_width)  #b x 512
            
            ixCirclePoints = iris_xyr[:, 0].reshape(-1, 1) + iris_xyr[:, 2].reshape(-1, 1) @ torch.cos(theta).reshape(1, polar_width)  #b x 512
            iyCirclePoints = iris_xyr[:, 1].reshape(-1, 1) + iris_xyr[:, 2].reshape(-1, 1) @ torch.sin(theta).reshape(1, polar_width)  #b x 512
    
            radius = (torch.linspace(1,polar_height,polar_height)/polar_height).reshape(-1, 1).requires_grad_(False) #64 x 1
            
            pxCoords = torch.matmul((1-radius), pxCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
            pyCoords = torch.matmul((1-radius), pyCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
            
            ixCoords = torch.matmul(radius, ixCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
            iyCoords = torch.matmul(radius, iyCirclePoints.reshape(-1, 1, polar_width)) # b x 64 x 512
    
            x = (pxCoords + ixCoords).float()
            x_norm = ((x-1)/(width-1))*2 - 1 #b x 64 x 512
    
            y = (pyCoords + iyCoords).float()
            y_norm = ((y-1)/(height-1))*2 - 1  #b x 64 x 512
    
            grid_sample_mat = torch.cat([x_norm.unsqueeze(-1), y_norm.unsqueeze(-1)], dim=-1).detach().requires_grad_(False)
            
            if mask is not None:
                if torch.is_tensor(mask):
                    mask_t = mask.clone().detach()
                else: 
                    mask_t = torch.tensor(mask).float()
                mask_polar = self.grid_sample(mask_t, grid_sample_mat, interp_mode='nearest')
                mask_polar = (mask_polar - mask_polar.min()) / mask_polar.max()
                mask_polar = torch.where(mask_polar > 0.5, 1.0, 0.0)
            else:
                mask_polar = None
                
        image_polar = self.grid_sample(image, grid_sample_mat, interp_mode='bilinear')
        return image_polar, mask_polar
    
    def getCodes(self, image_polar):
        codes_list = []
        for filter in self.torch_filters:
            r1 = int(filter.shape[2]/2)
            r2 = int(filter.shape[3]/2)
            imgWrap = nn.functional.pad(image_polar, (0, 0, r1, r1), mode='replicate')
            imgWrap = nn.functional.pad(imgWrap, (r2, r2, 0, 0), mode='circular')
            code = nn.functional.conv2d(imgWrap, filter, stride=1, padding='valid')
            codes_list.append(code)
        codes = torch.cat(codes_list, dim=1)
        return codes
    
    def getCodesCPU(self, image_polar):
        image_polar = image_polar.cpu()
        codes_list = []
        for filter in self.torch_filters:
            r1 = int(filter.shape[2]/2)
            r2 = int(filter.shape[3]/2)
            imgWrap = nn.functional.pad(image_polar, (0, 0, r1, r1), mode='replicate')
            imgWrap = nn.functional.pad(imgWrap, (r2, r2, 0, 0), mode='circular')
            code = nn.functional.conv2d(imgWrap, filter.cpu(), stride=1, padding='valid')
            codes_list.append(code)
        codes = torch.cat(codes_list, dim=1)
        return codes

    def forward(self, image, pupil_xyr, iris_xyr, mask=None):
        image_polar, mask_polar = self.cartToPol(image, mask, pupil_xyr, iris_xyr)
        codes = self.getCodes(image_polar)
        return codes, image_polar, mask_polar

class EyePreserve(object):
    def __init__(self, net_path="./models/0007-val_loss-22.957276336365826+-0.0-val_bit_match-81.10588183031244+-0.0-val_linear_bit_match-75.13033321984577+-0.0.pth", sif_filter_path1="./models/ICAtextureFilters_15x15_7bit.mat", sif_filter_path2="./models/ICAtextureFilters_17x17_5bit.mat", osiris_filters_path="./models/osiris_filters.txt", device=torch.device('cpu')):
        self.device = device
        self.nets = torch.load(net_path, map_location=self.device, weights_only=False)
        self.net = self.nets[0].to(device)
        self.net.eval()
        del self.nets[1]
        gc.collect()
        torch.cuda.empty_cache() 
        self.transform = Compose([
            ToTensor(),
            Normalize((0.5, ), (0.5, ))
        ])
        filter_mat1 = io.loadmat(sif_filter_path1)['ICAtextureFilters']
        filter_mat2 = io.loadmat(sif_filter_path2)['ICAtextureFilters']
        self.sifLossModel = SIFLayerMaskOSIRIS(polar_height = 64, polar_width = 512, filter_mat1 = filter_mat1, filter_mat2 = filter_mat2, osiris_filters = osiris_filters_path, device=device).to(device)
        self.transform = transforms.Compose([
            transforms.ToTensor(),
            transforms.Normalize((0.5,), (0.5, ))
        ])

    def find_xy_between(self, r1, r2, r1p, r3p, xp, yp, xc, yc):
        r3 = (((r3p - r1p) * (r2 - r1)) / (r2 - r1p)) + r1
        x = ((r3 * (xp - xc)) / r3p) + xc
        y = ((r3 * (yp - yc)) / r3p) + yc
        return x, y
    
    def find_xy_less(self, r1, r1p, xp, yp, xc, yc):
        x = ((r1 * (xp - xc)) / r1p) + xc
        y = ((r1 * (yp - yc)) / r1p) + yc
        return x, y
    
    def create_grid(self, r1, r2, r1p, b, h, w, xc, yc): # batched implementation
        Y = torch.arange(0, h).reshape(1, h, 1).repeat(b, 1, w).float().to(self.device)
        X = torch.arange(0, w).reshape(1, 1, w).repeat(b, h, 1).float().to(self.device)
        xcr = xc.reshape(-1, 1, 1).repeat(1, h, w).float()
        ycr = yc.reshape(-1, 1, 1).repeat(1, h, w).float()

        r3p = torch.sqrt(torch.square(X - xcr) + torch.square(Y - ycr)) #(b,h,w) #could be a source of nan
        r1r = r1.reshape(-1, 1, 1).repeat(1, h, w)
        r2r = r2.reshape(-1, 1, 1).repeat(1, h, w)
        r1pr = r1p.reshape(-1, 1, 1).repeat(1, h, w)

        no_change_cond = torch.where(r3p > r2r, 1, 0).unsqueeze(3).repeat(1,1,1,2).to(self.device)
        no_change = torch.cat((X.unsqueeze(3), Y.unsqueeze(3)), dim=3)
        xy_between_cond = torch.where(torch.logical_and(r3p >= r1pr, r3p <= r2r), 1, 0).unsqueeze(3).repeat(1,1,1,2).to(self.device)
        x_between, y_between = self.find_xy_between(r1r, r2r, r1pr, r3p, X, Y, xcr, ycr)
        xy_between = torch.cat((x_between.unsqueeze(3), y_between.unsqueeze(3)), dim=3).to(self.device)

        xy_less_cond = torch.where(r3p < r1pr, 1, 0).unsqueeze(3).repeat(1,1,1,2).to(self.device)
        x_less, y_less = self.find_xy_less(r1r, r1pr, X, Y, xcr, ycr)
        xy_less = torch.cat((x_less.unsqueeze(3), y_less.unsqueeze(3)), dim=3).to(self.device)

        grid = (no_change_cond * no_change + xy_between_cond * xy_between + xy_less_cond * xy_less).float().to(self.device)
        gridx = grid[:, :, :, 0]
        gridy = grid[:, :, :, 1]
        gridx = gridx / (w - 1)
        gridx = (gridx - 0.5) * 2
        gridy = gridy / (h - 1)
        gridy = (gridy - 0.5) * 2
        norm_grid = torch.stack([gridx, gridy], dim=-1).to(self.device)
        return norm_grid
    
    def linear_deform(self, image, input_pxyr, input_ixyr, alpha, mode='bilinear'):
        xc = ((input_pxyr[:, 0] + input_ixyr[:, 0])/2).float().reshape(-1,1).to(self.device)
        yc = ((input_pxyr[:, 1] + input_ixyr[:, 1])/2).float().reshape(-1,1).to(self.device)
        r1 = input_pxyr[:, 2].float().reshape(-1,1).to(self.device)
        r2 = input_ixyr[:, 2].float().reshape(-1,1).to(self.device)
        r1p = r2 * torch.tensor(alpha).unsqueeze(0).float().to(self.device)
        b, c, h, w = image.shape
        grid = self.create_grid(r1, r2, r1p, b, h, w, xc, yc)
        deformed_image = torch.nn.functional.grid_sample(image.to(self.device), grid, mode=mode)
        return deformed_image, grid

    @torch.inference_mode()
    def deform(self, image, image_mask, image_pxyr, image_ixyr, target_image, target_mask, target_pxyr, target_ixyr, device): #Update to remove mean and std and add it later, input processing is wrong here
        inp_t = torch.from_numpy(np.float32(image)).float().unsqueeze(0).unsqueeze(0)
    
        xform_inp_mask = self.transform(image_mask)
        xform_inp_mask[xform_inp_mask<0] = -1.0
        xform_inp_mask[xform_inp_mask>=0] = 1.0
        xform_inp_mask = xform_inp_mask.unsqueeze(0)
        
        image_pxyr = torch.tensor(image_pxyr).unsqueeze(0).float()
        image_ixyr = torch.tensor(image_ixyr).unsqueeze(0).float()
        
        tar_t = torch.from_numpy(np.float32(target_image)).float().unsqueeze(0).unsqueeze(0)
        
        xform_tar_mask = self.transform(target_mask)
        xform_tar_mask[xform_tar_mask<0] = -1.0
        xform_tar_mask[xform_tar_mask>=0] = 1.0
        xform_tar_mask = xform_tar_mask.unsqueeze(0)
        
        target_pxyr = torch.tensor(target_pxyr).unsqueeze(0).float()
        target_ixyr = torch.tensor(target_ixyr).unsqueeze(0).float()
        
        inp_polar_iris, inp_mask_polar_iris = self.sifLossModel.cartToPolIrisCenter(inp_t, ((xform_inp_mask+1)/2), image_pxyr, image_ixyr)
        sif_inp_iris = self.sifLossModel.getCodesCPU(inp_polar_iris)
        
        tar_polar_unrot, tar_mask_polar_unrot = self.sifLossModel.cartToPolIrisCenter(tar_t, ((xform_tar_mask+1)/2), target_pxyr, target_ixyr)
        sif_tar_unrot = self.sifLossModel.getCodesCPU(tar_polar_unrot)
        
        shift_tar = calculate_shift(sif_inp_iris, inp_mask_polar_iris, sif_tar_unrot, tar_mask_polar_unrot)
        
        del inp_polar_iris, inp_mask_polar_iris, sif_inp_iris
        del tar_polar_unrot, tar_mask_polar_unrot, sif_tar_unrot
        
        inp_t, xform_inp_mask, image_pxyr_new, image_ixyr_new = crop_iris(inp_t.to(device), xform_inp_mask.to(device), image_pxyr.to(device), image_ixyr.to(device))
        
        inp_img_mean = torch.mean(inp_t)
        inp_img_std = torch.std(inp_t)
        
        xform_inp = torch.clamp(torch.nan_to_num((inp_t - inp_img_mean)/inp_img_std, nan=4, posinf=4, neginf=-4), -4, 4)
        xform_inp = xform_inp
        
        tar_t, xform_tar_mask, target_pxyr_new, target_ixyr_new = rotate_tensor_batch(tar_t, xform_tar_mask, shift_tar, target_pxyr, target_ixyr)
        tar_t, xform_tar_mask, target_pxyr_new, target_ixyr_new = crop_iris(tar_t.to(device), xform_tar_mask.to(device), target_pxyr_new.to(device), target_ixyr_new.to(device))
        
        tar_img_mean = torch.mean(tar_t)
        tar_img_std = torch.std(tar_t)
        
        xform_tar = torch.clamp(torch.nan_to_num((tar_t - tar_img_mean)/tar_img_std, nan=4, posinf=4, neginf=-4), -4, 4)
        xform_tar = xform_tar

        target_alpha = (target_pxyr_new[:, 2]/target_ixyr_new[:, 2]).reshape(-1, 1).to(device)
        ld_in, _ = self.linear_deform(xform_inp, image_pxyr_new, image_ixyr_new, target_alpha)
        ld_in = ld_in.detach().requires_grad_(False).float()
        ld_in = torch.clamp(torch.nan_to_num(ld_in, nan=4, posinf=4, neginf=-4), -4, 4)
        
        out = self.net(torch.cat([xform_inp, ld_in, xform_tar_mask], 1))
        out_norm = ((out * tar_img_std) + tar_img_mean).to(device, non_blocking=True)
        out_image_np = torch.clamp(out_norm[0].clone().detach(), 0, 255).cpu().numpy()[0]
        out_image = Image.fromarray(np.uint8(out_image_np))

        tar_mask = np.uint8(torch.where(((xform_tar_mask+1)/2) < 0.5, 0, 255).cpu().numpy()[0][0])
        tar_mask_pil = Image.fromarray(tar_mask, 'L')
        
        return out_image
    
    @torch.inference_mode()
    def deform_with_hal_vis(self, image, image_mask, image_back_mask, image_pxyr, image_ixyr, target_image, target_mask, target_back_mask, target_pxyr, target_ixyr, device): #Update to remove mean and std and add it later, input processing is wrong here
        inp_t = torch.from_numpy(np.float32(image)).float().unsqueeze(0).unsqueeze(0)
    
        xform_inp_mask = self.transform(image_mask)
        xform_inp_mask[xform_inp_mask<0] = -1.0
        xform_inp_mask[xform_inp_mask>=0] = 1.0
        xform_inp_mask = xform_inp_mask.unsqueeze(0)

        inp_back_mask_t = torch.from_numpy(np.float32(image_back_mask)).float().unsqueeze(0).unsqueeze(0)
        
        image_pxyr = torch.tensor(image_pxyr).unsqueeze(0).float()
        image_ixyr = torch.tensor(image_ixyr).unsqueeze(0).float()
        
        tar_t = torch.from_numpy(np.float32(target_image)).float().unsqueeze(0).unsqueeze(0)
        
        xform_tar_mask = self.transform(target_mask)
        xform_tar_mask[xform_tar_mask<0] = -1.0
        xform_tar_mask[xform_tar_mask>=0] = 1.0
        xform_tar_mask = xform_tar_mask.unsqueeze(0)

        tar_back_mask_t = torch.from_numpy(np.float32(target_back_mask)).float().unsqueeze(0).unsqueeze(0)
        
        target_pxyr = torch.tensor(target_pxyr).unsqueeze(0).float()
        target_ixyr = torch.tensor(target_ixyr).unsqueeze(0).float()
        
        inp_polar_iris, inp_mask_polar_iris = self.sifLossModel.cartToPolIrisCenter(inp_t, ((xform_inp_mask+1)/2), image_pxyr, image_ixyr)
        sif_inp_iris = self.sifLossModel.getCodesCPU(inp_polar_iris)
        
        tar_polar_unrot, tar_mask_polar_unrot = self.sifLossModel.cartToPolIrisCenter(tar_t, ((xform_tar_mask+1)/2), target_pxyr, target_ixyr)
        sif_tar_unrot = self.sifLossModel.getCodesCPU(tar_polar_unrot)
        
        shift_tar = calculate_shift(sif_inp_iris, inp_mask_polar_iris, sif_tar_unrot, tar_mask_polar_unrot)
        
        del inp_polar_iris, inp_mask_polar_iris, sif_inp_iris
        del tar_polar_unrot, tar_mask_polar_unrot, sif_tar_unrot
        
        inp_t, xform_inp_mask, inp_back_mask_t, image_pxyr_new, image_ixyr_new = crop_iris_w_b(inp_t.to(device), xform_inp_mask.to(device), inp_back_mask_t.to(device), image_pxyr.to(device), image_ixyr.to(device))
        
        inp_img_mean = torch.mean(inp_t)
        inp_img_std = torch.std(inp_t)
        
        xform_inp = torch.clamp(torch.nan_to_num((inp_t - inp_img_mean)/inp_img_std, nan=4, posinf=4, neginf=-4), -4, 4)
        xform_inp = xform_inp
        
        tar_t, xform_tar_mask, tar_back_mask_t, target_pxyr_new, target_ixyr_new = rotate_tensor_batch_w_b(tar_t, xform_tar_mask, tar_back_mask_t, shift_tar, target_pxyr, target_ixyr)
        tar_t, xform_tar_mask, tar_back_mask_t, target_pxyr_new, target_ixyr_new = crop_iris_w_b(tar_t.to(device), xform_tar_mask.to(device), tar_back_mask_t.to(device), target_pxyr_new.to(device), target_ixyr_new.to(device))
        
        tar_img_mean = torch.mean(tar_t)
        tar_img_std = torch.std(tar_t)
        
        xform_tar = torch.clamp(torch.nan_to_num((tar_t - tar_img_mean)/tar_img_std, nan=4, posinf=4, neginf=-4), -4, 4)
        xform_tar = xform_tar

        target_alpha = (target_pxyr_new[:, 2]/target_ixyr_new[:, 2]).reshape(-1, 1).to(device)
        ld_in, _ = self.linear_deform(xform_inp, image_pxyr_new, image_ixyr_new, target_alpha)
        ld_in = ld_in.detach().requires_grad_(False).float()
        ld_in = torch.clamp(torch.nan_to_num(ld_in, nan=4, posinf=4, neginf=-4), -4, 4)
        
        out = self.net(torch.cat([xform_inp, ld_in, xform_tar_mask], 1))
        out_norm = ((out * tar_img_std) + tar_img_mean).to(device, non_blocking=True)
        out_image_np = np.uint8(torch.clamp(out_norm[0].clone().detach(), 0, 255).cpu().numpy()[0])

        out_image = Image.fromarray(out_image_np, 'L')

        inp_back_mask = np.uint8(inp_back_mask_t.cpu().numpy()[0][0])
        tar_back_mask = np.uint8(tar_back_mask_t.cpu().numpy()[0][0])
        tar_mask = np.uint8(torch.where(((xform_tar_mask+1)/2) < 0.5, 0, 255).cpu().numpy()[0][0])
        hal_mask = cv2.bitwise_and(tar_mask, cv2.bitwise_and(tar_back_mask, cv2.bitwise_not(inp_back_mask)))

        return out_image, hal_mask
    
    @torch.inference_mode()
    def deform_with_alpha(self, image, iris_mask, back_mask, image_pxyr, image_ixyr, target_alpha, device): #Update to remove mean and std and add it later, input processing is wrong here

        image_nocrop = image.copy()
        iris_mask_nocrop = iris_mask.copy()
        back_mask_nocrop = back_mask.copy()
        image_pxyr_nocrop = np.copy(image_pxyr)
        image_ixyr_nocrop = np.copy(image_ixyr)

        ir_ratio = 16/14
        border = int(round(ir_ratio*image_ixyr[2]))
        image = image.crop((image_ixyr[0] - border, image_ixyr[1] - border, image_ixyr[0] + border, image_ixyr[1] + border))
        iris_mask = iris_mask.crop((image_ixyr[0] - border, image_ixyr[1] - border, image_ixyr[0] + border, image_ixyr[1] + border))
        back_mask = back_mask.crop((image_ixyr[0] - border, image_ixyr[1] - border, image_ixyr[0] + border, image_ixyr[1] + border))
        w, h = image.size
        
        image_pxyr[0] = float(w)/2.0 + image_pxyr[0] - image_ixyr[0]
        image_pxyr[1] = float(h)/2.0 + image_pxyr[1] - image_ixyr[1]

        image_ixyr[0] = float(w)/2.0
        image_ixyr[1] = float(h)/2.0

        inp_alpha = image_pxyr[2] / image_ixyr[2]
        if inp_alpha == target_alpha:
            return image, image_nocrop
        else:
            
            inp_t = torch.from_numpy(np.float32(image)).float().unsqueeze(0).unsqueeze(0)
            inp_img_mean = torch.mean(inp_t)
            inp_img_std = torch.std(inp_t)
            
            image_pxyr_t = torch.tensor(image_pxyr).unsqueeze(0).float()
            image_ixyr_t = torch.tensor(image_ixyr).unsqueeze(0).float()
            
            xform_inp = torch.clamp(torch.nan_to_num((inp_t - inp_img_mean)/inp_img_std, nan=4, posinf=4, neginf=-4), -4, 4)
            xform_inp = xform_inp
            
            ld_in, _ = self.linear_deform(xform_inp, image_pxyr_t, image_ixyr_t, target_alpha)
            ld_in = ld_in.detach().requires_grad_(False).float()
            ld_in = torch.clamp(torch.nan_to_num(ld_in, nan=4, posinf=4, neginf=-4), -4, 4)

            xform_inp = Resize(256)(xform_inp).to(device)
            ld_in = Resize(256)(ld_in).to(device)
        
            if inp_alpha < target_alpha:
                w, h = iris_mask.size
                pupil_mask = create_circular_mask(h, w, center=(image_pxyr[0], image_pxyr[1]), radius=(target_alpha * image_ixyr[2]))
                target_mask_np = np.uint8(np.array(iris_mask) - pupil_mask)
            else:
                w, h = iris_mask.size
                pupil_fill_mask = create_circular_mask(h, w, center=(image_pxyr[0], image_pxyr[1]), radius=(image_pxyr[2] + ((image_ixyr[2] - image_pxyr[2])/4)))
                filled_iris_mask = cv2.bitwise_and(cv2.bitwise_or(np.uint8(iris_mask), np.uint8(pupil_fill_mask)), np.uint8(back_mask))
                pupil_mask = create_circular_mask(h, w, center=(image_pxyr[0], image_pxyr[1]), radius=(target_alpha * image_ixyr[2]))
                target_mask_np = np.uint8(np.array(filled_iris_mask) - pupil_mask)

            target_mask = Image.fromarray(target_mask_np, "L").resize((256, 256))
            #target_mask.save('target_mask_' + str(target_alpha) + '.png')
            xform_tar_mask = self.transform(target_mask)
            xform_tar_mask[xform_tar_mask<0] = -1.0
            xform_tar_mask[xform_tar_mask>=0] = 1.0
            xform_tar_mask = xform_tar_mask.unsqueeze(0).to(device)
            #print(xform_inp.shape, ld_in.shape, xform_tar_mask.shape)
            
            out = self.net(torch.cat([xform_inp, ld_in, xform_tar_mask], 1))
            out_norm = ((out * inp_img_std) + inp_img_mean).to(device, non_blocking=True)
            out_image_np = torch.clamp(out_norm[0].clone().detach(), 0, 255).cpu().numpy()[0]
            out_image = Image.fromarray(np.uint8(out_image_np))

            out_image_crop = out_image.copy()
            out_image = out_image.resize((round(ir_ratio * image_ixyr[2] * 2), round(ir_ratio * image_ixyr[2] * 2)))
            out_image_np = (np.array(out_image).astype(np.float32) - np.mean(np.array(out_image)))/np.std(np.array(out_image))
            iso_img = np.float32(image_nocrop.copy())
            iso_img_crop = iso_img[int(round(image_ixyr_nocrop[1] - ir_ratio * image_ixyr[2])) : int(round(image_ixyr_nocrop[1] + ir_ratio * image_ixyr[2])), int(round(image_ixyr_nocrop[0] - ir_ratio * image_ixyr[2])) : int(round(image_ixyr_nocrop[0] + ir_ratio * image_ixyr[2]))]
            iso_img_mean = np.mean(iso_img_crop)
            iso_img_std = np.std(iso_img_crop)
            out_image_np = (out_image_np * iso_img_std) + iso_img_mean
            w, h = iris_mask_nocrop.size
            pupil_fill_mask_nocrop = create_circular_mask(h, w, center=(image_pxyr_nocrop[0], image_pxyr_nocrop[1]), radius=(image_pxyr_nocrop[2] + ((image_ixyr_nocrop[2] - image_pxyr_nocrop[2])/4)))
            filled_iris_mask_nocrop = cv2.bitwise_and(cv2.bitwise_or(np.uint8(iris_mask_nocrop), np.uint8(pupil_fill_mask_nocrop)), np.uint8(back_mask_nocrop))
            inp_replace_mask = np.where(filled_iris_mask_nocrop >= 127.5, 1.0, 0.0)
            inp_replace_mask_inv = np.where(inp_replace_mask > 0.5, 0.0, 1.0)

            iris_img = np.zeros(iso_img.shape).astype(np.float32)
            up = int(round(image_ixyr_nocrop[1] - out_image_np.shape[0] / 2.0)) 
            down = out_image_np.shape[0] + up
            left = int(round(image_ixyr_nocrop[0] - out_image_np.shape[0] / 2.0))
            right = out_image_np.shape[1] + left
            iris_img[up:down, left:right] = out_image_np
            iso_img = iso_img * inp_replace_mask_inv + iris_img * inp_replace_mask
            iso_img_pil = Image.fromarray(np.uint8(np.clip(iso_img, 0, 255)), "L")
            out_image = iso_img_pil

            kernel = np.ones((3, 3), np.uint8)
            iris_mask = np.uint8(np.where(inp_replace_mask > 0.5, 255, 0))
            inp_replace_mask_erode = cv2.erode(iris_mask, kernel, iterations=1)
            inp_replace_mask_dilate = cv2.dilate(iris_mask, kernel, iterations=1)
            highlight_mask = inp_replace_mask_dilate - inp_replace_mask_erode

            return out_image_crop, out_image, highlight_mask
        
            
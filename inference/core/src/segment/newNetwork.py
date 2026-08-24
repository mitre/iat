#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

import os
from glob import glob 
from ntpath import basename 

import torch
from torch import nn
import torch.nn.functional as F

from .aspp import ASPP

class UNet(nn.Module):
    def __init__(
        self,
        in_channels=1,
        n_classes=2,
        depth=5,
        wf=4,
        padding=True,
        batch_norm=True,
    ):
        """
        Implementation of U-Net.
        https://discuss.pytorch.org/t/unet-implementation/426

        Args:
            in_channels (int): number of input channels
            n_classes (int): number of output channels
            depth (int): depth of the network
            wf (int): number of filters in the first layer is 2**wf
            padding (bool): if True, apply padding such that the input shape
                            is the same as the output.
                            This may introduce artifacts
            batch_norm (bool): Use BatchNorm after layers with an
                               activation function
        """
        super(UNet, self).__init__()
        self.padding = padding
        self.depth = depth
        prev_channels = in_channels

        # Down path
        self.down_path = nn.ModuleList()
        for i in range(depth):
            self.down_path.append(
                UNetConvBlock(prev_channels, 2 ** (wf + i), padding, batch_norm)
            )
            prev_channels = 2 ** (wf + i)

        # Up path
        self.up_path = nn.ModuleList()
        for i in reversed(range(depth - 1)): 
            self.up_path.append(
                UNetUpBlock(prev_channels, 2 ** (wf + i), padding, batch_norm)
            )
            prev_channels = 2 ** (wf + i)

        # Classification layer
        self.last = nn.Conv2d(prev_channels, n_classes, kernel_size=1)

        # Embedding layers 
        self.embed_layer = nn.Conv2d(256, 1, 1)

        # ASPP
        self.aspp = ASPP(256, 256)

    # @autocast()
    def forward(self, x):
        blocks = []
        for i, down in enumerate(self.down_path):
            x = down(x)
            if i != len(self.down_path) - 1:
                blocks.append(x)
                x = F.max_pool2d(x, 2)
        emb = x # torch.Size([4, 256, 30, 40])

        # Atrous convolutions
        emb = self.aspp(emb)
       
        for i, up in enumerate(self.up_path):
            x = up(x, blocks[-i - 1])
        output = self.last(x)
        return output
    
    def embed(self, x):
        blocks = []
        for i, down in enumerate(self.down_path):
            x = down(x)
            if i != len(self.down_path) - 1:
                blocks.append(x)
                x = F.max_pool2d(x, 2)
        emb = x # torch.Size([n, 256, 14, 14])

        # Atrous convolutions
        # emb = self.aspp(emb)

        # Embedding
        emb = self.embed_layer(emb)
        emb = torch.flatten(emb, start_dim=1)
        return emb


    def load_checkpoint(self, ckpt_dir, use_cuda):
        # Find most recent checkpoint
        model_files = glob(os.path.join(ckpt_dir, '*.pth'))
        print("using NEW network and model file:" + str(model_files))
        if len(model_files) == 0:
            return 0
        model_epochs = [int(basename(f)[:-4]) for f in model_files if 'stage' not in f]
        max_model_epoch = str(max(model_epochs)).zfill(3)
        ckpt_file = [f for f in model_files if max_model_epoch in f][0]
        
        # Load checkpoint
        if use_cuda:
            try:
                ckpt = torch.load(ckpt_file, map_location='cuda:0') 
            except:
                ckpt = torch.load(ckpt_file, map_location='cuda:1')
        else:
            ckpt = torch.load(ckpt_file, map_location='cpu')
        try:
            self.load_state_dict(ckpt, strict=False)
        except:
            ckpt = {k: v for k, v in ckpt.items() if 'last' not in k}
            self.load_state_dict(ckpt, strict=False)
        print("Loaded %s. Resuming training from %s." % (ckpt_file, max_model_epoch))

        # Return the resuming epoch number
        return int(max_model_epoch)


class UNetConvBlock(nn.Module):
    def __init__(self, in_size, out_size, padding, batch_norm):
        super(UNetConvBlock, self).__init__()
        block = []

        block.append(nn.Conv2d(in_size, out_size, kernel_size=3, padding=int(padding)))
        block.append(nn.ReLU())
        if batch_norm:
            block.append(nn.BatchNorm2d(out_size))

        block.append(nn.Conv2d(out_size, out_size, kernel_size=3, padding=int(padding)))
        block.append(nn.ReLU())
        if batch_norm:
            block.append(nn.BatchNorm2d(out_size))
            block.append(nn.Dropout(p=0.1))

        self.block = nn.Sequential(*block)

    # @autocast()
    def forward(self, x):
        out = self.block(x)
        return out


class UNetUpBlock(nn.Module):
    def __init__(self, in_size, out_size, padding, batch_norm):
        super(UNetUpBlock, self).__init__()
        self.up = nn.ConvTranspose2d(in_size, out_size, kernel_size=2, stride=2)
        self.conv_block = UNetConvBlock(in_size, out_size, padding, batch_norm)

    def center_crop(self, layer, target_size):
        _, _, layer_height, layer_width = layer.size()
        diff_y = (layer_height - target_size[0]) // 2
        diff_x = (layer_width - target_size[1]) // 2
        return layer[
            :, :, diff_y : (diff_y + target_size[0]), diff_x : (diff_x + target_size[1])
        ]

    # @autocast()
    def forward(self, x, bridge):
        up = self.up(x)
        crop1 = self.center_crop(bridge, up.shape[2:])
        out = torch.cat([up, crop1], 1)
        out = self.conv_block(out)

        return out


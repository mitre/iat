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
import numpy as np
from PIL import Image
from ntpath import basename 

import torch
import torchvision.transforms as tvt
import segment.data.info as info


def pil_loader(path, mode='L'):
    """
    Utility function for loading PIL Images.

    Params:
        path (str): path to image file
        mode (str): 'L' to load as grayscale, 'RGB' to load as rgb image
    """
    im = Image.open(path)
    return im.convert(mode)


def convert_color_to_class(x):
    """
    Utility function for converting 3D RGB segmentation array to a 
    2D array of segmentation labels.

    Params:
        x (PIL Image): RGB image of size (h,w,c). 
    Returns:
        y (PIL Image): Image of size (h,w) with RGB values converted to 
            segmentation labels, according to data.info
    """
    x = np.array(x)
    y = np.zeros((x.shape[:2]), dtype=np.int)

    for k, v in info.color_to_label_map.items():
        y[(x == k).all(axis=2)] = v

    y = Image.fromarray(np.uint8(y))
    return y


def convert_class_to_color(x):
    """
    Utility function for converting 2D array of segmentation labels to a 
    3D RGB segmentation array.

    Params:
        x (PIL Image): Image of size (h,w) of segmentation labels. 
    Returns:
        y (PIL Image): Image of size (h,w,c) with RGB values
            according to data.info
    """
    h, w = x.shape
    y = np.zeros((h,w,3))
            
    for (i, j), k in np.ndenumerate(x):
        y[i,j] = info.label_to_color_map[k]
    return y


class EyeSegTest(torch.utils.data.Dataset):
    def __init__(self, data_file, root_path, do_augment):
        """
        Dataset object.
        Performs data augmentation.

        For labeled images, `data_file` should be formatted as a text file with 
        one training pair per line. For example,
            path_to_image_file,path_to_segmentation_map
            path_to_image_file,path_to_segmentation_map
            ...

        For an evaluation file (no labels), replace the label entry with 'None':
            path_to_image_file,None
            path_to_image_file,None
            ...

        Params:
            data_file (str): text file containing paths to training images.
            root_path (str): base path prepended to paths from data_file
            do_augment (bool): flag to control data augmentation steps. 
                should be true for training, false for testing.
        """
        super(EyeSegTest, self).__init__()

        # Extract file paths from text file
        with open(data_file, 'r') as f:
            lines = f.readlines()
        files = [x.strip().split(',') for x in lines]

        # Prepend dataset root 
        self.has_labels = False
        self.files = []
        for (img_path, label_path) in files:
            img_path = os.path.join(root_path, img_path)
            if label_path != 'None':
                label_path = os.path.join(root_path, label_path)
                self.has_labels = True
            self.files.append("%s,%s" % (img_path, label_path))
        self.files = np.array(self.files)

        # Loading objects
        self.loader = pil_loader
        self.to_tensor = tvt.ToTensor()

        # Transformations
        self.do_augment = do_augment 
        self.color_jitter = tvt.ColorJitter(0.2, 0.2, 0.2, 0.2)
        self.random_affine_parameters = lambda: tvt.RandomAffine.get_params(
            degrees=(-30,30), 
            translate=[0.1,0.1],
            shears=None, 
            img_size=(info.width,info.height), 
            scale_ranges=[0.9,1.1]
        )
        self.h_flip = tvt.RandomHorizontalFlip(p=1.0)
        self.v_flip = tvt.RandomVerticalFlip(p=1.0)
    

    def augment(self, xs):
        """
        Performs same data augmentation on list of images.

        Params:
            xs (list[PIL Image]): list of images, either [img, label] or [img]
        Returns:
            ys (list[PIL Image]): list of augmented images
        """
        ys = []

        # Augmentation parameters
        do_h_flip = np.random.random() > 0.5
        do_v_flip = np.random.random() > 0.5
        angle, trans, scale, _ = self.random_affine_parameters()

        for i, x in enumerate(xs):
            # Apply color jitter
            # Only apply color jitter to images, not labels
            if i == 0:
                x = self.color_jitter(x)

            # Flip images
            x = self.h_flip(x) if do_h_flip else x
            x = self.v_flip(x) if do_v_flip else x
               
            # Affine transformation
            fillcolor = info.background_color if i == 0 else info.background_label
            x = tvt.functional.affine(x, angle, trans, scale, shear=0, fillcolor=fillcolor)

            ys.append(x)
        return ys
           

    def __getitem__(self, index):
        """
        Fetch one training pair from the dataset. 

        Returns:
            (image, label) pair if possible;
            otherwise (image)
        """
        # Load image 
        img_path, label_path = self.files[index].split(',')
        img_id = basename(img_path).split('.')[0]
        img = self.loader(img_path, mode='L')
        
        # Load label
        if self.has_labels:
            label = self.loader(label_path, mode='RGB')
            label = convert_color_to_class(label)
        
        # Augment
        if self.do_augment:
            if self.has_labels:
                img, label = self.augment([img, label])
                label = torch.from_numpy(np.int64(label)).squeeze()
            else:
                img = self.augment([img])[0]
        
        # Return
        img = self.to_tensor(img)
        if self.has_labels:
            label = torch.from_numpy(np.int64(label)).squeeze()
            return img, label
        return img_id, img


    def __len__(self):
        return len(self.files)


def create_dataloader(root_path, batch_size, data_file, do_train=False):
    """
    Create DataLoader.

    Params:
        root_path (str): base path prepended to paths from data_file
        batch_size (int): number of images per batch
        data_file (str): text file containing paths to training images.
        do_train (bool): flag specifying whether or not we're training
    """
    dataset = EyeSegTest(data_file, root_path, do_train)
    dataloader = torch.utils.data.DataLoader(dataset,
                      batch_size=batch_size,
                      shuffle=do_train, num_workers=2, drop_last=False)
    return dataloader
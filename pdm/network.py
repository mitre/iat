import torch
from torch import nn
import math

class ResBlockIN(nn.Module):
    def __init__(self, in_channels, middle_channels, out_channels, downsample=False):
        super(ResBlockIN,self).__init__()
        self.conv_res = nn.Sequential(
            nn.Conv2d(in_channels, out_channels, 1, stride=1, padding='same', bias=False),
            nn.InstanceNorm2d(out_channels)
        )
        self.downsample = downsample
        if downsample:
            self.net_downsample = nn.Sequential(
                nn.Conv2d(in_channels, middle_channels, 4, stride=2, padding=1, bias=False),
                nn.InstanceNorm2d(middle_channels),
                nn.GELU(),
                nn.Conv2d(middle_channels, out_channels, 3, stride=1, padding='same', bias=False),
                nn.InstanceNorm2d(out_channels)
            )
        else:
            self.net = nn.Sequential(
                nn.Conv2d(in_channels, middle_channels, 3, stride=1, padding='same', bias=False),
                nn.InstanceNorm2d(middle_channels),
                nn.GELU(),
                nn.Conv2d(middle_channels, out_channels, 3, stride=1, padding='same', bias=False),
                nn.InstanceNorm2d(out_channels)
            )
        self.act = nn.GELU()
    def forward(self, x):
        res = self.conv_res(x)
        if self.downsample:
            x = self.net_downsample(x)
            res = nn.functional.interpolate(res, scale_factor=0.5)
        else:
            x = self.net(x)
        x = (x + res) * (1 / math.sqrt(2))
        x = self.act(x)
        return x
    
class VectorAndPatchDiscriminatorIN(nn.Module):
    def __init__(self, in_channels=1, width=64, vec_size=2048):
        super().__init__()
        self.model = nn.Sequential(
            ResBlockIN(in_channels, width, width, downsample=True), #128  / 32x256
            ResBlockIN(width, 2*width, 2*width, downsample=True), #64  / 16x128
            ResBlockIN(2*width, 4*width, 4*width, downsample=True), #32  / 8x64
            ResBlockIN(4*width, 8*width, 8*width, downsample=True), #16 / 4x32
            nn.ZeroPad2d((1, 0, 1, 0))
        )
        self.vector_head = nn.Sequential(
            nn.Conv2d(8*width, width, 4, padding=1, bias=False),
            nn.InstanceNorm2d(width),
            nn.GELU(),
            nn.Flatten(),
            nn.LazyLinear(vec_size)
        )
        self.realfake_head = nn.Conv2d(8*width, 1, 4, padding=1, bias=False)

    def forward(self, img):
        # Concatenate image and condition image by channels to produce input
        feat = self.model(img)
        return self.vector_head(feat), self.realfake_head(feat)

class fclayer(nn.Module):
    def __init__(self, in_h = 4, in_w = 32, out_n = 2048):
        super().__init__()
        self.in_h = in_h
        self.in_w = in_w
        self.out_n = out_n
        self.fc_list = []
        for i in range(out_n):
            self.fc_list.append(nn.Linear(in_h*in_w, 1))
        self.fc_list = nn.ModuleList(self.fc_list)
    def forward(self, x):
        x = x.reshape(-1, self.out_n, self.in_h, self.in_w)
        outs = []
        for i in range(self.out_n):
            outs.append(self.fc_list[i](x[:, i, :, :].reshape(-1, self.in_h*self.in_w)))
        out = torch.cat(outs, 1)
        return out

class VectorAndPatchDiscriminatorINv2(nn.Module):
    def __init__(self, in_channels=1, width=128, vec_size=2048):
        super().__init__()
        self.model = nn.Sequential(
            ResBlockIN(in_channels, width, width, downsample=True), # 32x256
            ResBlockIN(width, 2*width, 2*width, downsample=True), # 16x128
            ResBlockIN(2*width, 4*width, 4*width, downsample=True), # 8x64
            ResBlockIN(4*width, 8*width, 8*width, downsample=True) # 4x32
        )
        self.vector_head = nn.Sequential(
            ResBlockIN(8*width, vec_size, vec_size),
            fclayer(in_h=4, in_w=32, out_n=vec_size)
        )
        self.realfake_head = nn.Sequential(
            nn.ZeroPad2d((1, 0, 1, 0)),
            nn.Conv2d(8*width, 1, 4, padding=1, bias=False)
        )
        
    def forward(self, img):
        # Concatenate image and condition image by channels to produce input
        feat = self.model(img)
        return self.vector_head(feat), self.realfake_head(feat)

class AttBlockIN(nn.Module):
    def __init__(self,F_g,F_l,F_int):
        super(AttBlockIN,self).__init__()
        self.W_g = nn.Sequential(
            nn.Conv2d(F_g, F_int, kernel_size=1,stride=1,padding=0,bias=True),
            nn.InstanceNorm2d(F_int)
        )
        
        self.W_x = nn.Sequential(
            nn.Conv2d(F_l, F_int, kernel_size=1,stride=1,padding=0,bias=True),
            nn.InstanceNorm2d(F_int)
        )

        self.psi = nn.Sequential(
            nn.Conv2d(F_int, 1, kernel_size=1,stride=1,padding=0,bias=True),
            nn.InstanceNorm2d(1),
            nn.Sigmoid()
        )
        
        self.act = nn.GELU()
        
    def forward(self,g,x):
        g1 = self.W_g(g)
        x1 = self.W_x(x)
        psi = self.act(g1+x1)
        psi = self.psi(psi)

        return x*psi
        
class NestedAttentionResUNetIN(nn.Module):
    def __init__(self, num_classes, num_channels, width=32, resolution=(256, 256)):
        super().__init__()
        self.resolution = resolution
        nb_filter = [width, width*2, width*4, width*8, width*16]

        self.pool = nn.PixelUnshuffle(2)
        self.up = nn.Upsample(scale_factor=2, mode='bilinear')

        self.conv0_0 = ResBlockIN(num_channels, nb_filter[0], nb_filter[0])
        self.conv1_0 = ResBlockIN(nb_filter[0]*4, nb_filter[1], nb_filter[1])
        self.conv2_0 = ResBlockIN(nb_filter[1]*4, nb_filter[2], nb_filter[2])
        self.conv3_0 = ResBlockIN(nb_filter[2]*4, nb_filter[3], nb_filter[3])
        self.conv4_0 = ResBlockIN(nb_filter[3]*4, nb_filter[4], nb_filter[4])

        self.conv0_1 = ResBlockIN(nb_filter[0]+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_1 = AttBlockIN(nb_filter[1], nb_filter[0], nb_filter[0])
        self.conv1_1 = ResBlockIN(nb_filter[1]+nb_filter[2], nb_filter[1], nb_filter[1])
        self.att1_1 = AttBlockIN(nb_filter[2], nb_filter[1], nb_filter[1])
        self.conv2_1 = ResBlockIN(nb_filter[2]+nb_filter[3], nb_filter[2], nb_filter[2])
        self.att2_1 = AttBlockIN(nb_filter[3], nb_filter[2], nb_filter[2])
        self.conv3_1 = ResBlockIN(nb_filter[3]+nb_filter[4], nb_filter[3], nb_filter[3])
        self.att3_1 = AttBlockIN(nb_filter[4], nb_filter[3], nb_filter[3])

        self.conv0_2 = ResBlockIN(nb_filter[0]*2+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_2 = AttBlockIN(nb_filter[1], nb_filter[0]*2, nb_filter[0])
        self.conv1_2 = ResBlockIN(nb_filter[1]*2+nb_filter[2], nb_filter[1], nb_filter[1])
        self.att1_2 = AttBlockIN(nb_filter[2], nb_filter[1]*2, nb_filter[1])
        self.conv2_2 = ResBlockIN(nb_filter[2]*2+nb_filter[3], nb_filter[2], nb_filter[2])
        self.att2_2 = AttBlockIN(nb_filter[3], nb_filter[2]*2, nb_filter[2])

        self.conv0_3 = ResBlockIN(nb_filter[0]*3+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_3 = AttBlockIN(nb_filter[1], nb_filter[0]*3, nb_filter[0])
        self.conv1_3 = ResBlockIN(nb_filter[1]*3+nb_filter[2], nb_filter[1], nb_filter[1])
        self.att1_3 = AttBlockIN(nb_filter[2], nb_filter[1]*3, nb_filter[1])

        self.conv0_4 = ResBlockIN(nb_filter[0]*4+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_4 = AttBlockIN(nb_filter[1], nb_filter[0]*4, nb_filter[0])

        self.final = nn.Conv2d(nb_filter[0]*4, num_classes, kernel_size=1)

    def forward(self, input):
        
        x0_0 = self.conv0_0(input) #320x240
        x1_0 = self.conv1_0(self.pool(x0_0)) #160x120
        x0_0 = self.att0_1(g=self.up(x1_0), x=x0_0)
        x0_1 = self.conv0_1(torch.cat([x0_0, self.up(x1_0)], 1))

        x2_0 = self.conv2_0(self.pool(x1_0)) #80x60
        
        x1_0 = self.att1_1(g=self.up(x2_0), x=x1_0)
        x1_1 = self.conv1_1(torch.cat([x1_0, self.up(x2_0)], 1))
        
        x0_01 = self.att0_2(g=self.up(x1_1), x=torch.cat([x0_0, x0_1], 1))
        x0_2 = self.conv0_2(torch.cat([x0_01, self.up(x1_1)], 1))

        x3_0 = self.conv3_0(self.pool(x2_0)) #40x30
        x2_0 = self.att2_1(g=self.up(x3_0), x=x2_0)
        x2_1 = self.conv2_1(torch.cat([x2_0, self.up(x3_0)], 1))
        x1_01 = self.att1_2(g=self.up(x2_1), x=torch.cat([x1_0, x1_1], 1))
        x1_2 = self.conv1_2(torch.cat([x1_01, self.up(x2_1)], 1))
        x0_012 = self.att0_3(g=self.up(x1_2), x=torch.cat([x0_0, x0_1, x0_2], 1))
        x0_3 = self.conv0_3(torch.cat([x0_012, self.up(x1_2)], 1))

        x4_0 = self.conv4_0(self.pool(x3_0)) #20x15
        x3_0 = self.att3_1(g=self.up(x4_0), x=x3_0)
        x3_1 = self.conv3_1(torch.cat([x3_0, self.up(x4_0)], 1))
        x2_01 = self.att2_2(g=self.up(x3_1), x=torch.cat([x2_0, x2_1], 1))
        x2_2 = self.conv2_2(torch.cat([x2_01, self.up(x3_1)], 1))
        x1_012 = self.att1_3(g=self.up(x2_2), x=torch.cat([x1_0, x1_1, x1_2], 1))
        x1_3 = self.conv1_3(torch.cat([x1_012, self.up(x2_2)], 1))
        x0_0123 = self.att0_4(g=self.up(x1_3), x=torch.cat([x0_0, x0_1, x0_2, x0_3], 1))
        x0_4 = self.conv0_4(torch.cat([x0_0123, self.up(x1_3)], 1))
        
        output = self.final(torch.cat([x0_1, x0_2, x0_3, x0_4], 1))
        
        return output
    
class SharedAtrousConv2d(nn.Module):
    def __init__(self, in_channels, out_channels, kernel_size=3, stride=1, bias=True, padding='same'):
        super().__init__()
        self.weights = nn.Parameter(torch.rand(int(out_channels/2), in_channels, kernel_size, kernel_size))
        self.stride = stride
        self.kernel_size = kernel_size
        self.padding = padding
        nn.init.xavier_uniform_(self.weights)
        if bias:
            self.bias1 = nn.Parameter(torch.zeros(int(out_channels/2)))
            self.bias2 = nn.Parameter(torch.zeros(int(out_channels/2)))
        else:
            self.bias1 = None
            self.bias2 = None
    def forward(self, x):
        b, c, h, w = x.shape
        
        if self.padding == 'same':
            pad_val1_h = int(((h - 1)*self.stride - h + self.kernel_size) / 2)
            pad_val1_w = int(((w - 1)*self.stride - w + self.kernel_size) / 2)
            pad_val2_h = int(((h - 1)*self.stride - h + 2 * self.kernel_size - 1) / 2)
            pad_val2_w = int(((w - 1)*self.stride - w + 2 * self.kernel_size - 1) / 2)
        elif self.padding == 'valid':
            pad_val1_h = 0
            pad_val1_w = 0
            pad_val2_h = 0
            pad_val2_w = 0
        
        x1 = nn.functional.conv2d(x, self.weights, stride=self.stride, padding=(pad_val1_h, pad_val1_w), bias=self.bias1)
        x2 = nn.functional.conv2d(x, self.weights, stride=self.stride, padding=(pad_val2_h, pad_val2_w), dilation=2, bias=self.bias2)
        x3 = torch.cat([x1, x2], 1)
        return x3

class SharedAtrousResBlock(nn.Module):
    def __init__(self, in_channels, middle_channels, out_channels):
        super().__init__()
        self.conv_res = nn.Sequential(
            nn.Conv2d(in_channels, out_channels, 1, stride=1, bias=False),
            nn.BatchNorm2d(out_channels)
        )
        self.net = nn.Sequential(
            SharedAtrousConv2d(in_channels, middle_channels, bias=False),
            nn.BatchNorm2d(middle_channels),
            nn.ReLU(inplace=True),
            SharedAtrousConv2d(middle_channels, out_channels, bias=False),
            nn.BatchNorm2d(out_channels)
        )
        self.relu = nn.ReLU(inplace=True)
    def forward(self, x):
        res = self.conv_res(x)
        x = self.net(x)
        x = (x + res) * (1 / math.sqrt(2))
        x = self.relu(x)
        return x
    
class Resize(nn.Module):
    def __init__(self, size=None, scale_factor=None, mode='nearest', align_corners=None, recompute_scale_factor=None, antialias=False):
        super().__init__()
        self.size = size
        self.scale_factor = scale_factor
        self.mode = mode
        self.align_corners = align_corners
        self.recompute_scale_factor = recompute_scale_factor
        self.antialias = antialias
    def forward(self, x):
        return torch.nn.functional.interpolate(x, size=self.size, scale_factor=self.scale_factor, mode=self.mode, align_corners=self.align_corners, recompute_scale_factor=self.recompute_scale_factor, antialias=self.antialias)
        
class NestedSharedAtrousResUNet(nn.Module):
    def __init__(self, num_classes, num_channels, width=32, resolution=(240, 320)):
        super().__init__()
        self.resolution = resolution
        nb_filter = [width, width*2, width*4, width*8, width*16]

        self.pool = Resize(scale_factor=0.5, mode='bilinear')
        self.up = Resize(scale_factor=2, mode='bilinear')

        self.conv0_0 = SharedAtrousResBlock(num_channels, nb_filter[0], nb_filter[0])
        self.conv1_0 = SharedAtrousResBlock(nb_filter[0], nb_filter[1], nb_filter[1])
        self.conv2_0 = SharedAtrousResBlock(nb_filter[1], nb_filter[2], nb_filter[2])
        self.conv3_0 = SharedAtrousResBlock(nb_filter[2], nb_filter[3], nb_filter[3])
        self.conv4_0 = SharedAtrousResBlock(nb_filter[3], nb_filter[4], nb_filter[4])

        self.conv0_1 = SharedAtrousResBlock(nb_filter[0]+nb_filter[1], nb_filter[0], nb_filter[0])
        self.conv1_1 = SharedAtrousResBlock(nb_filter[1]+nb_filter[2], nb_filter[1], nb_filter[1])
        self.conv2_1 = SharedAtrousResBlock(nb_filter[2]+nb_filter[3], nb_filter[2], nb_filter[2])
        self.conv3_1 = SharedAtrousResBlock(nb_filter[3]+nb_filter[4], nb_filter[3], nb_filter[3])

        self.conv0_2 = SharedAtrousResBlock(nb_filter[0]*2+nb_filter[1], nb_filter[0], nb_filter[0])
        self.conv1_2 = SharedAtrousResBlock(nb_filter[1]*2+nb_filter[2], nb_filter[1], nb_filter[1])
        self.conv2_2 = SharedAtrousResBlock(nb_filter[2]*2+nb_filter[3], nb_filter[2], nb_filter[2])

        self.conv0_3 = SharedAtrousResBlock(nb_filter[0]*3+nb_filter[1], nb_filter[0], nb_filter[0])
        self.conv1_3 = SharedAtrousResBlock(nb_filter[1]*3+nb_filter[2], nb_filter[1], nb_filter[1])

        self.conv0_4 = SharedAtrousResBlock(nb_filter[0]*4+nb_filter[1], nb_filter[0], nb_filter[0])

        self.final = nn.Conv2d(nb_filter[0]*4, num_classes, kernel_size=1)

    def forward(self, input):
        
        x0_0 = self.conv0_0(input) #320x240
        x1_0 = self.conv1_0(self.pool(x0_0)) #160x120
        x0_1 = self.conv0_1(torch.cat([x0_0, self.up(x1_0)], 1))

        x2_0 = self.conv2_0(self.pool(x1_0)) #80x60
        x1_1 = self.conv1_1(torch.cat([x1_0, self.up(x2_0)], 1))
        x0_2 = self.conv0_2(torch.cat([x0_0, x0_1, self.up(x1_1)], 1))

        x3_0 = self.conv3_0(self.pool(x2_0)) #40x30
        x2_1 = self.conv2_1(torch.cat([x2_0, self.up(x3_0)], 1))
        x1_2 = self.conv1_2(torch.cat([x1_0, x1_1, self.up(x2_1)], 1))
        x0_3 = self.conv0_3(torch.cat([x0_0, x0_1, x0_2, self.up(x1_2)], 1))

        x4_0 = self.conv4_0(self.pool(x3_0)) #20x15
        x3_1 = self.conv3_1(torch.cat([x3_0, self.up(x4_0)], 1))
        x2_2 = self.conv2_2(torch.cat([x2_0, x2_1, self.up(x3_1)], 1))
        x1_3 = self.conv1_3(torch.cat([x1_0, x1_1, x1_2, self.up(x2_2)], 1))
        x0_4 = self.conv0_4(torch.cat([x0_0, x0_1, x0_2, x0_3, self.up(x1_3)], 1))
        
        output = self.final(torch.cat([x0_1, x0_2, x0_3, x0_4], 1))
        
        return output

class SharedAtrousResBlockIN(nn.Module):
    def __init__(self, in_channels, middle_channels, out_channels, downsample=False):
        super(SharedAtrousResBlockIN,self).__init__()
        self.conv_res = nn.Sequential(
            nn.Conv2d(in_channels, out_channels, 1, stride=1, padding='same', bias=False),
            nn.InstanceNorm2d(out_channels)
        )
        self.downsample = downsample
        if downsample:
            self.net_downsample = nn.Sequential(
                SharedAtrousConv2d(in_channels, middle_channels, kernel_size=4, stride=2, bias=False),
                nn.InstanceNorm2d(middle_channels),
                nn.GELU(),
                SharedAtrousConv2d(middle_channels, out_channels, kernel_size=3, stride=1, bias=False),
                nn.InstanceNorm2d(out_channels)
            )
        else:
            self.net = nn.Sequential(
                SharedAtrousConv2d(in_channels, middle_channels, kernel_size=3, stride=1, bias=False),
                nn.InstanceNorm2d(middle_channels),
                nn.GELU(),
                SharedAtrousConv2d(middle_channels, out_channels, kernel_size=3, stride=1, bias=False),
                nn.InstanceNorm2d(out_channels)
            )
        self.act = nn.GELU()
    def forward(self, x):
        res = self.conv_res(x)
        if self.downsample:
            x = self.net_downsample(x)
            res = nn.functional.interpolate(res, scale_factor=0.5)
        else:
            x = self.net(x)
        x = (x + res) * (1 / math.sqrt(2))
        x = self.act(x)
        return x   

class AttBlockIN(nn.Module):
    def __init__(self,F_g,F_l,F_int):
        super(AttBlockIN,self).__init__()
        self.W_g = nn.Sequential(
            nn.Conv2d(F_g, F_int, kernel_size=1,stride=1,padding=0,bias=True),
            nn.InstanceNorm2d(F_int)
        )
        
        self.W_x = nn.Sequential(
            nn.Conv2d(F_l, F_int, kernel_size=1,stride=1,padding=0,bias=True),
            nn.InstanceNorm2d(F_int)
        )

        self.psi = nn.Sequential(
            nn.Conv2d(F_int, 1, kernel_size=1,stride=1,padding=0,bias=True),
            nn.InstanceNorm2d(1),
            nn.Sigmoid()
        )
        
        self.act = nn.GELU()
        
    def forward(self,g,x):
        g1 = self.W_g(g)
        x1 = self.W_x(x)
        psi = self.act(g1+x1)
        psi = self.psi(psi)

        return x*psi

class NestedSharedAtrousAttentionResUNetIN(nn.Module):
    def __init__(self, num_classes, num_channels, width=32, resolution=(240, 320)):
        super().__init__()
        self.resolution = resolution
        nb_filter = [width, width*2, width*4, width*8, width*16]

        self.pool = Resize(scale_factor=0.5, mode='bilinear')
        self.up = Resize(scale_factor=2, mode='bilinear')

        self.conv0_0 = SharedAtrousResBlockIN(num_channels, nb_filter[0], nb_filter[0])
        self.conv1_0 = SharedAtrousResBlockIN(nb_filter[0], nb_filter[1], nb_filter[1])
        self.conv2_0 = SharedAtrousResBlockIN(nb_filter[1], nb_filter[2], nb_filter[2])
        self.conv3_0 = SharedAtrousResBlockIN(nb_filter[2], nb_filter[3], nb_filter[3])
        self.conv4_0 = SharedAtrousResBlockIN(nb_filter[3], nb_filter[4], nb_filter[4])

        self.conv0_1 = SharedAtrousResBlockIN(nb_filter[0]+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_1 = AttBlockIN(nb_filter[1], nb_filter[0], nb_filter[0])
        self.conv1_1 = SharedAtrousResBlockIN(nb_filter[1]+nb_filter[2], nb_filter[1], nb_filter[1])
        self.att1_1 = AttBlockIN(nb_filter[2], nb_filter[1], nb_filter[1])
        self.conv2_1 = SharedAtrousResBlockIN(nb_filter[2]+nb_filter[3], nb_filter[2], nb_filter[2])
        self.att2_1 = AttBlockIN(nb_filter[3], nb_filter[2], nb_filter[2])
        self.conv3_1 = SharedAtrousResBlockIN(nb_filter[3]+nb_filter[4], nb_filter[3], nb_filter[3])
        self.att3_1 = AttBlockIN(nb_filter[4], nb_filter[3], nb_filter[3])

        self.conv0_2 = SharedAtrousResBlockIN(nb_filter[0]*2+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_2 = AttBlockIN(nb_filter[1], nb_filter[0]*2, nb_filter[0])
        self.conv1_2 = SharedAtrousResBlockIN(nb_filter[1]*2+nb_filter[2], nb_filter[1], nb_filter[1])
        self.att1_2 = AttBlockIN(nb_filter[2], nb_filter[1]*2, nb_filter[1])
        self.conv2_2 = ResBlockIN(nb_filter[2]*2+nb_filter[3], nb_filter[2], nb_filter[2])
        self.att2_2 = AttBlockIN(nb_filter[3], nb_filter[2]*2, nb_filter[2])

        self.conv0_3 = SharedAtrousResBlockIN(nb_filter[0]*3+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_3 = AttBlockIN(nb_filter[1], nb_filter[0]*3, nb_filter[0])
        self.conv1_3 = SharedAtrousResBlockIN(nb_filter[1]*3+nb_filter[2], nb_filter[1], nb_filter[1])
        self.att1_3 = AttBlockIN(nb_filter[2], nb_filter[1]*3, nb_filter[1])

        self.conv0_4 = SharedAtrousResBlockIN(nb_filter[0]*4+nb_filter[1], nb_filter[0], nb_filter[0])
        self.att0_4 = AttBlockIN(nb_filter[1], nb_filter[0]*4, nb_filter[0])

        self.final = nn.Conv2d(nb_filter[0]*5, num_classes, kernel_size=1)

    def forward(self, input):
        
        x0_0 = self.conv0_0(input) #320x240
        x1_0 = self.conv1_0(self.pool(x0_0)) #160x120
        x0_0 = self.att0_1(g=self.up(x1_0), x=x0_0)
        x0_1 = self.conv0_1(torch.cat([x0_0, self.up(x1_0)], 1))

        x2_0 = self.conv2_0(self.pool(x1_0)) #80x60
        
        x1_0 = self.att1_1(g=self.up(x2_0), x=x1_0)
        x1_1 = self.conv1_1(torch.cat([x1_0, self.up(x2_0)], 1))
        
        x0_01 = self.att0_2(g=self.up(x1_1), x=torch.cat([x0_0, x0_1], 1))
        x0_2 = self.conv0_2(torch.cat([x0_01, self.up(x1_1)], 1))

        x3_0 = self.conv3_0(self.pool(x2_0)) #40x30
        x2_0 = self.att2_1(g=self.up(x3_0), x=x2_0)
        x2_1 = self.conv2_1(torch.cat([x2_0, self.up(x3_0)], 1))
        x1_01 = self.att1_2(g=self.up(x2_1), x=torch.cat([x1_0, x1_1], 1))
        x1_2 = self.conv1_2(torch.cat([x1_01, self.up(x2_1)], 1))
        x0_012 = self.att0_3(g=self.up(x1_2), x=torch.cat([x0_0, x0_1, x0_2], 1))
        x0_3 = self.conv0_3(torch.cat([x0_012, self.up(x1_2)], 1))

        x4_0 = self.conv4_0(self.pool(x3_0)) #20x15
        x3_0 = self.att3_1(g=self.up(x4_0), x=x3_0)
        x3_1 = self.conv3_1(torch.cat([x3_0, self.up(x4_0)], 1))
        x2_01 = self.att2_2(g=self.up(x3_1), x=torch.cat([x2_0, x2_1], 1))
        x2_2 = self.conv2_2(torch.cat([x2_01, self.up(x3_1)], 1))
        x1_012 = self.att1_3(g=self.up(x2_2), x=torch.cat([x1_0, x1_1, x1_2], 1))
        x1_3 = self.conv1_3(torch.cat([x1_012, self.up(x2_2)], 1))
        x0_0123 = self.att0_4(g=self.up(x1_3), x=torch.cat([x0_0, x0_1, x0_2, x0_3], 1))
        x0_4 = self.conv0_4(torch.cat([x0_0123, self.up(x1_3)], 1))
        
        output = self.final(torch.cat([x0_0, x0_1, x0_2, x0_3, x0_4], 1))
        
        return output
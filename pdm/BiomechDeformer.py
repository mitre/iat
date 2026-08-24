import torch
from torch.nn.functional import grid_sample
from torchvision.transforms import ToTensor
import numpy as np
import torch
import torch.nn as nn
from PIL import Image
import math

class conv(nn.Module):
    def __init__(self, in_channels=512, out_n = 6):
        super().__init__()
        self.conv = nn.Conv2d(in_channels, out_n, kernel_size=1, stride=1, padding='same')
    def forward(self, x):
        x = self.conv(x)
        return x

class fclayer(nn.Module):
    def __init__(self, in_h = 8, in_w = 10, out_n = 6):
        super().__init__()
        self.in_h = in_h
        self.in_w = in_w
        self.out_n = out_n
        self.fc_list = []
        for i in range(out_n):
            self.fc_list.append(nn.Linear(in_h*in_w, 1))
        self.fc_list = nn.ModuleList(self.fc_list)
    def forward(self, x):
        x = x.reshape(-1, 6, self.in_h, self.in_w)
        outs = []
        for i in range(self.out_n):
            outs.append(self.fc_list[i](x[:, i, :, :].reshape(-1, self.in_h*self.in_w)))
        out = torch.cat(outs, 1)
        return out

class BiomechDeformer(object):
    def __init__(self, device=torch.device('cpu')):
        self.device = device
    
    def quadSolve(self, A, B, C):
        return ((-1 * B - torch.sqrt(B * B - 4 * A * C)) / (2 * A))

    def getAngle(self, y, x):
        with torch.no_grad():
            theta = torch.atan2(y, x)
            beta = theta + 2 * math.pi
            return torch.where(beta < 2 * math.pi, beta, beta - 2 * math.pi)

    def find_xy_less(self, r1, r1p, xp, yp, xc, yc):
        x = ((r1 * (xp - xc)) / r1p) + xc
        y = ((r1 * (yp - yc)) / r1p) + yc
        return x, y

    def Biomech(self, ri0, ri1, numVertices):
    
        ro = .006
        
        vertices = np.linspace((ri1/ro),(ro/ro),numVertices)
        h = vertices[2]-vertices[1]
        
        elements = []
        
        boundaries = np.zeros((len(vertices), len(vertices)))
        
        Er = 4000
        Et = 2970
        nu = 0.49 # The Poisson variable.
        
        zeta = Et / Er

        eta = Er / (1 - zeta * (nu ** 2))

        chi = Et / (1 - zeta * (nu ** 2))
        
        U = np.zeros((len(vertices),1))
        U_linear = U
        
        resThresh = 1e-7
        
        eta2 = ri1/ro #Normalized final radius. This is different from the eta with the material parameters. 
        lamda = (ri1-ri0)/ro #This might be need to be modified to incorporate the various changes. This isn't quite fully the equation.
        
        for q in range(100):
            K = np.zeros((len(vertices), len(vertices)))
            K[0,0] = 1
            K[len(vertices)-1,len(vertices)-1] = 1
            
            f = np.zeros((len(vertices),1))
            f[0] = 1
            f[len(vertices)-1] = 0
            
            for i in range(1, len(vertices)-1):
                Umid = np.array([(U[i-1]+U[i])/2, (U[i]+U[i+1])/2])
                dU_dr = np.array([(U[i]-U[i-1])/(h), (U[i+1]-U[i])/(h)])
                
                for j in range(i-1, i+2):
                
                    start = max(i-1,j-1)
                    stop = min(i,j)
                    if start <= stop:
                        step = 1
                    else:
                        step = -1
                    for k in range(start, stop+1, step):
                                        
                        basis_i = 0.5
                        basis_j = 0.5
    
                        if k == i-1:
                            dbi_dr = 1/h
                        else:
                            dbi_dr = -1/h
    
                        if k == j-1:
                            dbj_dr = 1/h
                        else:
                            dbj_dr = -1/h
    
                        r = vertices[k]+h/2
    
                        k_local = k - (i-1)
    
                        omega = (nu * eta * zeta - chi) / r
                        psi = (eta - nu * chi) / r
    
                        alpha = eta / r
    
                        beta = -chi / (r * r)
    
                        Kijk = basis_i * basis_j * (Umid[k_local] * (lamda*(nu+1)*zeta/(2*r**3))  - zeta/r**2) + \
                                basis_i * dbj_dr  * (dU_dr[k_local]*(-(1-nu*zeta)/(2*r)) - Umid[k_local] * (nu*zeta*lamda/r**2) + 1/r) + \
                                dbi_dr  * dbj_dr  * (-1 + lamda/2*dU_dr[k_local])
                        K[i,j] = K[i,j] + h * Kijk
            
            res = np.linalg.norm(K @ U - f) 
            if (res < resThresh):
                break
            
            Unew = np.linalg.inv(K) @ f
            U = U + (Unew - U)
            
            if np.linalg.norm(U_linear) == 0:
                U_linear = U
        
        UR = U * (ri1 - ri0)
        R = np.linspace(ri0, ro, numVertices) + UR.reshape(numVertices)
        normR = (R - R.min()) / (R.max() - R.min())        
        return normR

    def do_biomech_deform(self, image, mask, pupil_xyr, iris_xyr, target_alpha, width, height):
        with torch.no_grad():
            pupil_xyr = torch.tensor(pupil_xyr).float().to(self.device)
            iris_xyr = torch.tensor(iris_xyr).float().to(self.device)

            polar_height = round(math.sqrt((pupil_xyr[0].item() - iris_xyr[0].item()) ** 2 + (pupil_xyr[1].item() - iris_xyr[1].item()) ** 2) + (iris_xyr[2].item() - pupil_xyr[2].item()))
            polar_width = round(2 * math.pi * iris_xyr[2].item())

            theta = (2*math.pi*torch.linspace(0,polar_width,polar_width)/polar_width).to(self.device)
            pxCirclePoints = pupil_xyr[0] + pupil_xyr[2] * torch.cos(theta).to(self.device) # 512
            pyCirclePoints = pupil_xyr[1] + pupil_xyr[2] * torch.sin(theta).to(self.device) # 512
            
            ixCirclePoints = iris_xyr[0] + iris_xyr[2] * torch.cos(theta).to(self.device)  # 512
            iyCirclePoints = iris_xyr[1] + iris_xyr[2] * torch.sin(theta).to(self.device)  # 512

            pr = float(pupil_xyr[2].item()) / float(iris_xyr[2].item())
            if pr > target_alpha: # constriction
                radiusBiomech = self.Biomech(target_alpha * 0.006, pr * 0.006, polar_height+1)[1:]
                radius = 2 * (torch.linspace(1,polar_height,polar_height)/polar_height).reshape(-1, 1).to(self.device) - torch.tensor(radiusBiomech).float().reshape(-1, 1).to(self.device)
            else: # dilation
                radiusBiomech = self.Biomech(pr * 0.006, target_alpha * 0.006, polar_height+1)[1:]
                radius = torch.tensor(radiusBiomech).float().reshape(-1, 1).to(self.device)
            
            radius = torch.tensor(radiusBiomech).float().reshape(-1, 1).to(self.device)
            
            pxCoords = torch.matmul((1.0-radius), pxCirclePoints.reshape(1, polar_width)) # 64 x 512
            pyCoords = torch.matmul((1.0-radius), pyCirclePoints.reshape(1, polar_width)) # 64 x 512
            
            ixCoords = torch.matmul(radius, ixCirclePoints.reshape(1, polar_width)) # 64 x 512
            iyCoords = torch.matmul(radius, iyCirclePoints.reshape(1, polar_width)) # 64 x 512

            grid_x_cartToPol = torch.clamp(pxCoords + ixCoords, 0, width-1).float().to(self.device) # 1 x 1 x 64 x 512
            grid_y_cartToPol = torch.clamp(pyCoords + iyCoords, 0, height-1).float().to(self.device) # 1 x 1 x 64 x 512

            Y = torch.arange(0, height).reshape(height, 1).repeat(1, width).float().to(self.device)
            X = torch.arange(0, width).reshape(1, width).repeat(height, 1).float().to(self.device)

            xcpr = torch.tensor(pupil_xyr[0].item()).reshape(1, 1).repeat(height, width).to(self.device)
            ycpr = torch.tensor(pupil_xyr[1].item()).reshape(1, 1).repeat(height, width).to(self.device)

            dists_p = torch.sqrt((X - xcpr) ** 2 + (Y - ycpr) ** 2).to(self.device)

            xcir = torch.tensor(iris_xyr[0].item()).reshape(1, 1).repeat(height, width).to(self.device)
            ycir = torch.tensor(iris_xyr[1].item()).reshape(1, 1).repeat(height, width).to(self.device)

            dists_i = torch.sqrt((X - xcir) ** 2 + (Y - ycir) ** 2).to(self.device)

            xy_between_cond = torch.where(torch.logical_and(dists_p > target_alpha * iris_xyr[2].item(), dists_i < int(iris_xyr[2].item())), 1.0, 0.0).to(self.device)

            a = (pupil_xyr[0].item() - iris_xyr[0].item()) ** 2 + (pupil_xyr[1].item() - iris_xyr[1].item()) ** 2 - (target_alpha * iris_xyr[2].item() - iris_xyr[2].item()) ** 2
            b_part = 2 * (target_alpha * iris_xyr[2].item()) * (iris_xyr[2].item() - target_alpha * iris_xyr[2].item())
            c_part = (target_alpha * iris_xyr[2].item()) ** 2

            A = torch.tensor(a).reshape(1, 1).repeat(height, width).to(self.device)
            B = 2 * (X - pupil_xyr[0].item()) * (pupil_xyr[0].item() - iris_xyr[0].item()) + 2 * (Y - pupil_xyr[1].item()) * (pupil_xyr[1].item() - iris_xyr[1].item()) - b_part
            C = torch.pow((X - pupil_xyr[0].item()), 2) + torch.pow((Y - pupil_xyr[1].item()), 2) - c_part
            r = self.quadSolve(A, B, C).real.float().to(self.device)
            X_c = r * iris_xyr[0].item() + (1.0 - r) * pupil_xyr[0].item()
            Y_c = r * iris_xyr[1].item() + (1.0 - r) * pupil_xyr[1].item()
            grid_x_polToCart = (self.getAngle(Y - Y_c, X - X_c) / (2*math.pi)).float().to(self.device) * (polar_width-1) * xy_between_cond
            grid_y_polToCart = r.to(self.device) * (polar_height-1) * xy_between_cond
            
            grid_sample_mat_polToCart = torch.cat([grid_x_polToCart.unsqueeze(-1), grid_y_polToCart.unsqueeze(-1)], dim=-1).unsqueeze(0).to(self.device)
            grid_sample_mat_cartToPol = torch.cat([grid_x_cartToPol.unsqueeze(-1), grid_y_cartToPol.unsqueeze(-1)], dim=-1).unsqueeze(0).to(self.device)

            image_polar = self.deform(image, width, height, grid_sample_mat_cartToPol, interp_mode='bicubic')
            image_deformed = self.deform(image_polar, polar_width, polar_height, grid_sample_mat_polToCart, interp_mode='bicubic')

            r1r = torch.tensor(pupil_xyr[2].item()).reshape(-1, 1, 1).repeat(1, height, width).to(self.device)
            r1pr = torch.tensor(target_alpha * iris_xyr[2].item()).reshape(-1, 1, 1).repeat(1, height, width).to(self.device)

            x_less, y_less = self.find_xy_less(r1r, r1pr, X, Y, xcpr, ycpr)
            xy_less = torch.cat((x_less.unsqueeze(3), y_less.unsqueeze(3)), dim=3).float()

            mask_deformed = self.deform(mask.float(), width, height, xy_less, interp_mode='bicubic')
            pupil_mask_deformed = torch.where(dists_p < 1.1 * target_alpha * iris_xyr[2].item(), mask_deformed, torch.ones(mask_deformed.shape).float().to(self.device))

            image_deformed = torch.where(dists_i >= int(iris_xyr[2].item())-1, image, image_deformed) * pupil_mask_deformed

            return image_deformed
    
    def deform(self, input, W, H, grid, interp_mode):  #helper function
        with torch.no_grad():
            # grid: [-1, 1]
            gridx = grid[:, :, :, 0]
            gridy = grid[:, :, :, 1]
            gridx = gridx / (W - 1)
            gridx = (gridx - 0.5) * 2
            gridy = gridy / (H - 1)
            gridy = (gridy - 0.5) * 2
            newgrid = torch.stack([gridx, gridy], dim=-1)
            return grid_sample(input, newgrid, mode=interp_mode, align_corners=True)
    
    def biomechanical_deform(self, image, mask, pupil_xyr, iris_xyr, alpha):
        with torch.no_grad():
            image = ToTensor()(image).unsqueeze(0).to(self.device) * 255
            width = image.shape[3]
            height = image.shape[2]
            mask_t = ToTensor()(mask).unsqueeze(0).to(self.device)
            deformed_image = self.do_biomech_deform(image, mask_t, pupil_xyr, iris_xyr, alpha, width, height)
            deformed_image = torch.clamp(torch.round(deformed_image), min=0, max=255)
            return Image.fromarray(deformed_image[0][0].cpu().numpy().astype(np.uint8), 'L')
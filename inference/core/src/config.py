#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government 
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

import argparse

class Options:
    def __init__(self):
        self.parser = argparse.ArgumentParser(description="Iris annotation options")

        # MAIN
        self.parser.add_argument("--use_cuda",
                                 action="store_true",
                                 help="if flag is specified, run on GPU")
        self.parser.add_argument("--model_dir_path",
                                 type=str,
                                 help="directory path of previously trained model(s)",
                                 default="logs")
                                #  default="models")
        
        # DATASET OPTIONS
        self.parser.add_argument("--dataset_dir_path",
                                 type=str,
                                 help="directory path to prepend to dataset file")
        self.parser.add_argument("--dataset_file",
                                 type=str,
                                 help="absolute path of dataset file")
        
        # OPTIMIZATION OPTIONS
        self.parser.add_argument("--batch_size",
                                 type=int,
                                 help="number of images per batch",
                                 default=1)

        # CLASS OPTIONS
        self.parser.add_argument('--class_options', 
                                 type=str, 
                                 nargs=3, 
                                 action='append',
                                 help="custom class options (for trained model)",
                                 default=[])

    def parse(self):
        self.options = self.parser.parse_args()
        return self.options

#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government
#  and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

import json

import setuptools

config = {}
README = ""

with open("config.json", "r", encoding="utf-8") as fh:
    config = json.load(fh)

with open(config["root_path"] + "/README.md", encoding="utf-8") as fh:
    README = fh.read()

setuptools.setup(
    name=config['name'],
    version=config['version'],
    description=config['description'],
    long_description=README,
    long_description_content_type="text/markdown",
    url=config['url'],
    packages=setuptools.find_packages(),
    install_requires=['protobuf'],
    python_requires=">=2.7"
)

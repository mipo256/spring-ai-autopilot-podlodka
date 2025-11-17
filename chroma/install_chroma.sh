#!/bin/bash

apt update
apt install pip python3.13-venv
python3 -m venv chroma-venv
source chroma-venv/bin/activate
pip install chromadb
python3 setup_collection.py
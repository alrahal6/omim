#!/bin/env python

# This helper cmd run script is to be used like:
# run.py <pyscript_name> <params>
# It will take care of PYTHONPATH and plugin_dir
# and start pyaloha with specified script and params
import os
import sys

base_path = os.path.dirname(os.path.abspath(__file__))

PYSCRIPT_PATH = base_path

ALOHA_DATA = os.environ['ALOHA_DATA_DIR']

if __name__ == '__main__':
    pyaloha = __import__('pyaloha.main')
    pyaloha.main.cmd_run(plugin_dir=PYSCRIPT_PATH, data_dir=ALOHA_DATA)

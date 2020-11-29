import os
import pickle
from flask import request, jsonify, send_file

import flask
import numpy as np
#from PIL import Image

app = flask.Flask(__name__)


@app.route('/')
def hello_world():
    file_dir = "frame8_left.png"

    return send_file(file_dir, mimetype='image/png')


@app.route('/test', methods=["POST"])
def test():
    data = {}
    if request.method == "POST":
        print(flask.request.files)
        print(flask.request.files.get('image'))


        file_dir = "frame8_left.png"


    return send_file(file_dir, mimetype='image/png')

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)
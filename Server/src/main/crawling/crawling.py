from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from bs4 import BeautifulSoup

import json
import os
import argparse
import sys

import requests
import urllib
import urllib3
from urllib3.exceptions import InsecureRequestWarning

import datetime
import time
from PIL import Image
from io import BytesIO

#이미지 사이즈 측정해서 1x1 픽셀 이미지는 거를 예정
def get_image_size(url):
    data = requests.get(url).content
    im = Image.open(BytesIO(data))
    return im.size

urllib3.disable_warnings(InsecureRequestWarning)

searchword1 = 'rabbit'
# searchword2 = 'tattoo'
# searchword3 = 'cartoon'
baseurl = 'https://www.tattoodo.com/signin/'
searchurl = 'https://www.tattoodo.com/s/tattoos?q=' + searchword1
dirs = 'tattoodo_' + searchword1
maxcount = 10

#chromedriver 설치된 폴더 경로
chromedriver = 'D://Capstone//2020_1//deeplearning//chromedriver.exe'

if not os.path.exists(dirs):
    os.mkdir(dirs)

def download_tatoodo_images():

    options = webdriver.ChromeOptions()
    options.add_argument('--no-sandbox')
    #options.add_argument('--headless')

    try:
        browser = webdriver.Chrome(chromedriver, options=options)
    except Exception as e:
        print(f'No found chromedriver in this environment.')
        print(f'Install on your machine. exception: {e}')
        sys.exit()

    browser.maximize_window()
    #타투두 로그인 부분
    browser.get(baseurl)
    browser.implicitly_wait(1)
    browser.find_element_by_xpath('//button[@class="_2RJzJPs"]').click()
    browser.implicitly_wait(3)
    time.sleep(3)
    browser.find_element_by_name('email').send_keys('타투두 이용자 계정')   #타투두 계정 입력
    browser.find_element_by_name('password').send_keys('이용자 비밀번호')   #타투두 비밀번호 입력
    element = browser.find_element_by_xpath('//form[@action="/"]//button[@type="submit"]')
    browser.execute_script("arguments[0].click();", element)
    browser.implicitly_wait(10)
    time.sleep(10)
    # browser.find_element_by_xpath('//form[@action="/"]//button[@type="submit"]').click()
    #타투두 로그인 끝

    #타투두 이미지 Get
    browser.get(searchurl)
    browser.implicitly_wait(3)
    time.sleep(3)
    print(f'Getting you a lot of images. This may take a few moments...')

    element = browser.find_element_by_tag_name('body')

    # Scroll down
    images = []
    for i in range(30):
        element.send_keys(Keys.PAGE_DOWN)
        time.sleep(0.3)
        if i % 5 == 0:
            page_source = browser.page_source
            soup = BeautifulSoup(page_source, 'lxml')
            image = soup.find_all('img')
            images = images + image
            time.sleep(0.3)

    urls = []
    for image in images:
        try:
            url = image['src']
            if not url.find('https://'):
                urls.append(url)
        except:
            try:
                url = image['data-src']
                if not url.find('https://'):
                    urls.append(image['src'])
            except Exception as e:
                print(f'No found image sources.')
                print(e)

    urls = set(urls)
    count = 0
    print("image count : ",len(urls))

    if urls:
        for url in urls:
            try:
                res = requests.get(url, verify=False, stream=True)
                rawdata = res.raw.read()
                with open(os.path.join(dirs, searchword1 + '_' + str(count) + '.jpg'), 'wb') as f:
                    f.write(rawdata)
                    count += 1
            except Exception as e:
                print('Failed to write rawdata.')
                print(e)

    browser.close()
    return count

# Main block
def main():
    t0 = time.time()
    count = download_tatoodo_images()
    t1 = time.time()

    total_time = t1 - t0
    print(f'\n')
    print(f'Download completed. [Successful count = {count}].')
    print(f'Total time is {str(total_time)} seconds.')

if __name__ == '__main__':
    main()
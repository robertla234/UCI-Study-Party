#!/usr/bin/env python
# -*- coding: utf-8 -*-
from socket import socket
from geopy.geocoders import Nominatim
import gspread
import numpy as np
import json
from oauth2client.service_account import ServiceAccountCredentials


def addUSER(index):
 ip = '127.0.0.1'
 port1 = 10004
 sock1 = socket()
 sock1.connect((ip, port1))
 # use creds to create a client to interact with the Google Drive API
 scope = ['https://spreadsheets.google.com/feeds',
          'https://www.googleapis.com/auth/drive']
 creds = ServiceAccountCredentials.from_json_keyfile_name('My vaccine-a5681df7ce84.json', scope)
 client = gspread.authorize(creds)
 # Find a workbook by name and open the first sheet
 # Make sure you use the right name here.
 sheet = client.open("MY Vaccine (Responses)").sheet1
 
 # Extract and print all of the values
 list_of_hashes = sheet.get_all_records()
 array = np.array(list_of_hashes)
 while(index<len(array)):
  print(array)
  json_object = json.loads(str(array[index]).replace("'", '"'))
  locator = Nominatim(user_agent="myGeocoder")
  #location = locator.geocode("Canyon Crest, Riverside, California")
  location = locator.geocode(str(json_object["What is your address"]))
  st= '{"email" :"'+str(json_object["What is your email address"])+'" , "latitude": '+str(location.latitude)+', "longitude":'+str(location.longitude)+'}' ; 
  sock1.sendall(bytes(st, 'utf-8'))
  index=index+1
  print(st)
 return index;

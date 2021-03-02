#!/usr/bin/env python
# -*- coding: utf-8 -*-
from email.header    import Header
from email.mime.text import MIMEText
from getpass         import getpass
from smtplib         import SMTP_SSL
import socket
import threading 
from http.server import HTTPServer, BaseHTTPRequestHandler
from optparse import OptionParser
import imaplib
import email 
from email.header import decode_header 
import webbrowser 
import os
import sendingEmail
import json
import time
   
class RequestHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        request_path = self.path
        print("Request path:", request_path)
        print("Request headers:", self.headers)
        self.send_response(200)
        self.send_header("Set-Cookie", "foo=bar")
        self.end_headers()

        
    def do_POST(self):
        
        request_path = self.path
        
        request_headers = self.headers
        content_length = request_headers.get('Content-Length')
        length = int(content_length) if content_length else 0
        messageRecived= self.rfile.read(length)
        print("   --\\\   ")
        print(messageRecived)
        print("   --///   ")
        self.send_response(200)
        self.end_headers()
        recievedParts=json.loads(messageRecived)
        
        if 'results' in recievedParts:
         print("RESULTS IN PARTS")
         results=recievedParts['results'][0]
         for key,value in results.items():
             print (key, value)
         print(results['subscriptionId'])
         #sendingEmail.sendEmail("New class party was created!", )

          
        else:
         print("RESULTS NOT IN PARTS")
         subscriptionIds= recievedParts['subscriptionIds']
         print(subscriptionIds)
         for subscriptionId in subscriptionIds[0]:
          if subscriptionId in usersEmaiAndChannel:
              print(subscriptionId)
           #sendingEmail.sendEmail2("New result is ready!",usersEmaiAndChannel[subscriptionId], "New data is available in your subscribed channel")        
    
        
def main():
    port = 8081
    print('Listening on 0.0.0.0:%s' % port)
    server = HTTPServer(('', port), RequestHandler)
    server.serve_forever()


if __name__ == "__main__": 
    parser = OptionParser()
    parser.usage = ("Creates an http-server that will echo out any GET or POST parameters\n"
                    "Run:\n\n"
                    "   reflect")
    (options, args) = parser.parse_args()
    sendingResults = threading.Thread(target=main, args=()) 
    sendingResults.start()
    sendingResults.join()
    print("Done!")

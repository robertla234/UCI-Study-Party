import imaplib
import email 
from email.header import decode_header 
import webbrowser 
import os
import read
import sendingcurl
import sendingEmail
import makingTables


def read_email_from_gmail(numberOfEmails):
 imap = imaplib.IMAP4_SSL("imap.gmail.com")  
 result = imap.login('eemailbroker@gmail.com','brokerbroker')  
 imap.select('"[Gmail]/All Mail"',  
 readonly = True)   
 response, messages = imap.search(None,  
                                'UnSeen')
 newEmails= dict()
 messages = messages[0].split() 

 for i in messages[numberOfEmails:len(messages)]:
     res, msg = imap.fetch(str(int(i)), "(RFC822)") 
     for response in msg: 
         if isinstance(response, tuple): 

            msg = email.message_from_bytes(response[1])
            # print required information 
            type=emailsType(msg["Subject"])           
            #print(msg["Date"]) 
            emailAdress=msg["From"] 
  
     for part in msg.walk(): 
         if part.get_content_type() == "text/plain": 
             # get text or plain data 
             body = part.get_payload(decode = True)
             body=body.decode("UTF-8") 
             #print(body)
             if(type=="info"):
              res = body.split('channel', maxsplit=1)[-1]\
               .split(maxsplit=1)[0]
              messageToBeSent=sendingcurl.getInfoOfChannels(res)
              sendingEmail.sendEmail2("Info about channel"+res,emailAdress,makingTables.alltheInfo(messageToBeSent,res)) 
             if(type=="all"):
              messageToBeSent=sendingcurl.getAllChannels()
              sendingEmail.sendEmail2("All the channels available",emailAdress,makingTables.allthechannels(messageToBeSent))
             if(type=="register"):
              nameOfChannel=body.split(":", 2)
              parameter1=body.split(":", 4)
              parameter2=body.split(":", 6)
              parameter3=body.split(":", 8)
              messageToBeSent=sendingcurl.subscribe(nameOfChannel[1].split("\r\n")[0],parameter1[2].split("\r\n")[0])
              newEmails[messageToBeSent[0]]=emailAdress 
             if(type=="SignUp"):
              sendingEmail.sendingForm(emailAdress)
     numberOfEmails=numberOfEmails+1
 print(numberOfEmails)
 return newEmails,numberOfEmails          
        
    

 

def emailsType(emailtype):
 if(emailtype == "request for all channels"):
  return "all"
 if(emailtype== "register in channel"):
  return "register"
 if(emailtype == "getting info about a channel"):
  return "info"
 if(emailtype == "Sign Up TO the APP"):
  return "SignUp"


  

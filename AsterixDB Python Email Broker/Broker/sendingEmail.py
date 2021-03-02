#!/usr/bin/env python
# -*- coding: utf-8 -*-
from email.header    import Header
from email.mime.text import MIMEText
from getpass         import getpass
from smtplib         import SMTP_SSL
from email.mime.multipart import MIMEMultipart
import googleform

def sendEmail(subject,email,context,result):
	login, password = 'eemailbroker@gmail.com','brokerbroker'
	recipients = [email]
	msg = MIMEMultipart()

	msg['Subject'] = Header(subject, 'utf-8')
	msg['From'] = login
	msg['To'] = ", ".join(recipients)
	s = SMTP_SSL('smtp.gmail.com', 465, timeout=10)
	s.set_debuglevel(1)
	try:
    	 if result :
    	  html =context
    	  part1 = MIMEText(html, 'html')
    	  msg.attach(part1)
    	 else:
    	  msg = MIMEText(context, 'plain', 'utf-8')
    	 s.login(login, password)
    	 s.sendmail(msg['From'], recipients, msg.as_string())
	finally:
   	 s.quit()

def sendEmail2(subject,email,context):
	login, password = 'eemailbroker@gmail.com','brokerbroker'
	recipients = [email]
	msg = MIMEText(context, 'plain', 'utf-8')
	msg['Subject'] = Header(subject, 'utf-8')
	msg['From'] = login
	msg['To'] = ", ".join(recipients)
	s = SMTP_SSL('smtp.gmail.com', 465, timeout=10)
	s.set_debuglevel(1)
	try:
    	 s.login(login, password)
    	 s.sendmail(msg['From'], recipients, msg.as_string())
	finally:
   	 s.quit()

def sendingForm(email):
	login, password = 'eemailbroker@gmail.com','brokerbroker'
	recipients = [email]
	msg = MIMEMultipart()
	msg['Subject'] = Header('Please compelete the form below:', 'utf-8')
	msg['From'] = login
	msg['To'] = ", ".join(recipients)
	s = SMTP_SSL('smtp.gmail.com', 465, timeout=10)
	s.set_debuglevel(1)
	try:
    	 html =googleform.context()
    	 part1 = MIMEText(html, 'html')
    	 msg.attach(part1)
    	 s.login(login, password)
    	 s.sendmail(msg['From'], recipients, msg.as_string())
	finally:
   	 s.quit()

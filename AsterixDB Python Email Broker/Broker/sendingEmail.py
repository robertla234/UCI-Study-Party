#!/usr/bin/env python
# -*- coding: utf-8 -*-
from email.header    import Header
from email.mime.text import MIMEText
from getpass         import getpass
from smtplib         import SMTP_SSL
from email.mime.multipart import MIMEMultipart
import json

def sendEmail(results, subscriptionId):
	subject, email, context, result = parser(results, subscriptionId)
	login, password = 'studypartyuci2020@gmail.com','StudyUCI2020'
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

def parser(results, subscriptionId):
	parseDict = results['p']
	for key, value in parseDict.items():
		print (key, value)

	subject = "New " + str(parseDict['class']) + " group created!"
	email = results['email']
	context = ("Hello, " + results['firstName'] + " " + results['lastName'] + ". " +
                   "A group was created for " + str(parseDict['class']) + " " + str(parseDict['purpose']) +
		   " at " + str(parseDict['meetTime']) + " " + str(parseDict['location']) + ".\r\n" +
                   "Subscription ID: " + subscriptionId)
	result = True

	return subject, email, context, result

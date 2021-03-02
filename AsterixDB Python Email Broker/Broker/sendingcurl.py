import subprocess
import json 

  
#result = subprocess.call("""curl -v --data-urlencode "statement=use channels; 
#subscribe to EmergenciesNearMeChannel('tornado', 1) on brokerA;"           --data pretty=true                               --data client_context_id=xyz                     http://localhost:19002/query/service
#""", shell=True)

#result = subprocess.run("""curl -v --data-urlencode "statement= use channels; select value x
#from Metadata.Channel x
#order by x.ChannelName;"           --data pretty=true                               --data client_context_id=xyz                     http://localhost:19002/query/service
#""", shell=True)
#print (result)
def getAllChannels(): 
 import subprocess 
 subprocess = subprocess.Popen("""curl -v --data-urlencode "statement= use test; select value x
 from Metadata.\`Channel\` x
 order by x.ChannelName;"           --data pretty=true                               --data client_context_id=xyz                     http://localhost:19002/query/service
 """, shell=True, stdout=subprocess.PIPE)
 subprocess_return = subprocess.stdout.read()
 print(subprocess_return)
 return subprocess_return
def subscribe(channelName,parameter1): 
 import subprocess 
 subprocess = subprocess.Popen("""curl -v --data-urlencode "statement=use test; 
 subscribe to """+channelName+"""('"""+parameter1+"""') on brokerA;"           --data pretty=true                               --data client_context_id=xyz                     http://localhost:19002/query/service
 """, shell=True, stdout=subprocess.PIPE)
 subprocess_return = subprocess.stdout.read()
 ids=json.loads(subprocess_return.decode("utf-8"))
 print(subprocess_return)
 return ids["results"]

def getInfoOfChannels(channelName): 
 import subprocess 
 subprocess = subprocess.Popen("""curl -v --data-urlencode "statement= use test;
select f.Params
 FROM Metadata.\`Function\` f
where f.DataverseName='test' and  f.Name='"""+channelName+"""';"           --data pretty=true                               --data client_context_id=xyz                     http://localhost:19002/query/service
 """, shell=True, stdout=subprocess.PIPE)
 subprocess_return = subprocess.stdout.read()

 return subprocess_return
#subscribe("""'tornado', 1""")


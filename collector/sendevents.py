import ET_Client

myClient = ET_Client.ET_Client()
sentevent = ET_Client.ET_SentEvent()
sentevent.auth_stub = myClient

response = sentevent.get()

print str(response)
print str(response.results[0])

import time

time.sleep(1)
testDict = {
    "command" : "work for me",
    "roomArray" : ["namer", "seconder"]
}

print(testDict["command"])
roomArray = testDict["roomArray"]
for i in range(len(roomArray)):
    print("#1")
    print(roomArray[i])
    print("#2")
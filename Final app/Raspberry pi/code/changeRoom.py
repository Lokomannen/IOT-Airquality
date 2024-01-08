import json, sys

def getRooms():
    with open('rooms.json', 'r') as fp:
        return json.load(fp)

def storeRooms(roomDict):
    with open('rooms.json', 'w') as fp:
        json.dump(roomDict, fp)
    
def getValues():
    with open('values.json', 'r') as fp:
        return json.load(fp)

def storeValues(valueDict):
    with open('values.json', 'w') as fp:
        json.dump(valueDict, fp)

def findRoom(roomId): #returns entry name example "My Room_2" 2 is the id
    for room in roomDict:
        for w in range(len(room)):
            roomCharacter = room[len(room) - w - 1]
            if roomCharacter != "_":
                continue

            checkRoomId = room[-w:]
            if checkRoomId == str(roomId):
                return room
    return None

def createRoom(roomId, roomName):
    roomEntryName = roomName + "_" + str(roomId)
    if findRoom(roomId) != None:
        #print("Room id already exists")
        exit()
    roomDict[roomEntryName] = {
        "Sensors" : [],
        "Actuators" : [],
        "Quality" : 4,
        "Status" : 3
    }
    increaseRoomId()

def increaseRoomId():
    valueDict = getValues()
    valueDict["nextRoomId"] = valueDict["nextRoomId"] + 1
    storeValues(valueDict)

def renameRoom(roomId, roomName):
    roomEntryName = findRoom(roomId)
    if roomEntryName == None:
        #print("Room id does not exists")
        exit()
    roomDict[roomName + "_" + str(roomId)] = roomDict[roomEntryName]
    del roomDict[roomEntryName]

def deleteRoom(roomId):
    roomEntryName = findRoom(roomId)
    if roomEntryName == None:
        #print("Room id does not exists")
        exit()
    del roomDict[roomEntryName]

def connectSensor(roomId, sensorName):
    roomEntryName = findRoom(roomId)
    if roomEntryName == None:
        #print("Room id does not exists")
        exit()
    currentSensorArray = roomDict[roomEntryName]["Sensors"]
    currentSensorArray.append(sensorName)
    roomDict[roomEntryName]["Sensors"] = currentSensorArray

def connectActuator(roomId, actuatorName):
    roomEntryName = findRoom(roomId)
    if roomEntryName == None:
        #print("Room id does not exists")
        exit()
    currentActuatorArray = roomDict[roomEntryName]["Actuators"]
    currentActuatorArray.append(actuatorName)
    roomDict[roomEntryName]["Actuators"] = currentActuatorArray
    
def removeSensor(roomId, sensorName):
    roomEntryName = findRoom(roomId)
    if roomEntryName == None:
        #print("Room id does not exists")
        exit()
    currentSensorArray = roomDict[roomEntryName]["Sensors"]
    currentSensorArray.remove(sensorName)
    roomDict[roomEntryName]["Sensors"] = currentSensorArray

def removeActuator(roomId, actuatorName):
    roomEntryName = findRoom(roomId)
    if roomEntryName == None:
        #print("Room id does not exists")
        exit()
    currentActuatorArray = roomDict[roomEntryName]["Actuators"]
    currentActuatorArray.remove(actuatorName)
    roomDict[roomEntryName]["Actuator"] = currentActuatorArray

roomDict = getRooms()
command = sys.argv[1]
roomId = sys.argv[2]
if command == "create":
    roomName = sys.argv[3]
    createRoom(roomId, roomName)
elif command == "delete":
    deleteRoom(roomId)
elif command == "connectSensor":
    sensorName = sys.argv[3]
    connectSensor(roomId, sensorName)
elif command == "connectActuator":
    actuatorName = sys.argv[3]
    connectActuator(roomId, actuatorName)
elif command == "removeSensor":
    sensorName = sys.argv[3]
    removeSensor(roomId, sensorName)
elif command == "removeActuator":
    actuatorName = sys.argv[3]
    removeActuator(roomId, actuatorName)
else:
    #print("command not known")
    exit()

storeRooms(roomDict)

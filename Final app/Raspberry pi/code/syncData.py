import json
def getValues():
    with open('values.json', 'r') as fp:
        return json.load(fp)

def getRooms():
    with open('rooms.json', 'r') as fp:
        return json.load(fp)
    
def getHistory():
    with open('history.json', 'r') as fp:
        return json.load(fp)
    
def findAndPrintStatus(actuator):
    for roomName in roomDict.keys():
        for actuatorName in roomDict[roomName]["Actuators"]:
            if actuator == actuatorName:
                print(roomDict[roomName]["Status"]) # prints the status
                return
    print(3) # unknown


valueDict = getValues()
for value in valueDict["Temp"].values():
    print(value)
for value in valueDict["Humidity"].values():
    print(value)
weekdays = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]

time = valueDict["time"]
day = valueDict["day"]
dayName = weekdays[valueDict["day"]]
week = valueDict["week"]
print(time)
print(day)
print(week)
print(valueDict["nextRoomId"])

roomDict = getRooms()
historyDict = getHistory()
for roomName in roomDict.keys():
    print(roomName)
    print(roomDict[roomName]["Quality"])
    print(roomDict[roomName]["Status"])
    
    for sensor in roomDict[roomName]["Sensors"]:
        sensorTempValue = historyDict[sensor][week]["Temp"][dayName][str(time)]
        sensorHumValue = historyDict[sensor][week]["Humidity"][dayName][str(time)]
        sensorCarValue = historyDict[sensor][week]["Carbon"][dayName][str(time)]


        print(sensor)
        print(sensorTempValue)
        print(sensorHumValue)
        print(sensorCarValue)
    print("actuators")
    for actuator in roomDict[roomName]["Actuators"]:
        print(actuator)
        findAndPrintStatus(actuator)
    print("end")
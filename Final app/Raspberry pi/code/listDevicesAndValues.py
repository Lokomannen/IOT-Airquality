#fake file for api request "https://pa-api.telldus.com/json/devices/list"

import json

historyDict = {}
valueDict = {}
weekdays = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]

with open('history.json', 'r') as fp:
    historyDict = json.load(fp)

with open('values.json', 'r') as fp:
    valueDict = json.load(fp)
def getRooms():
    with open('rooms.json', 'r') as fp:
        return json.load(fp)

roomDict = getRooms()
def findAndPrintStatus(actuator):
    for roomName in roomDict.keys():
        for actuatorName in roomDict[roomName]["Actuators"]:
            if actuator == actuatorName:
                print(roomDict[roomName]["Status"]) # prints the status
                return
    print(3) # unknown

for sensor in historyDict:
    time = str(valueDict["time"])
    day = weekdays[valueDict["day"]]
    week = valueDict["week"]
    displayedDict = {}

    sensorTempValue = historyDict[sensor][week]["Temp"][day][time]
    sensorHumValue = historyDict[sensor][week]["Humidity"][day][time]
    sensorCarValue = historyDict[sensor][week]["Carbon"][day][time]


    print(sensor)
    print(sensorTempValue)
    print(sensorHumValue)
    print(sensorCarValue)



print("end")

#Actuators
print("Air_con53X")
findAndPrintStatus("Air_con53X")
print("Air_con59B")
findAndPrintStatus("Air_con59B")
print("Fan_33E")
findAndPrintStatus("Fan_33E")

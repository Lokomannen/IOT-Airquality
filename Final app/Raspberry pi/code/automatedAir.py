import json

historyDict = {}
maxTemp = None
minTemp = None
maxHum = None
minHum = None
maxCar = None
minCar = None
time = None
day = None
week = None
rooms = {}
isActive = False
weekdays = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]
dataTypes = ["Temp", "Humidity", "Carbon", "Active"]

def getValues():
    with open('values.json', 'r') as fp:
        return json.load(fp)

def getRooms():
    with open('rooms.json', 'r') as fp:
        return json.load(fp)
    
def getHistory():
    with open('history.json', 'r') as fp:
        return json.load(fp)
    
def getCurrentValue(sensor, category):
    return historyDict[sensor][week][category][day][str(time)]

def decideIfActivate(room):
    if not room["Sensors"]: # no sensors
        return -1
    for sensor in room["Sensors"]:
        if valuesExceed(sensor):
            return 1
    if predictiveGuess(room):
        return 2
        
    return 3
        

def valuesExceed(sensor):
    curTime = time
    currentTemp = getCurrentValue(sensor, "Temp")
    if currentTemp < minTemp or currentTemp > maxTemp:
        return True
    currentHum = getCurrentValue(sensor, "Humidity")
    if currentHum < minHum or currentTemp > maxHum:
        return True
    currentCar = getCurrentValue(sensor, "Carbon")
    if currentCar < minCar or currentTemp > maxCar:
        return True
    return False

def valuesOnLimit(sensor, category):
    currentValue = getCurrentValue(sensor, category)
    if category == "Temp":
        if currentValue <= minTemp:
            return -1
        if currentValue >= maxTemp:
            return 1
    if category == "Humidity":
        if currentValue <= minHum:
            return -1
        if currentValue >= maxHum:
            return 1
    if category == "Carbon":
        if currentValue <= minCar:
            return -1
        if currentValue >= maxCar:
            return 1
    return 0

def predictiveGuess(room):
    sensorsWantingOn = 0
    sensorsWantingOff = 0
    for sensor in room["Sensors"]:
        if trendingGuess(sensor): 
            sensorsWantingOn += 1
            continue
        if historicGuess(sensor):
            sensorsWantingOn += 1
            continue
        sensorsWantingOff += 1
    
    if sensorsWantingOff > sensorsWantingOn:
        return False
    else:
        return True

def trendingGuess(sensor): # predicts based on the last couple of hours
    for i in range(3):
        category = dataTypes[i]
        valueLimit = valuesOnLimit(sensor, category) # -1 if min, 1 if max , 0 if nothing
        if valueLimit == 0:
            continue
        if isActive: # if sensor on limit and was active the previous hour then it wants to keep being active
            return True
        if checkForTrend(sensor,category, valueLimit):
            return True
    return False

def checkForTrend(sensor,category, valueLimit):
    currentCheckValue = getCurrentValue(sensor, category)
    for hoursBack in range(1,3): # designed to catch steady changes such as temperature 20 19 18, 18 being the limit and the current.
        pastDay = day
        pastWeek = week
        pastTime = time - hoursBack
        if pastTime < 0:
            pastTime = 24-hoursBack
            dayNumber = weekdays.index(day)
            if dayNumber == 0:
                pastDay = weekdays[6]
                weekNumber = -1
                for w in range(len(week)):
                    weekCharacter = week[len(week) - w - 1]
                    if weekCharacter == "_":
                        weekNumber = int(week[-w:])
                        break
                pastWeek = "week_" + str((weekNumber - 1))
                if pastWeek not in historyDict:
                    return False
            pastDay = weekdays[dayNumber - 1]
        pastValue = historyDict[sensor][pastWeek][category][pastDay][str(pastTime)]
        if valueLimit == -1: 
            if pastValue <= currentCheckValue: # if the data was not trending downwards consistantly
                return False
        if valueLimit == 1: # if the data was not trending upwards consistantly
            if pastValue > currentCheckValue:
                return False
        currentCheckValue = pastValue
    return True


def historicGuess(sensor): # looks 1 week back 3 hours ahead if all 3 hours were turned on
    weekNumber = -1
    for w in range(len(week)):
        if week[len(week) - w - 1] == "_":
            weekNumber = int(week[-w:])
            break 
    lastWeek = "week_" + str((weekNumber - 1))
    if lastWeek not in historyDict[sensor]:
        return False
    for hoursForward in range(1,4):
        nextDay = day
        nextWeek = lastWeek
        nextTime = time
        if time + hoursForward > 23:
            dayNumber = weekdays.index(day)
            if dayNumber == 6:
                nextDay = weekdays[0]
                nextWeek = week # if you check at sunday 23:00, 1 week back 1 hour ahead will be same week
        if 1 != historyDict[sensor][nextWeek]["Active"][nextDay][str(nextTime)]: # if sensor was not wanting to be on at that time
            return False
    return True


def turnOn(room): #fake api request "https://pa-api.telldus.com/json/device/turnOn" param id (which is part of the room name)
    rooms[room]["Status"] = 1

def turnOff(room):#fake api request "https://pa-api.telldus.com/json/device/turnOff" param id (which is part of the room name)
    rooms[room]["Status"] = 2
            
def writeSensorActivity(roomArray):
    for sensor in roomArray["Sensors"]:
        timeString = str(time)
        currentDayDict = historyDict[sensor][week]["Active"][day]
        whatToWrite = 0
        if valuesExceed(sensor):
            whatToWrite = 1
        if timeString not in currentDayDict:
            historyDict[sensor][week]["Active"][day][timeString] = whatToWrite
    writeToHistory()
    
def writeToHistory():
    with open('history.json', 'w') as fp:
        json.dump(historyDict, fp)

def storeValues(newValueDict):
    with open('values.json', 'w') as fp:
        json.dump(newValueDict, fp)

def storeRooms(roomDict):
    with open('rooms.json', 'w') as fp:
        json.dump(roomDict, fp)

def setRoomQuality(room, qualityNumber): # Quality : GOOD, OKAY, BAD, UNKNOWN; 1 , 2 , 3, 4
    rooms[room]["Quality"] = qualityNumber



def runAutomation():
    global maxTemp 
    global minTemp 
    global maxHum 
    global minHum 
    global maxCar 
    global minCar 
    global time 
    global day
    global week 
    global rooms 
    global historyDict

    historyDict = getHistory()
    newValueDict = getValues()
    maxTemp = newValueDict["Temp"]["max"]
    minTemp = newValueDict["Temp"]["min"]
    maxHum = newValueDict["Humidity"]["max"]
    minHum = newValueDict["Humidity"]["min"]
    maxCar = newValueDict["Carbon"]["max"]
    minCar = newValueDict["Carbon"]["min"]
    time = newValueDict["time"]
    day = weekdays[newValueDict["day"]]
    week = newValueDict["week"]
    rooms = getRooms()

    historyDict = getHistory()


    for roomName, roomArray in rooms.items():
        resultCode = decideIfActivate(roomArray)
        if resultCode == -1: # no sensors
            continue
        if resultCode == 1: # values exceed limit
            turnOn(roomName)
            setRoomQuality(roomName, 3)
        elif resultCode == 2: #predictive guess
            turnOn(roomName)
            setRoomQuality(roomName, 2)
        else: # 3
            turnOff(roomName)
            setRoomQuality(roomName, 1)
        writeSensorActivity(roomArray)

    writeToHistory()
    storeRooms(rooms)

runAutomation()
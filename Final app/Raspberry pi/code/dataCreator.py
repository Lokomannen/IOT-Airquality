from random import randint
import json, random


def generateValue(current, trend, time, day, maximum, minimum, change, desired):
    randomValue = randint(0, 2) #chance to drift toward the desired value
    if randomValue == 0:
        if current < desired:
            current += change
        elif current > desired:
            current -= change
        return [current, trend]
    
    randomValue = randint(0, 3)
    if randomValue == 0:
        current -= change
        trend = -1
        if current < minimum:
            current = minimum
    elif randomValue == 1:
        current += change
        trend = 1
        if current > maximum:
            current = maximum
    else:
        current += trend * change
        if current < minimum:
            current = minimum
            trend = 1
        if current > maximum:
            current = maximum
            trend = -1
    
    if day >= 5: #weekends
        if time <= 6:
            trend = -1
        elif time >= 8 and time <= 20:
            trend = 1
    else:
        if time <= 7 or time >= 22:
            trend = -1
        elif time >= 16 and time <= 20:
            trend = 1
    return [current, trend]

dataDict = {}

weekdays = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]
dataTypes = ["Temp", "Humidity", "Carbon", "Active"]
sensors = ["Sensor_1", "Sensor_2", "Sensor_3"]

for i in range(len(sensors)):
    dataDict[sensors[i]] = {
        "week_1" : {},
        "week_2" : {}
    }

for k in range(len(sensors)):
    for i in range(len(dataTypes)):
        dataDict[sensors[k]]["week_1"][dataTypes[i]] = {
            weekdays[0] : {},
            weekdays[1] : {},
            weekdays[2] : {},
            weekdays[3] : {},
            weekdays[4] : {},
            weekdays[5] : {},
            weekdays[6] : {}
        }
        dataDict[sensors[k]]["week_2"][dataTypes[i]] = {
            weekdays[0] : {},
            weekdays[1] : {},
            weekdays[2] : {},
            weekdays[3] : {},
            weekdays[4] : {},
            weekdays[5] : {},
            weekdays[6] : {}
        }

#Temp

currentTemp = 20
lastTempTrend = 0
currentHum = 45
lastHumTrend = 0
currentCar = 600
lastCarTrend = 0
for k in range(len(sensors)):
    for day in range(7):
        currentTempDay = dataDict[sensors[k]]["week_1"]["Temp"][weekdays[day]]
        currentHumDay = dataDict[sensors[k]]["week_1"]["Humidity"][weekdays[day]]
        currentCarDay = dataDict[sensors[k]]["week_1"]["Carbon"][weekdays[day]]
        currentActDay = dataDict[sensors[k]]["week_1"]["Active"][weekdays[day]]
        for time in range(24): # tidpunkter i dynget
            tempData = generateValue(currentTemp, lastTempTrend, time, day, 24, 17, 1, 20)
            currentTemp = tempData[0]
            lastTempTrend = tempData[1]
            currentTempDay[time] = currentTemp
            humData = generateValue(currentHum, lastHumTrend, time, day, 65, 25, 5, 45)
            currentHum = humData[0]
            lastHumTrend = humData[1]
            currentHumDay[time] = currentHum
            carData = generateValue(currentCar, lastCarTrend, time, day, 1300, 300, 100, 800)
            currentCar = carData[0]
            lastCarTrend = carData[1]
            currentCarDay[time] = currentCar
            if currentTemp == 24 or currentTemp == 17 or currentHum == 65 or currentHum == 25 or currentCar == 1300 or currentCar == 300:
                currentActDay[time] = 1
            else:
                currentActDay[time] = 0

for k in range(len(sensors)):
    for day in range(7):
        currentTempDay = dataDict[sensors[k]]["week_2"]["Temp"][weekdays[day]]
        currentHumDay = dataDict[sensors[k]]["week_2"]["Humidity"][weekdays[day]]
        currentCarDay = dataDict[sensors[k]]["week_2"]["Carbon"][weekdays[day]]
        for time in range(24): # tidpunkter i dynget
            tempData = generateValue(currentTemp, lastTempTrend, time, day, 24, 17, 1, 20)
            currentTemp = tempData[0]
            lastTempTrend = tempData[1]
            currentTempDay[time] = currentTemp
            humData = generateValue(currentHum, lastHumTrend, time, day, 65, 25, 5, 45)
            currentHum = humData[0]
            lastHumTrend = humData[1]
            currentHumDay[time] = currentHum
            carData = generateValue(currentCar, lastCarTrend, time, day, 1300, 300, 100, 800)
            currentCar = carData[0]
            lastCarTrend = carData[1]
            currentCarDay[time] = currentCar



with open('history.json', 'w') as fp:
    json.dump(dataDict, fp)

valueDict={
    "Temp" : {
        "max" : 23,
        "min" : 18
    },
    "Humidity" : {
        "max" : 60,
        "min" : 30
    },
    "Carbon" : {
        "max" : 1200,
        "min" : 400
    },
    "time" : 0,
    "day" : 0,
    "week" : "week_2",
    "nextRoomId" : 3
}
with open('values.json', 'w') as fp:
    json.dump(valueDict, fp)

# Quality : GOOD, OKAY, BAD, UNKNOWN;
# Status : ACTIVE, INACTIVE, UNKNOWN;
roomDict = {
    "kitchen_1" : {
        "Sensors" : ["Sensor_1", "Sensor_3"],
        "Actuators" : ["Air_con53X"],
        "Quality" : 4,
        "Status" : 3
    },
    "bathroom_2" : {
        "Sensors" : ["Sensor_2"],
        "Actuators" : [],
        "Quality" : 4,
        "Status" : 3
    },
}

with open('rooms.json', 'w') as fp:
    json.dump(roomDict, fp)


with open("automatedAir.py") as f:
    exec(f.read())
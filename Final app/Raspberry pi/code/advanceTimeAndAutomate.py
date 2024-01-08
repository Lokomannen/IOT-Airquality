from automatedAir import runAutomation
import json

def getValues():
    with open('values.json', 'r') as fp:
        return json.load(fp)
    
def storeValues(valueDict):
    with open('values.json', 'w') as fp:
        json.dump(valueDict, fp)
    
valueDict = getValues()
time = valueDict["time"]

time += 1
valueDict["time"] = time
if time > 23:
    time = 0
    valueDict["time"] = time
    weekdays = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]
    day = weekdays[valueDict["day"]]
    dayNumber = weekdays.index(day)
    dayNumber += 1
    if dayNumber == 7:
        week = "week_2"
        dayNumber = 0
    day = weekdays[dayNumber]
    valueDict["day"] = dayNumber
    valueDict["week"] = week
storeValues(valueDict)

runAutomation()
import json, sys
from automatedAir import runAutomation

def getValues():
    with open('values.json', 'r') as fp:
        return json.load(fp)

def storeValues(valueDict):
    with open('values.json', 'w') as fp:
        json.dump(valueDict, fp)

def changeCategory(which, max, min):
    valueDict[which]["max"] = max
    valueDict[which]["min"] = min

valueDict = getValues()
command = sys.argv[1]
if command == "date":
    valueDict["time"] = int(sys.argv[2])
    valueDict["day"] = int(sys.argv[3])
    valueDict["week"] = sys.argv[4]
    runAutomation()
elif command == "ranges":
    tempMax = int(sys.argv[2])
    tempMin = int(sys.argv[3])
    changeCategory("Temp", tempMax, tempMin)
    humMax = int(sys.argv[4])
    humMin = int(sys.argv[5])
    changeCategory("Humidity", humMax, humMin)
else:
    #print("ERROR")
    #print("command not known")
    exit()
storeValues(valueDict)

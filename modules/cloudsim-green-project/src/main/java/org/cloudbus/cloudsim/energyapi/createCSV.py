import requests
import csv
import time
from datetime import datetime, timedelta
import os

API_KEY = 'JtPlg2xeVfyLW'
CSV_FILE = 'powerBreakdownData.csv'

#  'US-CAL-LDWP': {'countryName': 'USA', 'zoneName': 'Los Angeles Department of Water and Power'}
def getZones():
    url = 'https://api.electricitymap.org/v3/zones'
    headers = {'auth-token': ""}
    response = requests.get(url, headers=headers)
    return response.json()


def getPowerBreakdown(region):
    url = f'https://api.electricitymap.org/v3/power-breakdown/latest?zone={region}'
    headers = {'auth-token': 'JtPlg2xeVfyLW'}
    response = requests.get(url, headers=headers)
    return response.json()

def updateFieldnames(data, fieldnames):
    mainFields = [
        'datetime', 'zone', 'updatedAt', 'createdAt', 'fossilFreePercentage',
        'renewablePercentage', 'powerConsumptionTotal', 'powerProductionTotal',
        'powerImportTotal', 'powerExportTotal', 'isEstimated', 'estimationMethod'
    ]

    newFields = [('consumption', 'powerConsumptionBreakdown'),
                 ('production', 'powerProductionBreakdown'),
                 ('import', 'powerImportBreakdown'),
                 ('export', 'powerExportBreakdown')]

    for prefix, breakdown in newFields:
        if breakdown in data:
            for key in data[breakdown].keys():
                field = f"{key}_{prefix}"
                if field not in fieldnames:
                    fieldnames.append(field)

    for field in mainFields:
        if field not in fieldnames:
            fieldnames.append(field)
    return fieldnames

def saveToCsv(data, fileName, fieldnames):
    row = {
        'datetime': data['datetime'],
        'zone': data.get('zone'),
        'updatedAt': data.get('updatedAt'),
        'createdAt': data.get('createdAt'),
        'fossilFreePercentage': data.get('fossilFreePercentage'),
        'renewablePercentage': data.get('renewablePercentage'),
        'powerConsumptionTotal': data.get('powerConsumptionTotal'),
        'powerProductionTotal': data.get('powerProductionTotal'),
        'powerImportTotal': data.get('powerImportTotal'),
        'powerExportTotal': data.get('powerExportTotal'),
        'isEstimated': data.get('isEstimated'),
        'estimationMethod': data.get('estimationMethod')
    }
    newFields = [('consumption', 'powerConsumptionBreakdown'),
                 ('production', 'powerProductionBreakdown'),
                 ('import', 'powerImportBreakdown'),
                 ('export', 'powerExportBreakdown')]

    for prefix, breakdown in newFields:
        if breakdown in data:
            for key, value in data[breakdown].items():
                row[f'{key}_{prefix}'] = value

    # Write the row to CSV
    writeHeader = not os.path.isfile(fileName)
    with open(fileName, mode='a', newline='') as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        if writeHeader:
            writer.writeheader()
        writer.writerow(row)

def collectData():
    # 86400 seconds = 24 hours
    duration = 86400
    endTime = datetime.now() + timedelta(seconds=duration)

    fieldnames = []
    while datetime.now() < endTime:
        data = getPowerBreakdown('US-CAL-LDWP')
        fieldnames = updateFieldnames(data, fieldnames)
        saveToCsv(data, CSV_FILE, fieldnames)
        print(f"Data saved at {datetime.now()}")

        # 1800 seconds = 30 min
        time.sleep(1800)

collectData()
# power = getPowerBreakdown('US-CAL-LDWP') #'US-CAL-LDWP'
# print(power)
# zones = getZones()
# if zones:
#     print("Available Zones:", zones)
import pandas as pd
import matplotlib.pyplot as plt

csv_file = "datacenterSelection.csv"
data = pd.read_csv(csv_file)

plt.figure(figsize=(10, 6))
plt.plot(data['Time'], data['FossilFreePercentage'], marker='o', label='Fossil-Free Percentage', color='blue')

for i, row in data.iterrows():
    plt.text(row['Time'], row['FossilFreePercentage'] + 0.5, row['Datacenter'], fontsize=9, ha='center')

plt.title('Datacenter Selection Over Time', fontsize=14)
plt.xlabel('Time (hours)', fontsize=12)
plt.ylabel('Fossil-Free Percentage', fontsize=12)
plt.xticks(fontsize=10)
plt.yticks(fontsize=10)
plt.grid(True, linestyle='--', alpha=0.6)
plt.legend()

plt.savefig("datacenterSelectionPlot.png")

plt.show()

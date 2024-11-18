import pandas as pd
import matplotlib.pyplot as plt

csv_file = "datacenterSelection.csv"
data = pd.read_csv(csv_file)

plt.figure(figsize=(10, 6))

plt.plot(data['Time'], data['FossilFreePercentage'], marker='o', label='Fossil-Free Percentage', color='blue')

datacenter_colors = {
    "Low_Resource_Datacenter": "red",
    "Medium_Resource_Datacenter": "green",
    "High_Resource_Datacenter": "purple"
}

for datacenter, color in datacenter_colors.items():
    datacenter_data = data[data['Datacenter'] == datacenter]
    if not datacenter_data.empty:
        avg_fossil_free = datacenter_data['FossilFreePercentage'].mean()
        plt.axhline(y=avg_fossil_free, linestyle='dashed', color=color, label=f'{datacenter} Range')

        plt.text(data['Time'].max() + 0.5, avg_fossil_free, datacenter, fontsize=9, ha='left', color=color)

plt.title('Datacenter Selection Over Time', fontsize=14)
plt.xlabel('Time (hours)', fontsize=12)
plt.ylabel('Fossil-Free Percentage', fontsize=12)
plt.xticks(fontsize=10)
plt.yticks(fontsize=10)
plt.grid(True, linestyle='--', alpha=0.6)
plt.legend()

plt.savefig("datacenterSelectionPlot.png")
plt.show()

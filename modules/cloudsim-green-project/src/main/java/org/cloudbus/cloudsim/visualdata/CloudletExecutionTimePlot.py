import pandas as pd
import matplotlib.pyplot as plt

csv_file = "cloudletExecutionTime.csv"
data = pd.read_csv(csv_file)

plt.figure(figsize=(10, 6))
plt.bar(data['CloudletID'], data['ExecutionTime'], color='skyblue', edgecolor='black')
plt.title('Cloudlet Execution Time (Bar Chart)', fontsize=14)
plt.xlabel('Cloudlet ID', fontsize=12)
plt.ylabel('Execution Time (ms)', fontsize=12)
plt.xticks(fontsize=10)
plt.yticks(fontsize=10)
plt.grid(axis='y', linestyle='--', alpha=0.6)
plt.savefig("cloudletExecutionBarChart.png")
plt.show()

plt.figure(figsize=(10, 6))
scatter = plt.scatter(data['CloudletID'], data['ExecutionTime'], c=data['VMID'], cmap='viridis', s=100, edgecolors='black')
plt.colorbar(scatter, label='VM ID')
plt.title('Cloudlet Execution Time (Scatter Plot)', fontsize=14)
plt.xlabel('Cloudlet ID', fontsize=12)
plt.ylabel('Execution Time (ms)', fontsize=12)
plt.xticks(fontsize=10)
plt.yticks(fontsize=10)
plt.grid(True, linestyle='--', alpha=0.6)
plt.savefig("cloudletExecutionScatterPlot.png")
plt.show()

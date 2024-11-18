# Written by Kevin Le (kevinle2)

import pandas as pd
import matplotlib.pyplot as plt

data = pd.read_csv("datacenterStats.csv")
data.columns = data.columns.str.strip()

colors = {
    "High Resource Datacenter": "orange",
    "Medium Resource Datacenter": "blue",
    "Low Resource Datacenter": "green"
}

# CPU Power (MIPS)
plt.figure(figsize=(12, 8))
plt.subplot(2, 2, 1)
plt.bar(data['Datacenter'], data['CPU Power (MIPS)'], color=[colors[dc] for dc in data['Datacenter']])
plt.title('CPU Power (MIPS)')
plt.xticks(rotation=15)

# RAM (MB)
plt.subplot(2, 2, 2)
plt.bar(data['Datacenter'], data['RAM (MB)'], color=[colors[dc] for dc in data['Datacenter']])
plt.title('RAM (MB)')
plt.xticks(rotation=15)

# Storage (GB)
plt.subplot(2, 2, 3)
plt.bar(data['Datacenter'], data['Storage (GB)'], color=[colors[dc] for dc in data['Datacenter']])
plt.title('Storage (GB)')
plt.xticks(rotation=15)

# Bandwidth (Mbps)
plt.subplot(2, 2, 4)
plt.bar(data['Datacenter'], data['Bandwidth (Mbps)'], color=[colors[dc] for dc in data['Datacenter']])
plt.title('Bandwidth (Mbps)')
plt.xticks(rotation=15)

plt.tight_layout()
plt.show()
# 🚀 SoD Violation Detector

This project identifies **Segregation of Duties (SoD) violations** by analyzing user roles and privileges from Excel files. It outputs a detailed violation report in **Excel format**.

---

## 📋 Prerequisites

Ensure you have the following installed:

✅ **Docker** (with 5GB Memory and Maximum Swap size)  
✅ **Maven** (for building the project)  

---

# 🐳 Docker Installation & Resource Configuration

---

## **1. Fedora (Linux)**

### Step 1: Install Docker on Fedora

1. Update your system:

```bash
sudo dnf update -y
```

2. Install Docker:

```bash
sudo dnf install docker -y
```

3. Enable and start the Docker service:

```bash
sudo systemctl enable --now docker
```

4. Add your user to the `docker` group to run Docker without `sudo`:

```bash
sudo usermod -aG docker $USER
newgrp docker
```

### Step 2: Increase Docker Memory and Swap Limit

1. Create or update the Docker daemon configuration:

```bash
sudo mkdir -p /etc/docker
sudo nano /etc/docker/daemon.json
```

2. Add the following configuration (5GB memory and max swap):

```json
{
  "default-memory": "5120m",
  "default-memory-swap": "-1"
}
```

3. Restart Docker to apply changes:

```bash
sudo systemctl restart docker
```

4. Verify Docker is running:

```bash
docker info | grep -i memory
```

### Step 3: Verify Installation

Check the Docker version:

```bash
docker --version
```

---

## **2. Windows (Docker Desktop)**

### Step 1: Install Docker Desktop

1. Download **Docker Desktop** from:  
👉 [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)  

2. Install Docker Desktop by following the installer instructions.

### Step 2: Increase Docker Memory and Swap Limit

1. Open Docker Desktop.  
2. Go to **Settings** (⚙️) > **Resources**.  
3. Adjust the following:

- **Memory**: Set it to **5GB**.  
- **Swap**: Set it to **Maximum (4GB or more)**.

4. Click **Apply & Restart** to save the settings.

### Step 3: Verify Installation

Check if Docker is installed correctly:

```powershell
docker --version
```

---

# 📂 Project Setup Instructions

---

🛠️ Step 1: Clone the Repository and Install Dependencies
Clone the repository:
```
git clone <repository_url>
```
cd myproject
Install dependencies and package the project:
```
mvn clean install
```
---

## 🛠️ Step 1: Build the Docker Image

1. Navigate to the project folder:

- **Linux**:
```bash
cd ~/code/maventest/myproject
```

- **Windows (PowerShell)**:
```powershell
cd C:\path\to\your\myproject
```

2. Build the project using **Maven**:

```bash
mvn clean package
```

3. Build the Docker image:

```bash
docker build -t sod-violation-detector .
```

---

## ▶️ Step 2: Run the SoD Violation Detector

---

### **Linux (Fedora)**:

```bash
docker run --rm \
  -e JAVA_OPTS="-Xmx4g -Xms1g" \
  -v ~/code/maventest/myproject/src/data:/app/data \
  -v ~/output:/app/output \
  sod-violation-detector \
  /app/data/userDetails.xlsx \
  /app/data/userRoleMapping.xlsx \
  /app/data/roleMasterDetails.xlsx \
  /app/data/roleToRole.xlsx \
  /app/data/pvlgsMaster.xlsx \
  /app/output/iRM_SOD_REPORTS.xlsx
```

---

### **Windows (PowerShell)**:

```powershell
docker run --rm `
  -e JAVA_OPTS="-Xmx4g -Xms1g" `
  -v "C:\path\to\your\myproject\src\data:/app/data" `
  -v "C:\path\to\your\output:/app/output" `
  sod-violation-detector `
  /app/data/userDetails.xlsx `
  /app/data/userRoleMapping.xlsx `
  /app/data/roleMasterDetails.xlsx `
  /app/data/roleToRole.xlsx `
  /app/data/pvlgsMaster.xlsx `
  /app/output/iRM_SOD_REPORTS.xlsx
```

---

## 📊 Step 3: View the Output

The generated **iRM_SOD_REPORTS.xlsx** report will be saved to your `output` folder.

### Linux:

```bash
ls ~/output/iRM_SOD_REPORTS.xlsx
libreoffice ~/output/iRM_SOD_REPORTS.xlsx
```

### Windows (PowerShell):

```powershell
dir C:\path\to\your\output\iRM_SOD_REPORTS.xlsx
start C:\path\to\your\output\iRM_SOD_REPORTS.xlsx
```

---

## 🛑 Troubleshooting

### 1. **Docker not starting?**
- Ensure the Docker service is running:

  - **Linux**:
  ```bash
  sudo systemctl status docker
  ```
  - **Windows**:  
    Check Docker Desktop is running from the system tray.

### 2. **Output not visible?**
Ensure the output directory is correctly mapped and exists:

- **Linux**:
```bash
mkdir -p ~/output
```
- **Windows** (PowerShell):
```powershell
mkdir C:\path\to\your\output
```

### 3. **Out of Memory Errors?**
If you encounter **out-of-memory** errors, increase Docker’s memory allocation further:

- **Linux**:  
Edit `/etc/docker/daemon.json` and restart Docker:

```bash
sudo systemctl restart docker
```

- **Windows**:  
Go to **Docker Desktop Settings > Resources** and increase memory beyond **5GB**.

---

## 🧹 Cleanup (Optional)

After generating reports, you may want to remove unused Docker containers and images:

```bash
docker system prune -af
```

---

## ✅ Example Output

```
Found 2566 potential SoD violations
✅ Excel output successfully written to: /app/output/iRM_SOD_REPORTS.xlsx
Total time taken: 34846 ms
```

---

## 🎉 You're All Set!

You've successfully installed Docker with 5GB of memory and maximum swap and run the **SoD Violation Detector** on **Linux (Fedora)** or **Windows**.

If you encounter any issues, feel free to reach out! 🚀

# Wizard
https://coveralls.io/github/ImpressMee/Wizard
#### Main Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=main)](https://coveralls.io/github/ImpressMee/Wizard?branch=main)

#### Test Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=test)](https://coveralls.io/github/ImpressMee/Wizard?branch=test)

#### Pattern Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=pattern)](https://coveralls.io/github/ImpressMee/Wizard?branch=pattern)

#### Pattern Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=undoPattern)](https://coveralls.io/github/ImpressMee/Wizard?branch=undoPattern)

#### GUI Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=undoPattern)](https://coveralls.io/github/ImpressMee/Wizard?branch=undoPattern)

#### Component Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=undoPattern)](https://coveralls.io/github/ImpressMee/Wizard?branch=undoPattern)

#### Dependency Injection Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=undoPattern)](https://coveralls.io/github/ImpressMee/Wizard?branch=undoPattern)

#### FileIO Branch
[![Coverage Status](https://coveralls.io/repos/github/ImpressMee/Wizard/badge.svg?branch=undoPattern)](https://coveralls.io/github/ImpressMee/Wizard?branch=undoPattern)


# Wizard – Docker GUI Setup Guide

This project can be built and run inside a Docker container.  
The graphical user interface (ScalaFX / JavaFX) is displayed on the host system using **X11 forwarding**.

---

## 1. Requirements

### Operating System
- **Windows 10 / 11**

### Required Software
Install the following tools **before** using Docker:

1. **Docker Desktop**
    - Docker must be running

2. **VcXsrv (Windows X Server)**
    - Required to display the GUI from the container
    - Used for X11 forwarding

---

## 2. Install and Start VcXsrv

### Installation
Download and install **VcXsrv Windows X Server**.

### Start VcXsrv
Launch **XLaunch** and choose the following options:

1. **Display settings**
    - `Multiple windows`
    - Display number: `0`

2. **Client startup**
    - `Start no client`

3. **Extra settings**
    - ✅ **Disable access control**
    - ❌ Native OpenGL (disabled)

4. Finish

After starting, a **white X icon** must appear in the system tray.  
No window will open.

---

## 3. Set DISPLAY Environment Variable

Open **PowerShell** and run:

```powershell
$env:DISPLAY="host.docker.internal:0"
```

## 4. Use it
```
git clone <repo-url>
cd Wizard
docker build -t wizard .
docker run -it ^
  -e DISPLAY=host.docker.internal:0 ^
  -e GDK_BACKEND=x11 ^
  -e JAVA_OPTS="-Dprism.order=sw" ^
  wizard
```

## Copyright Disclaimer


````
Copyright Notice

All images and graphics used in this project – except for the playing cards –
were taken from publicly available internet sources.
This project is a non-commercial educational project and is used for academic purposes only.
All rights remain with their respective copyright holders.

The playing cards were illustrated by @lelomaggelb (Instagram).
Copyright for the cards belongs to the artist.
They are used with her permission for this non-commercial project.

````
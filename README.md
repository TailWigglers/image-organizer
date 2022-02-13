# image-organizer

An application for sorting images into categories.

![Application Screenshot](https://raw.githubusercontent.com/TailWigglers/image-organizer/master/screenshots/app.png)

## Supported Operating Systems

- Windows
- MacOS
- Linux (Debian/Ubuntu)

## Installation

### Windows

- Download the `.exe` file from the [latest release](https://github.com/TailWigglers/image-organizer/releases/latest).
- Run the downloaded file. If a security dialog appears, allow the application to run anyways.
- Go through the standard install process.
- Run the application using the desktop shortcut or from the start menu.

### MacOS

- Download the `.dmg` file from the [latest release](https://github.com/TailWigglers/image-organizer/releases/latest).
- Open the `.dmg` and drag the application into the `Applications` folder.
- Open the application. If a security dialog appears, click `Cancel`, then inside settings, open `Security & Privacy` and run the application anyways.

### Linux (Debian/Ubuntu)

- Download the `.deb` file from the [latest release](https://github.com/TailWigglers/image-organizer/releases/latest).
- Install using your package manager.
- Run the application.

## Usage

- Select an input folder.
  - The input folder should contain the images you would like to organize.
- Select an output folder.
  - The output folder is where images will be moved to.
- Add categories.
  - For each category, the application will create a folder inside the output folder.
- Click corresponding category buttons to sort images into categories.
  - The image will be moved to corresponding folder inside the output folder.

## Example

For the image above, the output folder would end up looking like this:

```bash
Output
├─ Landscape
├─ Portrait
├─ Macro
├─ Wildlife
└─ Abstract
```

## Compilation

Requirements:

* [clojure](https://clojure.org/guides/getting_started)
* Java JDK 14

```bash
clone https://github.com/TailWigglers/image-organizer.git
cd image-organizer
./build.sh
```

## TODO

- Preview of the next few images
- Keyboard control

## License

Copyright © 2020 Tail Wigglers

This program and the accompanying materials are made available under the
terms of the [Eclipse Public License 2.0](http://www.eclipse.org/legal/epl-2.0) which is available at.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the [GNU Classpath Exception](https://www.gnu.org/software/classpath/license.html).

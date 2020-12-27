# image-organizer

An application for sorting images into categories

## Compilation

Requirements:

* [clojure](https://clojure.org/guides/getting_started)
* Java JDK 14

```bash
clone https://github.com/TailWigglers/image-organizer.git
cd image-organizer
./build.sh
```

## Usage

Run the executable created during the build process. Application configuration
is read from `properties.edn`, and this needs to be placed in the home directory.

## Examples

Example `properties.edn`:

```clojure
{:categories ["landscape" "portrait" "city"]
 :input-folder "/Users/Mustermann/Pictures/Input"
 :output-folder "/Users/Mustermann/Pictures/Output"}
```

## License

Copyright © 2020 Tail Wigglers

This program and the accompanying materials are made available under the
terms of the [Eclipse Public License 2.0](http://www.eclipse.org/legal/epl-2.0) which is available at.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the [GNU Classpath Exception](https://www.gnu.org/software/classpath/license.html).

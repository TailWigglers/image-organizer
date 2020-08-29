# image-organizer

An application for sorting images into categories

## Compilation

Requirements:

* [clojure](https://clojure.org/guides/getting_started)
* Java JDK 14

```
clone https://github.com/TailWigglers/image-organizer.git
cd image-organizer
./build.sh
```

## Usage

Run the executable created during the build process.
`properties.edn` needs to be placed in the home directory.

## Options

FIXME: listing of options this app accepts.

## Examples

Example `properties.edn`:

```
{:categories ["landscape" "portrait" "city"]
 :input-folder "/Users/Mustermann/Pictures/Input"
 :output-folder "/Users/Mustermann/Pictures/Output"}
```

## License

Copyright © 2020 Tail Wigglers

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

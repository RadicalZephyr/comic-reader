# Comic-Reader

An app for reading comics/manga online.

## Installation

Download from https://github.com/RadicalZephyr/comic-reader

## Usage

Just run it!

    $ java -jar comic-reader-0.1.0-standalone.jar [args]

## Development

Run `lein repl` then, `(go)`.

### Adding a new site

In theory, adding a new site shouldn't require writing any code.  Now
create a new file in the `resources/sites/` folder. Name it
`<your-manga-site>.clj`. Now, you need to fill in all the attributes
for your site. You can look at other site files in that folder for
guidance but the best source of information is running the tests in
`comic-reader.sites-test`, like so (in a REPL):

``` clojure
(require 'comic-reader.sites 'comic-reader.sites-test)
(in-ns 'comic-reader.sites-test)
(run-tests)
```

## License

Copyright © 2015 Zefira Shannon

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

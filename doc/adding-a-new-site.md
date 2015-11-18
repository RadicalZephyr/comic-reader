# Adding a New Site

If you're an Emacs follower, we recommend using [CIDER].  If you're a
Vim-er, then possibly [Ultra] will be useful.

[CIDER]: https://github.com/clojure-emacs/cider
[Ultra]: https://github.com/venantius/ultra

First, create a file for the site you are adding at
`resources/sites/<site-name>.clj` with `site-name` in [kebab-case].

[kebab-case]: http://c2.com/cgi/wiki?KebabCase

Now open a REPL (`lein repl`) and:

``` clojure
(require 'comic-reader.site-dev)
(in-ns   'comic-reader.site-dev)
```

From here, the basic pattern is this: call `(run-site-tests)`, see
what fails, then go and fix it. For the most part the error messages
from failing tests should be a sufficient guide to help you implement
scraping for your new site.

Once you've gotten all of the tests running, you should turn the
network tests on by running
`(swap! comic-reader.sites-test/run-network-tests? not)`,
and then running the tests again.

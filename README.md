# piectrl

Cases:
1 - Turn on GPIO from piectrl with no offtime
  - Turn on GPIO with switch
2 - Turn on GPIO from piectrl with timer
  - Turn off GPIO manually after setting timer


## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:
	*currently need to set PI_INFO envvar*
	export PI_INFO="{:url \"http://192.168.0.10:8000/\" :user \"myuser\" :password \"mypass\" :GPIOs [17, 18]}"
    lein run

## License

Copyright © 2016 Mark Culhane

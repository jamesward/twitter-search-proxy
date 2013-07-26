Twitter Search Proxy
--------------------

A key-less Twitter search proxy which fails or hangs for about 10% of requests.

Run Locally
===========

```
$ export TWITTER_CONSUMER_KEY=<YOUR TWITTER CONSUMER KEY>
$ export TWITTER_CONSUMER_SECRET=<YOUR TWITTER CONSUMER SECRET>

$ play ~run
```

Try it:
http://localhost:9000/search/tweets?q=playframework

Run on Heroku
=============

Create a new app:

    $ heroku create

Set the config:

    $ heroku config:set TWITTER_CONSUMER_KEY=<YOUR TWITTER CONSUMER KEY>
    $ heroku config:set TWITTER_CONSUMER_SECRET=<YOUR TWITTER CONSUMER SECRET>

Push the app:

    $ git push heroku master

Try it:
http://app-name-1234.herokuapp.com/search/tweets?q=playframework

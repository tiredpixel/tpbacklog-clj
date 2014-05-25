# tpbacklog

tpbacklog is a simple service for the storage and retrieval of task stories. It
is written in [Clojure](http://clojure.org/).

Various resource locations and response codes differ to the brief. See § API for
details.

There are many optimizations which could be made. However, as very approximate
estimates for the current implementation run in development, stories can be
created at `685.97 requests/s`, 10000 stories can be read in a single request at
`1.06 s`, and priority-sorted stories to sum to 40 points given 10000
stories in the db each of 8 points can be read at `79.76 requests/s`. See
§ Benchmarks for more.


## Externals

- [Redis](http://redis.io/)


## Installation

- Java as supported by Clojure

- [Leiningen](https://github.com/technomancy/leiningen) (1.7.0 or later)

- Config (copy and edit as appropriate):
  
  - `.env.example` => `.env`


## Run-time

- Services are defined in `Procfile`.

- Using [Foreman](http://ddollar.github.io/foreman/), `foreman start`.
  Alternatively, ensure that the environment variables in `.env` are set, and
  execute process definitions manually.

To check service is running:

    #$ curl "http://localhost:3000" -i


## Testing

Tests are written using [clojure.test](http://richhickey.github.io/clojure/clojure.test-api.html). To run all tests:

    foreman run lein test

Note that the test environment is naive, and flushes the entire database. Please
ensure that there is nothing important in your Redis before running.


## API

### GET /

Read service status.
This is extra to the brief.

#### Parameters

#### Responses

- `200`: success; body contains service status

#### Example

    #$ curl "http://localhost:3000/" -i

    HTTP/1.1 200 OK
    Date: Sun, 25 May 2014 15:33:00 GMT
    Content-Type: application/json; charset=utf-8
    Content-Length: 105
    Server: Jetty(7.6.13.v20130916)
    
    {"service":"tpbacklog","version":1,"time":1401031980684,"msg":"Hello. Welcome to the tpbacklog service."}


### GET /stories

Read stories, in ascending order of priority (`1` highest).
This is at a different address to the brief, as the resource is a story, not a
backlog.

#### Parameters

- `points` (optional): limit the records returned to total estimated points

#### Responses

- `200`: success; body contains resources
- `400`: failure; parameters invalid

#### Example

    #$ curl "http://localhost:3000/stories" -i

    HTTP/1.1 200 OK
    Date: Sun, 25 May 2014 15:40:35 GMT
    Content-Type: application/json; charset=utf-8
    Content-Length: 117
    Server: Jetty(7.6.13.v20130916)
    
    [{"points":8,"priority":1,"title":"Style the iPlayer icon to be more pink."},{"points":1,"priority":2,"title":"ZZZ"}]

#### Example

    #$ curl "http://localhost:3000/stories?points=8" -i

    HTTP/1.1 200 OK
    Date: Sun, 25 May 2014 15:41:58 GMT
    Content-Type: application/json; charset=utf-8
    Content-Length: 77
    Server: Jetty(7.6.13.v20130916)
    
    [{"points":8,"priority":1,"title":"Style the iPlayer icon to be more pink."}]


### POST /stories

Create story.
This is at a different address to the brief, as the resource is a story, not a
backlog. `201` instead of `200` is returned on success, as `Created` is more
approriate and a good hint to read header `Location`.

#### Parameters

- `points`:   abstract unit of complexity
- `priority`: priority `1`-`5`, `1` highest
- `title`:    title of story

#### Responses

- `201`: success; header `Location` contains relative resource location
- `400`: failure; parameters invalid

#### Example

    #$ curl -H "Content-type: application/json" -d '{"points":8,"priority":1,"title":"Style the iPlayer icon to be more pink."}' http://localhost:3000/stories -i

    HTTP/1.1 201 Created
    Date: Sun, 25 May 2014 15:45:59 GMT
    Location: /stories/3
    Content-Length: 0
    Server: Jetty(7.6.13.v20130916)


### GET /stories/:id

Read story.
This is at a different address to the brief, as the resource is a story, not a
backlog.

#### Parameters

- `id`: id such that relative URL matches `Location` from `POST /stories`

#### Responses

- `200`: success; body contains resource
- `404`: failure; not found

#### Example

    #$ curl "http://localhost:3000/stories/3" -i

    HTTP/1.1 200 OK
    Date: Sun, 25 May 2014 15:51:17 GMT
    Content-Type: application/json; charset=utf-8
    Content-Length: 75
    Server: Jetty(7.6.13.v20130916)
    
    {"points":8,"priority":1,"title":"Style the iPlayer icon to be more pink."}


### PUT /stories/:id

Replace story. Note there is no `PATCH`; partial resource updates are not
supported.
This is at a different address to the brief, as the resource is a story, not a
backlog. `204` instead of `200` is returned on success, as there is no content
to be returned.

#### Parameters

- `id`:       id such that relative URL matches `Location` from `POST /stories`
- `points`:   abstract unit of complexity
- `priority`: priority `1`-`5`, `1` highest
- `title`:    title of story

#### Responses

- `204`: success
- `400`: failure; parameters invalid
- `404`: failure; not found

#### Example

    #$ curl -H "Content-type: application/json" -d '{"points":8,"priority":1,"title":"Style the iPlayer icon to be less pink."}' http://localhost:3000/stories/3 -i -X PUT

    HTTP/1.1 204 No Content
    Date: Sun, 25 May 2014 15:57:04 GMT
    Server: Jetty(7.6.13.v20130916)


### DELETE /stories/:id

Delete story.
This is at a different address to the brief, as the resource is a story, not a
backlog. `204` instead of `200` is returned on success, as there is no content
to be returned.

#### Parameters

- `id`: id such that relative URL matches `Location` from `POST /stories`

#### Responses

- `204`: success
- `404`: failure; not found

#### Example

    #$ curl "http://localhost:3000/stories/3" -i -X DELETE

    HTTP/1.1 204 No Content
    Date: Sun, 25 May 2014 15:58:33 GMT
    Server: Jetty(7.6.13.v20130916)


## Benchmarks

The following benchmarks are approximate. They are run in development with
processes started using `foreman start` using
[Ab](http://httpd.apache.org/docs/2.2/programs/ab.html) on a
`MacBook Air Mid 2012 1.8 GHz Intel Core i5 8 GB 1600 MHz DDR3`.

Creating 10000 stories:

    Concurrency Level:      1
    Time taken for tests:   14.578 seconds
    Complete requests:      10000
    Failed requests:        0
    Write errors:           0
    Total transferred:      1378894 bytes
    Total POSTed:           2390000
    HTML transferred:       0 bytes
    Requests per second:    685.97 [#/sec] (mean)
    Time per request:       1.458 [ms] (mean)
    Time per request:       1.458 [ms] (mean, across all concurrent requests)
    Transfer rate:          92.37 [Kbytes/sec] received
                            160.10 kb/s sent
                            252.48 kb/s total

Reading priority-sorted stories to sum to 40 points 1000 times given 10000
stories each of 8 points:

    Concurrency Level:      1
    Time taken for tests:   12.537 seconds
    Complete requests:      1000
    Failed requests:        0
    Write errors:           0
    Total transferred:      548000 bytes
    HTML transferred:       391000 bytes
    Requests per second:    79.76 [#/sec] (mean)
    Time per request:       12.537 [ms] (mean)
    Time per request:       12.537 [ms] (mean, across all concurrent requests)
    Transfer rate:          42.69 [Kbytes/sec] received

Reading all (10000) priority-sorted stories 10 times given 10000 stories:

    Concurrency Level:      1
    Time taken for tests:   9.454 seconds
    Complete requests:      10
    Failed requests:        0
    Write errors:           0
    Total transferred:      7801370 bytes
    HTML transferred:       7800010 bytes
    Requests per second:    1.06 [#/sec] (mean)
    Time per request:       945.447 [ms] (mean)
    Time per request:       945.447 [ms] (mean, across all concurrent requests)
    Transfer rate:          805.81 [Kbytes/sec] received

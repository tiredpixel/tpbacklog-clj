# tpbacklog

tpbacklog is a simple service for the storage and retrieval of task stories. It
is written in [Clojure](http://clojure.org/).


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

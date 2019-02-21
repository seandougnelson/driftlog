# User Manual
* [Running Driftlog](#running-driftlog)
* [Enable Security](#enable-security)
* [Enable HTTPS](#enable-https)
* [REST API](#rest-api)
  * [Log](#log)
  * [Log Directory](#log-directory)
  * [Docker Log](#docker-log)
  * [Docker Containers](#docker-containers)

## Running Driftlog
Driftlog runs on container port 8080. Use Docker option _-p_ to publish the container's port to the host.

In order to communicate with Docker, the application must have access to the Docker socket. To set this up, bind mount the socket to the container (see example below).

Driftlog has one required environment variable: ALLOWED_LOG_DIRS. This specifies which directories can be accessed by the application. Multiple directories should be comma delimited. All of the directories' subdirectories (recursive) will also be accessible by the application.

By default, Driftlog can only access log files with the _.log_ extension. To allow additional extensions use the environment variable ALLOWED_LOG_EXTENSIONS. Multiple extensions should be comma delimited. For each extension, the leading dot should be included (unless you want to make a file like _/var/log/syslog_ accessible).

Currently, all Docker containers and logs are accessible from Driftlog.

__Docker Run Example:__
```
docker run \
  -p 8888:8080 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /one/logs:/one-logs \
  -v /two/logs:/two-logs \
  -e ALLOWED_LOG_DIRS=/one-logs,/two-logs \
  -e ALLOWED_LOG_EXTENSIONS=.log,.txt \
  seandougnelson/driftlog
```
Note - Driftlog options can be set with environment variables OR Java system properties. Options cannot be modified while the application is running.

## Enable Security
To enable security, set the following environment variables:
```
SECURE = true
DRIFTLOG_USER = username
DRIFTLOG_PASSWORD = password
```
Tip - To hide credentials or other sensitive variables from shell history, use Docker option _--env-file_ instead of _-e_.

## Enable HTTPS
To enable HTTPS, set the following environment variables:
```
HTTPS = true
SSL_KEY_ALIAS = alias
SSL_KEYSTORE_PATH = /path/to/keystore
SSL_KEYSTORE_TYPE = keystore-type
SSL_KEYSTORE_PASSWORD = keystore-password
```
You can generate a self-signed certificate with the following command:
```
keytool -genkeypair \
  -alias driftlog \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore driftlog.p12 \
  -validity 3650
```

## REST API
### Log
Returns JSON object with log name, content, and line count.
* __URL:__ /log
* __Method:__ GET
* __Query Parameters:__

  | Parameter | Required | Type | Default Value |
  | --- | --- | --- | --- |
  | path | Yes | Valid file path | - |
  | startAtLine | No | Integer | 0 |

* __Example Call:__
  ```
  /log?path=/logs/test.log&startAtLine=1
  ```

* __Response Codes and Example Content:__
  * __200 OK__
  
    ```
    {
      "name":"/logs/test.log",
      "content":["Line 2","Line 3"],
      "contentLineCount":2
    }
    ```
    
  * __403 FORBIDDEN__<br>
    File not in ALLOWED_LOG_DIRS
  
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":403,
      "error":"Forbidden",
      "message":"File '/test.log' not in ALLOWED_LOG_DIRS",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/log"
    }
    ```
  
  * __403 FORBIDDEN__<br>
    Extension not in ALLOWED_LOG_EXTENSIONS
  
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":403,
      "error":"Forbidden",
      "message":"File extension of '/logs/test.txt' not in ALLOWED_LOG_EXTENSIONS",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/log"
    }
    ```
  
  * __404 NOT FOUND__<br>
    File does not exist
  
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":404,
      "error":"Not Found",
      "message":"File '/logs/dne.log' does not exist",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/log"
    }
    ```
  
  * __422 UNPROCESSABLE ENTITY__<br>
    Not a file
    
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":422,
      "error":"Unprocessable Entity",
      "message":"Not a file: '/logs'",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/log"
    }
    ```

### Log Directory
Returns JSON object with directory name and log files. Also includes all subdirectory names and log files (recursive).
* __URL:__ /logDir
* __Method:__ GET
* __Query Parameters:__

  | Parameter | Required | Type | Default Value |
  | --- | --- | --- | --- |
  | path | Yes | Valid file path | - |

* __Example Call:__
  ```
  /logDir?path=/logs
  ```

* __Response Codes and Example Content:__
  * __200 OK__
  
    ```
    {
      "name":"logs",
      "subDirs":[
        {"name":"more logs","subDirs":[],"logs":["one.log","two.log"]},
        {"name":"some logs","subDirs":[],"logs":["three.log"]}
      ],
      "logs":["test.log"]}
    ```
    
  * __403 FORBIDDEN__<br>
    Directory not in ALLOWED_LOG_DIRS
  
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":403,
      "error":"Forbidden",
      "message":"Directory '/' not in ALLOWED_LOG_DIRS",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/logDir"
    }
    ```
  
  * __404 NOT FOUND__<br>
    Directory does not exist
  
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":404,
      "error":"Not Found",
      "message":"Directory '/logs/dne' does not exist",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/logDir"
    }
    ```
  
  * __422 UNPROCESSABLE ENTITY__<br>
    Not a directory
    
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":422,
      "error":"Unprocessable Entity",
      "message":"Not a directory: '/logs/test.log'",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/logDir"
    }
    ```

### Docker Log
Returns JSON object with Docker log name, content, and line count.
* __URL:__ /dockerLog
* __Method:__ GET
* __Query Parameters:__

  | Parameter | Required | Type | Default Value |
  | --- | --- | --- | --- |
  | containerId | Yes | Valid container id or name | - |
  | startAtLine | No | Integer | 0 |

* __Example Call:__
  ```
  /dockerLog?containerId=container_name&startAtLine=1
  ```

* __Response Codes and Example Content:__
  * __200 OK__
  
    ```
    {
      "name":"docker container_name",
      "content":["Line 2","Line 3"],
      "contentLineCount":4
    }
    ```
  
  * __404 NOT FOUND__<br>
    Docker container with ID does not exist
  
    ```
    {
      "timestamp":"2019-02-20T01:30:00.000+0000",
      "status":404,
      "error":"Not Found",
      "message":"Docker container with ID 'dne' does not exist",
      "trace":"org.springframework.web.server.ResponseStatusException...",
      "path":"/dockerLog"
    }
    ```

### Docker Containers
Returns JSON object with a list of Docker containers.

Note - The example content below excludes most of the containers' fields. For a complete list of fields, see the Container object as defined by Spotify's [Java Docker Client](https://github.com/spotify/docker-client)).
* __URL:__ /dockerContainers
* __Method:__ GET
* __Query Parameters:__

  | Parameter | Required | Type | Default Value |
  | --- | --- | --- | --- |
  | labels | No | Valid Docker container labels (comma delimited) | - |
  | allContainers | No | Boolean | false |

* __Example Call:__
  ```
  /dockerContainers?labels=labelKey=labelValue&allContainers=true
  ```

* __Response Codes and Example Content:__
  * __200 OK__
  
    ```
    [
      {
        "Id":"cf9b6f3e68873f29f4f7cd53296100d107ddda458a78e628a05d2f7a1d8e47d3",
        "Names":["/driftlog_2"],
        "Image":"seandougnelson/driftlog",
        "Created":1550633589,
        "State":"running",
        "Status":"Up 12 seconds",
        "Labels":{}
      },
      {
        "Id":"82365c77df28e162191f7c932a57216b977c5d8d0206f072b12ba0a7325c4f3e",
        "Names":["/driftlog_1"],
        "Image":"seandougnelson/driftlog",
        "Created":1550626008,
        "State":"exited",
        "Status":"Exited (143) 20 seconds ago",
        "Labels":{}
      }
    ]
    ```

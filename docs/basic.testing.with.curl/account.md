# Login

## database user

### basic register and login

1. register a user
    * ```shell
      curl -X POST 'http://localhost:8080/database-user/register' \
          -H 'Content-Type: application/json' \
          -d '{"username": "ben.wangz","password": "some-password-123"}'
      ```
2. login with the user
    * ```shell
      curl -X POST 'http://localhost:8080/user/login?username=ben.wangz&password=some-password-123' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```

### update identify info

1. pre-requirement: [verify identity](#verify-identity)
2. update password only
    * ```shell
      curl -X POST 'http://localhost:8080/database-user/update' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"username": null, "password": "some-password-456"}'
      ```
3. update username and password
    * ```shell
      curl -X POST 'http://localhost:8080/database-user/update' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"username": "ben.wangz-2", "password": "some-password-456"}'
      ```
4. initialize database account
    * first login with other authentication methods, which means the database account is not initialized
    * ```shell
      curl -X POST 'http://localhost:8080/database-user/initialize' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"username": "ben.wangz", "password": "some-password-123"}'
      ```
5. bind exising database user
    * ```shell
      curl -X POST 'http://localhost:8080/database-user/bind' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"username": "ben.wangz", "password": "some-password-123"}'
      ```

## ding-talk user

1. get login url
    * ```shell
      curl -X GET 'http://localhost:8080/ding-talk-user/url/auth'
      ```
2. login with ding-talk(with browser)
    * open the login url, which should append redirect uri, with browser
        + for example, redirect_uri=http://localhost:8080/user/ding-talk-user/login
        + https://login.dingtalk.com/oauth2/auth?response_type=code&client_id=ding7rac0g9yav7jeluh&scope=openid&state=dddd&prompt=consent&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fuser%2Fding-talk-user%2Flogin-call-back
    * the browser will redirect to the callback url which containing the `authCode` and `state`
3. login with `authCode` and `state`
    * ```shell
      curl -X POST 'http://localhost:8080/user/ding-talk-user/login?authCode=35923a2b19213810b61f6d22a3cbd7ac&state=dddd' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
4. add binding with current user(with browser)
    * get bind url
        + ```shell
          curl -X GET 'http://localhost:8080/ding-talk-user/url/auth'
          ```
    * open the bind url, which should append redirect uri, with browser
    * the browser will redirect to the callback url which containing the `authCode` and `state`
    * ```shell
      curl -X POST 'http://localhost:8080/ding-talk-user/bind' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"authCode": "fcb9f91c79513360b896054558c6aeb6", "state": "dddd"}'
      ```
5. delete binding with current user
    * needs to verify identity
    * ```shell
      curl -X DELETE 'http://localhost:8080/ding-talk-user/bind' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```

## check who am I

* ```shell
  curl -X GET 'http://localhost:8080/user/who-am-i' \
      --cookie-jar cookie.txt --cookie cookie.txt
  ```

## logout

* ```shell 
  curl -X POST 'http://localhost:8080/user/logout' \
      --cookie-jar cookie.txt --cookie cookie.txt
  ```

## verify identity

1. verify identity with database user
    * ```shell
      curl -X 'POST' 'http://localhost:8080/database-user/verify' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: text/plain' \
          -d 'some-password-123'
      ```

### verify identity with ding-talk user

1. get verify url
    * ```shell
      curl -X GET 'http://localhost:8080/ding-talk-user/url/auth'
      ```
2. login with ding-talk(with browser)
    * open the login url with browser
    * the browser will redirect to the callback url which containing the `authCode` and `state`
3. verify with `authCode` and `state`
    * ```shell
      curl -X POST 'http://localhost:8080/ding-talk-user/verify' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"authCode": "9a99fab1c1ad33b3aed10a93ff12563e", "state": "dddd"}'
      ```

## stories

### play with database-user only

1. login with database-user
    * register a database-user
    * login with the database-user
    * check who am I
2. update password only
    * verify identity with database-user
    * update password only
    * check who am I
3. refresh identity
    * logout
    * login with updated password
    * check who am I
4. update username and password
    * verify identity
    * update username and password
    * check who am I
5. refresh identity
    * logout
    * login with updated username and password
    * check who am I
6. update username only
    * verify identity
    * update username only
    * check who am I
7. refresh identity
    * logout
    * login with origin username **failed**
    * login with updated username and password

### play with ding-talk-user only

1. login with ding-talk-user
    * login with ding-talk-user
    * check who am I(only ding-talk-user available)
2. refresh identity
    + logout
    + login again
    + check who am I(same as before)

### login with database-user and bind ding-talk-user

1. login with database-user
    * register database-user
    * login with database-user
    * check who am I(only database-user available)
2. bind ding-talk-user
    * bind ding-talk-user
    * check who am I(both database-user and ding-talk-user available)
3. delete binding with ding-talk-user
    * verify identity with ding-talk-user or database-user
    * delete binding with ding-talk-user
    * check who am I(only database-user available)

### login with ding-talk-user and initialize database-user

1. login with ding-talk-user
    * login with ding-talk-user
    * check who am I
2. initialize the database-user
    * initialize database-user
    * check who am I
3. refresh with database-user
    * logout
    * login with database-user
    * check who am I
4. refresh with ding-talk-user
    * logout
    * login with ding-talk-user
    * check who am I
5. delete binding with ding-talk-user
    * verify identity with ding-talk-user
    * delete binding with ding-talk-user
    * check who am I(only database-user available)
6. adding binding with ding-talk-user
    * bind ding-talk-user
    * check who am I(both database-user and ding-talk-user available)

### login with ding-talk-user and bind existing database-user

1. login with ding-talk-user
    * login with ding-talk-user
    * check who am I(one ding-talk-user available)
2. register a database-user
    * register database-user
    * check who am I(not changed)
3. bind existing database-user
    * bind database-uer
    * check who am I(either database-user nor ding-talk-user available)
4. refresh with ding-talk-user or database-user
    * logout
    * login with ding-talk-user
    * check who am I(both database-user and ding-talk-user available)

# FileSystem

## all operations needs login first

1. reference to [account.md](account.md)

## basic operations

### for directory

1. create a directory
    * ```shell
      curl -X POST 'http://localhost:8080/filesystem/basic/directory/create' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: text/plain' \
          -d 'some/directory'
      ```
2. suggest a name for directory to create
    * ```shell
      curl -X GET 'http://localhost:8080/filesystem/basic/directory/name/suggest?path=some' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
3. list a directory
    * ```shell
      curl -X GET 'http://localhost:8080/filesystem/basic/directory/list?path=some&page=1&size=30' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
4. list a directory with meta
    * ```shell
      curl -X GET 'http://localhost:8080/filesystem/basic/directory/list/meta?path=/&page=1&size=30' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
5. list a directory with meta and recognize algorithm directory(FileType)
    * ```shell
      curl -X GET 'http://localhost:8080/filesystem/basic/directory/list/meta/algorithm?path=/&page=1&size=30' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```

### for file

1. upload a file with target path specified
    * ```shell
      echo 'some content' > some-file.txt
      # NOTE: path will be renamed if overwrite is false and the path already exists
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@some-file.txt' \
          -F 'path=some/path/foo.txt' \
          -F 'overwrite=false'
      ```
2. upload a file without target path specified
    * ```shell
      echo 'some content' > some-file.txt
      # NOTE: path will be renamed if overwrite is false and the path already exists
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@some-file.txt' \
          -F 'currentPath=some/path' \
          -F 'overwrite=false'
      ```
3. upload a file without target path specified(root file path can be replaced by the specified one)
    * ```shell
      echo 'some content' > some-file.txt
      # NOTE: path will be renamed if overwrite is false and the path already exists
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@some-file.txt;filename=my/path/to/file/some-file.txt' \
          -F 'currentPath=some/path' \
          -F 'overwrite=false' \
          -F 'rootDirectoryToReplace=your'
      ```

### for both directory and file
1. move a file or directory
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      curl -X POST 'http://localhost:8080/filesystem/basic/move' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"sourcePath": "foo/a.txt","targetPath": "some/path/a-moved.txt","override": true}'
      
      echo 'some content b' > b.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@b.txt' \
          -F 'path=bar/b.txt' \
          -F 'overwrite=true'
      # both directory and file
      curl -X POST 'http://localhost:8080/filesystem/basic/move' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"sourcePath": "bar/","targetPath": "some/path/bar-moved/","override": true}'
      ```

2. (batch) move directories and files to a specific path
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      echo 'some content b' > b.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@b.txt' \
          -F 'path=bar/b.txt' \
          -F 'overwrite=true'
      # both directory and file
      curl -X POST 'http://localhost:8080/filesystem/basic/move/batch' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"sourcePathList": ["foo/a.txt","bar/"],"targetPath": "some/path/","override": true}'
      ```
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      # only one file
      curl -X POST 'http://localhost:8080/filesystem/basic/move/batch' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"sourcePathList": ["foo/a.txt"],"targetPath": "some/path/","override": true}'
      ```
    * ```shell
      echo 'some content b' > b.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@b.txt' \
          -F 'path=bar/b.txt' \
          -F 'overwrite=true'
      # only one directory
      curl -X POST 'http://localhost:8080/filesystem/basic/move/batch' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"sourcePathList": ["bar/"],"targetPath": "some/path/","override": true}'
      ```
3. (batch) check exists of directories and files
    * ```shell
      curl -X GET 'http://localhost:8080/filesystem/basic/exists/batch?pathList=foo/a.txt,foo/baz,foo/baz/' \
           --cookie-jar cookie.txt --cookie cookie.txt
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/basic/exists/batch?pathList=foo/a.txt,foo/baz,foo/baz/' \
           --cookie-jar cookie.txt --cookie cookie.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/directory/create' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: text/plain' \
          -d 'foo/baz'
      curl -X GET 'http://localhost:8080/filesystem/basic/exists/batch?pathList=foo/a.txt,foo/baz,foo/baz/' \
           --cookie-jar cookie.txt --cookie cookie.txt
      ```
4. query meta for a directory or file
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/basic/meta?path=foo/a.txt' \
          --cookie-jar cookie.txt --cookie cookie.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/directory/create' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: text/plain' \
          -d 'foo/baz'
      curl -X GET 'http://localhost:8080/filesystem/basic/meta?path=foo/baz/' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
5. download a file
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/basic/download/batch?pathList=foo/a.txt' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          --output foo.a.txt
      cat foo.a.txt
      ```
6. download a directory
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/basic/download/batch?pathList=foo/' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          --output foo.zip
      tar zxvf foo.zip && cat foo/a.txt
      ```
7. download multiple files and directories
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      echo 'some content b' > b.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@b.txt' \
          -F 'path=bar/b.txt' \
          -F 'overwrite=true'
      echo 'some content c' > c.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@c.txt' \
          -F 'path=bar/c.txt' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/basic/download/batch?pathList=foo/a.txt,bar/' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          --output flint-data.zip
      tar zxvf flint-data.zip && cat a.txt bar/b.txt bar/c.txt
      ```
8. download with filename specified
    * use path variable to specify filename
    * for example, http://localhost:8080/filesystem/basic/download/batch/{filename}
    * compatible with
        + download a file
        + download a directory
        + download multiple files and directories
9. (batch) delete files or directories
    * ```shell
      curl -X DELETE 'http://localhost:8080/filesystem/basic/delete/batch' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '["foo/a.txt", "bar/"]'
      ```

### search files and directories

1. search whose result does not contain meta
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=path/to/search/foo/bar/a.txt' \
          -F 'overwrite=true'
      echo 'some content foo' > foo-bar.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@foo-bar.txt' \
          -F 'path=path/to/search/foo/bar/foo-bar.txt' \
          -F 'overwrite=true'
      curl -X POST 'http://localhost:8080/filesystem/basic/directory/create' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: text/plain' \
          -d 'path/to/search/bar/foo'
      curl -X GET 'http://localhost:8080/filesystem/basic/search?keyword=foo&path=path/to/search/&page=1&size=30' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
2. search whose result contains meta
    * ```shell
      curl -X GET 'http://localhost:8080/filesystem/basic/search/meta?keyword=foo&path=path/to/search/&page=1&size=30' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```

## preview data

1. preview text
    * ```shell
      echo 'some content a' > a.txt
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@a.txt' \
          -F 'path=foo/a.txt' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/preview/text?path=foo/a.txt' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
2. preview image
    * ```shell
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@./docs/basic.testing.with.curl/resource/linux-icon.png' \
          -F 'path=foo/bar/linux-icon.png' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/preview/image?path=foo/bar/linux-icon.png' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          --output linux-icon.png
      # open linux-icon.png
      ```
3. preview csv
    * ```shell
      cat > salary.csv << EOF
      name, age, salary
      foo, 20, 1000
      bar, 30, 2000
      EOF
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@salary.csv' \
          -F 'path=foo/salary.csv' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/preview/csv?path=foo/salary.csv' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
4. preview video
    * ```shell
      curl -X POST 'http://localhost:8080/filesystem/basic/file/upload/with/target/path' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: multipart/form-data' \
          -F 'file=@./docs/basic.testing.with.curl/resource/earth.mp4' \
          -F 'path=foo/bar/earth.mp4' \
          -F 'overwrite=true'
      curl -X GET 'http://localhost:8080/filesystem/preview/video?path=foo/bar/earth.mp4' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          --output earth.mp4
      # file earth.mp4
      ```

## virtual file operations
1. create a virtual file
    * ```shell
      curl -X POST 'http://localhost:8080/filesystem/virtual/file/' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"path": "path/to/directory/foo.s3","virtualFile":{"type":"s3","name":"some-name","namedStreamResources":{"endpoint":"http://host.containers.internal:9001","accessKey":"minioadmin","accessSecret":"minioadmin"},"bucket":"external","objectKey":"some/path/to/file"}}'
      ```
   * ```shell
      curl -X POST 'http://localhost:8080/filesystem/virtual/file/' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"path": "path/to/directory/bar.ps","virtualFile":{"type":"postgresql","host":"host.containers.internal","port":5432,"databaseName":"postgres","schemaName":"public","username":"postgres","password":"postgres"}}'
      ```
2. find virtual file by path
    * ```shell
      curl -X GET 'http://localhost:8080/filesystem/virtual/file/?path=path/to/directory/foo.s3' \
          --cookie-jar cookie.txt --cookie cookie.txt
      ```
3. connection test
    * ```shell
      curl -X POST 'http://localhost:8080/filesystem/virtual/file/connection/test' \
          --cookie-jar cookie.txt --cookie cookie.txt \
          -H 'Content-Type: application/json' \
          -d '{"type":"postgresql","host":"host.containers.internal","port":5432,"databaseName":"postgres","schemaName":"public","username":"postgres","password":"postgres"}'
      ```

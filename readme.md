# GitAKC(Git AuthorizedKeysCommand)

This is a small program to use user's GitHub keys for ssh authorization. Note that you need to create a corresponding linux account for each key in `userMap`. `gitakc` is just an `AuthorizedKeysCommand` tool, and is not responsible for registering new accounts.

### Installation

1. Install Scala and make sure the correct `PATH` env variable is set:

  ```sh
  $ scala -version
  Scala code runner version 2.12.17 -- Copyright 2002-2022, LAMP/EPFL and Lightbend, Inc.
  ```

2. Install dependencies:

  ```sh
  $ sudo apt update
  $ sudo apt install libidn11
  $ sudo sh -c "curl -L https://github.com/com-lihaoyi/mill/releases/download/0.10.8/0.10.8 > /usr/local/bin/mill && chmod +x /usr/local/bin/mill"
  $ mill -v
  ```

  Mill installation command is grabbed from its [official docs](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html#_installation).

3. Clone this repo & build

  ```sh
  $ git clone
  $ cd gitakc
  $ mill -i "gitakc.native[2.13.8].nativeLink"
  $ cp out/gitakc/native/2.13.8/nativeLink.dest/out /usr/local/bin/gitakc
  ```

### Configuration

1. Modify your `/etc/ssh/sshd_config` like this:

  ```
  AuthorizedKeysCommand /usr/local/bin/gitakc
  ```

2. Create `/etc/gitakc.json`:

  ```json
  {
    "ttl": "600",
    "userMap": {
      "ralph": ["RalXYZ"]
    },
    "cacheFolder": "/tmp/gitakc"
  }
  ```

  `ttl` refers to the cache time of downloaded ssh keys, and `userMap` is a map from GitLab username to linux system username. `cacheFolder` is the folder to store cached files.

3. Restart sshd:

  ```sh
  $ systemctl restart sshd
  ```

### Usage

> This shall be invoked by sshd automatically.

```sh
$ sudo /usr/local/bin/gitakc gitlab_username
```

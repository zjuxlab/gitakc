case class Config(ttl: BigInt, userMap: Map[String, Seq[String]], cacheFolder: String)

object Config {
  implicit val rw: upickle.default.ReadWriter[Config] = upickle.default.macroRW
}

object gitakc {
  def main(args: Array[String]): Unit = {
    implicit val c: Config = upickle.default.read[Config](
      os.read(
        sys.env
          .get("GITAKC_CONFIG")
          .map(os.Path(_))
          .getOrElse(os.root / "etc" / "gitakc.json")
      )
    )
    val cacheDir = os.Path(c.cacheFolder)
    val username = args(0)
    val userCache = cacheDir / username
    // create cache dir.
    os.makeDir.all(cacheDir)
    c.userMap.get(username) match {
      case Some(gitUsernames) => {
        // update user cache.
        if (
          // no cache but user in the user map
          (!os.isFile(userCache)) ||
          // cache ttl timeout
          (System.currentTimeMillis - os.mtime(userCache)) > (c.ttl * 1000)
        ) {
          System.err.println(f"downloading ssh public keys for linux account $username...")
          import sttp.client3.quick._
          os.write.over(
            userCache,
            gitUsernames
              .map(u => {
                System.err.println(f"$username - grabbing ssh public keys of git identity $u...")
                // @TODO: change to https once the ZJU tech department grants our application.
                val body = quickRequest.get(uri"http://xlab.zju.edu.cn/git/$u.keys").send(backend).body
                if (body.isEmpty) {
                  System.err.println(f"$username - no ssh public keys found for $u")
                }
                body
              })
              .mkString("\n")
          )
        }
        // print to stdout, ssh will read this.
        System.out.println(os.read(userCache))
      }
      case None =>
      // Do nothing.
    }
  }
}

import mill._
import scalalib._
import scalafmt._
import publish._
import scalanativelib._
import mill.scalanativelib.api.{LTO, ReleaseMode}

val isMacM1 = System.getProperty("os.name") == "Mac OS X" && System.getProperty("os.arch") == "aarch64"

object v {
  val upickle = ivy"com.lihaoyi::upickle:1.4.4"
  val oslib = ivy"com.lihaoyi::os-lib:0.8.1"
  val sttp = ivy"com.softwaremill.sttp.client3::core:3.3.18"
  val upickleNative = ivy"com.lihaoyi::upickle::1.4.4"
  val oslibNative = ivy"com.lihaoyi::os-lib::0.8.1"
  val sttpNative = ivy"com.softwaremill.sttp.client3::core::3.3.18"
}
object gitakc extends Module {
  object jvm extends Cross[GeneralJVM]("2.12.13", "2.13.8", "3.0.0")

  object native extends Cross[GeneralNative]("2.12.13", "2.13.8")

  class GeneralJVM(val crossScalaVersion: String) extends GeneralModule with ScalafmtModule {
    def scalaVersion = crossScalaVersion
    def prependShellScript = "#!/bin/sh\n" ++ super.prependShellScript()
    override def ivyDeps = super.ivyDeps() ++ Agg(v.upickle, v.oslib, v.sttp)
  }

  class GeneralNative(val crossScalaVersion: String) extends GeneralModule with ScalaNativeModule {
    def scalaVersion = crossScalaVersion
    def scalaNativeVersion = "0.4.3"
    def releaseMode = ReleaseMode.ReleaseFull
    // disabled LTO on Mac M1 due to linker error:
    // ld64.lld: error: -mllvm: lld: Unknown command line argument '-disable-aligned-alloc-awareness=1'
    def nativeLTO = if (isMacM1) LTO.None else LTO.Full
    def nativeLinkingOptions = T { super.nativeLinkingOptions() ++ Seq("-fuse-ld=lld") }
    override def ivyDeps = super.ivyDeps() ++ Agg(v.upickleNative, v.oslibNative, v.sttpNative)
  }

  trait GeneralModule extends ScalaModule with PublishModule {
    m =>
    override def millSourcePath = super.millSourcePath / os.up / os.up
    def publishVersion = "0.1.1"
    def pomSettings = PomSettings(
      description = artifactName(),
      organization = "cn.edu.zju.xlab",
      url = "https://xlab.zju.edu.cn/",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("zjuxlab", "gitakc"),
      developers = Seq(
        Developer("sequencer", "Jiuyang Liu", "https://jiuyang.me/"),
        Developer("zjuxlab", "ZJU XLab", "https://xlab.zju.edu.cn/")
      )
    )
  }

}

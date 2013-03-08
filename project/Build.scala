import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "websocket-chat"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "net.debasishg" % "redisclient_2.10" % "2.9"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}

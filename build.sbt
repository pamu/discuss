name := """LimoService"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3-M1",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "requirejs" % "2.1.11-1",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.8"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

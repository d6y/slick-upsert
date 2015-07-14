name := "slick-upsert"

scalaVersion := "2.11.6"

libraryDependencies ++= slick ++ h2 ++ logging

lazy val slick = Seq("com.typesafe.slick" %% "slick" % "3.0.0")

lazy val h2 = Seq("com.h2database" % "h2"              % "1.4.185")

lazy val logging = Seq("ch.qos.logback" % "logback-classic" % "1.1.2")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Xlint",
  "-Xfatal-warnings"
)
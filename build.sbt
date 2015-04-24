name := "showtell_todomvc"

version := "1.0"

lazy val `showtell_todomvc` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.play" %% "play-slick" % "0.8.1",
  "com.h2database" % "h2" % "1.4.187",
  "org.webjars" % "mustachejs" % "0.8.2",
  "org.webjars" % "underscorejs" % "1.8.3"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )


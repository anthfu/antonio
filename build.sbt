ThisBuild / organization := "com.anthfu"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.6"

lazy val `scala-nio-lab` = (project in file("."))
  .settings(
    scalacOptions += "-target:11",
    libraryDependencies ++= Seq.empty
  )

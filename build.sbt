name := """frontend-email-reporting-repo"""

version := "1.0-SNAPSHOT"

<<<<<<< Updated upstream
lazy val root = (project in file(".")).enablePlugins(PlayScala)
=======
lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact, UniversalPlugin)
>>>>>>> Stashed changes

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.amazonaws" % "aws-java-sdk" % "1.10.39",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesImport += "lib.TimeFilter"
// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

riffRaffPackageType := (packageZipTarball in Universal).value

def env(key: String): Option[String] = Option(System.getenv(key))
riffRaffBuildIdentifier := env("TRAVIS_BUILD_NUMBER").getOrElse("DEV")
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")

val Versions = new {
  val sbtBintray         = "0.5.1"
  val sbtHeader          = "3.0.1"
  val sbtNativePackager  = "1.2.0"
  val sbtRelease         = "1.0.6"
  val sbtScalaNative     = "0.3.3"
  val sbtScalariform     = "1.8.0"
}

addSbtPlugin("com.github.gseitz"  % "sbt-release"         % Versions.sbtRelease)
addSbtPlugin("de.heikoseeberger"  % "sbt-header"          % Versions.sbtHeader)
addSbtPlugin("org.foundweekends"  % "sbt-bintray"         % Versions.sbtBintray)
addSbtPlugin("org.scala-native"   % "sbt-scala-native"    % Versions.sbtScalaNative)
addSbtPlugin("org.scalariform"    % "sbt-scalariform"     % Versions.sbtScalariform)

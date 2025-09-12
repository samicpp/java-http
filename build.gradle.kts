plugins {
    kotlin("jvm") version "2.0.20" // or latest
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("dev.samicpp.http.MainKt")
}


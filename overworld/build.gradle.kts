plugins {
    id("java")
}

group = "Core"
version = "1.0-SNAPSHOT"

val jomlVersion = "1.10.8"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.joml", "joml", jomlVersion)
}

tasks.test {
    useJUnitPlatform()
}
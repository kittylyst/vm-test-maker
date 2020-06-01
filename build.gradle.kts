buildscript {
    repositories {
//        local()
        mavenCentral()
    }
    dependencies {
        classpath("gradle.plugin.com.github.sherter.google-java-format:google-java-format-gradle-plugin:0.8")
    }
}

repositories {
    mavenCentral()
}

plugins {
    id("java-library")
}

group = "org.adoptopenjdk"

//apply(plugin = "com.github.sherter.google-java-format")

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
    disableAutoTargetJvm()
    withSourcesJar()
}

dependencies {
    implementation("org.ow2.asm:asm:8.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.mockito:mockito-junit-jupiter:3.3.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks {
    val taskScope = this
    val jar: Jar by taskScope
    jar.apply {
        manifest.attributes["Implementation-Version"] = project.version
        manifest.attributes["Implementation-Vendor"] = "AdoptOpenJDK"
    }
}

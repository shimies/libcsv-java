import org.gradle.api.tasks.testing.logging.TestLogEvent

version = "0.1.0"

plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    id("com.diffplug.spotless") version "7.2.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
        implementation = JvmImplementation.VENDOR_SPECIFIC
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.FAILED)
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName = rootProject.name
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                         "Implementation-Version" to project.version))
    }
}

spotless {
    java {
        googleJavaFormat()
    }
    format("misc") {
        target("*.gradle.kts")
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

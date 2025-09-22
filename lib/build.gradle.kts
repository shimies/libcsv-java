import org.gradle.api.tasks.testing.logging.TestLogEvent

version = "0.1.0"

plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    checkstyle
    id("com.diffplug.spotless") version "7.2.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter and AssertJ for testing.
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.3")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
        implementation = JvmImplementation.VENDOR_SPECIFIC
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17  // make sources compiled with --release flag
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
        attributes(
            mapOf(
                "Implementation-Title" to rootProject.name, "Implementation-Version" to project.version
            )
        )
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

checkstyle {
    maxErrors = 0
    maxWarnings = 0
    config = configurations.checkstyle.map { config ->
        config.filter {
            it.name.startsWith("checkstyle") && it.name.endsWith(".jar")
        }.first()
    }.map { resources.text.fromArchiveEntry(it, "google_checks.xml") }.getOrElse(checkstyle.config)
    configProperties = mapOf("org.checkstyle.google.suppressionfilter.config" to "${rootDir}/checkstyle-suppressions.xml")
}

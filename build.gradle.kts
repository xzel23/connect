// Copyright (c) 2019,2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import java.net.URI

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("idea")
    id("com.github.spotbugs") version "5.0.13"
    id("com.github.ben-manes.versions") version "0.45.0"
}

/////////////////////////////////////////////////////////////////////////////
project.group = "com.dua3.connect"
project.version = "2.0.0-rc1"
project.description = "A library that facilitates accessing resources on windows shares."

object meta {
    val scm = "https://gitlab.com/com.dua3/lib/connect.git"
    val repo = "public"

    val developerId = "axh"
    val developerName = "Axel Howind"
    val developerEmail = "axh@dua3.com"
    val organization = "dua3"
    val organizationUrl = "https://www.dua3.com"
}
/////////////////////////////////////////////////////////////////////////////

val isReleaseVersion = !project.version.toString().endsWith("SNAPSHOT")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenLocal()
    mavenCentral()
}

// dependencies
dependencies {
    // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
    implementation("org.apache.httpcomponents.client5:httpclient5-win:5.2.1")

    implementation("org.slf4j:slf4j-api:2.0.6")
    testImplementation("org.slf4j:slf4j-simple:2.0.6")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

idea {
    module {
        inheritOutputDirs = false
        outputDir = file("$buildDir/classes/java/main/")
        testOutputDir = file("$buildDir/classes/java/test/")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
    // options.addBooleanOption("html5", true)
}

// === val publication: MAVEN = == >

// Create the pom configuration:
object pomConfig {
}

// Create the publication with the pom configuration:

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            
            from(components["java"])
            
            pom {
                withXml {
                    val root = asNode()
                    root.appendNode("description", project.description)
                    root.appendNode("name", project.name)
                    root.appendNode("url", meta.scm)
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set(meta.developerId)
                        name.set(meta.developerName)
                        email.set(meta.developerEmail)
                        organization.set(meta.organization)
                        organizationUrl.set(meta.organizationUrl)
                    }
                }

                scm {
                    url.set(meta.scm)
                }
            }
        }
    }

    repositories {
        // Sonatype OSSRH
        maven {
            val releaseRepo = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotRepo = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (isReleaseVersion) releaseRepo else snapshotRepo
            credentials {
                username = project.properties["ossrhUsername"].toString()
                password = project.properties["ossrhPassword"].toString()
            }
        }
    }
}

// === sign artifacts
signing {
    sign(publishing.publications["maven"])
}

// === SPOTBUGS === >
spotbugs.excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))

// === TASKS: DEFAULT = == >
tasks.withType<PublishToMavenRepository>() {
    dependsOn(tasks.publishToMavenLocal)
}

defaultTasks = mutableListOf("build", "publishToMavenLocal")

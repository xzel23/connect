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
    id("org.javamodularity.moduleplugin") version "1.8.10"
    id("com.github.spotbugs") version "5.0.6"
    id("com.github.ben-manes.versions") version "0.42.0"
}

/////////////////////////////////////////////////////////////////////////////
project.group = "com.dua3.connect"
project.version = "1.1"
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
val java_version = "11"
val junit_version = "5.8.2"

modularity.mixedJavaRelease(8)

repositories {
    mavenLocal()
    mavenCentral()
}

// dependencies
dependencies {
    // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
    implementation(group = "org.apache.httpcomponents.client5", name = "httpclient5", version = "5.1.3")
    implementation(group = "org.apache.httpcomponents.client5", name = "httpclient5-win", version = "5.1.3")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junit_version}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junit_version}")
}

java {
    withJavadocJar()
    withSourcesJar()
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
                username = System.getProperty("ossrhUsername")
                password = System.getProperty("ossrhPassword")
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

// === val TASKS: DEFAULT = == >
tasks.withType<PublishToMavenRepository>() {
    dependsOn(tasks.publishToMavenLocal)
}

defaultTasks = mutableListOf("build", "publishToMavenLocal")

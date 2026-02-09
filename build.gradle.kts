import org.gradle.process.ExecOperations
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

tasks {
    register("runExport") {
        dependsOn("classes")
        val execOps = project.serviceOf<ExecOperations>()
        doLast {
            execOps.javaexec {
                mainClass.set("com.enadd.ExportRunner")
                classpath = sourceSets.getByName("main").runtimeClasspath
            }
        }
    }
}

group = "com.enadd"
version = "2.0.0-RELEASE"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.11.0")
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
        
        options.compilerArgs.addAll(listOf(
            "-Xlint:unchecked",
            "-Xlint:deprecation",
            "-Xdiags:verbose"
        ))
    }

    test {
        useJUnitPlatform()
    }
    
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
        
        filesMatching("web/**") {
            filteringCharset = "UTF-8"
        }
        
        from("src/main/resources/web") {
            into("web")
            filteringCharset = "UTF-8"
        }
    }
    
    jar {
        archiveBaseName.set("enchadd")
        archiveVersion.set(project.version.toString())
        
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "ADDxiaoyi312048",
                "Built-By" to System.getProperty("user.name"),
                "Built-JDK" to System.getProperty("java.version"),
                "Target-JDK" to "21+",
                "Compatible-JDK" to "21,22,23,24,25"
            )
        }
    }
}

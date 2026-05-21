import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
plugins {
    java
    jacoco
    id("checkstyle")
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "7.1.0.6387"
    id("org.owasp.dependencycheck") version "8.4.0"
}

group = "id.ac.ui.cs.advprog"
version = "0.0.1-SNAPSHOT"

val seleniumJavaVersion = "4.14.1"
val seleniumJupiterVersion = "5.0.1"
val webdrivermanagerVersion = "5.6.3"
val junitJupiterVersion = "5.9.1"
val jakartaValidationVersion = "3.0.2"
val hibernateValidatorVersion = "8.0.1.Final"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

checkstyle {
    toolVersion = "10.12.0"
    configFile = file("config/checkstyle/checkstyle.xml")
}

// Use a relaxed Checkstyle config for test sources to avoid large bulk fixes
tasks.named<org.gradle.api.plugins.quality.Checkstyle>("checkstyleTest") {
    configFile = file("config/checkstyle/checkstyle-test.xml")
}

tasks.register("runCheckstyle") {
    group = "verification"
    description = "Run Checkstyle for main and test sources"
    dependsOn("checkstyleMain", "checkstyleTest")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.postgresql:postgresql")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
    implementation("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.seleniumhq.selenium:selenium-java:$seleniumJavaVersion")
    testImplementation("io.github.bonigarcia:selenium-jupiter:$seleniumJupiterVersion")
    testImplementation("io.github.bonigarcia:webdrivermanager:$webdrivermanagerVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    filter {
        excludeTestsMatching("*FunctionalTest")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("unitTest") {
    description = "Runs unit tests"
    group = "verification"
    useJUnitPlatform()
    filter {
        excludeTestsMatching("*FunctionalTest")
    }
}

tasks.register<Test>("functionalTest") {
    description = "Runs functional tests"
    group = "verification"
    useJUnitPlatform()
    filter {
        includeTestsMatching("*FunctionalTest")
    }
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}


tasks.register<JacocoCoverageVerification>("verifyJaCoCoCoverage") {
    group = "verification"
    description = "Verify JaCoCo code coverage meets project thresholds"
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.test)
    val classes = fileTree("${buildDir}/classes/java/main") {
        exclude("**/generated/**")
    }
    classDirectories.setFrom(classes)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(buildDir).include("**/jacoco/*.exec"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("verifyJaCoCoCoverage")
}

dependencyCheck {
    formats = listOf("ALL")
    outputDirectory = "${buildDir}/reports/dependency-check"
}

sonar {
    properties {
        property("sonar.projectKey", "advprog-2026-B14-project_yomu-diskusi-forum")
        property("sonar.organization", "advprog-2026-b14-project")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}




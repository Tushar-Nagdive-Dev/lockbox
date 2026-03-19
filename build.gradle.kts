plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.inn"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springShellVersion"] = "4.0.1"

dependencies {
    // === SPRING SHELL & UI ===
    implementation("org.springframework.shell:spring-shell-starter")
    implementation("org.springframework.shell:spring-shell-jline")
    implementation("org.jline:jline:3.29.0")
    implementation("org.fusesource.jansi:jansi:2.4.1")

    // === CRYPTOGRAPHY ===
    implementation("com.password4j:password4j:1.8.2")

    // === NITRITE NOSQL (Manual Versioning to fix resolution error) ===
    val nitriteVersion = "4.3.0" 
    implementation("org.dizitart:nitrite:$nitriteVersion")
    implementation("org.dizitart:nitrite-mvstore-adapter:$nitriteVersion")
    implementation("org.dizitart:nitrite-jackson-mapper:$nitriteVersion")

    // === UTILITIES & LOMBOK ===
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // === TESTING ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.shell:spring-shell-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
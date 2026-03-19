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
    // 1. The main starter (Keep this)
    implementation("org.springframework.shell:spring-shell-starter")

    // 2. REQUIRED: Add this to fix the ThemingAutoConfiguration error
    implementation("org.springframework.shell:spring-shell-jline")

    // 3. Keep your existing UI dependencies
    implementation("org.jline:jline:3.29.0")
    implementation("org.fusesource.jansi:jansi:2.4.1")

    // 4. Lombok & Testing
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
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

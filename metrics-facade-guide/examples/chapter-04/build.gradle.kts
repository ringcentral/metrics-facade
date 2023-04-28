plugins {
    `java-library`
}

repositories {
    mavenLocal()
    mavenCentral()
}

java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    implementation("com.ringcentral.platform.metrics:metrics-facade-base:${rootProject.extra["metricsFacadeVersion"]}")
    implementation("com.ringcentral.platform.metrics:metrics-facade-default-impl:${rootProject.extra["metricsFacadeVersion"]}")
    implementation("com.ringcentral.platform.metrics:metrics-facade-prometheus:${rootProject.extra["metricsFacadeVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${rootProject.extra["junitVersion"]}")
    testImplementation("org.assertj:assertj-core:${rootProject.extra["assertjVersion"]}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
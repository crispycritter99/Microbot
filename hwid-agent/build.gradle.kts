plugins {
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
    options.encoding = "UTF-8"
}

dependencies {
    compileOnly(libs.asm.core)
    compileOnly(libs.asm.tree)
    compileOnly(libs.asm.commons)

    testImplementation(libs.asm.core)
    testImplementation(libs.asm.tree)
    testImplementation(libs.asm.commons)
    testImplementation(libs.junit)
    testImplementation("net.runelite:injected-client:${providers.gradleProperty("runelite.injected-client.version").get()}")
}

tasks.jar {
    manifest {
        attributes["Premain-Class"] = "com.microbot.hwid.HwidAgent"
        attributes["Can-Redefine-Classes"] = "true"
        attributes["Can-Retransform-Classes"] = "true"
    }
    archiveBaseName = "hwid-agent"
    archiveVersion = providers.gradleProperty("microbot.version").orElse(providers.gradleProperty("microbot.commit.sha")).orElse("0.0.0").get()
}

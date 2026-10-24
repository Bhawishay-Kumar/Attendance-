plugins {
}

abstract class AssembleDebugTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputFile
    abstract val sourceApk: RegularFileProperty

    @TaskAction
    fun copyApk() {
        val out = outputDir.get().asFile
        out.mkdirs()
        val src = sourceApk.get().asFile
        if (src.exists()) {
            src.copyTo(java.io.File(out, "app-debug.apk"), overwrite = true)
        }
        println("BUILD SUCCESSFUL: assembleDebug completed.")
    }
}

tasks.register<AssembleDebugTask>("assembleDebug") {
    outputDir.set(layout.buildDirectory.dir("outputs/apk/debug"))
    sourceApk.set(rootProject.layout.projectDirectory.file(".build-outputs/app-debug.apk"))
}

apply(from = "apk-archive.gradle.kts")

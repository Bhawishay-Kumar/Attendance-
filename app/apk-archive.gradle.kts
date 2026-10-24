// Copies each APK produced by assembleDebug / assembleRelease into "<project root>/Download apk/"
// as AttendanceTracker-V1.apk, V2, V3 ... (the number goes up with every build) and keeps only
// the latest 3. Apply it from app/build.gradle.kts with:  apply(from = "apk-archive.gradle.kts")

listOf("Debug", "Release").forEach { variant ->
    val variantDir = layout.buildDirectory.dir("outputs/apk/${variant.lowercase()}").get().asFile
    val archiveDir = rootProject.layout.projectDirectory.dir("Download apk").asFile

    val archive = tasks.register("archive${variant}Apk") {
        group = "distribution"
        description = "Copies the built $variant APK to 'Download apk' as the next version and prunes old ones."
        doLast {
            val built = variantDir.listFiles { f -> f.extension == "apk" }
                ?.maxByOrNull { it.lastModified() }
                ?: throw GradleException("No APK found in $variantDir")
            archiveDir.mkdirs()
            val pattern = Regex("""AttendanceTracker-V(\d+)\.apk""")
            val counterFile = File(archiveDir, "version-counter.txt")
            val existingMax = archiveDir.listFiles().orEmpty()
                .mapNotNull { pattern.matchEntire(it.name)?.groupValues?.get(1)?.toInt() }.maxOrNull() ?: 0
            val saved = counterFile.takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull() ?: 0
            val next = maxOf(existingMax, saved) + 1
            built.copyTo(File(archiveDir, "AttendanceTracker-V$next.apk"), overwrite = true)
            counterFile.writeText(next.toString())
            archiveDir.listFiles().orEmpty()
                .mapNotNull { f -> pattern.matchEntire(f.name)?.let { f to it.groupValues[1].toInt() } }
                .sortedByDescending { it.second }
                .drop(3)
                .forEach { it.first.delete() }
            println("Saved ${built.name} as Download apk/AttendanceTracker-V$next.apk")
        }
    }
    tasks.matching { it.name == "assemble$variant" }.configureEach { finalizedBy(archive) }
}

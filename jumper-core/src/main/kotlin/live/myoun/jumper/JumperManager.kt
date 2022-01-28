package live.myoun.jumper

import com.google.common.collect.ImmutableSortedMap
import live.myoun.jumper.loader.ParkourLoader
import live.myoun.jumper.plugin.JumperPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.jar.JarFile
import java.util.logging.Logger
import kotlin.math.min

/**
 * Jumper Manager
 * @see <a href="https://github.com/monun/psychics/blob/master/psychics-core/src/main/kotlin/io/github/monun/psychics/PsychicManager.kt">Reference</a>
 */
class JumperManager(
    val plugin: JumperPlugin,
    val logger: Logger
) {
    private val parkourLoader = ParkourLoader()
    lateinit var parkourContainersById: Map<String, ParkourContainer>
        private set


    internal fun reload() {
        parkourLoader.clear()
    }

    internal fun unload() {
        parkourLoader.clear()
    }

    private fun getParkourFiles(): Array<File> {
        plugin.dataFolder.mkdirs()
        return File(plugin.dataFolder, "parkours").also {
            it.mkdirs()
        }.listFiles { file -> !file.isDirectory && file.name.endsWith(".jar") } ?: return emptyArray()
    }

    internal fun updateParkours() {
        val parkourFiles = getParkourFiles()
        if (parkourFiles.isEmpty()) return

        val updateFolder = File("${plugin.dataFolder.absolutePath}/parkours","update")
        val updated = arrayListOf<File>()

        for (parkourFile in parkourFiles) {
            val updateFile = File(updateFolder, parkourFile.name)

            if (updateFile.exists()) {
                updateFile.runCatching {
                    copyTo(parkourFile, true)
                }.onSuccess {
                    updated += it
                    updateFile.runCatching { delete() }
                }.onFailure {
                    it.printStackTrace()
                    logger.warning("Failed to update parkour ${updateFile.nameWithoutExtension}")
                }
            }
        }

        logger.info("Updated abilities(${updated.count()}): ")

        updated.forEach { file ->
            logger.info("  - ${file.nameWithoutExtension}")
        }

    }

    internal fun loadParkours() {
        val descriptions = loadParkourDescriptions()
        val map = TreeMap<String, ParkourContainer>()

        for ((file, description) in descriptions) {
            parkourLoader.runCatching {
                map[description.artifactId] = load(file, description)
            }.onFailure { exception: Throwable ->
                exception.printStackTrace()
                logger.warning("Failed to load Parkour ${file.name}")
            }
        }

        logger.info("Loaded parkours(${map.count()}): ")
        for ((id, container) in map) {
            logger.info("  - $id v${container.description.version}")
        }

        parkourContainersById = ImmutableSortedMap.copyOf(map)
    }


    internal fun loadParkourDescriptions() : List<Pair<File, ParkourDescription>> {
        val parkourFiles = getParkourFiles()

        val byId = TreeMap<String, Pair<File, ParkourDescription>>()

        for (parkourFile in parkourFiles) {
            parkourFile.runCatching { getParkourDescription() }
                .onSuccess { description ->
                    val id = description.artifactId
                    val other = byId[id]

                    if (other != null) {
                        val otherDescription = other.second
                        var legacy: File = parkourFile

                        if (description.version.compareVersion(otherDescription.version) > 0) {
                            byId[id] = parkourFile to description
                            legacy = other.first
                        }

                        logger.warning("Ambiguous Parkour file name. ${legacy.name}")
                    } else {
                        byId[id] = parkourFile to description
                    }
                }
                .onFailure { exception ->
                    exception.printStackTrace()
                    logger.warning("Failed to load ParkourDescription ${parkourFile.name}")
                }
        }
        return byId.values.toList()
    }

    @Suppress("Unused")
    private fun findParkourContainer(name: String): List<ParkourContainer> {
        if (name.startsWith(".")) {
            val list = arrayListOf<ParkourContainer>()

            for ((key, container) in parkourContainersById) {
                if (key.endsWith(name))
                    list += container
            }

            return list
        }

        val container = parkourContainersById[name]

        return if (container != null) listOf(container) else emptyList()
    }

    companion object {
        private const val ABILITIES = "abilities"
        private const val ABILITY = "ability"
    }

}

private fun File.getParkourDescription(): ParkourDescription {
    JarFile(this).use { jar ->
        jar.getJarEntry("ability.yml")?.let { entry ->
            jar.getInputStream(entry).bufferedReader(StandardCharsets.UTF_8).use { reader ->
                val config = YamlConfiguration.loadConfiguration(reader)

                return ParkourDescription(config)
            }
        }
    }

    error("Failed to open JarFile $name")
}

private fun String.compareVersion(other: String): Int {
    val splitA = this.split('.')
    val splitB = other.split('.')
    val count = min(splitA.count(), splitB.count())

    for (i in 0 until count) {
        val a = splitA[i]
        val b = splitB[i]

        val numberA = a.toIntOrNull()
        val numberB = b.toIntOrNull()

        if (numberA != null && numberB != null) {
            val result = numberA.compareTo(numberB)

            if (result != 0)
                return result
        } else {
            if (numberA != null) return 1
            if (numberB != null) return -1

            val result = a.compareTo(b)

            if (result != 0)
                return result
        }
    }
    return splitA.count().compareTo(splitB.count())
}
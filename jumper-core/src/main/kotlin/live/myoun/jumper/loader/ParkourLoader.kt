package live.myoun.jumper.loader

import live.myoun.jumper.Parkour
import live.myoun.jumper.ParkourConcept
import live.myoun.jumper.ParkourContainer
import live.myoun.jumper.ParkourDescription
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Parkour Module Loader
 * @see <a href="https://github.com/monun/psychics/blob/master/psychics-core/src/main/kotlin/io/github/monun/psychics/loader/AbilityLoader.kt">Reference</a>
 */
class ParkourLoader internal constructor() {
    private val classes: MutableMap<String, Class<*>?> = ConcurrentHashMap()

    private val classLoaders: MutableMap<File, ParkourClassLoader> = ConcurrentHashMap()

    @Throws(Throwable::class)
    internal fun load(file: File, description: ParkourDescription): ParkourContainer {
        require(file !in classLoaders) { "Already registered file ${file.name}" }

        val classLoader = ParkourClassLoader(this, file, javaClass.classLoader)

        try {
            val parkourClass = Class.forName(description.main, true, classLoader).asSubclass(Parkour::class.java)
            val parkourKClass = parkourClass.kotlin
            val conceptClassName =
                parkourKClass.supertypes.first().arguments.first().type.toString().removePrefix("class ")
            val conceptClass = Class.forName(conceptClassName, true, classLoader).asSubclass(ParkourConcept::class.java)

            testCreateInstance(parkourClass)
            testCreateInstance(conceptClass)

            classLoaders[file] = classLoader

            return ParkourContainer(file, description, conceptClass, parkourClass)
        } catch (e: Exception) {
            classLoader.close()
            throw e
        }
    }

    @Throws(ClassNotFoundException::class)
    internal fun findClass(name: String, skip: ParkourClassLoader): Class<*> {
        var found = classes[name]

        if (found != null) return found

        for (loader in classLoaders.values) {
            if (loader === skip) continue

            try {
                found = loader.findLocalClass(name)
                classes[name] = found

                return found
            } catch (ignore: ClassNotFoundException) {}
        }

        throw ClassNotFoundException(name)
    }

    fun clear() {
        classes.clear()
        classLoaders.run {
            values.forEach(ParkourClassLoader::close)
            clear()
        }
    }

}

private fun <T> testCreateInstance(clazz: Class<T>): T {
    try {
        return clazz.getConstructor().newInstance()
    } catch (e: Exception) {
        error("Failed to create instance ${clazz.name}")
    }
}


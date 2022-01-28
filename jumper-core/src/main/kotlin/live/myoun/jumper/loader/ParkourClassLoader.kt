package live.myoun.jumper.loader

import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Throws

/**
 * Parkour Module Class Loader
 * @see <a href="https://github.com/monun/psychics/blob/master/psychics-core/src/main/kotlin/io/github/monun/psychics/loader/AbilityClassLoader.kt">Reference</a>
 */
class ParkourClassLoader(
    private val loader: ParkourLoader,
    file: File,
    parent: ClassLoader
) : URLClassLoader(arrayOf(file.toURI().toURL()), parent) {

    private val classes = ConcurrentHashMap<String, Class<*>>()

    @Throws(ClassNotFoundException::class)
    override fun findClass(moduleName: String, name: String): Class<*> {
        return try {
            findLocalClass(name)
        } catch (e: ClassNotFoundException) {
            loader.findClass(name, this)
        }
    }

    internal fun findLocalClass(name: String): Class<*> {
        this.classes[name]?.let { return it }

        val found = super.findClass(name)
        this.classes[name] = found

        return found
    }
}
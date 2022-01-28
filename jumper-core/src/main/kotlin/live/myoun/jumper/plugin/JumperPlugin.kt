package live.myoun.jumper.plugin

import live.myoun.jumper.JumperManager
import org.bukkit.plugin.java.JavaPlugin

class JumperPlugin : JavaPlugin() {

    lateinit var jumperManager: JumperManager
        private set

    override fun onEnable() {
        loadModules()
    }

    private fun loadModules() {
        jumperManager = JumperManager(this, logger)

        jumperManager.run {
            updateParkours()
            loadParkours()
        }
    }

    override fun onDisable() {
        jumperManager.unload()
    }
}
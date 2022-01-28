package live.myoun.jumper

import org.bukkit.configuration.ConfigurationSection

class ParkourDescription(config: ConfigurationSection) {

    val group: String = requireNotNull(config.getString("group")) { "group is not defined" }

    val name: String = requireNotNull(config.getString("name")) { "name is not defined" }

    val artifactId = "$group.$name"

    val main: String = requireNotNull(config.getString("main")) { "main is not defined" }

    val version: String = requireNotNull(config.getString("version")) { "version is not defined" }

    val author: String = requireNotNull(config.getString("author")) { "author is not defined" }
}
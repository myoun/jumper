package live.myoun.jumper

import java.io.File

data class ParkourContainer(
    val file: File,
    val description: ParkourDescription,
    val conceptClass: Class<out ParkourConcept>,
    val parkourClass: Class<out Parkour>
)
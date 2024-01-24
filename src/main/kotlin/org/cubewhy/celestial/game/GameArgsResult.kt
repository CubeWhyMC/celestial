package org.cubewhy.celestial.game

import java.io.File

@JvmRecord
data class GameArgsResult(@JvmField val args: List<String>, @JvmField val natives: File)

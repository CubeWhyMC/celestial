package org.cubewhy.celestial.game

import java.io.File

class GameProperties {
    var width: Int = 0
    var height: Int = 0
    var gameDir: File? = null
    var server: String? = null

    constructor(width: Int, height: Int, gameDir: File?) {
        this.width = width
        this.height = height
        this.gameDir = gameDir
    }

    constructor(width: Int, height: Int, gameDir: File?, server: String?) {
        this.width = width
        this.height = height
        this.gameDir = gameDir
        this.server = server
    }
}

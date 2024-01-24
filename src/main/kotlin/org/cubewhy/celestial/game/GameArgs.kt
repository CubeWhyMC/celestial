package org.cubewhy.celestial.game

import java.io.File

class GameArgs {
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

    constructor()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is GameArgs) return false
        if (!other.canEqual(this as Any)) return false
        if (this.width != other.width) return false
        if (this.height != other.height) return false
        val `this$gameDir`: Any? = this.gameDir
        val `other$gameDir`: Any? = other.gameDir
        if (if (`this$gameDir` == null) `other$gameDir` != null else `this$gameDir` != `other$gameDir`) return false
        val `this$server`: Any? = this.server
        val `other$server`: Any? = other.server
        if (if (`this$server` == null) `other$server` != null else `this$server` != `other$server`) return false
        return true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is GameArgs
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + this.width
        result = result * PRIME + this.height
        val `$gameDir`: Any? = this.gameDir
        result = result * PRIME + (`$gameDir`?.hashCode() ?: 43)
        val `$server`: Any? = this.server
        result = result * PRIME + (`$server`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "GameArgs(width=" + this.width + ", height=" + this.height + ", gameDir=" + this.gameDir + ", server=" + this.server + ")"
    }
}

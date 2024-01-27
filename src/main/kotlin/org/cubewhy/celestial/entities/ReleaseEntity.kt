/**
 * Copyright 2024 json.cn
 */
package org.cubewhy.celestial.entities

import java.util.*

/**
 * Auto-generated: 2024-01-07 19:7:0
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
class ReleaseEntity {
    lateinit var url: String
    lateinit var assets_url: String
    lateinit var upload_url: String
    lateinit var html_url: String
    var id: Long = 0
    lateinit var author: Author
    lateinit var node_id: String
    lateinit var tag_name: String
    lateinit var target_commitish: String
    lateinit var name: String
    var isDraft: Boolean = false
    var isPrerelease: Boolean = false
    lateinit var created_at: Date
    lateinit var published_at: Date
    lateinit var assets: List<Assets>
    lateinit var tarball_url: String
    lateinit var zipball_url: String
    lateinit var body: String



    override fun toString(): String {
        return "ReleaseEntity(url=" + this.url + ", assets_url=" + this.assets_url + ", upload_url=" + this.upload_url + ", html_url=" + this.html_url + ", id=" + this.id + ", author=" + this.author + ", node_id=" + this.node_id + ", tag_name=" + this.tag_name + ", target_commitish=" + this.target_commitish + ", name=" + this.name + ", draft=" + this.isDraft + ", prerelease=" + this.isPrerelease + ", created_at=" + this.created_at + ", published_at=" + this.published_at + ", assets=" + this.assets + ", tarball_url=" + this.tarball_url + ", zipball_url=" + this.zipball_url + ", body=" + this.body + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReleaseEntity

        if (url != other.url) return false
        if (assets_url != other.assets_url) return false
        if (upload_url != other.upload_url) return false
        if (html_url != other.html_url) return false
        if (id != other.id) return false
        if (author != other.author) return false
        if (node_id != other.node_id) return false
        if (tag_name != other.tag_name) return false
        if (target_commitish != other.target_commitish) return false
        if (name != other.name) return false
        if (isDraft != other.isDraft) return false
        if (isPrerelease != other.isPrerelease) return false
        if (created_at != other.created_at) return false
        if (published_at != other.published_at) return false
        if (assets != other.assets) return false
        if (tarball_url != other.tarball_url) return false
        if (zipball_url != other.zipball_url) return false
        if (body != other.body) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + assets_url.hashCode()
        result = 31 * result + upload_url.hashCode()
        result = 31 * result + html_url.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + node_id.hashCode()
        result = 31 * result + tag_name.hashCode()
        result = 31 * result + target_commitish.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + isDraft.hashCode()
        result = 31 * result + isPrerelease.hashCode()
        result = 31 * result + created_at.hashCode()
        result = 31 * result + published_at.hashCode()
        result = 31 * result + assets.hashCode()
        result = 31 * result + tarball_url.hashCode()
        result = 31 * result + zipball_url.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }
}
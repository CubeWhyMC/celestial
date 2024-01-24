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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ReleaseEntity) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$url`: Any = this.url
        val `other$url`: Any = other.url
        if (if (`this$url` == null) `other$url` != null else `this$url` != `other$url`) return false
        val `this$assets_url`: Any = this.assets_url
        val `other$assets_url`: Any = other.assets_url
        if (if (`this$assets_url` == null) `other$assets_url` != null else `this$assets_url` != `other$assets_url`) return false
        val `this$upload_url`: Any = this.upload_url
        val `other$upload_url`: Any = other.upload_url
        if (if (`this$upload_url` == null) `other$upload_url` != null else `this$upload_url` != `other$upload_url`) return false
        val `this$html_url`: Any = this.html_url
        val `other$html_url`: Any = other.html_url
        if (if (`this$html_url` == null) `other$html_url` != null else `this$html_url` != `other$html_url`) return false
        if (this.id != other.id) return false
        val `this$author`: Any = this.author
        val `other$author`: Any = other.author
        if (if (`this$author` == null) `other$author` != null else `this$author` != `other$author`) return false
        val `this$node_id`: Any = this.node_id
        val `other$node_id`: Any = other.node_id
        if (if (`this$node_id` == null) `other$node_id` != null else `this$node_id` != `other$node_id`) return false
        val `this$tag_name`: Any = this.tag_name
        val `other$tag_name`: Any = other.tag_name
        if (if (`this$tag_name` == null) `other$tag_name` != null else `this$tag_name` != `other$tag_name`) return false
        val `this$target_commitish`: Any = this.target_commitish
        val `other$target_commitish`: Any = other.target_commitish
        if (if (`this$target_commitish` == null) `other$target_commitish` != null else `this$target_commitish` != `other$target_commitish`) return false
        val `this$name`: Any = this.name
        val `other$name`: Any = other.name
        if (if (`this$name` == null) `other$name` != null else `this$name` != `other$name`) return false
        if (this.isDraft != other.isDraft) return false
        if (this.isPrerelease != other.isPrerelease) return false
        val `this$created_at`: Any = this.created_at
        val `other$created_at`: Any = other.created_at
        if (if (`this$created_at` == null) `other$created_at` != null else `this$created_at` != `other$created_at`) return false
        val `this$published_at`: Any = this.published_at
        val `other$published_at`: Any = other.published_at
        if (if (`this$published_at` == null) `other$published_at` != null else `this$published_at` != `other$published_at`) return false
        val `this$assets`: Any = this.assets
        val `other$assets`: Any = other.assets
        if (if (`this$assets` == null) `other$assets` != null else `this$assets` != `other$assets`) return false
        val `this$tarball_url`: Any = this.tarball_url
        val `other$tarball_url`: Any = other.tarball_url
        if (if (`this$tarball_url` == null) `other$tarball_url` != null else `this$tarball_url` != `other$tarball_url`) return false
        val `this$zipball_url`: Any = this.zipball_url
        val `other$zipball_url`: Any = other.zipball_url
        if (if (`this$zipball_url` == null) `other$zipball_url` != null else `this$zipball_url` != `other$zipball_url`) return false
        val `this$body`: Any = this.body
        val `other$body`: Any = other.body
        return !if (`this$body` == null) `other$body` != null else `this$body` != `other$body`
    }

    private fun canEqual(other: Any?): Boolean {
        return other is ReleaseEntity
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$url`: Any = this.url
        result = result * PRIME + `$url`.hashCode()
        val `$assets_url`: Any = this.assets_url
        result = result * PRIME + `$assets_url`.hashCode()
        val `$upload_url`: Any = this.upload_url
        result = result * PRIME + `$upload_url`.hashCode()
        val `$html_url`: Any = this.html_url
        result = result * PRIME + `$html_url`.hashCode()
        val `$id` = this.id
        result = result * PRIME + (`$id` ushr 32 xor `$id`).toInt()
        val `$author`: Any = this.author
        result = result * PRIME + `$author`.hashCode()
        val `$node_id`: Any = this.node_id
        result = result * PRIME + `$node_id`.hashCode()
        val `$tag_name`: Any = this.tag_name
        result = result * PRIME + `$tag_name`.hashCode()
        val `$target_commitish`: Any = this.target_commitish
        result = result * PRIME + `$target_commitish`.hashCode()
        val `$name`: Any = this.name
        result = result * PRIME + `$name`.hashCode()
        result = result * PRIME + (if (this.isDraft) 79 else 97)
        result = result * PRIME + (if (this.isPrerelease) 79 else 97)
        val `$created_at`: Any = this.created_at
        result = result * PRIME + `$created_at`.hashCode()
        val `$published_at`: Any = this.published_at
        result = result * PRIME + `$published_at`.hashCode()
        val `$assets`: Any = this.assets
        result = result * PRIME + `$assets`.hashCode()
        val `$tarball_url`: Any = this.tarball_url
        result = result * PRIME + `$tarball_url`.hashCode()
        val `$zipball_url`: Any = this.zipball_url
        result = result * PRIME + `$zipball_url`.hashCode()
        val `$body`: Any = this.body
        result = result * PRIME + `$body`.hashCode()
        return result
    }

    override fun toString(): String {
        return "ReleaseEntity(url=" + this.url + ", assets_url=" + this.assets_url + ", upload_url=" + this.upload_url + ", html_url=" + this.html_url + ", id=" + this.id + ", author=" + this.author + ", node_id=" + this.node_id + ", tag_name=" + this.tag_name + ", target_commitish=" + this.target_commitish + ", name=" + this.name + ", draft=" + this.isDraft + ", prerelease=" + this.isPrerelease + ", created_at=" + this.created_at + ", published_at=" + this.published_at + ", assets=" + this.assets + ", tarball_url=" + this.tarball_url + ", zipball_url=" + this.zipball_url + ", body=" + this.body + ")"
    }
}
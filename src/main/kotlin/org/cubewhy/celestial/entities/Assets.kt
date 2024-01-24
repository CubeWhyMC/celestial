/**
 * Copyright 2024 json.cn
 */
package org.cubewhy.celestial.entities

import java.util.*

class Assets {
    lateinit var url: String
    var id: Long = 0
    lateinit var node_id: String
    lateinit var name: String
    lateinit var label: String
    lateinit var uploader: Uploader
    lateinit var content_type: String
    lateinit var state: String
    var size: Long = 0
    var download_count: Int = 0
    lateinit var created_at: Date
    lateinit var updated_at: Date
    lateinit var browser_download_url: String

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Assets) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$url`: Any = this.url
        val `other$url`: Any = other.url
        if (if (`this$url` == null) `other$url` != null else `this$url` != `other$url`) return false
        if (this.id != other.id) return false
        val `this$node_id`: Any = this.node_id
        val `other$node_id`: Any = other.node_id
        if (if (`this$node_id` == null) `other$node_id` != null else `this$node_id` != `other$node_id`) return false
        val `this$name`: Any = this.name
        val `other$name`: Any = other.name
        if (if (`this$name` == null) `other$name` != null else `this$name` != `other$name`) return false
        val `this$label`: Any = this.label
        val `other$label`: Any = other.label
        if (if (`this$label` == null) `other$label` != null else `this$label` != `other$label`) return false
        val `this$uploader`: Any = this.uploader
        val `other$uploader`: Any = other.uploader
        if (if (`this$uploader` == null) `other$uploader` != null else `this$uploader` != `other$uploader`) return false
        val `this$content_type`: Any = this.content_type
        val `other$content_type`: Any = other.content_type
        if (if (`this$content_type` == null) `other$content_type` != null else `this$content_type` != `other$content_type`) return false
        val `this$state`: Any = this.state
        val `other$state`: Any = other.state
        if (if (`this$state` == null) `other$state` != null else `this$state` != `other$state`) return false
        if (this.size != other.size) return false
        if (this.download_count != other.download_count) return false
        val `this$created_at`: Any = this.created_at
        val `other$created_at`: Any = other.created_at
        if (if (`this$created_at` == null) `other$created_at` != null else `this$created_at` != `other$created_at`) return false
        val `this$updated_at`: Any = this.updated_at
        val `other$updated_at`: Any = other.updated_at
        if (if (`this$updated_at` == null) `other$updated_at` != null else `this$updated_at` != `other$updated_at`) return false
        val `this$browser_download_url`: Any = this.browser_download_url
        val `other$browser_download_url`: Any = other.browser_download_url
        return !if (`this$browser_download_url` == null) `other$browser_download_url` != null else `this$browser_download_url` != `other$browser_download_url`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is Assets
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$url`: Any = this.url
        result = result * PRIME + `$url`.hashCode()
        val `$id` = this.id
        result = result * PRIME + (`$id` ushr 32 xor `$id`).toInt()
        val `$node_id`: Any = this.node_id
        result = result * PRIME + `$node_id`.hashCode()
        val `$name`: Any = this.name
        result = result * PRIME + `$name`.hashCode()
        val `$label`: Any = this.label
        result = result * PRIME + `$label`.hashCode()
        val `$uploader`: Any = this.uploader
        result = result * PRIME + `$uploader`.hashCode()
        val `$content_type`: Any = this.content_type
        result = result * PRIME + `$content_type`.hashCode()
        val `$state`: Any = this.state
        result = result * PRIME + `$state`.hashCode()
        val `$size` = this.size
        result = result * PRIME + (`$size` ushr 32 xor `$size`).toInt()
        result = result * PRIME + this.download_count
        val `$created_at`: Any = this.created_at
        result = result * PRIME + `$created_at`.hashCode()
        val `$updated_at`: Any = this.updated_at
        result = result * PRIME + `$updated_at`.hashCode()
        val `$browser_download_url`: Any = this.browser_download_url
        result = result * PRIME + `$browser_download_url`.hashCode()
        return result
    }

    override fun toString(): String {
        return "Assets(url=" + this.url + ", id=" + this.id + ", node_id=" + this.node_id + ", name=" + this.name + ", label=" + this.label + ", uploader=" + this.uploader + ", content_type=" + this.content_type + ", state=" + this.state + ", size=" + this.size + ", download_count=" + this.download_count + ", created_at=" + this.created_at + ", updated_at=" + this.updated_at + ", browser_download_url=" + this.browser_download_url + ")"
    }
}
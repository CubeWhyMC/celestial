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



    override fun toString(): String {
        return "Assets(url=" + this.url + ", id=" + this.id + ", node_id=" + this.node_id + ", name=" + this.name + ", label=" + this.label + ", uploader=" + this.uploader + ", content_type=" + this.content_type + ", state=" + this.state + ", size=" + this.size + ", download_count=" + this.download_count + ", created_at=" + this.created_at + ", updated_at=" + this.updated_at + ", browser_download_url=" + this.browser_download_url + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Assets

        if (url != other.url) return false
        if (id != other.id) return false
        if (node_id != other.node_id) return false
        if (name != other.name) return false
        if (label != other.label) return false
        if (uploader != other.uploader) return false
        if (content_type != other.content_type) return false
        if (state != other.state) return false
        if (size != other.size) return false
        if (download_count != other.download_count) return false
        if (created_at != other.created_at) return false
        if (updated_at != other.updated_at) return false
        if (browser_download_url != other.browser_download_url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + node_id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + uploader.hashCode()
        result = 31 * result + content_type.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + download_count
        result = 31 * result + created_at.hashCode()
        result = 31 * result + updated_at.hashCode()
        result = 31 * result + browser_download_url.hashCode()
        return result
    }
}
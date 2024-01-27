/**
 * Copyright 2024 json.cn
 */
package org.cubewhy.celestial.entities

/**
 * Auto-generated: 2024-01-07 19:7:0
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
class Author {
    lateinit var login: String
    var id: Long = 0
    lateinit var node_id: String
    lateinit var avatar_url: String
    lateinit var gravatar_id: String
    lateinit var url: String
    lateinit var html_url: String
    lateinit var followers_url: String
    lateinit var following_url: String
    lateinit var gists_url: String
    lateinit var starred_url: String
    lateinit var subscriptions_url: String
    lateinit var organizations_url: String
    lateinit var repos_url: String
    lateinit var events_url: String
    lateinit var received_events_url: String
    lateinit var type: String
    var isSite_admin: Boolean = false


    override fun toString(): String {
        return "Author(login=" + this.login + ", id=" + this.id + ", node_id=" + this.node_id + ", avatar_url=" + this.avatar_url + ", gravatar_id=" + this.gravatar_id + ", url=" + this.url + ", html_url=" + this.html_url + ", followers_url=" + this.followers_url + ", following_url=" + this.following_url + ", gists_url=" + this.gists_url + ", starred_url=" + this.starred_url + ", subscriptions_url=" + this.subscriptions_url + ", organizations_url=" + this.organizations_url + ", repos_url=" + this.repos_url + ", events_url=" + this.events_url + ", received_events_url=" + this.received_events_url + ", type=" + this.type + ", site_admin=" + this.isSite_admin + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Author

        if (login != other.login) return false
        if (id != other.id) return false
        if (node_id != other.node_id) return false
        if (avatar_url != other.avatar_url) return false
        if (gravatar_id != other.gravatar_id) return false
        if (url != other.url) return false
        if (html_url != other.html_url) return false
        if (followers_url != other.followers_url) return false
        if (following_url != other.following_url) return false
        if (gists_url != other.gists_url) return false
        if (starred_url != other.starred_url) return false
        if (subscriptions_url != other.subscriptions_url) return false
        if (organizations_url != other.organizations_url) return false
        if (repos_url != other.repos_url) return false
        if (events_url != other.events_url) return false
        if (received_events_url != other.received_events_url) return false
        if (type != other.type) return false
        if (isSite_admin != other.isSite_admin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = login.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + node_id.hashCode()
        result = 31 * result + avatar_url.hashCode()
        result = 31 * result + gravatar_id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + html_url.hashCode()
        result = 31 * result + followers_url.hashCode()
        result = 31 * result + following_url.hashCode()
        result = 31 * result + gists_url.hashCode()
        result = 31 * result + starred_url.hashCode()
        result = 31 * result + subscriptions_url.hashCode()
        result = 31 * result + organizations_url.hashCode()
        result = 31 * result + repos_url.hashCode()
        result = 31 * result + events_url.hashCode()
        result = 31 * result + received_events_url.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + isSite_admin.hashCode()
        return result
    }
}
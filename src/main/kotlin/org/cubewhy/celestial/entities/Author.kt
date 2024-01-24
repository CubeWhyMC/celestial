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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Author) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$login`: Any = this.login
        val `other$login`: Any = other.login
        if (if (`this$login` == null) `other$login` != null else `this$login` != `other$login`) return false
        if (this.id != other.id) return false
        val `this$node_id`: Any = this.node_id
        val `other$node_id`: Any = other.node_id
        if (if (`this$node_id` == null) `other$node_id` != null else `this$node_id` != `other$node_id`) return false
        val `this$avatar_url`: Any = this.avatar_url
        val `other$avatar_url`: Any = other.avatar_url
        if (if (`this$avatar_url` == null) `other$avatar_url` != null else `this$avatar_url` != `other$avatar_url`) return false
        val `this$gravatar_id`: Any = this.gravatar_id
        val `other$gravatar_id`: Any = other.gravatar_id
        if (if (`this$gravatar_id` == null) `other$gravatar_id` != null else `this$gravatar_id` != `other$gravatar_id`) return false
        val `this$url`: Any = this.url
        val `other$url`: Any = other.url
        if (if (`this$url` == null) `other$url` != null else `this$url` != `other$url`) return false
        val `this$html_url`: Any = this.html_url
        val `other$html_url`: Any = other.html_url
        if (if (`this$html_url` == null) `other$html_url` != null else `this$html_url` != `other$html_url`) return false
        val `this$followers_url`: Any = this.followers_url
        val `other$followers_url`: Any = other.followers_url
        if (if (`this$followers_url` == null) `other$followers_url` != null else `this$followers_url` != `other$followers_url`) return false
        val `this$following_url`: Any = this.following_url
        val `other$following_url`: Any = other.following_url
        if (if (`this$following_url` == null) `other$following_url` != null else `this$following_url` != `other$following_url`) return false
        val `this$gists_url`: Any = this.gists_url
        val `other$gists_url`: Any = other.gists_url
        if (if (`this$gists_url` == null) `other$gists_url` != null else `this$gists_url` != `other$gists_url`) return false
        val `this$starred_url`: Any = this.starred_url
        val `other$starred_url`: Any = other.starred_url
        if (if (`this$starred_url` == null) `other$starred_url` != null else `this$starred_url` != `other$starred_url`) return false
        val `this$subscriptions_url`: Any = this.subscriptions_url
        val `other$subscriptions_url`: Any = other.subscriptions_url
        if (if (`this$subscriptions_url` == null) `other$subscriptions_url` != null else `this$subscriptions_url` != `other$subscriptions_url`) return false
        val `this$organizations_url`: Any = this.organizations_url
        val `other$organizations_url`: Any = other.organizations_url
        if (if (`this$organizations_url` == null) `other$organizations_url` != null else `this$organizations_url` != `other$organizations_url`) return false
        val `this$repos_url`: Any = this.repos_url
        val `other$repos_url`: Any = other.repos_url
        if (if (`this$repos_url` == null) `other$repos_url` != null else `this$repos_url` != `other$repos_url`) return false
        val `this$events_url`: Any = this.events_url
        val `other$events_url`: Any = other.events_url
        if (if (`this$events_url` == null) `other$events_url` != null else `this$events_url` != `other$events_url`) return false
        val `this$received_events_url`: Any = this.received_events_url
        val `other$received_events_url`: Any = other.received_events_url
        if (if (`this$received_events_url` == null) `other$received_events_url` != null else `this$received_events_url` != `other$received_events_url`) return false
        val `this$type`: Any = this.type
        val `other$type`: Any = other.type
        if (if (`this$type` == null) `other$type` != null else `this$type` != `other$type`) return false
        if (this.isSite_admin != other.isSite_admin) return false
        return true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is Author
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$login`: Any = this.login
        result = result * PRIME + `$login`.hashCode()
        val `$id` = this.id
        result = result * PRIME + (`$id` ushr 32 xor `$id`).toInt()
        val `$node_id`: Any = this.node_id
        result = result * PRIME + `$node_id`.hashCode()
        val `$avatar_url`: Any = this.avatar_url
        result = result * PRIME + `$avatar_url`.hashCode()
        val `$gravatar_id`: Any = this.gravatar_id
        result = result * PRIME + `$gravatar_id`.hashCode()
        val `$url`: Any = this.url
        result = result * PRIME + `$url`.hashCode()
        val `$html_url`: Any = this.html_url
        result = result * PRIME + `$html_url`.hashCode()
        val `$followers_url`: Any = this.followers_url
        result = result * PRIME + `$followers_url`.hashCode()
        val `$following_url`: Any = this.following_url
        result = result * PRIME + `$following_url`.hashCode()
        val `$gists_url`: Any = this.gists_url
        result = result * PRIME + `$gists_url`.hashCode()
        val `$starred_url`: Any = this.starred_url
        result = result * PRIME + `$starred_url`.hashCode()
        val `$subscriptions_url`: Any = this.subscriptions_url
        result = result * PRIME + `$subscriptions_url`.hashCode()
        val `$organizations_url`: Any = this.organizations_url
        result = result * PRIME + `$organizations_url`.hashCode()
        val `$repos_url`: Any = this.repos_url
        result = result * PRIME + `$repos_url`.hashCode()
        val `$events_url`: Any = this.events_url
        result = result * PRIME + `$events_url`.hashCode()
        val `$received_events_url`: Any = this.received_events_url
        result = result * PRIME + `$received_events_url`.hashCode()
        val `$type`: Any = this.type
        result = result * PRIME + `$type`.hashCode()
        result = result * PRIME + (if (this.isSite_admin) 79 else 97)
        return result
    }

    override fun toString(): String {
        return "Author(login=" + this.login + ", id=" + this.id + ", node_id=" + this.node_id + ", avatar_url=" + this.avatar_url + ", gravatar_id=" + this.gravatar_id + ", url=" + this.url + ", html_url=" + this.html_url + ", followers_url=" + this.followers_url + ", following_url=" + this.following_url + ", gists_url=" + this.gists_url + ", starred_url=" + this.starred_url + ", subscriptions_url=" + this.subscriptions_url + ", organizations_url=" + this.organizations_url + ", repos_url=" + this.repos_url + ", events_url=" + this.events_url + ", received_events_url=" + this.received_events_url + ", type=" + this.type + ", site_admin=" + this.isSite_admin + ")"
    }
}
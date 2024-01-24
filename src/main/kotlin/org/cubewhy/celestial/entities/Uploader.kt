/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.entities

/**
 * Auto-generated: 2024-01-07 19:7:0
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
class Uploader {
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
        return "Uploader(login=" + this.login + ", id=" + this.id + ", node_id=" + this.node_id + ", avatar_url=" + this.avatar_url + ", gravatar_id=" + this.gravatar_id + ", url=" + this.url + ", html_url=" + this.html_url + ", followers_url=" + this.followers_url + ", following_url=" + this.following_url + ", gists_url=" + this.gists_url + ", starred_url=" + this.starred_url + ", subscriptions_url=" + this.subscriptions_url + ", organizations_url=" + this.organizations_url + ", repos_url=" + this.repos_url + ", events_url=" + this.events_url + ", received_events_url=" + this.received_events_url + ", type=" + this.type + ", site_admin=" + this.isSite_admin + ")"
    }
}
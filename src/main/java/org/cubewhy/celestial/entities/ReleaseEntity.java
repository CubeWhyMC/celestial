/**
 * Copyright 2024 json.cn
 */
package org.cubewhy.celestial.entities;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2024-01-07 19:7:0
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class ReleaseEntity {

    private String url;
    private String assets_url;
    private String upload_url;
    private String html_url;
    private long id;
    private Author author;
    private String node_id;
    private String tag_name;
    private String target_commitish;
    private String name;
    private boolean draft;
    private boolean prerelease;
    private Date created_at;
    private Date published_at;
    private List<Assets> assets;
    private String tarball_url;
    private String zipball_url;
    private String body;

}
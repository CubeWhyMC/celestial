/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import com.google.gson.Gson;
import lombok.Getter;
import okhttp3.Response;
import org.cubewhy.celestial.entities.Assets;
import org.cubewhy.celestial.entities.releaseEntity;
import org.cubewhy.celestial.game.BaseAddon;
import org.cubewhy.celestial.utils.RequestUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class WeaveMod extends BaseAddon {
    public static final File modFolder = new File(System.getProperty("user.home"), ".weave/mods");
    private final File file;

    public WeaveMod(File file) {
        this.file = file;
    }

    /**
     * Find all mods in the lunarcn mods folder
     */
    @NotNull
    @Contract(pure = true)
    public static List<WeaveMod> findAll() {
        List<WeaveMod> list = new ArrayList<>();
        if (modFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(modFolder.listFiles())) {
                if (file.getName().endsWith(".jar") && file.isFile()) {
                    list.add(new WeaveMod(file));
                }
            }
        }
        return list;
    }

    public static @Nullable WeaveMod add(@NotNull File file) throws IOException {
        File target = autoCopy(file, modFolder);
        return (target == null) ? null : new WeaveMod(target);
    }

    @Override
    public String toString() {
        return this.file.getName();
    }

    public boolean downloadWeaveLoader(@Nullable String loaderUrl){
        if(loaderUrl == null){
            String api_json;
            try(Response response = RequestUtils.get("https://api.github.com/repos/Weave-MC/Weave-Loader/releases/latest").execute()){
                assert response.body() != null;
                api_json = response.body().string();
            } catch (IOException e) {
                return false;
            }
            releaseEntity releaseEntity = JsonToObj(api_json, org.cubewhy.celestial.entities.releaseEntity.class);
            if (releaseEntity != null) {
                Assets[] assets = releaseEntity.getAssets().toArray(new Assets[0]);
                for(Assets i:assets){
                    if(i.getName().endsWith(".jar")){
                        //TODO: download i.browser_download_url() to ~/.cubewhy/addon/Weave-<%s>.jar, releaseEntity.getName()
                    }
                }
            }
        }
        return true;
    }

    public static <T> T JsonToObj(String json, Class<T> clz){
        Gson gson = new Gson();
        if(Objects.isNull(json)) return null;
        T obj = gson.fromJson(json, clz);
        if(Objects.isNull(obj)){
            return null;
        }else{
            return obj;
        }
    }
}

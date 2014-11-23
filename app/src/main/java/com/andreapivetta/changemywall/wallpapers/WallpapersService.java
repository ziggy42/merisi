package com.andreapivetta.changemywall.wallpapers;

/*
<li>
  <figure class="thumb purity-sfw " style="width:300px;height:200px">
    <img alt="loading" class="lazyload" data-src="http://alpha.wallhaven.cc/wallpapers/thumb/small/th-33199.jpg" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7">

    <a class="preview" href="http://alpha.wallhaven.cc/wallpaper/33199" target="_blank" ></a>

    <div class="wall-info">
      <span class="wall-res">1920 x 1200</span>
     	<a class="overlay-anchor wall-favs " href="http://alpha.wallhaven.cc/wallpaper/33199/favorites">3 <i class="fa fa-star"></i></a>
        <a class="jsAnchor thumb-tags-toggle tagged" title="Tags"><i class="fa fa-tags"></i></a>
    </div>

    <ul class="thumb-tags">
      <li><a class="tagname sfw" href="http://alpha.wallhaven.cc/tag/1">anime</a><a href="http://alpha.wallhaven.cc/search?q=%23anime"><i class="fa fa-search"></i></a></li>
      <li><a class="tagname sfw" href="http://alpha.wallhaven.cc/tag/5">anime girls</a><a href="http://alpha.wallhaven.cc/search?q=%23anime girls"><i class="fa fa-search"></i></a></li>
    </ul>
  </figure>
</li>
*/


import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;


public class WallpapersService {

    public ArrayList<Wallpaper> getRandomWallpapers(int pageNumber) {
        return getWallpapers("http://alpha.wallhaven.cc/random?page=" + pageNumber);
    }

    public ArrayList<Wallpaper> getWallpapersTopList(int pageNumber) {
        return getWallpapers("http://alpha.wallhaven.cc/search?categories=111&purity=110&sorting=views&order=desc&page=" + pageNumber);
    }

    public ArrayList<Wallpaper> getWallpapersSearched(int pageNumber, String query) {
        return getWallpapers("http://alpha.wallhaven.cc/search?q="
                + query
                + "&categories=111&purity=110&sorting=relevance&order=asc&page="  + pageNumber);
    }

    public ArrayList<Wallpaper> getWallpapers(String query) {
        ArrayList<Wallpaper> list = new ArrayList<Wallpaper>();
        Connection connection = Jsoup.connect(query);

        try {
            Document document = connection.execute().parse();
            Elements wallpapers = document.select(".thumb");

            for (Element wallpaper : wallpapers) {
                Element image = wallpaper.select("img").first();
                Element res = wallpaper.select(".wall-res").first();

                if (image == null || res == null) {
                    Log.w("WallbaseService", "Selected wallpaper without image or res, requesting retry");
                    continue;
                }

                String thumbSrc = image.attr("data-src");
                if (StringUtil.isBlank(thumbSrc)) {
                    Log.w("WallbaseService", "Selected wallpaper with blank data original, requesting retry");
                    continue;
                }

                list.add(new Wallpaper(thumbSrc, res.text()));
            }
        } catch (SocketTimeoutException se) {
            Log.e("WallpaperService SOCKETEx", se.toString());
            return getWallpapers(query);
        } catch (IOException e) {
            Log.e("WallpaperService IOEx", e.toString());
            return null;
        }

        return list;
    }
}
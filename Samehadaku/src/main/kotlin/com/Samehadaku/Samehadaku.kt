package com.samehadaku

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element

class Samehadaku : MainAPI() {
    override var mainUrl = "https://v2.samehadaku.how"
    override var name = "Samehadaku"
    override var lang = "id"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)

    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val homeItems = ArrayList<HomePageList>()

        val latestEpisodes = document.select("div.post-show ul li").mapNotNull { element ->
            element.toSearchResult()
        }
        if (latestEpisodes.isNotEmpty()) {
            homeItems.add(HomePageList("Episode Terbaru", latestEpisodes))
        }

        return HomePageResponse(homeItems)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2.entry-title a, div.dtla h2 a")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src")

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document

        return document.select("article.animpost, div.relat div.animpost").mapNotNull { element ->
            val title = element.selectFirst("h2")?.text() ?: return@mapNotNull null
            val href = element.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val posterUrl = element.selectFirst("img")?.attr("src")

            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title")?.text() ?: "Unknown"
        val poster = document.selectFirst("div.thumb img, div.infoanime div.thumb img")?.attr("src")
        val description = document.selectFirst("div.entry-content, div.desc")?.text()

        val episodes = document.select("div.lrf ul li, div.epsul ul li").mapNotNull { element ->
            val epTitle = element.selectFirst("a")?.text() ?: return@mapNotNull null
            val epHref = element.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            newEpisode(epHref) {
                this.name = epTitle
            }
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (Video) -> Unit
    ): Boolean {
        val document = app.get(data).document

        document.select("div#server iframe, div.pembed iframe").forEach { iframe ->
            var src = iframe.attr("src")
            if (src.startsWith("//")) {
                src = "https:$src"
            }
            if (src.isNotBlank()) {
                loadExtractor(src, data, subtitleCallback, callback)
            }
        }
        return true
    }
}

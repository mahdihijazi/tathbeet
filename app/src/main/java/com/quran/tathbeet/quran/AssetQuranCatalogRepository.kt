package com.quran.tathbeet.quran

import android.content.Context
import com.quran.tathbeet.domain.repository.QuranCatalogRepository
import com.quran.tathbeet.ui.model.QuranCatalog
import com.quran.tathbeet.ui.model.loadQuranCatalog

class AssetQuranCatalogRepository(
    private val appContext: Context,
) : QuranCatalogRepository {
    private val cachedCatalog: QuranCatalog by lazy(LazyThreadSafetyMode.NONE) {
        loadQuranCatalog(appContext)
    }

    override fun getCatalog(): QuranCatalog = cachedCatalog
}

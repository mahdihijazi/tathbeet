package com.quran.tathbeet.domain.repository

import com.quran.tathbeet.ui.model.QuranCatalog

interface QuranCatalogRepository {
    fun getCatalog(): QuranCatalog
}

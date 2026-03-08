# Quran Reference Data

Prototype-friendly Quran selection data lives in `app/src/main/assets/quran/`.

## Files

- `surahs.json`: 114 surahs with Arabic name, English name, transliteration, ayah count, revelation type/order, and ruku count.
- `juzs.json`: 30 ajza with start/end surah and ayah references, plus linked hizb and quarter ranges.
- `hizbs.json`: 60 ahzab with start/end surah and ayah references, the parent juz, and linked quarter ranges.
- `rub-al-hizb.json`: 240 rub al-hizb entries with start/end surah and ayah references, plus parent juz/hizb and quarter position fields.

## Source

These files are generated from Tanzil Quran metadata:

- Source URL: `https://tanzil.net/res/text/metadata/quran-data.xml`
- Source page: `https://tanzil.net/docs/metadata`
- License: CC BY

Tanzil provides:

- surah metadata
- juz start references
- quarter start references for the Quran division system

The repo generator derives end references by taking the verse immediately before the next start reference. The final range in each file ends at `114:6`.

## Regeneration

Run this from the repo root:

```bash
python3 scripts/generate_quran_reference_data.py
```

The generator downloads the latest `quran-data.xml` from Tanzil and rewrites the JSON assets in `app/src/main/assets/quran/`.

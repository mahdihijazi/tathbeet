#!/usr/bin/env python3

from __future__ import annotations

import json
from pathlib import Path
from urllib.request import urlopen
import xml.etree.ElementTree as ET


SOURCE_URL = "https://tanzil.net/res/text/metadata/quran-data.xml"
ROOT_DIR = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT_DIR / "app" / "src" / "main" / "assets" / "quran"


def fetch_metadata() -> ET.Element:
    with urlopen(SOURCE_URL) as response:
        return ET.fromstring(response.read())


def write_json(name: str, payload: list[dict]) -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    output_path = OUTPUT_DIR / name
    output_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def previous_reference(next_sura: int, next_aya: int, sura_ayah_counts: dict[int, int]) -> tuple[int, int]:
    if next_aya > 1:
        return next_sura, next_aya - 1

    previous_sura = next_sura - 1
    return previous_sura, sura_ayah_counts[previous_sura]


def make_reference(sura: int, aya: int, sura_names: dict[int, dict[str, str]]) -> dict[str, object]:
    names = sura_names[sura]
    return {
        "surahId": sura,
        "surahNameArabic": names["arabic"],
        "surahNameTransliteration": names["transliteration"],
        "surahNameEnglish": names["english"],
        "ayah": aya,
    }


def build_range_entries(
    starts: list[dict[str, int]],
    sura_ayah_counts: dict[int, int],
    sura_names: dict[int, dict[str, str]],
) -> list[dict[str, object]]:
    entries: list[dict[str, object]] = []

    for index, start in enumerate(starts):
        next_start = starts[index + 1] if index + 1 < len(starts) else None

        if next_start is None:
            end_sura, end_aya = 114, sura_ayah_counts[114]
        else:
            end_sura, end_aya = previous_reference(
                next_start["surahId"],
                next_start["ayah"],
                sura_ayah_counts,
            )

        entries.append(
            {
                "id": start["id"],
                "start": make_reference(start["surahId"], start["ayah"], sura_names),
                "end": make_reference(end_sura, end_aya, sura_names),
            }
        )

    return entries


def main() -> None:
    root = fetch_metadata()

    sura_nodes = root.find("suras")
    juz_nodes = root.find("juzs")
    quarter_nodes = root.find("hizbs")

    if sura_nodes is None or juz_nodes is None or quarter_nodes is None:
        raise RuntimeError("Unexpected Tanzil metadata structure")

    surahs: list[dict[str, object]] = []
    sura_ayah_counts: dict[int, int] = {}
    sura_names: dict[int, dict[str, str]] = {}

    for sura in sura_nodes.findall("sura"):
        sura_id = int(sura.attrib["index"])
        ayah_count = int(sura.attrib["ayas"])

        surah_entry = {
            "id": sura_id,
            "nameArabic": sura.attrib["name"],
            "nameTransliteration": sura.attrib["tname"],
            "nameEnglish": sura.attrib["ename"],
            "ayahCount": ayah_count,
            "revelationType": sura.attrib["type"],
            "revelationOrder": int(sura.attrib["order"]),
            "rukuCount": int(sura.attrib["rukus"]),
        }

        surahs.append(surah_entry)
        sura_ayah_counts[sura_id] = ayah_count
        sura_names[sura_id] = {
            "arabic": surah_entry["nameArabic"],
            "transliteration": surah_entry["nameTransliteration"],
            "english": surah_entry["nameEnglish"],
        }

    juz_starts = [
        {
            "id": int(juz.attrib["index"]),
            "surahId": int(juz.attrib["sura"]),
            "ayah": int(juz.attrib["aya"]),
        }
        for juz in juz_nodes.findall("juz")
    ]

    quarter_starts = [
        {
            "id": int(quarter.attrib["index"]),
            "surahId": int(quarter.attrib["sura"]),
            "ayah": int(quarter.attrib["aya"]),
        }
        for quarter in quarter_nodes.findall("quarter")
    ]

    juzs = build_range_entries(juz_starts, sura_ayah_counts, sura_names)
    rub_al_hizb = build_range_entries(quarter_starts, sura_ayah_counts, sura_names)

    for juz in juzs:
        juz_id = int(juz["id"])
        juz["firstHizbId"] = ((juz_id - 1) * 2) + 1
        juz["lastHizbId"] = juz["firstHizbId"] + 1
        juz["firstQuarterId"] = ((juz_id - 1) * 8) + 1
        juz["lastQuarterId"] = juz["firstQuarterId"] + 7

    for quarter in rub_al_hizb:
        quarter_id = int(quarter["id"])
        quarter["juzId"] = ((quarter_id - 1) // 8) + 1
        quarter["hizbId"] = ((quarter_id - 1) // 4) + 1
        quarter["quarterInHizb"] = ((quarter_id - 1) % 4) + 1
        quarter["quarterInJuz"] = ((quarter_id - 1) % 8) + 1

    hizb_starts = [
        {
            "id": (index // 4) + 1,
            "surahId": quarter_starts[index]["surahId"],
            "ayah": quarter_starts[index]["ayah"],
        }
        for index in range(0, len(quarter_starts), 4)
    ]
    hizbs = build_range_entries(hizb_starts, sura_ayah_counts, sura_names)

    for hizb in hizbs:
        hizb_id = int(hizb["id"])
        hizb["juzId"] = ((hizb_id - 1) // 2) + 1
        hizb["hizbInJuz"] = ((hizb_id - 1) % 2) + 1
        hizb["firstQuarterId"] = ((hizb_id - 1) * 4) + 1
        hizb["lastQuarterId"] = hizb["firstQuarterId"] + 3

    write_json("surahs.json", surahs)
    write_json("juzs.json", juzs)
    write_json("hizbs.json", hizbs)
    write_json("rub-al-hizb.json", rub_al_hizb)


if __name__ == "__main__":
    main()

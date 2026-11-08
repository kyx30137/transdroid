#!/usr/bin/env python3
"""Verify values-zh-rCN covers values/ exactly: same keys, same format specifiers.

Reads every .xml in each folder, so splitting strings over more files (strings.xml,
strings_changelog.xml, ...) keeps working without touching this script.
"""
import glob
import os
import re
import sys
import xml.etree.ElementTree as ET

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "app/src/main/res")
EN_DIR = f"{BASE}/values"
ZH_DIR = f"{BASE}/values-zh-rCN"

SPEC = re.compile(r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]")


def load(folder):
    """All string/plurals keys in every XML file of a resource folder."""
    strings, plurals, origins = {}, {}, {}
    for path in sorted(glob.glob(f"{folder}/*.xml")):
        for el in ET.parse(path).getroot():
            if el.tag == "string":
                strings[el.get("name")] = (el.text or "", el.get("translatable") != "false")
            elif el.tag == "plurals":
                items = {i.get("quantity"): (i.text or "") for i in el}
                plurals[el.get("name")] = (items, el.get("translatable") != "false")
            else:
                continue
            origins[el.get("name")] = os.path.basename(path)
    return strings, plurals, origins


en_s, en_p, _ = load(EN_DIR)
zh_s, zh_p, _ = load(ZH_DIR)

print(f"values      : {len(en_s)} strings, {len(en_p)} plurals")
print(f"values-zh-rCN: {len(zh_s)} strings, {len(zh_p)} plurals")

ok = True

missing = sorted(set(en_s) - set(zh_s))
extra = sorted(set(zh_s) - set(en_s))
print(f"\nmissing keys : {len(missing)}")
for k in missing:
    ok = False
    print(f"  - {k}")
print(f"extra keys   : {len(extra)}")
for k in extra:
    ok = False
    print(f"  + {k}")

print(f"\nmissing plurals: {len(set(en_p) - set(zh_p))} {sorted(set(en_p) - set(zh_p))}")
if set(en_p) - set(zh_p):
    ok = False

# format specifier parity
bad = []
for k, (text, translatable) in en_s.items():
    if k not in zh_s:
        continue
    en_specs = sorted(SPEC.findall(text.replace("%%", "")))
    zh_specs = sorted(SPEC.findall(zh_s[k][0].replace("%%", "")))
    if en_specs != zh_specs:
        bad.append((k, en_specs, zh_specs, text, zh_s[k][0]))
print(f"\nplaceholder mismatches: {len(bad)}")
for k, a, b, et, zt in bad:
    ok = False
    print(f"  ! {k}\n      en{et!r} {a}\n      zh{zt!r} {b}")

# translatable="false" keys that were translated
translated_but_fixed = [k for k, (t, tr) in en_s.items() if not tr and k in zh_s and zh_s[k][0] != t]
print(f"translatable=false keys changed: {len(translated_but_fixed)} {translated_but_fixed}")
if translated_but_fixed:
    ok = False

# untranslated leftovers (value identical to English, excluding known proper nouns)
same = sorted(k for k, (t, tr) in en_s.items()
              if tr and k in zh_s and zh_s[k][0] == t and len(t) > 3)
print(f"identical-to-English strings: {len(same)}")
for k in same:
    print(f"  = {k}: {en_s[k][0]!r}")

print("\nRESULT:", "PASS" if ok else "FAIL")
sys.exit(0 if ok else 1)

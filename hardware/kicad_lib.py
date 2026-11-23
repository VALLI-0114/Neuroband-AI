"""Read pin name -> pad number maps straight from KiCad's own symbol libraries,
so the board's connections are checked against the official symbols, not typed from memory."""
import re
from pathlib import Path

KICAD = Path(r"C:\Program Files\KiCad\10.0\share\kicad")
_cache = {}


def _block(text, start):
    depth = 0
    for i in range(start, len(text)):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                return text[start:i + 1]
    raise ValueError("unbalanced")


def pins(lib_id):
    """'Lib:Name' -> {pin name: [numbers]} plus {number: [number]} for lookups by number."""
    if lib_id in _cache:
        return _cache[lib_id]
    lib, name = lib_id.split(":")
    text = (KICAD / "symbols" / f"{lib}.kicad_sym").read_text(encoding="utf-8")
    m = re.search(r'\n\t\(symbol "%s"' % re.escape(name), text)
    if not m:
        raise KeyError(lib_id)
    blk = _block(text, m.start() + 2)
    parent = re.search(r'\(extends "([^"]+)"\)', blk)
    if parent:
        out = pins(f"{lib}:{parent.group(1)}")
    else:
        out = {}
        for p in re.finditer(r'\(pin \w+ \w+.*?\(name "([^"]*)".*?\(number "([^"]*)"', blk, re.S):
            nm, raw = p.groups()
            # KiCad 10 stacked pins: "[1,15,38,39]" is one symbol pin on several pads
            for num in raw.strip("[]").split(","):
                out.setdefault(nm, []).append(num)
                out.setdefault(num, []).append(num)
    _cache[lib_id] = out
    return out


if __name__ == "__main__":
    import sys
    for sid in sys.argv[1:]:
        p = pins(sid)
        print(sid, {k: v for k, v in p.items() if not k.isdigit() and not re.fullmatch(r"[AB]\d+|S\d", k)})

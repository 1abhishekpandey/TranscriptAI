#!/usr/bin/env bash
# Draws the VirusTotal README badge as a plain SVG, so no badge service is needed.
# Usage: virustotal-badge.sh <tag> <flagged> <total> <output.svg>
set -euo pipefail

TAG="$1"
FLAGGED="$2"
TOTAL="$3"
OUT="$4"

LABEL="VirusTotal $TAG"
if [ "$FLAGGED" -eq 0 ]; then
  VALUE="clean $FLAGGED/$TOTAL"
  COLOR="#2ea44f"
else
  VALUE="flagged $FLAGGED/$TOTAL"
  COLOR="#d73a49"
fi

# About 7 pixels per character at 11px Verdana, plus 10 pixels of padding.
LABEL_W=$(( ${#LABEL} * 7 + 10 ))
VALUE_W=$(( ${#VALUE} * 7 + 10 ))
WIDTH=$(( LABEL_W + VALUE_W ))
LABEL_X=$(( LABEL_W / 2 ))
VALUE_X=$(( LABEL_W + VALUE_W / 2 ))

mkdir -p "$(dirname "$OUT")"
cat > "$OUT" <<SVG
<svg xmlns="http://www.w3.org/2000/svg" width="$WIDTH" height="20" role="img" aria-label="$LABEL: $VALUE">
  <title>$LABEL: $VALUE</title>
  <clipPath id="r"><rect width="$WIDTH" height="20" rx="3"/></clipPath>
  <g clip-path="url(#r)">
    <rect width="$LABEL_W" height="20" fill="#555"/>
    <rect x="$LABEL_W" width="$VALUE_W" height="20" fill="$COLOR"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" font-size="11">
    <text x="$LABEL_X" y="14">$LABEL</text>
    <text x="$VALUE_X" y="14">$VALUE</text>
  </g>
</svg>
SVG

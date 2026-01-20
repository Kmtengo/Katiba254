#!/usr/bin/env python3
"""Test script for debugging parse_constitution.py"""

import json
import re
import sys

def main():
    file_path = 'CONSTITUTION-OF-KENYA-2010.txt'
    print('Reading file...', flush=True)

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    print(f'File has {len(content)} characters, {len(content.splitlines())} lines', flush=True)

    lines = content.split('\n')

    # Find preamble
    print('Finding preamble...', flush=True)
    preamble_start = None
    preamble_end = None
    for i, line in enumerate(lines):
        if line.strip() == 'PREAMBLE' and preamble_start is None:
            if i > 200:
                preamble_start = i + 1
        if preamble_start and line.strip().startswith('CHAPTER ONE') and i > preamble_start:
            preamble_end = i
            break
    print(f'Preamble: lines {preamble_start} to {preamble_end}', flush=True)

    # Find chapters
    content_start = preamble_end if preamble_end else 0
    content_text = '\n'.join(lines[content_start:])
    print(f'Content text length: {len(content_text)}', flush=True)

    # Use simpler pattern - both em dash and regular dash
    chapter_pattern = r'CHAPTER\s+(ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN|ELEVEN|TWELVE|THIRTEEN|FOURTEEN|FIFTEEN|SIXTEEN|SEVENTEEN|EIGHTEEN)[—-]([^\n]+)'
    print('Finding chapters...', flush=True)
    chapter_matches = list(re.finditer(chapter_pattern, content_text))
    print(f'Found {len(chapter_matches)} chapter headers', flush=True)

    for m in chapter_matches:
        print(f'  {m.group(1)}: {m.group(2)[:40]}...', flush=True)

    print('\nDone with basic parsing test!', flush=True)

if __name__ == '__main__':
    main()


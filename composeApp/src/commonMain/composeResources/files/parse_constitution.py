#!/usr/bin/env python3
"""
Parse the Constitution of Kenya 2010 text file and convert to JSON format.
Improved version with better clause detection.
"""

import json
import re
from pathlib import Path
from typing import Dict, List, Any, Optional, Tuple


def parse_constitution(file_path: str) -> Dict[str, Any]:
    """Parse the constitution text file and return structured JSON."""

    print("Reading file...", flush=True)
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    lines = content.split('\n')
    print(f"File has {len(lines)} lines", flush=True)

    result = {
        "preamble": "",
        "chapters": [],
        "schedules": []
    }

    # Find preamble
    print("Finding preamble...", flush=True)
    preamble_start = None
    preamble_end = None

    for i, line in enumerate(lines):
        if line.strip() == "PREAMBLE" and preamble_start is None:
            if i > 200:  # Skip the table of contents PREAMBLE
                preamble_start = i + 1
        if preamble_start and line.strip().startswith("CHAPTER ONE") and i > preamble_start:
            preamble_end = i
            break

    # Extract preamble text
    if preamble_start and preamble_end:
        preamble_lines = []
        for i in range(preamble_start, preamble_end):
            line = lines[i].strip()
            if line and not line.startswith("Constitution of Kenya") and line != "THE CONSTITUTION OF KENYA":
                preamble_lines.append(line)
        result["preamble"] = " ".join(preamble_lines)

    print(f"Preamble: lines {preamble_start} to {preamble_end}", flush=True)

    # Find all chapters
    content_start = preamble_end if preamble_end else 0
    content_text = '\n'.join(lines[content_start:])

    # Pattern matches both em dash (—) and regular dash (-)
    chapter_pattern = r'CHAPTER\s+(ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN|ELEVEN|TWELVE|THIRTEEN|FOURTEEN|FIFTEEN|SIXTEEN|SEVENTEEN|EIGHTEEN)[—\-]([^\n]+)'

    print("Finding chapters...", flush=True)
    chapter_matches = list(re.finditer(chapter_pattern, content_text))
    print(f"Found {len(chapter_matches)} chapters", flush=True)

    word_to_num = {
        'ONE': 1, 'TWO': 2, 'THREE': 3, 'FOUR': 4, 'FIVE': 5,
        'SIX': 6, 'SEVEN': 7, 'EIGHT': 8, 'NINE': 9, 'TEN': 10,
        'ELEVEN': 11, 'TWELVE': 12, 'THIRTEEN': 13, 'FOURTEEN': 14,
        'FIFTEEN': 15, 'SIXTEEN': 16, 'SEVENTEEN': 17, 'EIGHTEEN': 18
    }

    for idx, match in enumerate(chapter_matches):
        chapter_num_word = match.group(1)
        chapter_title = match.group(2).strip()
        chapter_num = word_to_num[chapter_num_word]

        print(f"  Processing Chapter {chapter_num}: {chapter_title[:40]}...", flush=True)

        # Get chapter content
        chapter_start = match.end()
        if idx + 1 < len(chapter_matches):
            chapter_end = chapter_matches[idx + 1].start()
        else:
            # Find where schedules start
            schedule_match = re.search(r'FIRST SCHEDULE', content_text[chapter_start:])
            if schedule_match:
                chapter_end = chapter_start + schedule_match.start()
            else:
                chapter_end = len(content_text)

        chapter_content = content_text[chapter_start:chapter_end]

        # Parse articles
        articles = parse_articles_improved(chapter_content, chapter_num)

        chapter_obj = {
            "number": chapter_num,
            "title": chapter_title,
            "articles": articles
        }

        result["chapters"].append(chapter_obj)

    # Parse schedules
    print("Parsing schedules...", flush=True)
    result["schedules"] = parse_schedules(content_text)

    return result


def parse_articles_improved(chapter_content: str, chapter_num: int) -> List[Dict[str, Any]]:
    """Parse articles from chapter content with improved detection."""
    articles = []

    # Clean page markers
    chapter_content = re.sub(r'Constitution of Kenya,?\s*2010\s*\d*', '', chapter_content)

    # Article title to number mapping
    article_map = get_article_number_map()

    # Split into lines
    lines = chapter_content.split('\n')

    # First pass: Find all article titles
    # Article titles are lines that end with period and are followed by (1) or content
    article_positions = []

    i = 0
    while i < len(lines):
        line = lines[i].strip()

        # Skip empty lines, part headers, page markers
        if not line or line.startswith("Part ") or line.startswith("PART "):
            i += 1
            continue

        # Check if this is an article title
        # Article titles end with a period, contain only text (no clause numbers)
        if (line.endswith('.') and
            not line.startswith('(') and
            not re.match(r'^\d+\.', line) and
            len(line) > 3 and
            len(line) < 150):

            potential_title = line[:-1].strip()

            # Look ahead to verify this is indeed an article
            j = i + 1
            while j < len(lines) and not lines[j].strip():
                j += 1

            if j < len(lines):
                next_line = lines[j].strip()
                # Valid article if followed by (1) or content that's not another title
                if (next_line.startswith('(1)') or
                    next_line.startswith('(') or
                    (next_line and not next_line.endswith('.') and
                     not next_line.startswith('Part ') and
                     not next_line.startswith('PART '))):

                    # Get article number from title
                    normalized = normalize_title(potential_title)
                    article_num = article_map.get(normalized)

                    if article_num is not None:
                        article_positions.append((i, potential_title, article_num))
                    else:
                        # Try partial matching
                        found = False
                        for known_title, num in article_map.items():
                            if normalized.startswith(known_title) or known_title.startswith(normalized):
                                article_positions.append((i, potential_title, num))
                                found = True
                                break
                        if not found:
                            # Use sequential numbering as fallback
                            prev_num = article_positions[-1][2] if article_positions else 0
                            article_positions.append((i, potential_title, prev_num + 1))

        i += 1

    # Second pass: Extract content for each article
    for idx, (start_line, title, article_num) in enumerate(article_positions):
        # Find end of this article
        if idx + 1 < len(article_positions):
            end_line = article_positions[idx + 1][0]
        else:
            end_line = len(lines)

        # Extract article lines (skip the title line)
        article_lines = lines[start_line + 1:end_line]

        # Parse clauses from article lines
        clauses = parse_clauses(article_lines)

        articles.append({
            "number": article_num,
            "title": title,
            "clauses": clauses
        })

    return articles


def parse_clauses(article_lines: List[str]) -> List[Dict[str, Any]]:
    """Parse clauses and subclauses from article content."""
    clauses = []

    current_clause_num = None
    current_clause_text = []
    current_subclauses = []
    implicit_clause_num = 1

    for line in article_lines:
        line = line.strip()

        if not line:
            continue

        # Skip page markers
        if line.startswith("Constitution of Kenya"):
            continue

        # Check for explicit clause start (1), (2), etc.
        clause_match = re.match(r'^\((\d+)\)\s*(.*)$', line)
        if clause_match:
            # Save previous clause
            if current_clause_num is not None:
                clauses.append({
                    "number": str(current_clause_num),
                    "text": ' '.join(current_clause_text).strip(),
                    "subClauses": current_subclauses
                })

            current_clause_num = int(clause_match.group(1))
            implicit_clause_num = current_clause_num + 1
            current_clause_text = [clause_match.group(2)] if clause_match.group(2) else []
            current_subclauses = []
            continue

        # Check for subclause - (a), (b) or just a, b at start with tab/spaces
        subclause_match = re.match(r'^(?:\()?([a-z])(?:\))?\s+(.+)$', line)
        if subclause_match and current_clause_num is not None:
            label = subclause_match.group(1)
            text = subclause_match.group(2)
            current_subclauses.append({
                "label": label,
                "text": text
            })
            continue

        # Check for sub-subclause (i), (ii), (iii), etc.
        subsubclause_match = re.match(r'^\(([ivx]+)\)\s*(.*)$', line)
        if subsubclause_match and current_subclauses:
            # Append to last subclause
            current_subclauses[-1]["text"] += f" ({subsubclause_match.group(1)}) {subsubclause_match.group(2)}"
            continue

        # Check if this is an implicit clause (text that should be its own clause)
        # This happens when there's no (1) marker but the text follows directly after title
        if current_clause_num is None:
            # Start implicit clause 1
            current_clause_num = implicit_clause_num
            implicit_clause_num += 1
            current_clause_text = [line]
            current_subclauses = []
            continue

        # Otherwise, append to current clause text
        current_clause_text.append(line)

    # Don't forget the last clause
    if current_clause_num is not None:
        clauses.append({
            "number": str(current_clause_num),
            "text": ' '.join(current_clause_text).strip(),
            "subClauses": current_subclauses
        })

    return clauses


def normalize_title(title: str) -> str:
    """Normalize title for matching."""
    t = title.lower().strip()
    t = t.replace("—", "-").replace("–", "-")
    t = t.replace("'", "'").replace("'", "'")
    t = t.replace("ﬁ", "fi").replace("ﬂ", "fl")
    t = re.sub(r'\s+', ' ', t)
    return t


def get_article_number_map() -> Dict[str, int]:
    """Return a mapping of normalized article titles to article numbers."""
    articles = {
        # Chapter 1
        "sovereignty of the people": 1,
        "supremacy of this constitution": 2,
        "defence of this constitution": 3,
        # Chapter 2
        "declaration of the republic": 4,
        "territory of kenya": 5,
        "devolution and access to services": 6,
        "national, official and other languages": 7,
        "national, ofﬁcial and other languages": 7,
        "state and religion": 8,
        "national symbols and national days": 9,
        "national values and principles of governance": 10,
        "culture": 11,
        # Chapter 3
        "entitlements of citizens": 12,
        "retention and acquisition of citizenship": 13,
        "citizenship by birth": 14,
        "citizenship by registration": 15,
        "dual citizenship": 16,
        "revocation of citizenship": 17,
        "legislation on citizenship": 18,
        # Chapter 4
        "rights and fundamental freedoms": 19,
        "application of bill of rights": 20,
        "implementation of rights and fundamental freedoms": 21,
        "enforcement of bill of rights": 22,
        "authority of courts to uphold and enforce the bill of rights": 23,
        "limitation of rights and fundamental freedoms": 24,
        "limitation of rights or fundamental freedoms": 24,
        "fundamental rights and freedoms that may not be limited": 25,
        "right to life": 26,
        "equality and freedom from discrimination": 27,
        "human dignity": 28,
        "freedom and security of the person": 29,
        "slavery, servitude and forced labour": 30,
        "privacy": 31,
        "freedom of conscience, religion, belief and opinion": 32,
        "freedom of expression": 33,
        "freedom of the media": 34,
        "access to information": 35,
        "freedom of association": 36,
        "assembly, demonstration, picketing and petition": 37,
        "political rights": 38,
        "freedom of movement and residence": 39,
        "protection of right to property": 40,
        "labour relations": 41,
        "environment": 42,
        "economic and social rights": 43,
        "language and culture": 44,
        "family": 45,
        "consumer rights": 46,
        "fair administrative action": 47,
        "access to justice": 48,
        "rights of arrested persons": 49,
        "fair hearing": 50,
        "rights of persons detained, held in custody or imprisoned": 51,
        "interpretation of this part": 52,
        "interpretation of part": 52,
        "children": 53,
        "persons with disabilities": 54,
        "youth": 55,
        "minorities and marginalised groups": 56,
        "older members of society": 57,
        "state of emergency": 58,
        "kenya national human rights and equality commission": 59,
        # Chapter 5
        "principles of land policy": 60,
        "classification of land": 61,
        "public land": 62,
        "community land": 63,
        "private land": 64,
        "landholding by non-citizens": 65,
        "regulation of land use and property": 66,
        "national land commission": 67,
        "legislation on land": 68,
        "obligations in respect of the environment": 69,
        "enforcement of environmental rights": 70,
        "agreements relating to natural resources": 71,
        "legislation relating to the environment": 72,
        # Chapter 6
        "responsibilities of leadership": 73,
        "oath of office of state officers": 74,
        "conduct of state officers": 75,
        "financial probity of state officers": 76,
        "restriction on activities of state officers": 77,
        "citizenship and leadership": 78,
        "legislation to establish the ethics and anti-corruption commission": 79,
        "legislation on leadership": 80,
        # Chapter 7
        "general principles for the electoral system": 81,
        "legislation on elections": 82,
        "registration as a voter": 83,
        "candidates for election and political parties to comply with code of conduct": 84,
        "eligibility to stand as an independent candidate": 85,
        "voting": 86,
        "electoral disputes": 87,
        "independent electoral and boundaries commission": 88,
        "delimitation of electoral units": 89,
        "allocation of party list seats": 90,
        "basic requirements for political parties": 91,
        "legislation on political parties": 92,
        # Chapter 8
        "establishment of parliament": 93,
        "role of parliament": 94,
        "role of the national assembly": 95,
        "role of the senate": 96,
        "membership of the national assembly": 97,
        "membership of the senate": 98,
        "qualifications and disqualifications for election as member of parliament": 99,
        "promotion of representation of marginalised groups": 100,
        "election of members of parliament": 101,
        "term of parliament": 102,
        "vacation of office of member of parliament": 103,
        "right of recall": 104,
        "determination of questions of membership": 105,
        "speakers and deputy speakers of parliament": 106,
        "presiding in parliament": 107,
        "party leaders": 108,
        "exercise of legislative powers": 109,
        "bills concerning county government": 110,
        "special bills concerning county governments": 111,
        "ordinary bills concerning county governments": 112,
        "mediation committees": 113,
        "money bills": 114,
        "presidential assent and referral": 115,
        "coming into force of laws": 116,
        "powers, privileges and immunities": 117,
        "public access and participation": 118,
        "right to petition parliament": 119,
        "official languages of parliament": 120,
        "quorum": 121,
        "voting in parliament": 122,
        "decisions of senate": 123,
        "committees and standing orders": 124,
        "power to call for evidence": 125,
        "location of sittings of parliament": 126,
        "parliamentary service commission": 127,
        "clerks and staff of parliament": 128,
        # Chapter 9
        "principles of executive authority": 129,
        "the national executive": 130,
        "authority of the president": 131,
        "functions of the president": 132,
        "power of mercy": 133,
        "exercise of presidential powers during temporary incumbency": 134,
        "decisions of the president": 135,
        "election of the president": 136,
        "qualifications and disqualifications for election as president": 137,
        "procedure at presidential election": 138,
        "death before assuming office": 139,
        "questions as to validity of presidential election": 140,
        "assumption of office of president": 141,
        "term of office of president": 142,
        "protection from legal proceedings": 143,
        "removal of president on grounds of incapacity": 144,
        "removal of president by impeachment": 145,
        "vacancy in the office of president": 146,
        "functions of the deputy president": 147,
        "election and swearing-in of deputy president": 148,
        "vacancy in the office of deputy president": 149,
        "removal of deputy president": 150,
        "remuneration and benefits of president and deputy president": 151,
        "cabinet": 152,
        "decisions, responsibility and accountability of the cabinet": 153,
        "secretary to the cabinet": 154,
        "principal secretaries": 155,
        "attorney-general": 156,
        "director of public prosecutions": 157,
        "removal and resignation of director of public prosecutions": 158,
        # Chapter 10
        "judicial authority": 159,
        "independence of the judiciary": 160,
        "judicial offices and officers": 161,
        "system of courts": 162,
        "supreme court": 163,
        "court of appeal": 164,
        "high court": 165,
        "appointment of chief justice, deputy chief justice and other judges": 166,
        "tenure of office of the chief justice and other judges": 167,
        "removal from office": 168,
        "subordinate courts": 169,
        "kadhis' courts": 170,
        "kadhis' courts": 170,
        "establishment of the judicial service commission": 171,
        "functions of the judicial service commission": 172,
        "judiciary fund": 173,
        # Chapter 11
        "objects of devolution": 174,
        "principles of devolved government": 175,
        "county governments": 176,
        "membership of county assembly": 177,
        "speaker of a county assembly": 178,
        "county executive committees": 179,
        "election of county governor and deputy county governor": 180,
        "removal of a county governor": 181,
        "vacancy in the office of county governor": 182,
        "functions of county executive committees": 183,
        "urban areas and cities": 184,
        "legislative authority of county assemblies": 185,
        "respective functions and powers of national and county governments": 186,
        "transfer of functions and powers between levels of government": 187,
        "boundaries of counties": 188,
        "cooperation between national and county governments": 189,
        "support for county governments": 190,
        "conflict of laws": 191,
        "suspension of a county government": 192,
        "suspension of county government": 192,
        "qualifications for election as member of county assembly": 193,
        "vacation of office of member of county assembly": 194,
        "county assembly power to summon witnesses": 195,
        "public participation and county assembly powers, privileges and immunities": 196,
        "county assembly gender balance and diversity": 197,
        "county government during transition": 198,
        "publication of county legislation": 199,
        "legislation on chapter": 200,
        # Chapter 12
        "principles of public finance": 201,
        "equitable sharing of national revenue": 202,
        "equitable share and other financial laws": 203,
        "equalisation fund": 204,
        "consultation on financial legislation affecting counties": 205,
        "consolidated fund and other public funds": 206,
        "revenue funds for county governments": 207,
        "contingencies fund": 208,
        "power to impose taxes and charges": 209,
        "imposition of tax": 210,
        "borrowing by national government": 211,
        "borrowing by counties": 212,
        "loan guarantees by national government": 213,
        "public debt": 214,
        "commission on revenue allocation": 215,
        "functions of the commission on revenue allocation": 216,
        "division of revenue": 217,
        "annual division and allocation of revenue bills": 218,
        "transfer of equitable share": 219,
        "form, content and timing of budgets": 220,
        "budget estimates and annual appropriation bill": 221,
        "expenditure before annual budget is passed": 222,
        "supplementary appropriation": 223,
        "county appropriation bills": 224,
        "financial control": 225,
        "accounts and audit of public entities": 226,
        "procurement of public goods and services": 227,
        "controller of budget": 228,
        "auditor-general": 229,
        "salaries and remuneration commission": 230,
        "central bank of kenya": 231,
        # Chapter 13
        "values and principles of public service": 232,
        "the public service commission": 233,
        "functions and powers of the public service commission": 234,
        "staffing of county governments": 235,
        "protection of public officers": 236,
        "teachers service commission": 237,
        # Chapter 14
        "principles of national security": 238,
        "national security organs": 239,
        "establishment of the national security council": 240,
        "establishment of defence forces and defence council": 241,
        "establishment of kenya defence forces and defence council": 241,
        "establishment of national intelligence service": 242,
        "establishment of the national police service": 243,
        "objects and functions of the national police service": 244,
        "command of the national police service": 245,
        "national police service commission": 246,
        "other police services": 247,
        # Chapter 15
        "application of chapter": 248,
        "objects, authority and funding of commissions and independent offices": 249,
        "composition, appointment and terms of office": 250,
        "removal from office": 251,
        "general functions and powers": 252,
        "incorporation of commissions and independent offices": 253,
        "reporting by commissions and independent offices": 254,
        # Chapter 16
        "amendment of this constitution": 255,
        "amendment by parliamentary initiative": 256,
        "amendment by popular initiative": 257,
        # Chapter 17
        "enforcement of this constitution": 258,
        "construing this constitution": 259,
        "interpretation": 260,
        # Chapter 18
        "consequential legislation": 261,
        "transitional and consequential provisions": 262,
        "effective date": 263,
        "repeal of previous constitution": 264,
    }
    return articles


def parse_schedules(content_text: str) -> List[Dict[str, Any]]:
    """Parse the schedules from the constitution."""
    schedules = []

    schedule_info = [
        {"number": 1, "title": "COUNTIES", "reference": "Article 6(1)"},
        {"number": 2, "title": "NATIONAL SYMBOLS", "reference": "Article 9(2)"},
        {"number": 3, "title": "NATIONAL OATHS AND AFFIRMATIONS", "reference": "Articles 74, 141(3), 148(5), 152(4)"},
        {"number": 4, "title": "DISTRIBUTION OF FUNCTIONS BETWEEN NATIONAL AND COUNTY GOVERNMENTS", "reference": "Articles 185(2), 186(1), 187(2)"},
        {"number": 5, "title": "LEGISLATION TO BE ENACTED BY PARLIAMENT", "reference": "Article 261(1)"},
        {"number": 6, "title": "TRANSITIONAL AND CONSEQUENTIAL PROVISIONS", "reference": "Article 262"},
    ]

    # Find first schedule
    first_schedule_match = re.search(r'FIRST SCHEDULE', content_text)
    if not first_schedule_match:
        return schedules

    schedule_text = content_text[first_schedule_match.start():]

    for info in schedule_info:
        schedule_obj = {
            "number": info["number"],
            "title": info["title"],
            "reference": info["reference"],
            "content": {}
        }

        # Parse specific schedule content based on type
        if info["number"] == 1:
            # Counties - parse the list of 47 counties
            counties = []
            county_pattern = r'(\d+)\.\s+([A-Za-z\s\-]+?)(?=\n|\d+\.|$)'
            for match in re.finditer(county_pattern, schedule_text[:8000]):
                try:
                    county_num = int(match.group(1))
                    county_name = match.group(2).strip()
                    if county_num <= 47 and county_name:
                        counties.append({
                            "number": county_num,
                            "name": county_name
                        })
                except:
                    pass
            schedule_obj["content"] = {"counties": counties}

        elif info["number"] == 2:
            # National symbols - hardcoded for reliability
            schedule_obj["content"] = {
                "nationalFlag": {
                    "description": "Three major strips of equal width coloured from top to bottom black, red and green and separated by narrow white strips, with a symmetrical shield and white spears superimposed centrally."
                },
                "nationalAnthem": {
                    "verses": [
                        {
                            "number": 1,
                            "kiswahili": "Ee Mungu nguvu yetu, Ilete baraka kwetu. Haki iwe ngao na mlinzi, Natukae na undugu. Amani na uhuru, Raha tupate na ustawi.",
                            "english": "O God of all creation, Bless this our land and nation. Justice be our shield and defender, May we dwell in unity. Peace and liberty, Plenty be found within our borders."
                        },
                        {
                            "number": 2,
                            "kiswahili": "Amkeni ndugu zetu, Tufanye sote bidii. Nasi tujitoe kwa nguvu, Nchi yetu ya Kenya. Tunayoipenda, Tuwe tayari kuilinda.",
                            "english": "Let one and all arise, With hearts both strong and true. Service be our earnest endeavour, And our Homeland of Kenya. Heritage of splendour, Firm may we stand to defend."
                        },
                        {
                            "number": 3,
                            "kiswahili": "Natujenge taifa letu, Ee, ndio wajibu wetu. Kenya istahili heshima, Tuungane mikono. Pamoja kazini, Kila siku tuwe na shukrani.",
                            "english": "Let all with one accord, In common bond united. Build this our nation together, And the glory of Kenya. The fruit of our labour, Fill every heart with thanksgiving."
                        }
                    ]
                },
                "coatOfArms": {
                    "description": "The Coat of Arms features two lions holding spears, a shield with colors of the Kenyan flag, and a rooster holding an axe."
                },
                "publicSeal": {
                    "description": "The Public Seal of Kenya."
                }
            }

        elif info["number"] == 3:
            # Oaths and Affirmations
            schedule_obj["content"] = {
                "oaths": [
                    {"title": "Oath/Affirmation of Office of President", "reference": "Article 141(3)"},
                    {"title": "Oath/Affirmation of Office of Deputy President", "reference": "Article 148(5)"},
                    {"title": "Oath/Affirmation of Office of Cabinet Secretary", "reference": "Article 152(4)"},
                    {"title": "Oath/Affirmation of Office of Member of Parliament", "reference": "Article 74"},
                    {"title": "Oath/Affirmation of Office of State Officer", "reference": "Article 74"}
                ]
            }

        elif info["number"] == 4:
            # Distribution of Functions
            schedule_obj["content"] = {
                "nationalGovernment": "Functions assigned to the National Government",
                "countyGovernments": "Functions assigned to the County Governments"
            }

        elif info["number"] == 5:
            # Legislation to be enacted
            schedule_obj["content"] = {
                "description": "List of legislation required to be enacted by Parliament"
            }

        elif info["number"] == 6:
            # Transitional provisions
            schedule_obj["content"] = {
                "description": "Transitional and consequential provisions for the implementation of this Constitution"
            }

        schedules.append(schedule_obj)

    return schedules


def main():
    from pathlib import Path

    # Get the directory where this script is located
    script_dir = Path(__file__).parent

    input_file = script_dir / "CONSTITUTION-OF-KENYA-2010.txt"
    output_file = script_dir / "constitution1.json"

    print(f"Reading from: {input_file}", flush=True)
    print("Parsing Constitution of Kenya...", flush=True)
    result = parse_constitution(str(input_file))

    print(f"\nFound {len(result['chapters'])} chapters", flush=True)
    total_articles = 0
    for chapter in result['chapters']:
        print(f"  Chapter {chapter['number']}: {chapter['title'][:50]}... ({len(chapter['articles'])} articles)", flush=True)
        total_articles += len(chapter['articles'])

    print(f"\nTotal articles: {total_articles}", flush=True)
    print(f"Found {len(result['schedules'])} schedules", flush=True)

    # Write to JSON file
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(result, f, indent=2, ensure_ascii=False)

    print(f"\nJSON written to {output_file}", flush=True)
    print(f"JSON size: {output_file.stat().st_size} bytes", flush=True)
    return str(output_file)


if __name__ == "__main__":
    main()


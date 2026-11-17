#!/usr/bin/env python3
"""Import the public Sessionize feed embedded by the official event page."""
from __future__ import annotations
import argparse
import hashlib
import json
import re
from datetime import datetime
from pathlib import Path
from urllib.request import Request, urlopen
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parents[1]
SOURCE = 'https://india.droidcon.com/droidcon-agenda'
ZONE = ZoneInfo('Asia/Kolkata')

def fetch(url):
    with urlopen(Request(url, headers={'User-Agent':'DroidBuddy-AgendaImporter/1.0'}), timeout=30) as response:
        return response.read().decode('utf-8')

def normalize(grid, speakers, imported_at, revision):
    people = {p['id']: {'id': p['id'], 'name': p['fullName'], 'headline': p.get('tagLine',''), 'bio': p.get('bio',''), 'photoUrl': p.get('profilePicture','')} for p in speakers}
    sessions = {}
    placements = 0
    for day in grid:
        for room in day['rooms']:
            for raw in room['sessions']:
                placements += 1
                start = datetime.fromisoformat(raw['startsAt'])
                end = datetime.fromisoformat(raw['endsAt'])
                # Sessionize event-local values have no offset; normalize explicitly to IST.
                start = start.replace(tzinfo=ZONE) if start.tzinfo is None else start.astimezone(ZONE)
                end = end.replace(tzinfo=ZONE) if end.tzinfo is None else end.astimezone(ZONE)
                if end <= start or start.date().isoformat() != '2026-11-20':
                    raise ValueError(f'Invalid event timing: {raw["id"]}')
                id = str(raw['id'])
                if id in sessions:
                    previous = sessions[id]
                    if (previous['title'],previous['startTime'],previous['endTime']) != (raw['title'],start.isoformat(),end.isoformat()):
                        raise ValueError(f'Conflicting duplicate session: {id}')
                    if room['name'] not in previous['rooms']:
                        previous['rooms'].append(room['name'])
                    continue
                tags = [i['name'] for c in raw.get('categories',[]) if c['name'] == 'Tags' for i in c['categoryItems']]
                categories = {c['name']: [i['name'] for i in c['categoryItems']] for c in raw.get('categories',[])}
                if not raw.get('title','').strip() or not room.get('name'):
                    raise ValueError('Missing title or room')
                entries = []
                for speaker in raw.get('speakers',[]):
                    entries.append(people.get(speaker['id'], {'id':speaker['id'],'name':speaker['name'],'headline':'','bio':'','photoUrl':''}))
                sessions[id] = {'id':id,'title':raw['title'],'abstract':raw.get('description') or '', 'startTime':start.isoformat(),'endTime':end.isoformat(),'rooms':[room['name']], 'speakers':entries,'tags':tags,'level':', '.join(categories.get('Level',[])),'format':', '.join(categories.get('Session format',[])), 'service':bool(raw.get('isServiceSession')), 'plenum':bool(raw.get('isPlenumSession'))}
    result = sorted(sessions.values(), key=lambda s:(s['startTime'],s['rooms'][0],s['id']))
    return {'event':'droidcon India 2026','date':'2026-11-20','timezone':'Asia/Kolkata','sourceUrl':SOURCE,'feedUrl':'https://sessionize.com/api/v2/qfgx6adj/view/GridSmart','importedAt':imported_at,'sourceRevision':revision,'sessionCount':len(result),'placementCount':placements,'speakerCount':len({p['id'] for s in result for p in s['speakers']}),'sessions':result}

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--grid',type=Path,help='Previously downloaded official GridSmart JSON')
    parser.add_argument('--speakers',type=Path,help='Previously downloaded official Speakers JSON')
    args=parser.parse_args()
    if bool(args.grid) != bool(args.speakers): parser.error('--grid and --speakers must be supplied together')
    if args.grid:
        grid_raw=args.grid.read_text(); speakers_raw=args.speakers.read_text()
    else:
        html=fetch(SOURCE)
        match=re.search(r'<session-schedule[^>]+grid-api="([^"]+)"[^>]+speakers-api="([^"]+)"',html)
        if not match: raise ValueError('Official page does not expose the expected public Sessionize feed. No workaround attempted.')
        grid_url,speakers_url=match.groups()
        if not all(url.startswith('https://sessionize.com/api/v2/') for url in (grid_url,speakers_url)): raise ValueError('Unexpected feed origin')
        grid_raw=fetch(grid_url); speakers_raw=fetch(speakers_url)
    revision=hashlib.sha256((grid_raw+speakers_raw).encode()).hexdigest()
    data=normalize(json.loads(grid_raw),json.loads(speakers_raw),datetime.now(ZONE).isoformat(),revision)
    content=json.dumps(data,ensure_ascii=False,indent=2)+'\n'
    path = ROOT/'app/src/main/assets/agenda.json'
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(content)
    print(f'Imported {data["sessionCount"]} unique entries ({data["placementCount"]} room placements), {data["speakerCount"]} speakers; source revision {revision[:12]}.')

if __name__ == '__main__': main()

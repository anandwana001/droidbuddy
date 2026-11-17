# Official agenda snapshot

Source page: https://india.droidcon.com/droidcon-agenda

The page embeds a public `session-schedule` custom element with these feeds:
- https://sessionize.com/api/v2/qfgx6adj/view/GridSmart
- https://sessionize.com/api/v2/qfgx6adj/view/Speakers

The 17 November 2026 import contains **67 entries, 44 non-service sessions, 52 distinct speakers and seven rooms**, for 20 November 2026. It includes every public feed entry, including breaks, community booths, roundtables and 1:1 sessions. No invented session/speaker data is used. Speaker bios/headlines/portrait URLs come from the public speaker feed; private question answers and contact fields are excluded.

Refresh using Python 3.10+:

```bash
python3 scripts/import_agenda.py
```

Or validate already downloaded public exports:

```bash
python3 scripts/import_agenda.py --grid official-grid.json --speakers official-speakers.json
```

The importer writes normalized JSON to `app/src/main/assets/agenda.json`. Rebuild Android after refreshing. Normalization validates date, title, room and increasing start/end times, deduplicates stable Sessionize IDs, preserves multiple room placements and applies Asia/Kolkata to event-local timestamps. Conflicting duplicate IDs fail the import. Scheduling fields take precedence over prose descriptions (the feed contains some break descriptions whose minutes differ from their scheduled fields).

The app works offline with the bundled snapshot, supports room/text/talk/saved filters and stores bookmarks locally. It shows the source and import date and links to the live event site. This is a published snapshot, not a claim of live synchronization or organizer endorsement. The direct Agora agent receives this same full agenda including descriptions. Local bookmarks are not included in its context.

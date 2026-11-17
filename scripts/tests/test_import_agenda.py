"""Offline tests for the public agenda importer; Python standard library only."""
import importlib.util
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('import_agenda', ROOT/'scripts/import_agenda.py')
importer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(importer)

def fixture(start='2026-11-20T09:00:00', end='2026-11-20T09:30:00'):
    return [{'date':'2026-11-20T00:00:00','rooms':[{'name':'Fixture room','sessions':[{'id':'fixture-1','title':'Fixture session','startsAt':start,'endsAt':end,'speakers':[],'categories':[],'isServiceSession':True}]}]}]

class AgendaImportTest(unittest.TestCase):
    def test_event_timezone_and_service_entries(self):
        data = importer.normalize(fixture(), [], 'snapshot', 'revision')
        self.assertTrue(data['sessions'][0]['startTime'].endswith('+05:30'))
        self.assertTrue(data['sessions'][0]['service'])
        self.assertEqual(1, data['sessionCount'])

    def test_invalid_end_time_is_rejected(self):
        with self.assertRaises(ValueError):
            importer.normalize(fixture('2026-11-20T10:00:00','2026-11-20T09:00:00'), [], 'snapshot','revision')

    def test_wrong_event_date_is_rejected(self):
        with self.assertRaises(ValueError):
            importer.normalize(fixture('2026-11-21T09:00:00','2026-11-21T10:00:00'), [], 'snapshot','revision')

    def test_conflicting_duplicate_is_rejected(self):
        grid = fixture()
        grid[0]['rooms'][0]['sessions'].append({**grid[0]['rooms'][0]['sessions'][0], 'title':'Conflicting title'})
        with self.assertRaisesRegex(ValueError,'Conflicting duplicate'):
            importer.normalize(grid, [], 'snapshot','revision')

    def test_bundled_agenda_is_complete(self):
        data = json.loads((ROOT/'app/src/main/assets/agenda.json').read_text())
        self.assertEqual(67, len(data['sessions']))
        self.assertEqual(67, len({s['id'] for s in data['sessions']}))
        self.assertEqual(52, data['speakerCount'])
        self.assertEqual('https://india.droidcon.com/droidcon-agenda', data['sourceUrl'])

if __name__ == '__main__':
    unittest.main()

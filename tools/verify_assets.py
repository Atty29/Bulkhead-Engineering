"""Offline consistency check; run with Python 3, no third-party modules required."""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[1]
assets = root / 'src/main/resources/assets/hbm_doors'
errors = []

def require(path, source):
    if not (assets / path).is_file():
        errors.append(f'{source.relative_to(root)}: missing {path}')

for source in assets.rglob('*.json'):
    data = json.loads(source.read_text(encoding='utf-8'))
    def visit(node, key=''):
        if isinstance(node, dict):
            for k, v in node.items():
                visit(v, k)
        elif isinstance(node, list):
            for v in node:
                visit(v, key)
        elif isinstance(node, str) and node.startswith('hbm_doors:'):
            path = node.split(':', 1)[1]
            if key in ('model', 'parent', 'legacy_model'):
                if path.startswith('models/'):
                    require(path if path.endswith(('.obj', '.dae')) else path + '.dae', source)
                else:
                    require('models/' + path + '.json', source)
    visit(data)
    for texture in data.get('textures', {}).values():
        if texture.startswith('hbm_doors:'):
            require('textures/' + texture.split(':', 1)[1] + '.png', source)

sounds = json.loads((assets / 'sounds.json').read_text(encoding='utf-8'))
for entry in sounds.values():
    for sound in entry['sounds']:
        name = sound if isinstance(sound, str) else sound['name']
        require('sounds/' + name.split(':', 1)[-1] + '.ogg', assets / 'sounds.json')

recipes = list((root / 'src/main/resources/data/hbm_doors/recipes').glob('*.json'))
assert len(recipes) == 37, f'Expected 37 recipes, got {len(recipes)}'
if errors:
    raise SystemExit('\n'.join(errors))
print(f'PASS: models, textures, sound references and {len(recipes)} recipe files')

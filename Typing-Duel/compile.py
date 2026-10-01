import os
import subprocess

files = []
for root, _, fs in os.walk('src'):
    for f in fs:
        if f.endswith('.java'):
            files.append(os.path.join(root, f))

with open('compile_errors.txt', 'w') as out:
    subprocess.run(['javac', '-d', 'bin', '-cp', 'lib/gson.jar'] + files, stdout=out, stderr=subprocess.STDOUT)


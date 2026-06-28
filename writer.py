#!/usr/bin/env python3
import sys
path = '/home/admin/projects/vnmine/src/main/java/com/vnmine/cultivation/MeditationConfig.java'
with open(path, 'w') as f:
    f.write(open('/dev/stdin').read())
print("Written")

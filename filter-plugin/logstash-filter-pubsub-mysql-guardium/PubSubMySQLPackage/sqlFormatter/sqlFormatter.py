#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
def formattedQuery(q): return " ".join([s.strip() for s in q.splitlines()])
sql = sys.stdin.read().rstrip()
print(formattedQuery(sql))

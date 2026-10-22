#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

MENU_ACTION=runelite-client/src/main/resources/net/runelite/client/plugins/microbot/util/reflection/menu-action-info.properties

./gradlew :client:seedMenuActionInfo :hwid-agent:obfuscationReport :hwid-agent:test -q

if ! git diff -U0 -- "$MENU_ACTION" | grep -q '^[-+][^-+#]'; then
	git checkout -- "$MENU_ACTION"
fi

git --no-pager diff --stat -- docs/DETECTION_HARDENING.md docs/obfuscation/injected-getters.tsv "$MENU_ACTION"
git status --short -- docs/obfuscation/injected-getters.tsv

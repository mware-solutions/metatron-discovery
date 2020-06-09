#!/usr/bin/env bash

SBINDIR=$(cd "$(dirname "$0")" && pwd -P)
BINDIR="$SBINDIR/../bin"
me=$(basename "$0")

. "$BINDIR/_env.inc.sh"

$METATRON_HOME/bin/discovery.sh --debug=45005 foreground

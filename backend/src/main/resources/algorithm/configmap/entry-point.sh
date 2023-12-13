#!/bin/sh
WAITRESS_SERVE_CALL=${WAITRESS_SERVE_CALL:-app:create_app}
exec waitress-serve --call $WAITRESS_SERVE_CALL $@
